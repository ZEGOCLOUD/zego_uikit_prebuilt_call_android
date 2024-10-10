package com.zegocloud.uikit.prebuilt.call.core.invite;

import android.app.Activity;
import android.app.Application;
import android.text.TextUtils;
import androidx.annotation.NonNull;
import com.google.gson.Gson;
import com.zegocloud.uikit.ZegoUIKit;
import com.zegocloud.uikit.plugin.adapter.plugins.signaling.ZegoSignalingPluginNotificationConfig;
import com.zegocloud.uikit.plugin.adapter.utils.GenericUtils;
import com.zegocloud.uikit.plugin.common.PluginCallbackListener;
import com.zegocloud.uikit.plugin.invitation.ZegoInvitationType;
import com.zegocloud.uikit.plugin.signaling.ZegoSignalingPlugin;
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallService;
import com.zegocloud.uikit.prebuilt.call.core.CallInvitationServiceImpl;
import com.zegocloud.uikit.prebuilt.call.core.JsonUtil;
import com.zegocloud.uikit.prebuilt.call.core.PrebuiltCallExpressBridge;
import com.zegocloud.uikit.prebuilt.call.core.PrebuiltCallZIMBridge;
import com.zegocloud.uikit.prebuilt.call.core.invite.PrebuiltCallInviteExtendedData.Data;
import com.zegocloud.uikit.prebuilt.call.core.invite.PrebuiltCallInviteExtendedData.Data.Invitee;
import com.zegocloud.uikit.prebuilt.call.core.notification.PrebuiltCallNotificationManager;
import com.zegocloud.uikit.prebuilt.call.core.push.PrebuiltCallPushRepository;
import com.zegocloud.uikit.prebuilt.call.core.push.ZIMPushMessage;
import com.zegocloud.uikit.prebuilt.call.invite.ZegoUIKitPrebuiltCallInvitationConfig;
import com.zegocloud.uikit.prebuilt.call.invite.internal.CallInviteActivity;
import com.zegocloud.uikit.prebuilt.call.invite.internal.CallStateListener;
import com.zegocloud.uikit.prebuilt.call.invite.internal.ZegoCallType;
import com.zegocloud.uikit.prebuilt.call.invite.internal.ZegoCallUser;
import com.zegocloud.uikit.prebuilt.call.invite.internal.ZegoInvitationCallListener;
import com.zegocloud.uikit.service.defines.ZegoUIKitUser;
import im.zego.zim.ZIM;
import im.zego.zim.callback.ZIMCallAcceptanceSentCallback;
import im.zego.zim.callback.ZIMCallCancelSentCallback;
import im.zego.zim.callback.ZIMCallEndSentCallback;
import im.zego.zim.callback.ZIMCallInvitationSentCallback;
import im.zego.zim.callback.ZIMCallQuitSentCallback;
import im.zego.zim.callback.ZIMCallingInvitationSentCallback;
import im.zego.zim.callback.ZIMEventHandler;
import im.zego.zim.entity.ZIMCallAcceptConfig;
import im.zego.zim.entity.ZIMCallCancelConfig;
import im.zego.zim.entity.ZIMCallEndConfig;
import im.zego.zim.entity.ZIMCallInfo;
import im.zego.zim.entity.ZIMCallInvitationAcceptedInfo;
import im.zego.zim.entity.ZIMCallInvitationCancelledInfo;
import im.zego.zim.entity.ZIMCallInvitationCreatedInfo;
import im.zego.zim.entity.ZIMCallInvitationEndedInfo;
import im.zego.zim.entity.ZIMCallInvitationReceivedInfo;
import im.zego.zim.entity.ZIMCallInvitationRejectedInfo;
import im.zego.zim.entity.ZIMCallInvitationSentInfo;
import im.zego.zim.entity.ZIMCallInvitationTimeoutInfo;
import im.zego.zim.entity.ZIMCallInviteConfig;
import im.zego.zim.entity.ZIMCallQuitConfig;
import im.zego.zim.entity.ZIMCallUserInfo;
import im.zego.zim.entity.ZIMCallUserStateChangeInfo;
import im.zego.zim.entity.ZIMCallingInvitationSentInfo;
import im.zego.zim.entity.ZIMCallingInviteConfig;
import im.zego.zim.entity.ZIMError;
import im.zego.zim.entity.ZIMPushConfig;
import im.zego.zim.entity.ZIMUserFullInfo;
import im.zego.zim.entity.ZIMUserInfo;
import im.zego.zim.entity.ZIMUsersInfoQueryConfig;
import im.zego.zim.enums.ZIMCallInvitationMode;
import im.zego.zim.enums.ZIMCallUserState;
import im.zego.zim.enums.ZIMErrorCode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.json.JSONObject;
import timber.log.Timber;

/**
 * call data and logic related to zim SDK
 */
public class PrebuiltCallRepository {

    public static final int NONE_CALL_NO_REPLY = -5;
    public static final int NONE_RECEIVE_MISSED = -4;
    public static final int NONE_REJECTED = -3;
    public static final int NONE_REJECTED_BUSY = -4;
    public static final int NONE_CANCELED = -2;
    public static final int NONE_HANG_UP = -1;
    public static final int NONE = 0;
    public static final int OUTGOING = 1;
    public static final int CONNECTED = 2;
    public static final int INCOMING = 3;
    private int callState = NONE;
    private ZegoCallInvitationData callInvitationData;
    private List<CallStateListener> callStateListeners;
    private ZegoInvitationCallListener invitationCallListener;

    private PrebuiltCallZIMBridge zimBridge;
    private PrebuiltCallNotificationManager notificationManager;
    private PrebuiltCallPushRepository pushRepository = new PrebuiltCallPushRepository();
    private PrebuiltCallExpressBridge expressBridge;

    private Gson gson = new Gson();
    private static final String TAG = "PrebuiltCallRepository";

    public PrebuiltCallRepository(PrebuiltCallZIMBridge zimBridge, PrebuiltCallExpressBridge expressBridge,
        PrebuiltCallNotificationManager notificationManager) {
        this.zimBridge = zimBridge;
        this.notificationManager = notificationManager;
        this.expressBridge = expressBridge;
    }

    public ZegoCallInvitationData getCallInvitationData() {
        return callInvitationData;
    }

    public int getCallState() {
        return callState;
    }

    public boolean isCallStateOutgoing() {
        return callState == OUTGOING;
    }

    private void showIncomingCallDialog(ZegoCallInvitationData callInvitationData) {
        CallInvitationServiceImpl.getInstance().showIncomingCallDialog(callInvitationData);
    }

    private void hideIncomingCallDialog() {
        CallInvitationServiceImpl.getInstance().hideIncomingCallDialog();
    }

    private void dismissIncomingCallNotification() {
        Activity topActivity = CallInvitationServiceImpl.getInstance().getTopActivity();
        notificationManager.dismissCallNotification(topActivity);
    }

    private static void stopRingTone() {
        CallInvitationServiceImpl.getInstance().stopRingTone();
    }

    private void playOutgoingRingTone() {
        CallInvitationServiceImpl.getInstance().playOutgoingRingTone();
    }

    public void setCallState(int callState) {
        Timber.d("setCallState() called with: before = [" + this.callState + "],after:" + callState);
        int before = this.callState;
        this.callState = callState;
        if (callState <= 0) {
            clearInvitationData();
        }
        if (before != callState && callStateListeners != null) {
            for (CallStateListener callStateListener : callStateListeners) {
                callStateListener.onStateChanged(before, callState);
            }
        }
    }

    public void clearInvitationData() {
        callInvitationData = null;
        stopRingTone();
    }

    public void addCallStateListener(CallStateListener callStateListener) {
        if (callStateListeners == null) {
            callStateListeners = new CopyOnWriteArrayList<>();
        }
        this.callStateListeners.add(callStateListener);
    }

    public void removeCallStateListener(CallStateListener callStateListener) {
        if (callStateListeners == null) {
            return;
        }
        callStateListeners.remove(callStateListener);
    }

    public void clearCallStateListener() {
        if (callStateListeners == null) {
            return;
        }
        callStateListeners.clear();
    }

    public void zimCallInvitationCreated(ZIM zim, ZIMCallInvitationCreatedInfo info, String callID) {

    }

    public void zimCallInvitationReceived(ZIM zim, ZIMCallInvitationReceivedInfo info, String callID) {
        ZIMCallInfo zimCallInfo = ZegoSignalingPlugin.getInstance().getZIMCallInfo(callID);
        if (zimCallInfo == null) {
            return;
        }
        List<String> callUserList = zimCallInfo.callUserList.stream().map(zimCallUserInfo -> zimCallUserInfo.userID)
            .collect(Collectors.toList());
        ZegoSignalingPlugin.getInstance()
            .queryUserInfo(callUserList, new ZIMUsersInfoQueryConfig(), (userList, errorUserList, errorInfo) -> {
                if (errorInfo.code == ZIMErrorCode.SUCCESS) {
                    PrebuiltCallInviteExtendedData extendedData = gson.fromJson(info.extendedData,
                        PrebuiltCallInviteExtendedData.class);
                    if (extendedData.getType() != ZegoCallType.VOICE_CALL.value()
                        && extendedData.getType() != ZegoCallType.VIDEO_CALL.value()) {
                        return;
                    }
                    String currentRoomID = ZegoUIKit.getRoom().roomID;
                    if (callState > 0 || !TextUtils.isEmpty(currentRoomID)) {
                        String autoRejectJsonString = PrebuiltCallUtil.getAutoRejectJsonString(callID);
                        ZegoUIKit.getSignalingPlugin()
                            .refuseInvitation(extendedData.getInviterId(), autoRejectJsonString, null);
                        return;
                    }
                    PrebuiltCallInviteExtendedData.Data data = gson.fromJson(extendedData.getData(),
                        PrebuiltCallInviteExtendedData.Data.class);
                    onPrebuiltReceiveCallComing(zimCallInfo, extendedData, data);
                }
            });
    }

    private void onPrebuiltReceiveCallComing(ZIMCallInfo zimCallInfo, PrebuiltCallInviteExtendedData extendedData,
        PrebuiltCallInviteExtendedData.Data data) {
        Activity topActivity = CallInvitationServiceImpl.getInstance().getTopActivity();
        ZIMPushMessage pushMessage = pushRepository.getPushMessage();
        String notificationAction = pushRepository.getNotificationAction();

        Timber.d(
            "onInvitationReceived() called with: zimCallInfo = [" + zimCallInfo + "], topActivity = [" + topActivity
                + "], pushMessage = [" + pushMessage + "], notificationAction = [" + notificationAction
                + "],callState: " + callState);

        ZegoUIKitUser caller = getUiKitUserFromUserID(zimCallInfo.caller);
        callInvitationData = new ZegoCallInvitationData();
        callInvitationData.caller = caller;
        callInvitationData.inviter = caller;
        ZIMUserFullInfo zimInviter = zimBridge.getMemoryUserInfo(zimCallInfo.inviter);
        if (zimInviter != null) {
            callInvitationData.inviter = new ZegoUIKitUser(zimInviter.baseInfo.userID, zimInviter.baseInfo.userName);
        }

        callInvitationData.invitationID = zimCallInfo.callID;
        callInvitationData.type = extendedData.getType();
        callInvitationData.callID = data.getCallId();
        callInvitationData.customData = data.getCustomData();
        callInvitationData.invitees = zimCallInfo.callUserList.stream()
            .filter(zimCallUserInfo -> !Objects.equals(zimCallInfo.caller, zimCallUserInfo.userID))
            .map(zimCallUserInfo -> getUiKitUserFromUserID(zimCallUserInfo.userID)).collect(Collectors.toList());

        CallInvitationServiceImpl.getInstance().generateCallConfigFromInvite(callInvitationData);
        setCallState(INCOMING);

        // is offline start app and receive the same zimCallID with offline message
        if (pushMessage != null && Objects.equals(pushMessage.invitationID, callInvitationData.invitationID)) {
            // receive offline notification,click accept,start service and start app
            Application application = CallInvitationServiceImpl.getInstance().getApplication();
            String actionAccept =
                application.getPackageName() + "." + PrebuiltCallNotificationManager.ACTION_ACCEPT_CALL;
            String actionClick = application.getPackageName() + "." + PrebuiltCallNotificationManager.ACTION_CLICK;
            String actionDecline =
                application.getPackageName() + "." + PrebuiltCallNotificationManager.ACTION_DECLINE_CALL;
            if (Objects.equals(actionAccept, notificationAction)) {
                stopRingTone();
                if (callInvitationData != null && caller.equals(callInvitationData.inviter)) {
                    ZegoUIKit.getSignalingPlugin().acceptInvitation(caller.userID, "", new PluginCallbackListener() {
                        @Override
                        public void callback(Map<String, Object> result) {
                            Activity topActivity = CallInvitationServiceImpl.getInstance().getTopActivity();
                            if (topActivity != null) {
                                CallInviteActivity.startCallPage(topActivity);
                            }
                        }
                    });
                    clearPushMessage();
                    setCallState(CONNECTED);
                    dismissIncomingCallNotification();
                }
            } else if (Objects.equals(actionClick, notificationAction)) {
                // receive offline notification,click notification,show dialog normally
                showIncomingCallDialog(callInvitationData);
                clearPushMessage();
            } else if (Objects.equals(actionDecline, notificationAction)) {
                // offline push
                ZegoUIKit.getSignalingPlugin().refuseInvitation(caller.userID, "", new PluginCallbackListener() {
                    @Override
                    public void callback(Map<String, Object> result) {
                        CallInvitationServiceImpl.getInstance().unInitSDK();
                    }
                });
                clearPushMessage();
                setCallState(NONE_REJECTED);
                dismissIncomingCallNotification();
            } else {
                // receive offline notification,click app
                showIncomingCallDialog(callInvitationData);
                clearPushMessage();
            }
        } else {
            if (topActivity != null) {
                if (PrebuiltCallUtil.isAppBackground(topActivity)) {
                    notificationManager.showCallNotification(topActivity);
                } else {
                    showIncomingCallDialog(callInvitationData);
                }
            }
            clearPushMessage();
            notifyIncomingCallReceived(zimCallInfo.callID, caller, extendedData.getType());
        }
    }

    public void zimCallInvitationCancelled(ZIM zim, ZIMCallInvitationCancelledInfo info, String callID) {
        if (callInvitationData == null || !Objects.equals(callID, callInvitationData.invitationID)) {
            return;
        }
        List<String> userIDList = Collections.singletonList(info.inviter);
        ZIMUsersInfoQueryConfig queryConfig = new ZIMUsersInfoQueryConfig();
        ZegoSignalingPlugin.getInstance()
            .queryUserInfo(userIDList, queryConfig, (userList, errorUserList, errorInfo) -> {
                if (errorInfo.code == ZIMErrorCode.SUCCESS) {
                    ZegoUIKitUser inviter = getUiKitUserFromUserID(info.inviter);

                    hideIncomingCallDialog();
                    dismissIncomingCallNotification();
                    clearPushMessage();
                    if (callState == CONNECTED) {
                        return;
                    }
                    setCallState(NONE_CANCELED);
                    notifyIncomingCallCanceled(inviter, callID);
                }
            });
    }

    public void zimCallInvitationTimeout(ZIM zim, ZIMCallInvitationTimeoutInfo info, String callID) {
        if (callInvitationData == null || !Objects.equals(callID, callInvitationData.invitationID)) {
            return;
        }
        ZIMCallInfo zimCallInfo = ZegoSignalingPlugin.getInstance().getZIMCallInfo(callID);
        if (zimCallInfo == null) {
            return;
        }
        ZegoUIKitUser uiKitUser = getUiKitUserFromUserID(zimCallInfo.caller);

        hideIncomingCallDialog();
        dismissIncomingCallNotification();
        setCallState(NONE_RECEIVE_MISSED);
        notifyIncomingCallTimeout(uiKitUser, callID);
    }

    public void zimCallUserStateChanged(ZIM zim, ZIMCallUserStateChangeInfo info, String zimCallID) {
        ZIMCallInfo zimCallInfo = ZegoSignalingPlugin.getInstance().getZIMCallInfo(zimCallID);
        ZIMUserInfo selfUserInfo = ZegoSignalingPlugin.getInstance().getUserInfo();
        if (zimCallInfo == null || selfUserInfo == null) {
            return;
        }
        if (callInvitationData == null || !Objects.equals(zimCallID, callInvitationData.invitationID)) {
            return;
        }

        callInvitationData.invitees = zimCallInfo.callUserList.stream()
            .filter(zimCallUserInfo -> !Objects.equals(zimCallInfo.caller, zimCallUserInfo.userID))
            .map(zimCallUserInfo -> new ZegoUIKitUser(zimCallUserInfo.userID)).collect(Collectors.toList());

        List<String> changedUserIDList = info.callUserList.stream().map(zimCallUserInfo -> zimCallUserInfo.userID)
            .collect(Collectors.toList());
        ZegoSignalingPlugin.getInstance()
            .queryUserInfo(changedUserIDList, new ZIMUsersInfoQueryConfig(), (userList, errorUserList, errorInfo) -> {
                if (errorInfo.code == ZIMErrorCode.SUCCESS) {
                    if (callInvitationData != null) {
                        callInvitationData.invitees = zimCallInfo.callUserList.stream()
                            .filter(zimCallUserInfo -> !Objects.equals(zimCallInfo.caller, zimCallUserInfo.userID))
                            .map(zimCallUserInfo -> getUiKitUserFromUserID(zimCallUserInfo.userID))
                            .collect(Collectors.toList());

                    }
                    Timber.d("zimCallUserStateChanged() called with: callInvitationData : [" + callInvitationData);
                    processRejectedUsers(info, zimCallID);
                    processAcceptedUser(info, zimCallID);
                    processTimeoutUser(info, zimCallID);
                }
            });
    }

    private static @NonNull ZegoUIKitUser getUiKitUserFromUserID(String userID) {
        ZIMUserFullInfo memoryUserInfo = ZegoSignalingPlugin.getInstance().getMemoryUserInfo(userID);
        if (memoryUserInfo == null) {
            return new ZegoUIKitUser(userID, userID);
        } else {
            return new ZegoUIKitUser(memoryUserInfo.baseInfo.userID, memoryUserInfo.baseInfo.userName);
        }
    }

    private void processTimeoutUser(ZIMCallUserStateChangeInfo info, String zimCallID) {
        ZIMUserInfo selfUserInfo = ZegoSignalingPlugin.getInstance().getUserInfo();
        List<ZIMCallUserInfo> timeoutZIMUsers = info.callUserList.stream().filter(zimCallUserInfo -> {
            boolean notSelf = !Objects.equals(selfUserInfo.userID, zimCallUserInfo.userID);
            boolean isTimeout = zimCallUserInfo.state == ZIMCallUserState.TIMEOUT;
            return notSelf && isTimeout;
        }).collect(Collectors.toList());

        List<ZegoUIKitUser> timeoutUIKitUsers = timeoutZIMUsers.stream()
            .map(zimCallUserInfo -> getUiKitUserFromUserID(zimCallUserInfo.userID)).collect(Collectors.toList());

        if (!timeoutUIKitUsers.isEmpty()) {
            onPrebuiltReceiveCallNoResponse(zimCallID, timeoutUIKitUsers);
        }
    }

    private void processAcceptedUser(ZIMCallUserStateChangeInfo info, String zimCallID) {
        ZIMUserInfo selfUserInfo = ZegoSignalingPlugin.getInstance().getUserInfo();

        List<ZIMCallUserInfo> acceptZIMUsers = info.callUserList.stream().filter(zimCallUserInfo -> {
            boolean notSelf = !Objects.equals(selfUserInfo.userID, zimCallUserInfo.userID);
            boolean isAccept = zimCallUserInfo.state == ZIMCallUserState.ACCEPTED;
            return notSelf && isAccept;
        }).collect(Collectors.toList());

        acceptZIMUsers.forEach(zimCallUserInfo -> {
            ZegoUIKitUser uiKitUser = getUiKitUserFromUserID(zimCallUserInfo.userID);
            onPrebuiltReceiveCallAccepted(zimCallID, uiKitUser, zimCallUserInfo.extendedData);
        });
    }

    private void processRejectedUsers(ZIMCallUserStateChangeInfo info, String zimCallID) {
        ZIMUserInfo selfUserInfo = ZegoSignalingPlugin.getInstance().getUserInfo();

        List<ZIMCallUserInfo> rejectZIMUsers = info.callUserList.stream().filter(zimCallUserInfo -> {
            boolean notSelf = !Objects.equals(selfUserInfo.userID, zimCallUserInfo.userID);
            boolean isReject = zimCallUserInfo.state == ZIMCallUserState.REJECTED;
            return notSelf && isReject;
        }).collect(Collectors.toList());

        rejectZIMUsers.forEach(zimCallUserInfo -> {
            ZegoUIKitUser rejectUser = getUiKitUserFromUserID(zimCallUserInfo.userID);
            onPrebuiltReceiveCallRejected(zimCallID, rejectUser, zimCallUserInfo.extendedData);
        });
    }

    private void onPrebuiltReceiveCallNoResponse(String zimCallID, List<ZegoUIKitUser> invitees) {
        if (callInvitationData != null && Objects.equals(zimCallID, callInvitationData.invitationID)) {
            ZIMUserInfo selfUserInfo = ZegoSignalingPlugin.getInstance().getUserInfo();
            ZIMCallInfo zimCallInfo = ZegoSignalingPlugin.getInstance().getZIMCallInfo(zimCallID);

            long activeUserCount = zimCallInfo.callUserList.stream().filter(new Predicate<ZIMCallUserInfo>() {
                @Override
                public boolean test(ZIMCallUserInfo zimCallUserInfo) {
                    boolean notSelf = !Objects.equals(zimCallUserInfo.userID, selfUserInfo.userID);
                    boolean activeUser = (zimCallUserInfo.state == ZIMCallUserState.INVITING
                        || zimCallUserInfo.state == ZIMCallUserState.ACCEPTED
                        || zimCallUserInfo.state == ZIMCallUserState.RECEIVED);

                    return notSelf && activeUser;
                }
            }).count();

            if (activeUserCount == 0) {
                if (zimCallInfo.callUserList.size() > 2) {
                    setCallState(NONE);
                } else {
                    setCallState(NONE_CALL_NO_REPLY);
                }
            }
            notifyOutgoingCallTimeout(invitees, zimCallID);
            clearPushMessage();
        }
    }

    private void onPrebuiltReceiveCallAccepted(String zimCallID, ZegoUIKitUser invitee, String data) {
        if (callInvitationData != null && Objects.equals(zimCallID, callInvitationData.invitationID)) {
            setCallState(CONNECTED);
            stopRingTone();
            notifyOutgoingCallAccepted(invitee, zimCallID);
        }
    }

    private void onPrebuiltReceiveCallRejected(String zimCallID, ZegoUIKitUser invitee, String data) {
        if (callInvitationData != null && Objects.equals(zimCallID, callInvitationData.invitationID)) {
            ZIMUserInfo selfUserInfo = ZegoSignalingPlugin.getInstance().getUserInfo();
            ZIMCallInfo zimCallInfo = ZegoSignalingPlugin.getInstance().getZIMCallInfo(zimCallID);

            long activeUserCount = zimCallInfo.callUserList.stream().filter(new Predicate<ZIMCallUserInfo>() {
                @Override
                public boolean test(ZIMCallUserInfo zimCallUserInfo) {
                    boolean notSelf = !Objects.equals(zimCallUserInfo.userID, selfUserInfo.userID);
                    boolean activeUser = (zimCallUserInfo.state == ZIMCallUserState.INVITING
                        || zimCallUserInfo.state == ZIMCallUserState.ACCEPTED
                        || zimCallUserInfo.state == ZIMCallUserState.RECEIVED);

                    return notSelf && activeUser;
                }
            }).count();

            if (activeUserCount == 0) {
                if (zimCallInfo.callUserList.size() > 2) {
                    setCallState(NONE);
                } else {
                    JSONObject jsonObject = JsonUtil.getJsonObjectFromString(data);
                    String reason = JsonUtil.getStringValueFromJson(jsonObject, "reason", null);
                    if ("busy".equals(reason)) {
                        setCallState(NONE_REJECTED_BUSY);
                    } else {
                        setCallState(NONE_REJECTED);
                    }

                }
            }

            notifyOutgoingCallRejected0rDeclined(invitee, data, zimCallID);
        }
    }

    public void zimCallInvitationEnded(ZIM zim, ZIMCallInvitationEndedInfo info, String callID) {
        if (callInvitationData != null && Objects.equals(callID, callInvitationData.invitationID)) {
            setCallState(NONE_HANG_UP);
            CallInvitationServiceImpl.getInstance().endCall();
        }
    }

    public void notifyIncomingCallReceived(String zimCallID, ZegoUIKitUser inviter, int type) {
        ZegoInvitationCallListener invitationCallListener = ZegoUIKitPrebuiltCallService.events.invitationEvents.getInvitationListener();
        if (invitationCallListener != null) {
            ZIMCallInfo zimCallInfo = ZegoSignalingPlugin.getInstance().getZIMCallInfo(zimCallID);
            List<ZegoCallUser> callUserList = zimCallInfo.callUserList.stream()
                .filter(zimCallUserInfo -> !Objects.equals(zimCallInfo.caller, zimCallUserInfo.userID))
                .map(zimCallUserInfo -> {
                    ZIMUserFullInfo memoryUserInfo = ZegoSignalingPlugin.getInstance()
                        .getMemoryUserInfo(zimCallUserInfo.userID);
                    if (memoryUserInfo == null) {
                        return new ZegoCallUser(zimCallUserInfo.userID, zimCallUserInfo.userID);
                    } else {
                        return new ZegoCallUser(memoryUserInfo.baseInfo.userID, memoryUserInfo.baseInfo.userName);
                    }
                }).collect(Collectors.toList());

            ZegoCallType callType =
                type == ZegoCallType.VIDEO_CALL.value() ? ZegoCallType.VIDEO_CALL : ZegoCallType.VOICE_CALL;
            ZegoCallUser inviteCaller = new ZegoCallUser(inviter.userID, inviter.userName);

            invitationCallListener.onIncomingCallReceived(callInvitationData.callID, inviteCaller, callType,
                callUserList);
        }
    }

    public void notifyIncomingCallCanceled(ZegoUIKitUser inviter, String callID) {
        ZegoInvitationCallListener invitationCallListener = ZegoUIKitPrebuiltCallService.events.invitationEvents.getInvitationListener();
        if (invitationCallListener != null) {
            ZegoCallUser inviteCaller = new ZegoCallUser(inviter.userID, inviter.userName);
            invitationCallListener.onIncomingCallCanceled(callID, inviteCaller);
        }
    }

    public void notifyIncomingCallTimeout(ZegoUIKitUser inviter, String callID) {
        ZegoInvitationCallListener invitationCallListener = ZegoUIKitPrebuiltCallService.events.invitationEvents.getInvitationListener();
        if (invitationCallListener != null) {
            ZegoCallUser inviteCaller = new ZegoCallUser(inviter.userID, inviter.userName);
            invitationCallListener.onIncomingCallTimeout(callID, inviteCaller);
        }
    }

    public void notifyOutgoingCallAccepted(ZegoUIKitUser uiKitUser, String callID) {
        ZegoInvitationCallListener invitationCallListener = ZegoUIKitPrebuiltCallService.events.invitationEvents.getInvitationListener();
        if (invitationCallListener != null) {
            ZegoCallUser inviteCaller = new ZegoCallUser(uiKitUser.userID, uiKitUser.userName);
            invitationCallListener.onOutgoingCallAccepted(callID, inviteCaller);
        }
    }

    public void notifyOutgoingCallRejected0rDeclined(ZegoUIKitUser uiKitUser, String data, String callID) {
        ZegoInvitationCallListener invitationCallListener = ZegoUIKitPrebuiltCallService.events.invitationEvents.getInvitationListener();
        if (invitationCallListener != null) {
            JSONObject jsonObject = JsonUtil.getJsonObjectFromString(data);
            String reason = JsonUtil.getStringValueFromJson(jsonObject, "reason", null);
            ZegoCallUser inviteCaller = new ZegoCallUser(uiKitUser.userID, uiKitUser.userName);
            if ("busy".equals(reason)) {
                invitationCallListener.onOutgoingCallRejectedCauseBusy(callID, inviteCaller);
            } else {
                invitationCallListener.onOutgoingCallDeclined(callID, inviteCaller);
            }
        }
    }

    public void notifyOutgoingCallTimeout(List<ZegoUIKitUser> invitees, String callID) {
        ZegoInvitationCallListener invitationCallListener = ZegoUIKitPrebuiltCallService.events.invitationEvents.getInvitationListener();
        if (invitationCallListener != null) {
            List<ZegoCallUser> callees = new ArrayList<>();
            for (ZegoUIKitUser user : invitees) {
                callees.add(new ZegoCallUser(user.userID, user.userName));
            }
            invitationCallListener.onOutgoingCallTimeout(callID, callees);
        }
    }

    public void sendInvitationWithUIChange(Activity activity, List<ZegoUIKitUser> invitees,
        ZegoInvitationType invitationType, String customData, int timeout, String callID,
        ZegoSignalingPluginNotificationConfig notificationConfig, PluginCallbackListener callbackListener) {

        sendInvitation(invitees, invitationType, customData, timeout, callID, notificationConfig,
            new PluginCallbackListener() {
                @Override
                public void callback(Map<String, Object> result) {
                    int code = (int) result.get("code");
                    String message = (String) result.get("message");
                    List<ZegoUIKitUser> errorInvitees = (List<ZegoUIKitUser>) result.get("errorInvitees");
                    if (code == 0) {
                        if (errorInvitees.isEmpty() || errorInvitees.size() != invitees.size()) {
                            CallInviteActivity.startOutgoingPage(activity);
                        }
                    }
                    if (callbackListener != null) {
                        callbackListener.callback(result);
                    }
                }
            });
    }


    public void sendInvitation(List<ZegoUIKitUser> invitees, ZegoInvitationType invitationType, String customData,
        int timeout, String callID, ZegoSignalingPluginNotificationConfig notificationConfig,
        PluginCallbackListener callbackListener) {

        List<String> idList = invitees.stream().map(zegoUIKitUser -> zegoUIKitUser.userID).collect(Collectors.toList());
        PrebuiltCallInviteExtendedData.Data data = generateCallExtData(invitees, callID, customData);
        ZegoSignalingPluginNotificationConfig pushConfig = generatePushConfig(invitees, invitationType,
            notificationConfig);
        pushRepository.setCallResourceID(pushConfig.getResourceID());

        ZegoUIKitPrebuiltCallInvitationConfig invitationConfig = CallInvitationServiceImpl.getInstance()
            .getCallInvitationConfig();
        if (invitationConfig.callingConfig == null || !invitationConfig.callingConfig.canInvitingInCalling) {
            // normal mode
            if (getCallState() > 0) {
                if (callbackListener != null) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("code", -3);
                    map.put("message", "You are not idle,cannot send invitation now");
                    map.put("invitationID", "");
                    callbackListener.callback(map);
                }
                return;
            }
            // pre set to reject other call when sending call
            setCallState(PrebuiltCallRepository.OUTGOING);
            ZegoUIKit.getSignalingPlugin()
                .sendInvitation(idList, timeout, invitationType.getValue(), gson.toJson(data), pushConfig, result -> {
                    int code = (int) result.get("code");
                    List<ZegoUIKitUser> errorInvitees = (List<ZegoUIKitUser>) result.get("errorInvitees");
                    String invitationID = (String) result.get("invitationID");
                    if (code == 0) {
                        if (errorInvitees == null || errorInvitees.size() < invitees.size()) {
                            onCallInvitationSentSucceed(invitees, invitationType, customData, invitationID, data);
                        }
                    } else {
                        setCallState(PrebuiltCallRepository.NONE);
                    }
                    if (callbackListener != null) {
                        callbackListener.callback(result);
                    }
                });
        } else {
            // advanced mode

            ZIMUserInfo localUser = zimBridge.getLocalUser();
            PrebuiltCallInviteExtendedData extendedDataObj = new PrebuiltCallInviteExtendedData();
            extendedDataObj.setType(invitationType.getValue());
            extendedDataObj.setInviterId(localUser.userID);
            extendedDataObj.setInviterName(localUser.userName);
            extendedDataObj.setData(gson.toJson(data));
            String extendedData = gson.toJson(extendedDataObj);

            if (callInvitationData == null) {
                // pre set to reject other call when sending call
                setCallState(PrebuiltCallRepository.OUTGOING);
                callInviteAdvanced(idList, extendedData, timeout, pushConfig, new ZIMCallInvitationSentCallback() {
                    @Override
                    public void onCallInvitationSent(String zimCallID, ZIMCallInvitationSentInfo info,
                        ZIMError errorInfo) {
                        if (errorInfo.code == ZIMErrorCode.SUCCESS) {
                            if (info.errorUserList.size() < idList.size()) {
                                onCallInvitationSentSucceed(invitees, invitationType, customData, zimCallID, data);
                            }
                        } else {
                            setCallState(PrebuiltCallRepository.NONE);
                        }

                        Map<String, Object> result = new HashMap<>();
                        result.put("code", errorInfo.code.value());
                        result.put("message", errorInfo.message);
                        result.put("invitationID", zimCallID);
                        List<String> errorUserIDList = info.errorUserList.stream()
                            .map(zimErrorUserInfo -> zimErrorUserInfo.userID).collect(Collectors.toList());
                        List<ZegoUIKitUser> errorUserList = invitees.stream()
                            .filter(zegoUIKitUser -> errorUserIDList.contains(zegoUIKitUser.userID))
                            .collect(Collectors.toList());
                        result.put("errorInvitees", errorUserList);

                        if (callbackListener != null) {
                            callbackListener.callback(result);
                        }
                    }
                });
            } else {
                boolean onlyInitiatorCanInvite = invitationConfig.callingConfig.onlyInitiatorCanInvite;
                String zimCallID = callInvitationData.invitationID;
                boolean selfIsNotCaller = !Objects.equals(callInvitationData.inviter.userID, localUser.userID);
                if (onlyInitiatorCanInvite && selfIsNotCaller) {
                    // not initiator,cannot calling invite
                    Map<String, Object> result = new HashMap<>();
                    result.put("code", ZIMErrorCode.CALL_ERROR);
                    result.put("invitationID", zimCallID);
                    result.put("message", "Only Initiator Can Invite");
                    if (callbackListener != null) {
                        callbackListener.callback(result);
                    }
                } else {
                    callingInvite(idList, extendedData, zimCallID, pushConfig, new ZIMCallingInvitationSentCallback() {
                        @Override
                        public void onCallingInvitationSent(String zimCallID, ZIMCallingInvitationSentInfo info,
                            ZIMError errorInfo) {

                            Map<String, Object> result = new HashMap<>();
                            result.put("code", errorInfo.code.value());
                            result.put("message", errorInfo.message);
                            result.put("invitationID", zimCallID);
                            if (info != null) {
                                List<String> errorUserIDList = info.errorUserList.stream()
                                    .map(zimErrorUserInfo -> zimErrorUserInfo.userID).collect(Collectors.toList());
                                List<ZegoUIKitUser> errorUserList = invitees.stream()
                                    .filter(zegoUIKitUser -> errorUserIDList.contains(zegoUIKitUser.userID))
                                    .collect(Collectors.toList());
                                result.put("errorInvitees", errorUserList);
                            }

                            if (callbackListener != null) {
                                callbackListener.callback(result);
                            }
                        }
                    });
                }
            }
        }
    }

    private void onCallInvitationSentSucceed(List<ZegoUIKitUser> invitees, ZegoInvitationType invitationType,
        String customData, String invitationID, Data data) {
        callInvitationData = new ZegoCallInvitationData();
        callInvitationData.type = invitationType.getValue();
        callInvitationData.inviter = ZegoUIKit.getLocalUser();
        callInvitationData.caller = ZegoUIKit.getLocalUser();
        callInvitationData.invitationID = invitationID;
        callInvitationData.callID = data.getCallId();
        callInvitationData.customData = customData;
        callInvitationData.invitees = invitees;

        CallInvitationServiceImpl.getInstance().generateCallConfigFromInvite(callInvitationData);

        playOutgoingRingTone();
    }

    private PrebuiltCallInviteExtendedData.Data generateCallExtData(List<ZegoUIKitUser> uiKitUsers, String callID,
        String customData) {
        PrebuiltCallInviteExtendedData.Data data = new Data();
        String roomId = TextUtils.isEmpty(callID) ? PrebuiltCallUtil.generatePrebuiltCallRoomID() : callID;
        data.setCallId(roomId);
        data.setCustomData(customData);
        List<Invitee> collect = uiKitUsers.stream().map(zegoUIKitUser -> {
            Invitee invitee = new Invitee();
            invitee.setUserId(zegoUIKitUser.userID);
            invitee.setUserName(zegoUIKitUser.userName);
            return invitee;
        }).collect(Collectors.toList());
        data.setInvitees(collect);
        return data;
    }

    private ZegoSignalingPluginNotificationConfig generatePushConfig(List<ZegoUIKitUser> invitees,
        ZegoInvitationType invitationType, ZegoSignalingPluginNotificationConfig notificationConfig) {
        ZegoSignalingPluginNotificationConfig pushNotificationConfig =
            notificationConfig == null ? new ZegoSignalingPluginNotificationConfig() : notificationConfig;

        String resourceID = pushRepository.getCallResourceID();
        boolean isVideoCall = invitationType == ZegoInvitationType.VIDEO_CALL;
        ZIMUserInfo localUser = zimBridge.getLocalUser();
        boolean isGroup = invitees.size() > 1;

        if (TextUtils.isEmpty(pushNotificationConfig.getTitle())) {
            String offlineTitle = PrebuiltCallNotificationManager.getBackgroundNotificationTitle(isVideoCall, isGroup,
                localUser.userName);
            pushNotificationConfig.setTitle(offlineTitle);
        }
        if (TextUtils.isEmpty(pushNotificationConfig.getMessage())) {
            String offlineMessage = PrebuiltCallNotificationManager.getBackgroundNotificationMessage(isVideoCall,
                isGroup);
            pushNotificationConfig.setMessage(offlineMessage);
        }
        if (TextUtils.isEmpty(pushNotificationConfig.getResourceID())) {
            pushNotificationConfig.setResourceID(resourceID);
        }
        return pushNotificationConfig;
    }

    public void cancelInvitation(PluginCallbackListener callbackListener) {
        if (getCallInvitationData() != null) {
            List<String> idList = GenericUtils.map(getCallInvitationData().invitees, uiKitUser -> uiKitUser.userID);
            cancelInvitation(idList, getCallInvitationData().invitationID, "", null, callbackListener);
        }
    }

    public void cancelInvitation(List<String> invitees, String invitationID, String data,
        ZegoSignalingPluginNotificationConfig pushConfig, PluginCallbackListener callbackListener) {
        if (pushConfig == null) {
            pushConfig = new ZegoSignalingPluginNotificationConfig();
            if (TextUtils.isEmpty(pushRepository.getCallResourceID())) {
                pushConfig.setResourceID("zegouikit_call");
            } else {
                pushConfig.setResourceID(pushRepository.getCallResourceID());
            }
        }

        ZegoUIKit.getSignalingPlugin()
            .callCancel(invitees, invitationID, data, pushConfig, new PluginCallbackListener() {
                @Override
                public void callback(Map<String, Object> result) {
                    if (callbackListener != null) {
                        callbackListener.callback(result);
                    }
                }
            });
        setCallState(PrebuiltCallRepository.NONE_CANCELED);
        stopRingTone();
    }

    public void rejectInvitation(PluginCallbackListener callbackListener) {
        if (getCallInvitationData() != null) {
            String string = PrebuiltCallUtil.getManualRejectJsonString(getCallInvitationData().invitationID);
            rejectInvitation(getCallInvitationData().invitationID, string, callbackListener);
        }
    }

    public void rejectInvitation(String invitationID, String data, PluginCallbackListener callbackListener) {
        ZegoUIKit.getSignalingPlugin().callReject(invitationID, data, new PluginCallbackListener() {
            @Override
            public void callback(Map<String, Object> result) {
                if (callbackListener != null) {
                    callbackListener.callback(result);
                }
            }
        });
        setCallState(PrebuiltCallRepository.NONE_REJECTED);
        stopRingTone();
    }

    public void acceptInvitation(PluginCallbackListener callbackListener) {
        if (getCallInvitationData() != null) {
            acceptInvitation(getCallInvitationData().invitationID, "", callbackListener);
        }
    }

    public void acceptInvitation(String invitationID, String data, PluginCallbackListener callbackListener) {
        ZegoUIKit.getSignalingPlugin().callAccept(invitationID, data, new PluginCallbackListener() {
            @Override
            public void callback(Map<String, Object> result) {
                if (callbackListener != null) {
                    callbackListener.callback(result);
                }
            }
        });
        setCallState(PrebuiltCallRepository.CONNECTED);
        stopRingTone();
    }

    private ZIMEventHandler zimEventHandler = new ZIMEventHandler() {

        public void onCallInvitationCreated(ZIM zim, ZIMCallInvitationCreatedInfo info, String callID) {
            super.onCallInvitationCreated(zim, info, callID);
            Timber.d(
                "onCallInvitationCreated() called with: zim = [" + zim + "], info = [" + info + "], callID = [" + callID
                    + "]");
            zimCallInvitationCreated(zim, info, callID);
        }

        @Override
        public void onCallInvitationReceived(ZIM zim, ZIMCallInvitationReceivedInfo info, String callID) {
            super.onCallInvitationReceived(zim, info, callID);
            Timber.d("onCallInvitationReceived() called with: zim = [" + zim + "], info = [" + info + "], callID = ["
                + callID + "]");
            zimCallInvitationReceived(zim, info, callID);
        }

        @Override
        public void onCallInvitationCancelled(ZIM zim, ZIMCallInvitationCancelledInfo info, String callID) {
            super.onCallInvitationCancelled(zim, info, callID);
            Timber.d("onCallInvitationCancelled() called with: zim = [" + zim + "], info = [" + info + "], callID = ["
                + callID + "]");
            zimCallInvitationCancelled(zim, info, callID);
        }

        @Override
        public void onCallInvitationTimeout(ZIM zim, ZIMCallInvitationTimeoutInfo info, String callID) {
            super.onCallInvitationTimeout(zim, info, callID);
            Timber.d(
                "onCallInvitationTimeout() called with: zim = [" + zim + "], info = [" + info + "], callID = [" + callID
                    + "]");
            zimCallInvitationTimeout(zim, info, callID);
        }

        @Override
        public void onCallUserStateChanged(ZIM zim, ZIMCallUserStateChangeInfo info, String callID) {
            super.onCallUserStateChanged(zim, info, callID);
            Timber.d(
                "onCallUserStateChanged() called with: zim = [" + zim + "], info = [" + info + "], callID = [" + callID
                    + "]");
            zimCallUserStateChanged(zim, info, callID);
        }

        @Override
        public void onCallInvitationEnded(ZIM zim, ZIMCallInvitationEndedInfo info, String callID) {
            super.onCallInvitationEnded(zim, info, callID);
            Timber.d(
                "onCallInvitationEnded() called with: zim = [" + zim + "], info = [" + info + "], callID = [" + callID
                    + "]");
            zimCallInvitationEnded(zim, info, callID);
        }

        @Deprecated
        public void onCallInvitationRejected(ZIM zim, ZIMCallInvitationRejectedInfo info, String callID) {
            Timber.d("onCallInvitationRejected() called with: zim = [" + zim + "], info = [" + info + "], callID = ["
                + callID + "]");
        }


        @Deprecated
        public void onCallInvitationAccepted(ZIM zim, ZIMCallInvitationAcceptedInfo info, String callID) {
            Timber.d("onCallInvitationAccepted() called with: zim = [" + zim + "], info = [" + info + "], callID = ["
                + callID + "]");
        }

        @Deprecated
        public void onCallInviteesAnsweredTimeout(ZIM zim, ArrayList<String> invitees, String callID) {
            Timber.d("onCallInviteesAnsweredTimeout() called with: zim = [" + zim + "], invitees = [" + invitees
                + "], callID = [" + callID + "]");
        }

        @Override
        public void onCallInvitationTimeout(ZIM zim, String callID) {
            super.onCallInvitationTimeout(zim, callID);
            Timber.d("onCallInvitationTimeout() called with: zim = [" + zim + "], callID = [" + callID + "]");
        }
    };

    private void callAccept(String callID, String extendedData, ZIMCallAcceptanceSentCallback callback) {
        ZIMCallAcceptConfig config = new ZIMCallAcceptConfig();
        zimBridge.callAccept(callID, config, callback);

        setCallState(PrebuiltCallRepository.CONNECTED);
        stopRingTone();
    }

    private void callingInvite(List<String> invitees, String extendedData, String zimCallID,
        ZegoSignalingPluginNotificationConfig notificationConfig, ZIMCallingInvitationSentCallback callback) {
        ZIMPushConfig pushConfig = new ZIMPushConfig();
        if (notificationConfig != null) {
            pushConfig.title = notificationConfig.getTitle();
            pushConfig.content = notificationConfig.getMessage();
            pushConfig.resourcesID = notificationConfig.getResourceID();
            pushConfig.payload = extendedData;
        }
        ZIMCallingInviteConfig config = new ZIMCallingInviteConfig();
        config.pushConfig = pushConfig;
        zimBridge.callingInvite(invitees, zimCallID, config, callback);
    }

    private void callInviteAdvanced(List<String> invitees, String extendedData, int timeout,
        ZegoSignalingPluginNotificationConfig notificationConfig, ZIMCallInvitationSentCallback callback) {
        ZIMPushConfig pushConfig = new ZIMPushConfig();
        if (notificationConfig != null) {
            pushConfig.title = notificationConfig.getTitle();
            pushConfig.content = notificationConfig.getMessage();
            pushConfig.resourcesID = notificationConfig.getResourceID();
            pushConfig.payload = extendedData;
        }
        callInvite(invitees, true, extendedData, timeout, pushConfig, callback);
    }

    private void callInvite(List<String> invitees, boolean advanced, String extendedData, int timeout,
        ZIMPushConfig pushConfig, ZIMCallInvitationSentCallback callback) {

        ZIMCallInviteConfig config = new ZIMCallInviteConfig();
        if (advanced) {
            config.mode = ZIMCallInvitationMode.ADVANCED;
        }
        config.extendedData = extendedData;
        config.timeout = timeout;
        config.pushConfig = pushConfig;

        zimBridge.callInvite(invitees, config, callback);
    }

    public void callCancel(List<String> invitees, String zimCallID, String resourceID,
        ZIMCallCancelSentCallback callback) {
        ZIMCallCancelConfig config = new ZIMCallCancelConfig();
        config.pushConfig = new ZIMPushConfig();
        config.pushConfig.resourcesID = resourceID;

        zimBridge.callCancel(invitees, zimCallID, config, callback);
        setCallState(PrebuiltCallRepository.NONE_CANCELED);
        stopRingTone();
    }

    public void callEnd(String zimCallID, String resourcesID, ZIMCallEndSentCallback callback) {
        ZIMCallEndConfig config = new ZIMCallEndConfig();
        config.pushConfig = new ZIMPushConfig();
        config.pushConfig.resourcesID = resourcesID;

        zimBridge.callEnd(zimCallID, config, callback);

        setCallState(PrebuiltCallRepository.NONE);
        stopRingTone();
    }

    public void callQuit(String zimCallID, String resourcesID, ZIMCallQuitSentCallback callback) {
        ZIMCallQuitConfig config = new ZIMCallQuitConfig();
        config.pushConfig = new ZIMPushConfig();
        config.pushConfig.resourcesID = resourcesID;

        zimBridge.callQuit(zimCallID, config, callback);
    }

    // if callInvitationData was cleared before,no request will send
    public void onPrebuiltRoomLeft() {
        ZegoUIKitPrebuiltCallInvitationConfig invitationConfig = CallInvitationServiceImpl.getInstance()
            .getCallInvitationConfig();
        if (invitationConfig != null && callInvitationData != null) {
            String zimCallID = callInvitationData.invitationID;
            String callResourceID = pushRepository.getCallResourceID();
            ZIMCallInfo zimCallInfo = zimBridge.getZIMCallInfo(zimCallID);
            ZIMUserInfo localUser = zimBridge.getLocalUser();
            if (zimCallInfo.mode == ZIMCallInvitationMode.GENERAL) {
                if (Objects.equals(localUser.userID, zimCallInfo.inviter)) {
                    List<String> collect = zimCallInfo.callUserList.stream().filter(zimCallUserInfo -> {
                        boolean notSelf = !Objects.equals(localUser.userID, zimCallUserInfo.userID);
                        boolean received = zimCallUserInfo.state == ZIMCallUserState.RECEIVED;
                        return notSelf && received;
                    }).map(zimCallUserInfo -> zimCallUserInfo.userID).collect(Collectors.toList());
                    if (!collect.isEmpty()) {
                        callCancel(collect, zimCallInfo.callID, callResourceID, null);
                    }
                }
            } else {
                //                if (invitationConfig.endCallWhenInitiatorLeave && Objects.equals(localUser.userID, zimCallInfo.inviter)) {
                //                    callEnd(zimCallInfo.callID, callResourceID, null);
                //                } else {
                //                    callQuit(zimCallID, callResourceID, null);
                //                }
                // endCallWhenInitiatorLeave was processed in onPrebuiltCallRoomUserLeft.
                callQuit(zimCallID, callResourceID, null);
            }
        }

        if (getCallState() > 0) {
            setCallState(PrebuiltCallRepository.NONE);
        }
        pushRepository.clearPushMessage();
    }

    public void onPrebuiltUserLogin() {
        zimBridge.registerZIMEventHandler(zimEventHandler);
        pushRepository.onPrebuiltUserLogin();
    }

    public void onPrebuiltUserLogout() {
        removePrebuiltLoginUserData();
        removeCallbacks();
        pushRepository.onPrebuiltUserLogout();
    }

    private void removePrebuiltLoginUserData() {
        setCallState(PrebuiltCallRepository.NONE);
        clearInvitationData();
        clearCallStateListener();
    }

    private void removeCallbacks() {
        zimBridge.unregisterZIMEventHandler(zimEventHandler);
    }

    public void clearPushMessage() {
        pushRepository.clearPushMessage();
    }

    public void setCallResourceID(String resourceID) {
        pushRepository.setCallResourceID(resourceID);
    }

    public ZIMPushMessage getPushMessage() {
        return pushRepository.getPushMessage();
    }

    public void setPushMessage(ZIMPushMessage pushMessage) {
        if (pushMessage != null && !TextUtils.isEmpty(pushMessage.payLoad)) {
            PrebuiltCallInviteExtendedData extendedData = gson.fromJson(pushMessage.payLoad,
                PrebuiltCallInviteExtendedData.class);
            pushMessage.zimExtendedData = extendedData;
            if (extendedData != null && !TextUtils.isEmpty(extendedData.getData())) {
                pushMessage.callData = gson.fromJson(extendedData.getData(), Data.class);
            }
        }
        pushRepository.setPushMessage(pushMessage);
    }

    public void disableFCMPush() {
        pushRepository.disableFCMPush();
    }

    public void enableHWPush(String hwAppID) {
        pushRepository.enableHWPush(hwAppID);
    }

    public void enableMiPush(String miAppID, String miAppKey) {
        pushRepository.enableMiPush(miAppID, miAppKey);
    }

    public void enableVivoPush(String vivoAppID, String vivoAppKey) {
        pushRepository.enableVivoPush(vivoAppID, vivoAppKey);
    }

    public void enableOppoPush(String oppoAppID, String oppoAppKey, String oppoAppSecret) {
        pushRepository.enableOppoPush(oppoAppID, oppoAppKey, oppoAppSecret);
    }

    public boolean isOtherPushEnable() {
        return pushRepository.isOtherPushEnable();
    }

    public boolean isFCMPushEnable() {
        return pushRepository.isFCMPushEnable();
    }

    public void enableFCMPush() {
        pushRepository.enableFCMPush();
    }

    public void setAppType(int appType) {
        pushRepository.setAppType(appType);
    }

    public void setNotificationAction(String action) {
        pushRepository.setNotificationAction(action);
    }

    public void onPrebuiltCallRoomUserLeft(List<String> userList, String roomID) {
        ZegoUIKitPrebuiltCallInvitationConfig invitationConfig = CallInvitationServiceImpl.getInstance()
            .getCallInvitationConfig();
        if (invitationConfig != null && callInvitationData != null) {
            String zimCallID = callInvitationData.invitationID;
            ZIMCallInfo zimCallInfo = zimBridge.getZIMCallInfo(zimCallID);

            if (userList.contains(zimCallInfo.caller) && invitationConfig.endCallWhenInitiatorLeave) {
                CallInvitationServiceImpl.getInstance().endCall();
            }
        }
    }
}
