package com.zegocloud.uikit.prebuilt.call.invite.internal;

import android.app.Activity;
import android.app.Application;
import android.text.TextUtils;

import com.zegocloud.uikit.ZegoUIKit;
import com.zegocloud.uikit.plugin.common.PluginCallbackListener;
import com.zegocloud.uikit.plugin.invitation.ZegoInvitationType;
import com.zegocloud.uikit.prebuilt.call.R;
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallFragment;
import com.zegocloud.uikit.prebuilt.call.invite.ZegoCallInvitationData;
import com.zegocloud.uikit.prebuilt.call.invite.ZegoUIKitPrebuiltCallConfigProvider;
import com.zegocloud.uikit.prebuilt.call.invite.ZegoUIKitPrebuiltCallInvitationConfig;
import com.zegocloud.uikit.service.defines.ZegoSignalingPluginNotificationConfig;
import com.zegocloud.uikit.service.defines.ZegoUIKitSignalingPluginInvitationListener;
import com.zegocloud.uikit.service.defines.ZegoScenario;
import com.zegocloud.uikit.service.defines.ZegoUIKitUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CallInvitationServiceImpl {

    private Application application;

    private CallInvitationServiceImpl() {
    }

    private static final class Holder {

        private static final CallInvitationServiceImpl INSTANCE = new CallInvitationServiceImpl();
    }

    public static CallInvitationServiceImpl getInstance() {
        return CallInvitationServiceImpl.Holder.INSTANCE;
    }

    public AppActivityManager appActivityManager;
    private CallInvitationDialog invitationDialog;
    private ZegoUIKitPrebuiltCallConfigProvider provider;
    public static final int NONE_CALL_NO_REPLY = -5;
    public static final int NONE_RECEIVE_MISSED = -4;
    public static final int NONE_REJECTED = -3;
    public static final int NONE_CANCELED = -2;
    public static final int NONE_HANG_UP = -1;
    public static final int NONE = 0;
    public static final int OUTGOING = 1;
    public static final int CONNECTED = 2;
    public static final int INCOMING = 3;
    private int callState = NONE;
    private ZegoCallInvitationData callInvitationData;
    private Map<ZegoUIKitUser, CallInvitationState> callUserStates = new HashMap<>();
    private boolean isInit;
    private List<CallStateListener> callStateListeners;
    private ZegoInvitationCallListener invitationCallListenerList;
    private ZegoUIKitPrebuiltCallInvitationConfig config;
    private OutgoingCallButtonListener outgoingCallButtonListener;
    private IncomingCallButtonListener incomingCallButtonListener;
    private ZegoUIKitPrebuiltCallFragment zegoUIKitPrebuiltCallFragment;
    private String callID;

    private ZegoUIKitSignalingPluginInvitationListener invitationListener = new ZegoUIKitSignalingPluginInvitationListener() {
        @Override
        public void onInvitationReceived(ZegoUIKitUser inviter, int type, String data) {
            if (callState > 0) {
                JSONObject jsonObject = new JSONObject();
                try {
                    JSONObject dataJson = new JSONObject(data);
                    jsonObject.put("reason", "busy");
                    String invitationID = getStringFromJson(dataJson, "invitationID");
                    jsonObject.put("invitationID", invitationID);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                ZegoUIKit.getSignalingPlugin().refuseInvitation(inviter.userID, jsonObject.toString(), null);
                return;
            }
            Activity topActivity = appActivityManager.getTopActivity();
            if (topActivity != null) {
                try {
                    JSONObject jsonObject = new JSONObject(data);
                    JSONArray invitees = jsonObject.getJSONArray("invitees");
                    List<ZegoUIKitUser> list = new ArrayList<>();
                    for (int i = 0; i < invitees.length(); i++) {
                        JSONObject invitee = invitees.getJSONObject(i);
                        String user_id = getStringFromJson(invitee, "user_id");
                        String user_name = getStringFromJson(invitee, "user_name");
                        list.add(new ZegoUIKitUser(user_id, user_name));
                    }
                    String call_id = getStringFromJson(jsonObject, "call_id");
                    String customData = getStringFromJson(jsonObject, "custom_data");
                    RingtoneManager.playRingTone(true);
                    callInvitationData = new ZegoCallInvitationData(call_id, type, list, inviter, customData);
                    for (ZegoUIKitUser zegoUIKitUser : list) {
                        changeUserState(zegoUIKitUser, CallInvitationState.WAITING);
                    }
                    setCallState(INCOMING);
                    invitationDialog = new CallInvitationDialog(topActivity, callInvitationData);
                    invitationDialog.show();
                    callID = call_id;
                    showNotification(callInvitationData);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            notifyIncomingCallReceived(inviter, type, data);
        }

        @Override
        public void onInvitationTimeout(ZegoUIKitUser inviter, String data) {
            hideDialog();
            setCallState(NONE_RECEIVE_MISSED);
            notifyIncomingCallTimeout(inviter);
        }

        @Override
        public void onInvitationResponseTimeout(List<ZegoUIKitUser> invitees, String data) {
            for (ZegoUIKitUser invitee : invitees) {
                changeUserState(invitee, CallInvitationState.TIMEOUT);
            }
            if (callInvitationData != null) {
                if (callInvitationData.invitees.size() > 1) {
                    boolean allChecked = true;
                    for (ZegoUIKitUser uiKitUser : callInvitationData.invitees) {
                        if (callUserStates.get(uiKitUser) == CallInvitationState.WAITING) {
                            allChecked = false;
                            break;
                        }
                    }
                    if (allChecked) {
                        setCallState(NONE);
                    }
                } else {
                    setCallState(NONE_CALL_NO_REPLY);
                }
            }
            notifyOutgoingCallTimeout(invitees);
        }

        @Override
        public void onInvitationAccepted(ZegoUIKitUser invitee, String data) {
            changeUserState(invitee, CallInvitationState.ACCEPT);
            setCallState(CONNECTED);
            clearInvitationData();
            notifyOutgoingCallAccepted(invitee);
        }

        @Override
        public void onInvitationRefused(ZegoUIKitUser invitee, String data) {
            changeUserState(invitee, CallInvitationState.REFUSE);
            if (callInvitationData != null) {
                if (callInvitationData.invitees.size() > 1) {
                    boolean allChecked = true;
                    for (ZegoUIKitUser uiKitUser : callInvitationData.invitees) {
                        if (callUserStates.get(uiKitUser) == CallInvitationState.WAITING) {
                            allChecked = false;
                            break;
                        }
                    }
                    if (allChecked) {
                        setCallState(NONE);
                    }
                } else {
                    setCallState(NONE_REJECTED);
                }
            }
            notifyOutgoingCallRejected0rDeclined(invitee, data);
        }

        @Override
        public void onInvitationCanceled(ZegoUIKitUser inviter, String data) {
            hideDialog();
            if (callState == CONNECTED) {
                return;
            }
            setCallState(NONE_CANCELED);

            notifyIncomingCallCanceled(inviter);
        }
    };

    private String getStringFromJson(JSONObject jsonObject, String key) throws JSONException {
        String value = "";
        if (jsonObject.has(key)) {
            value = jsonObject.getString(key);
        }
        return value;
    }

    private void changeUserState(ZegoUIKitUser invitee, CallInvitationState accept) {
        callUserStates.put(invitee, accept);
    }

    private void hideDialog() {
        if (invitationDialog != null) {
            invitationDialog.hide();
        }
        invitationDialog = null;
    }

    public void init(Application application, long appID, String appSign, String userID, String userName,
                     ZegoUIKitPrebuiltCallInvitationConfig config) {
        this.config = config;
        isInit = true;
        ZegoUIKit.installPlugins(config.plugins);
        ZegoUIKit.init(application, appID, appSign, ZegoScenario.GENERAL);
        ZegoUIKit.login(userID, userName);
        ZegoUIKit.getSignalingPlugin().login(userID, userName, null);
        this.application = application;

        appActivityManager = new AppActivityManager();
        this.application.registerActivityLifecycleCallbacks(appActivityManager);
        ZegoUIKit.getSignalingPlugin().addInvitationListener(invitationListener);
    }

    public void unInit() {
        if (!config.notifyWhenAppRunningInBackgroundOrQuit) {
            ZegoUIKit.logout();
            ZegoUIKit.getSignalingPlugin().logout();
        }
        this.application.unregisterActivityLifecycleCallbacks(appActivityManager);
        this.application = null;
        ZegoUIKit.getSignalingPlugin().removeInvitationListener(invitationListener);
    }

    public void setPrebuiltConfigProvider(ZegoUIKitPrebuiltCallConfigProvider provider) {
        this.provider = provider;
    }

    public ZegoUIKitPrebuiltCallConfigProvider getProvider() {
        return provider;
    }

    public int getCallState() {
        return callState;
    }

    public void setCallState(int callState) {
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

    private void clearInvitationData() {
        RingtoneManager.stopRingTone();
        callInvitationData = null;
        callUserStates.clear();
    }

    public void addCallStateListener(CallStateListener callStateListener) {
        if (callStateListeners == null) {
            callStateListeners = new ArrayList<>();
        }
        this.callStateListeners.add(callStateListener);
    }

    public void removeCallStateListener(CallStateListener callStateListener) {
        callStateListeners.remove(callStateListener);
    }

    public void leaveRoom() {
        if (isInit) {
            if (callState == OUTGOING) {
                if (callInvitationData != null) {
                    List<String> waitedUserIDs = new ArrayList<>();
                    for (ZegoUIKitUser invitee : callInvitationData.invitees) {
                        if (callUserStates.get(invitee) == CallInvitationState.WAITING) {
                            waitedUserIDs.add(invitee.userID);
                        }
                    }
                    if (!waitedUserIDs.isEmpty()) {
                        cancelInvitation(waitedUserIDs, "", null);
                    }
                }
            }
            if (CallInvitationServiceImpl.getInstance().getCallState() > 0) {
                CallInvitationServiceImpl.getInstance().setCallState(CallInvitationServiceImpl.NONE);
            }
        }
    }

    public void sendInvitation(String callID,List<String> invitees, int timeout, int type, String data,
                               ZegoSignalingPluginNotificationConfig notificationConfig, PluginCallbackListener callbackListener) {
        if (callState > 0) {
            return;
        }
        this.callID = callID;
        ZegoUIKit.getSignalingPlugin().sendInvitation(invitees, timeout, type, data, notificationConfig, new PluginCallbackListener() {
            @Override
            public void callback(Map<String, Object> result) {
                int code = (int) result.get("code");
                List<ZegoUIKitUser> errorInvitees = (List<ZegoUIKitUser>) result.get("errorInvitees");
                if (code == 0 && errorInvitees != null && errorInvitees.size() < invitees.size()) {
                    try {
                        JSONObject jsonObject = new JSONObject(data);
                        JSONArray inviteUsers = jsonObject.getJSONArray("invitees");
                        List<ZegoUIKitUser> list = new ArrayList<>();
                        for (int i = 0; i < inviteUsers.length(); i++) {
                            JSONObject invitee = inviteUsers.getJSONObject(i);
                            String user_id = invitee.getString("user_id");
                            String user_name = invitee.getString("user_name");
                            list.add(new ZegoUIKitUser(user_id, user_name));
                        }
                        String call_id = jsonObject.getString("call_id");
                        String customData = jsonObject.getString("custom_data");
                        callInvitationData = new ZegoCallInvitationData(call_id, type, list, ZegoUIKit.getLocalUser(),
                                customData);
                        for (ZegoUIKitUser zegoUIKitUser : list) {
                            CallInvitationState state;
                            if (errorInvitees.contains(zegoUIKitUser)) {
                                state = CallInvitationState.ERROR;
                            } else {
                                state = CallInvitationState.WAITING;
                            }
                            changeUserState(zegoUIKitUser, state);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    setCallState(CallInvitationServiceImpl.OUTGOING);
                    RingtoneManager.playRingTone(false);
                }
                if (callbackListener != null) {
                    callbackListener.callback(result);
                }
            }
        });

    }

    public void cancelInvitation(List<String> invitees, String data, PluginCallbackListener callbackListener) {
        ZegoUIKit.getSignalingPlugin().cancelInvitation(invitees, data, new PluginCallbackListener() {
            @Override
            public void callback(Map<String, Object> result) {
                if (callbackListener != null) {
                    callbackListener.callback(result);
                }
            }
        });
        for (Entry<ZegoUIKitUser, CallInvitationState> entry : callUserStates.entrySet()) {
            if (invitees.contains(entry.getKey().userID)) {
                entry.setValue(CallInvitationState.CANCEL);
            }
        }
        if (callInvitationData == null || callInvitationData.invitees.size() == 1) {
            setCallState(NONE_CANCELED);
        }
        RingtoneManager.stopRingTone();
    }

    public void refuseInvitation(String inviterID, String data, PluginCallbackListener callbackListener) {
        ZegoUIKit.getSignalingPlugin().refuseInvitation(inviterID, data, new PluginCallbackListener() {
            @Override
            public void callback(Map<String, Object> result) {
                if (callbackListener != null) {
                    callbackListener.callback(result);
                }
            }
        });
        setCallState(NONE_REJECTED);
        RingtoneManager.stopRingTone();
    }

    public void acceptInvitation(String inviterID, String data, PluginCallbackListener callbackListener) {
        ZegoUIKit.getSignalingPlugin().acceptInvitation(inviterID, data, new PluginCallbackListener() {
            @Override
            public void callback(Map<String, Object> result) {
                if (callbackListener != null) {
                    callbackListener.callback(result);
                }
            }
        });
        setCallState(CONNECTED);
        clearInvitationData();
    }

    public Application getApplication() {
        return application;
    }

    public void addIncomingCallButtonListener(IncomingCallButtonListener listener) {
        this.incomingCallButtonListener = listener;
    }

    public void addOutgoingCallButtonListener(OutgoingCallButtonListener listener) {
        this.outgoingCallButtonListener = listener;
    }

    public void onIncomingCallAcceptButtonPressed() {
        if (incomingCallButtonListener != null) {
            incomingCallButtonListener.onIncomingCallAcceptButtonPressed();
        }
    }

    public void onIncomingCallDeclineButtonPressed() {
        if (incomingCallButtonListener != null) {
            incomingCallButtonListener.onIncomingCallDeclineButtonPressed();
        }
    }

    public void onOutgoingCallCancelButtonPressed() {
        if (outgoingCallButtonListener != null) {
            outgoingCallButtonListener.onOutgoingCallCancelButtonPressed();
        }
    }

    public void addInvitationCallListener(ZegoInvitationCallListener listener) {
        this.invitationCallListenerList = listener;
    }

    public void addZegoUIKitPrebuiltCallFragment(ZegoUIKitPrebuiltCallFragment zegoUIKitPrebuiltCallFragment) {
        this.zegoUIKitPrebuiltCallFragment = zegoUIKitPrebuiltCallFragment;
    }

    public ZegoUIKitPrebuiltCallFragment getZegoUIKitPrebuiltCallFragment() {
        return zegoUIKitPrebuiltCallFragment;
    }

    public void removeInvitationCallListener() {
        this.invitationCallListenerList = null;
    }

    public ZegoUIKitPrebuiltCallInvitationConfig getConfig() {
        return config;
    }

    public void notifyIncomingCallReceived(ZegoUIKitUser inviter, int type, String extendedData) {
        if(invitationCallListenerList != null){
            try {
                JSONObject jsonObject = new JSONObject(extendedData);
                JSONArray invitees = jsonObject.getJSONArray("invitees");
                List<ZegoCallUser> list = new ArrayList<>();
                for (int i = 0; i < invitees.length(); i++) {
                    JSONObject invitee = invitees.getJSONObject(i);
                    String user_id = getStringFromJson(invitee, "user_id");
                    String user_name = getStringFromJson(invitee, "user_name");
                    list.add(new ZegoCallUser(user_id, user_name));
                }

                ZegoCallType callType = type == ZegoCallType.VIDEO_CALL.value() ? ZegoCallType.VIDEO_CALL : ZegoCallType.VOICE_CALL;
                ZegoCallUser inviteCaller = new ZegoCallUser(inviter.userID, inviter.userName);

                invitationCallListenerList.onIncomingCallReceived(callID, inviteCaller, callType, list);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void notifyIncomingCallCanceled(ZegoUIKitUser inviter) {
        if(invitationCallListenerList != null){
            ZegoCallUser inviteCaller = new ZegoCallUser(inviter.userID, inviter.userName);
            invitationCallListenerList.onIncomingCallCanceled(callID, inviteCaller);
        }
    }

    public void notifyIncomingCallTimeout(ZegoUIKitUser inviter) {
        if(invitationCallListenerList != null){
            ZegoCallUser inviteCaller = new ZegoCallUser(inviter.userID, inviter.userName);
            invitationCallListenerList.onIncomingCallTimeout(callID, inviteCaller);
        }
    }

    public void notifyOutgoingCallAccepted(ZegoUIKitUser uiKitUser) {
        if(invitationCallListenerList != null){
            ZegoCallUser inviteCaller = new ZegoCallUser(uiKitUser.userID, uiKitUser.userName);
            invitationCallListenerList.onOutgoingCallAccepted(callID, inviteCaller);
        }
    }

    public void notifyOutgoingCallRejected0rDeclined(ZegoUIKitUser uiKitUser, String data) {
        if(invitationCallListenerList != null){
            try {
                JSONObject jsonObject = new JSONObject(data);
                String reason = getStringFromJson(jsonObject, "reason");
                ZegoCallUser inviteCaller = new ZegoCallUser(uiKitUser.userID, uiKitUser.userName);
                if ("busy".equals(reason)) {
                    invitationCallListenerList.onOutgoingCallRejectedCauseBusy(callID, inviteCaller);
                } else {
                    invitationCallListenerList.onOutgoingCallDeclined(callID, inviteCaller);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void notifyOutgoingCallTimeout(List<ZegoUIKitUser> invitees) {
        if(invitationCallListenerList != null){
            List<ZegoCallUser> callees = new ArrayList<>();
            for (ZegoUIKitUser user : invitees) {
                callees.add(new ZegoCallUser(user.userID, user.userName));
            }
            invitationCallListenerList.onOutgoingCallTimeout(callID, callees);
        }
    }

    private void showNotification(ZegoCallInvitationData invitationData) {
        if (AppActivityManager.isBackground()) {
            NotificationsUtils.NotifyConfig notifyConfig = new NotificationsUtils.NotifyConfig();
            boolean isVideoCall = invitationData.type == ZegoInvitationType.VIDEO_CALL.getValue();
            boolean isGroup = invitationData.invitees.size() > 1;
            notifyConfig.userId = invitationData.inviter.userID;
            notifyConfig.title = getNotificationTitle(isVideoCall, isGroup, invitationData.inviter.userName);
            notifyConfig.message = getNotificationMessage(isVideoCall, isGroup);
            NotificationsUtils.showNotification(notifyConfig);
        }
    }

    public String getNotificationMessage(boolean isVideoCall, boolean isGroup) {
        String notificationMessage = "";
        if (getConfig() != null && getConfig().innerText != null) {
            ZegoInnerText innerText = CallInvitationServiceImpl.getInstance().getConfig().innerText;
            if (isVideoCall) {
                notificationMessage = isGroup ? innerText.incomingGroupVideoCallDialogMessage : innerText.incomingVideoCallDialogMessage;
            } else {
                notificationMessage = isGroup ? innerText.incomingGroupVoiceCallDialogMessage : innerText.incomingVoiceCallDialogMessage;
            }
        }

        if (TextUtils.isEmpty(notificationMessage)) {
            if (isVideoCall) {
                notificationMessage = isGroup ? application.getString(R.string.incoming_group_video_call) : application.getString(R.string.incoming_video_call);
            } else {
                notificationMessage = isGroup ? application.getString(R.string.incoming_group_voice_call) : application.getString(R.string.incoming_voice_call);
            }
        }

        return notificationMessage;
    }

    public String getNotificationTitle(boolean isVideoCall, boolean isGroup, String userName) {
        String notificationTitle = "";
        if (getConfig() != null && getConfig().innerText != null) {
            ZegoInnerText innerText = CallInvitationServiceImpl.getInstance().getConfig().innerText;
            if (isVideoCall) {
                notificationTitle = isGroup ? String.format(innerText.incomingGroupVideoCallDialogTitle, userName) : String.format(innerText.incomingVideoCallDialogTitle, userName);
            } else {
                notificationTitle = isGroup ? String.format(innerText.incomingGroupVoiceCallDialogTitle, userName) : String.format(innerText.incomingVoiceCallDialogTitle, userName);
            }
        }

        if (TextUtils.isEmpty(notificationTitle)) {
            notificationTitle = userName;
        }

        return notificationTitle;
    }

}
