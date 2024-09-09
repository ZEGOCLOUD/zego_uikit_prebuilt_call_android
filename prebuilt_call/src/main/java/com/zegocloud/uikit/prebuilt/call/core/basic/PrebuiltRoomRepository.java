package com.zegocloud.uikit.prebuilt.call.core.basic;

import com.zegocloud.uikit.ZegoUIKit;
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallFragment;
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallService;
import com.zegocloud.uikit.prebuilt.call.config.DurationUpdateListener;
import com.zegocloud.uikit.prebuilt.call.core.CallInvitationServiceImpl;
import com.zegocloud.uikit.prebuilt.call.core.PrebuiltCallExpressBridge;
import com.zegocloud.uikit.prebuilt.call.event.CallEndListener;
import com.zegocloud.uikit.prebuilt.call.event.ZegoCallEndReason;
import com.zegocloud.uikit.service.defines.ZegoOnlySelfInRoomListener;
import com.zegocloud.uikit.service.defines.ZegoUIKitCallback;
import com.zegocloud.uikit.service.defines.ZegoUIKitUser;
import com.zegocloud.uikit.service.express.IExpressEngineEventHandler;
import im.zego.zegoexpress.constants.ZegoRoomStateChangedReason;
import im.zego.zegoexpress.constants.ZegoUpdateType;
import im.zego.zegoexpress.entity.ZegoUser;
import java.util.ArrayList;
import java.util.Objects;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import timber.log.Timber;

public class PrebuiltRoomRepository {

    private boolean inCallRoom;
    private String prebuiltRoomID;
    private PrebuiltCallExpressBridge expressBridge;
    private PrebuiltCallTimer callTimer = new PrebuiltCallTimer();
    private ZegoUIKitPrebuiltCallFragment callFragment;
    private boolean leaveWhenOnlySelf;

    private IExpressEngineEventHandler eventHandler = new IExpressEngineEventHandler() {
        @Override
        public void onRoomStateChanged(String roomID, ZegoRoomStateChangedReason reason, int errorCode,
            JSONObject extendedData) {
            super.onRoomStateChanged(roomID, reason, errorCode, extendedData);
            Timber.d(
                "onRoomStateChanged() called with: roomID = [" + roomID + "], reason = [" + reason + "], errorCode = ["
                    + errorCode + "], extendedData = [" + extendedData + "]");
            if (reason == ZegoRoomStateChangedReason.LOGINED) {
                CallInvitationServiceImpl.getInstance().onPrebuiltCallRoomJoined(roomID);
            }
            if (reason == ZegoRoomStateChangedReason.LOGOUT) {
                if (Objects.equals(prebuiltRoomID, roomID)) { //in case multi room
                    clearRoomData();
                    CallInvitationServiceImpl.getInstance().onPrebuiltCallRoomLeft(roomID);
                    removeCallbacks();
                }
            }
            if (reason == ZegoRoomStateChangedReason.KICK_OUT) {
                if (Objects.equals(prebuiltRoomID, roomID)) {
                    leaveSDKRoomInner();
                    clearRoomData();
                    CallInvitationServiceImpl.getInstance().onPrebuiltCallRoomLeft(roomID);
                    invokeKickOutCallback("");
                    removeCallbacks();
                }
            }
        }

        @Override
        public void onIMRecvCustomCommand(String roomID, ZegoUser fromUser, String command) {
            super.onIMRecvCustomCommand(roomID, fromUser, command);
            try {
                JSONObject jsonObject = new JSONObject(command);
                Timber.d("onIMRecvCustomCommand() called with: roomID = [" + roomID + "], fromUser = [" + fromUser
                    + "], command = [" + command + "]");
                if (jsonObject.has("zego_remove_user")) {
                    JSONArray userIDArray = jsonObject.getJSONArray("zego_remove_user");
                    ZegoUIKitUser localUser = ZegoUIKit.getLocalUser();
                    for (int i = 0; i < userIDArray.length(); i++) {
                        String userID = userIDArray.getString(i);
                        if (localUser != null && Objects.equals(userID, localUser.userID)) {
                            leaveSDKRoomInner();
                            clearRoomData();
                            CallInvitationServiceImpl.getInstance().onPrebuiltCallRoomLeft(roomID);
                            invokeKickOutCallback(fromUser.userID);
                            removeCallbacks();
                        }
                    }
                }
            } catch (JSONException e) {
            }
        }

        @Override
        public void onRoomUserUpdate(String roomID, ZegoUpdateType updateType, ArrayList<ZegoUser> userList) {
            super.onRoomUserUpdate(roomID, updateType, userList);
            if (updateType == ZegoUpdateType.DELETE) {
                CallInvitationServiceImpl.getInstance().onPrebuiltCallRoomUserLeft(userList,roomID);
            }
        }
    };

    public PrebuiltRoomRepository(PrebuiltCallExpressBridge expressBridge) {
        this.expressBridge = expressBridge;
    }

    public void joinRoom(String roomID, ZegoUIKitCallback callback) {
        Timber.d("joinRoom() called with: roomID = [" + roomID + "], callback = [" + callback + "]");
        setupCallbacks();
        expressBridge.joinSDKRoom(roomID, new ZegoUIKitCallback() {
            @Override
            public void onResult(int errorCode) {
                Timber.d("joinRoom  Express onResult() called with: errorCode = [" + errorCode + "]");
                inCallRoom = errorCode == 0;
                if (inCallRoom) {
                    prebuiltRoomID = roomID;
                    callTimer.startRoomTimeCount();
                }
                if (callback != null) {
                    callback.onResult(errorCode);
                }
            }
        });
    }

    public String getPrebuiltRoomID() {
        return prebuiltRoomID;
    }

    public boolean isInCallRoom() {
        return inCallRoom;
    }

    // usually used when you want to invoke a different callback
    private void leaveSDKRoomInner() {
        Timber.d("leaveSDKRoomInner() called : " + prebuiltRoomID);
        expressBridge.leaveSDKRoom();
    }

    // leaveRoom --> leaveSDKRoom --> onRoomStateChanged --> endCall --> leaveRoom ,then stopped!!!
    // invoke LocalHangUp callback
    public void leaveSDKRoom() {
        Timber.d("leaveSDKRoom() called");
        if (inCallRoom) {
            inCallRoom = false;
            leaveSDKRoomInner();
            invokeLocalHangUpCallback();
        }
    }


    private void clearRoomData() {
        inCallRoom = false;
        prebuiltRoomID = null;
        if (callTimer != null) {
            callTimer.stopRoomTimeCount();
            callTimer.clear();
        }
        if (callFragment != null) {
            callFragment.endCall();
            callFragment = null;
        }
    }

    public void setDurationUpdateListener(DurationUpdateListener updateListener) {
        callTimer.setDurationUpdateListener(updateListener);
    }

    public long getStartTimeLocal() {
        return callTimer.getStartTimeLocal();
    }

    private void setupCallbacks() {
        expressBridge.addEventHandler(eventHandler);

        ZegoUIKit.addOnOnlySelfInRoomListener(() -> {
            ZegoOnlySelfInRoomListener selfInRoomListener = ZegoUIKitPrebuiltCallService.events.callEvents.getOnlySelfInRoomListener();
            if (selfInRoomListener != null) {
                selfInRoomListener.onOnlySelfInRoom();
            } else {
                if (leaveWhenOnlySelf) {
                    leaveSDKRoomInner();
                    invokeRemoteHangUpCallback();
                }
            }
        });
    }

    private void removeCallbacks() {
        expressBridge.removeEventHandler(eventHandler);
    }

    public void setPrebuiltCallFragment(ZegoUIKitPrebuiltCallFragment zegoUIKitPrebuiltCallFragment) {
        callFragment = zegoUIKitPrebuiltCallFragment;
    }

    public void setLeaveWhenOnlySelf(boolean leaveWhenOnlySelf) {
        this.leaveWhenOnlySelf = leaveWhenOnlySelf;
    }

    public ZegoUIKitPrebuiltCallFragment getCallFragment() {
        return callFragment;
    }

    private void invokeLocalHangUpCallback() {
        CallEndListener callEndListener = ZegoUIKitPrebuiltCallService.events.callEvents.getCallEndListener();
        if (callEndListener != null) {
            callEndListener.onCallEnd(ZegoCallEndReason.LOCAL_HANGUP, null);
        }
    }

    private void invokeKickOutCallback(String extendData) {
        CallEndListener callEndListener = ZegoUIKitPrebuiltCallService.events.callEvents.getCallEndListener();
        if (callEndListener != null) {
            callEndListener.onCallEnd(ZegoCallEndReason.KICK_OUT, extendData);
        }
    }

    private void invokeRemoteHangUpCallback() {
        CallEndListener callEndListener = ZegoUIKitPrebuiltCallService.events.callEvents.getCallEndListener();
        if (callEndListener != null) {
            callEndListener.onCallEnd(ZegoCallEndReason.REMOTE_HANGUP, "");
        }
    }
}
