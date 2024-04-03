package com.zegocloud.uikit.prebuilt.call.invite.internal;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.AppTask;
import android.app.ActivityManager.RecentTaskInfo;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Application;
import android.app.Application.ActivityLifecycleCallbacks;
import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.tencent.mmkv.MMKV;
import com.zegocloud.uikit.ZegoUIKit;
import com.zegocloud.uikit.plugin.adapter.plugins.signaling.ZegoSignalingPluginNotificationConfig;
import com.zegocloud.uikit.plugin.adapter.utils.GenericUtils;
import com.zegocloud.uikit.plugin.common.PluginCallbackListener;
import com.zegocloud.uikit.plugin.invitation.ZegoInvitationType;
import com.zegocloud.uikit.plugin.signaling.ZegoSignalingPlugin;
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallConfig;
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallFragment;
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallService;
import com.zegocloud.uikit.prebuilt.call.config.DurationUpdateListener;
import com.zegocloud.uikit.prebuilt.call.config.ZegoMenuBarButtonName;
import com.zegocloud.uikit.prebuilt.call.event.CallEndListener;
import com.zegocloud.uikit.prebuilt.call.event.ErrorEventsListener;
import com.zegocloud.uikit.prebuilt.call.event.SignalPluginConnectListener;
import com.zegocloud.uikit.prebuilt.call.event.ZegoCallEndReason;
import com.zegocloud.uikit.prebuilt.call.invite.ZegoUIKitPrebuiltCallInvitationConfig;
import com.zegocloud.uikit.prebuilt.call.invite.ZegoUIKitPrebuiltCallInvitationService;
import com.zegocloud.uikit.service.defines.ZegoScenario;
import com.zegocloud.uikit.service.defines.ZegoUIKitCallback;
import com.zegocloud.uikit.service.defines.ZegoUIKitPluginCallback;
import com.zegocloud.uikit.service.defines.ZegoUIKitSignalingPluginInvitationListener;
import com.zegocloud.uikit.service.defines.ZegoUIKitUser;
import com.zegocloud.uikit.service.express.IExpressEngineEventHandler;
import im.zego.zegoexpress.constants.ZegoRoomStateChangedReason;
import im.zego.zegoexpress.entity.ZegoUser;
import im.zego.zim.ZIM;
import im.zego.zim.callback.ZIMEventHandler;
import im.zego.zim.enums.ZIMConnectionEvent;
import im.zego.zim.enums.ZIMConnectionState;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import timber.log.Timber;

public class CallInvitationServiceImpl {

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
    private Map<ZegoUIKitUser, CallInvitationState> callUserStates = new ConcurrentHashMap<>();
    private List<CallStateListener> callStateListeners;
    private ZegoUIKitPrebuiltCallFragment zegoUIKitPrebuiltCallFragment;

    private boolean alreadyInit = false;
    private boolean alreadyLogin = false;
    private boolean inRoom = false;
    private Application application;
    private long appID;
    private String appSign;
    private ZegoUIKitPrebuiltCallInvitationConfig invitationConfig;
    private ZegoUIKitPrebuiltCallConfig callConfig;
    private ZIMPushMessage pushMessage;
    private long elapsedTime;
    private long startTimeLocal;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable checkTimeRunnable = new Runnable() {
        @Override
        public void run() {
            elapsedTime = System.currentTimeMillis() - startTimeLocal;

            if (callConfig != null && callConfig.durationConfig != null
                && callConfig.durationConfig.durationUpdateListener != null) {
                callConfig.durationConfig.durationUpdateListener.onDurationUpdate(elapsedTime / 1000);
            }
            if (updateListener != null) {
                updateListener.onDurationUpdate(elapsedTime / 1000);
            }
            handler.postDelayed(checkTimeRunnable, 1000);
        }
    };
    private DurationUpdateListener updateListener;
    private String notificationAction;
    private String callResourceID;
    private CallNotificationManager callNotificationManager = new CallNotificationManager();
    private IExpressEngineEventHandler expressEventHandler = new IExpressEngineEventHandler() {
        @Override
        public void onRoomStateChanged(String roomID, ZegoRoomStateChangedReason reason, int errorCode,
            JSONObject extendedData) {
            super.onRoomStateChanged(roomID, reason, errorCode, extendedData);
            CallEndListener callEndListener = ZegoUIKitPrebuiltCallService.events.callEvents.getCallEndListener();
            if (reason == ZegoRoomStateChangedReason.KICK_OUT) {
                if (zegoUIKitPrebuiltCallFragment != null) {
                    zegoUIKitPrebuiltCallFragment.endCall();
                }
                CallInvitationServiceImpl.getInstance().leaveRoom();
                if (callEndListener != null) {
                    callEndListener.onCallEnd(ZegoCallEndReason.KICK_OUT, "");
                }
            }
        }

        @Override
        public void onIMRecvCustomCommand(String roomID, ZegoUser fromUser, String command) {
            super.onIMRecvCustomCommand(roomID, fromUser, command);
            try {
                JSONObject jsonObject = new JSONObject(command);
                if (jsonObject.has("zego_remove_user")) {
                    JSONArray userIDArray = jsonObject.getJSONArray("zego_remove_user");
                    ZegoUIKitUser localUser = ZegoUIKit.getLocalUser();
                    for (int i = 0; i < userIDArray.length(); i++) {
                        String userID = userIDArray.getString(i);
                        if (localUser != null && Objects.equals(userID, localUser.userID)) {
                            if (zegoUIKitPrebuiltCallFragment != null) {
                                zegoUIKitPrebuiltCallFragment.endCall();
                            }
                            CallInvitationServiceImpl.getInstance().leaveRoom();
                            CallEndListener callEndListener = ZegoUIKitPrebuiltCallService.events.callEvents.getCallEndListener();
                            if (callEndListener != null) {
                                callEndListener.onCallEnd(ZegoCallEndReason.KICK_OUT, fromUser.userID);
                            }
                        }
                    }
                }
            } catch (JSONException e) {
            }
        }
    };

    private ZIMEventHandler zimEventHandler = new ZIMEventHandler() {
        @Override
        public void onConnectionStateChanged(ZIM zim, ZIMConnectionState state, ZIMConnectionEvent event,
            JSONObject extendedData) {
            super.onConnectionStateChanged(zim, state, event, extendedData);
            SignalPluginConnectListener pluginConnectListener = ZegoUIKitPrebuiltCallService.events.invitationEvents.getPluginConnectListener();
            if (pluginConnectListener != null) {
                pluginConnectListener.onSignalPluginConnectionStateChanged(state, event, extendedData);
            }
        }
    };

    private ZegoUIKitSignalingPluginInvitationListener invitationListener = new ZegoUIKitSignalingPluginInvitationListener() {
        @Override
        public void onInvitationReceived(ZegoUIKitUser inviter, int type, String data) {
            Activity topActivity = appActivityManager.getTopActivity();
            Timber.d("onInvitationReceived() called with: inviter = [" + inviter + "], type = [" + type + "], data = ["
                + data + "], topActivity = [" + topActivity + "], pushMessage = [" + pushMessage
                + "], notificationAction = [" + notificationAction + "],callState: " + callState);

            if (type != ZegoCallType.VOICE_CALL.value() && type != ZegoCallType.VIDEO_CALL.value()) {
                return;
            }
            JSONObject jsonObject = new JSONObject();
            String invitationID = null;
            try {
                JSONObject dataJson = new JSONObject(data);
                jsonObject.put("reason", "busy");
                invitationID = getStringFromJson(dataJson, "invitationID");
                jsonObject.put("invitationID", invitationID);

                String currentRoomID = ZegoUIKit.getRoom().roomID;

                if (callState > 0 || !TextUtils.isEmpty(currentRoomID)) {
                    ZegoUIKit.getSignalingPlugin().refuseInvitation(inviter.userID, jsonObject.toString(), null);
                    return;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            callInvitationData = ZegoCallInvitationData.parseString(data);
            if (callInvitationData != null) {
                callInvitationData.type = type;
                callInvitationData.inviter = inviter;
                callInvitationData.invitationID = invitationID;
                for (ZegoUIKitUser zegoUIKitUser : callInvitationData.invitees) {
                    changeUserState(zegoUIKitUser, CallInvitationState.WAITING);
                }
            }
            setCallState(INCOMING);

            if (pushMessage == null) {
                if (topActivity != null) {
                    if (isBackground(topActivity)) {
                        callNotificationManager.showCallBackgroundNotification(topActivity);
                    } else {
                        RingtoneManager.playRingTone(true);
                        invitationDialog = new CallInvitationDialog(topActivity, callInvitationData);
                        invitationDialog.show();
                    }
                }
                notifyIncomingCallReceived(inviter, type, data);
                clearPushMessage();
            } else {
                // is offline start app
                if (pushMessage.invitationID.equals(callInvitationData.invitationID)) {
                    // receive offline notification,click accept,start service and start app
                    if (CallNotificationManager.ACTION_ACCEPT_CALL.equals(notificationAction)) {
                        RingtoneManager.stopRingTone();
                        if (callInvitationData != null && inviter.equals(callInvitationData.inviter)) {
                            ZegoUIKit.getSignalingPlugin()
                                .acceptInvitation(inviter.userID, "", new PluginCallbackListener() {
                                    @Override
                                    public void callback(Map<String, Object> result) {
                                        Activity topActivity = appActivityManager.getTopActivity();
                                        if (topActivity != null) {
                                            CallInviteActivity.startCallPage(topActivity);
                                        }
                                    }
                                });
                            clearPushMessage();
                            setCallState(CONNECTED);
                            dismissCallNotification();
                        }
                    } else if (CallNotificationManager.ACTION_CLICK.equals(notificationAction)) {
                        // receive offline notification,click notification,show dialog normally
                        RingtoneManager.playRingTone(true);
                        invitationDialog = new CallInvitationDialog(topActivity, callInvitationData);
                        invitationDialog.show();
                        clearPushMessage();
                    } else if (CallNotificationManager.ACTION_DECLINE_CALL.equals(notificationAction)) {
                        // offline push
                        ZegoUIKit.getSignalingPlugin()
                            .refuseInvitation(inviter.userID, "", new PluginCallbackListener() {
                                @Override
                                public void callback(Map<String, Object> result) {
                                    unInitToReceiveOffline();
                                }
                            });
                        clearPushMessage();
                        setCallState(NONE_REJECTED);
                        dismissCallNotification();
                    } else {
                        // receive offline notification,click app
                        RingtoneManager.playRingTone(true);
                        invitationDialog = new CallInvitationDialog(topActivity, callInvitationData);
                        invitationDialog.show();
                        clearPushMessage();
                    }
                } else {
                    clearPushMessage();
                    if (topActivity != null) {
                        if (isBackground(topActivity)) {
                            callNotificationManager.showCallBackgroundNotification(topActivity);
                        } else {
                            RingtoneManager.playRingTone(true);
                            invitationDialog = new CallInvitationDialog(topActivity, callInvitationData);
                            invitationDialog.show();
                        }
                    }
                    notifyIncomingCallReceived(inviter, type, data);
                }
            }
        }

        @Override
        public void onInvitationTimeout(ZegoUIKitUser inviter, String data) {
            String invitationID = getStringFromJson(getJsonObjectFromString(data), "invitationID");
            if (callInvitationData != null && callInvitationData.invitationID.equals(invitationID)) {
                String callID = callInvitationData.callID;
                hideDialog();
                dismissCallNotification();
                setCallState(NONE_RECEIVE_MISSED);
                notifyIncomingCallTimeout(inviter, callID);
            }
        }

        @Override
        public void onInvitationResponseTimeout(List<ZegoUIKitUser> invitees, String data) {
            String invitationID = getStringFromJson(getJsonObjectFromString(data), "invitationID");
            if (callInvitationData != null && callInvitationData.invitationID.equals(invitationID)) {
                String callID = callInvitationData.callID;
                for (ZegoUIKitUser invitee : invitees) {
                    changeUserState(invitee, CallInvitationState.TIMEOUT);
                }
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
                notifyOutgoingCallTimeout(invitees, callID);
                clearPushMessage();
            }
        }

        @Override
        public void onInvitationAccepted(ZegoUIKitUser invitee, String data) {
            String invitationID = getStringFromJson(getJsonObjectFromString(data), "invitationID");
            if (callInvitationData != null && callInvitationData.invitationID.equals(invitationID)) {
                String callID = callInvitationData.callID;
                changeUserState(invitee, CallInvitationState.ACCEPT);
                setCallState(CONNECTED);
                RingtoneManager.stopRingTone();
                notifyOutgoingCallAccepted(invitee, callID);
            }
        }

        @Override
        public void onInvitationRefused(ZegoUIKitUser invitee, String data) {
            String invitationID = getStringFromJson(getJsonObjectFromString(data), "invitationID");
            if (callInvitationData != null && callInvitationData.invitationID.equals(invitationID)) {
                changeUserState(invitee, CallInvitationState.REFUSE);
                String callID = callInvitationData.callID;
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
                notifyOutgoingCallRejected0rDeclined(invitee, data, callID);
            }
        }

        @Override
        public void onInvitationCanceled(ZegoUIKitUser inviter, String data) {
            String invitationID = getStringFromJson(getJsonObjectFromString(data), "invitationID");
            if (callInvitationData != null && callInvitationData.invitationID.equals(invitationID)) {
                String callID = callInvitationData.callID;
                hideDialog();
                dismissCallNotification();
                clearPushMessage();
                if (callState == CONNECTED) {
                    return;
                }
                setCallState(NONE_CANCELED);
                notifyIncomingCallCanceled(inviter, callID);
            }
        }
    };

    public void clearPushMessage() {
        setNotificationClickAction(null, null);
        setZIMPushMessage(null);
    }


    public void startApp(Context context) {
        Intent intent = null;
        try {
            intent = new Intent(context, Class.forName(getLauncherActivity()));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            context.startActivity(intent);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public String getLauncherActivity() {
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setPackage(getApplication().getPackageName());
        PackageManager pm = getApplication().getPackageManager();
        List<ResolveInfo> info = pm.queryIntentActivities(intent, 0);
        if (info == null || info.size() == 0) {
            return "";
        }
        return info.get(0).activityInfo.name;
    }

    private JSONObject getJsonObjectFromString(String s) {
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(s);
        } catch (JSONException e) {
            jsonObject = new JSONObject();
        }
        return jsonObject;
    }

    private String getStringFromJson(JSONObject jsonObject, String key) {
        try {
            if (jsonObject.has(key)) {
                return jsonObject.getString(key);
            } else {
                return null;
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
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

    public static boolean isBackground(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.processName.equals(context.getPackageName())) {
                if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    return false;
                } else {
                    return true;
                }
            }
        }
        return false;
    }

    public void openCamera(boolean open) {
        ZegoUIKit.openCamera(open);
    }

    public void openMicrophone(boolean open) {
        ZegoUIKit.openMicrophone(open);
    }

    public boolean isMicrophoneOn(String userID) {
        return ZegoUIKit.isMicrophoneOn(userID);
    }

    public boolean isMicrophoneOn() {
        if (ZegoUIKit.getLocalUser() == null) {
            return false;
        }
        return ZegoUIKit.isMicrophoneOn(ZegoUIKit.getLocalUser().userID);
    }

    public boolean isCameraOn() {
        if (ZegoUIKit.getLocalUser() == null) {
            return false;
        }
        return ZegoUIKit.isCameraOn(ZegoUIKit.getLocalUser().userID);
    }

    public boolean isCameraOn(String userID) {
        return ZegoUIKit.isCameraOn(userID);
    }

    public ZegoUIKitUser getLocalUser() {
        return ZegoUIKit.getLocalUser();
    }

    public ZegoCallInvitationData getCallInvitationData() {
        return callInvitationData;
    }

    private static void initRingtoneManager(Application application, ZegoUIKitPrebuiltCallInvitationConfig config) {
        RingtoneManager.init(application);
        String outgoing;
        if (config == null || TextUtils.isEmpty(config.outgoingCallRingtone)) {
            outgoing = "zego_outgoing";
        } else {
            outgoing = config.outgoingCallRingtone;
        }
        Uri ongoingUri = RingtoneManager.getUriFromRaw(application, outgoing);
        RingtoneManager.setOutgoingUri(ongoingUri);
        String incoming;
        if (config == null || TextUtils.isEmpty(config.incomingCallRingtone)) {
            incoming = "zego_incoming";
        } else {
            incoming = config.incomingCallRingtone;
        }
        Uri inComingUri = RingtoneManager.getUriFromRaw(application, incoming);
        RingtoneManager.setIncomingUri(inComingUri);
    }

    public void autoInitAndLoginUser(Application application) {
        MMKV.initialize(application);
        MMKV mmkv = MMKV.defaultMMKV(MMKV.SINGLE_PROCESS_MODE, getClass().getName());
        if (mmkv.contains("appID")) {
            long preAppID = mmkv.getLong("appID", 0);
            String preAppSign = mmkv.getString("appSign", "");
            String preUserID = mmkv.getString("userID", "");
            String preUserName = mmkv.getString("userName", "");
            String token = mmkv.getString("appToken", "");
            init(application, preAppID, preAppSign, token);
            loginUser(preUserID, preUserName);

            ZegoUIKit.getSignalingPlugin().enableNotifyWhenAppRunningInBackgroundOrQuit(true);
        }
    }

    public void renewToken(String token) {
        ZegoUIKit.renewToken(token);
        ZegoUIKit.getSignalingPlugin().renewToken(token);
    }


    public boolean init(Application application, long appID, String appSign, String token) {
        if (alreadyInit) {
            // we assume that user not changed his appID and appSign
            ErrorEventsListener errorEvents = ZegoUIKitPrebuiltCallService.events.getErrorEventsListener();
            if (errorEvents != null) {
                errorEvents.onError(ErrorEventsListener.INIT_ALREADY,
                    "ZEGO Express Engine is already initialized, do not initialize again");
            }
            return true;
        }
        boolean result = ZegoUIKit.init(application, appID, appSign, ZegoScenario.GENERAL);
        if (result) {
            alreadyInit = true;
            this.application = application;
            this.appID = appID;
            this.appSign = appSign;

            MMKV.initialize(application);

            MMKV mmkv = MMKV.defaultMMKV(MMKV.SINGLE_PROCESS_MODE, getClass().getName());

            mmkv.putLong("appID", appID);
            mmkv.putString("appSign", appSign);
            mmkv.putString("appToken", token);

            if (!TextUtils.isEmpty(token)) {
                renewToken(token);
            }

            ZegoUIKit.addEventHandler(expressEventHandler, false);
            ZegoUIKit.getSignalingPlugin().addInvitationListener(invitationListener);
            if (appActivityManager == null) {
                appActivityManager = new AppActivityManager();
                this.application.registerActivityLifecycleCallbacks(appActivityManager);
            }
            ZegoSignalingPlugin.getInstance().registerZIMEventHandler(zimEventHandler);
        }
        if (!result) {
            ErrorEventsListener errorEvents = ZegoUIKitPrebuiltCallService.events.getErrorEventsListener();
            if (errorEvents != null) {
                errorEvents.onError(ErrorEventsListener.INIT_PARAM_ERROR,
                    "Create engine error,please check if your AppID and AppSign is correct");
            }
        }
        return result;
    }

    public void setCallResourceID(String resourceID) {
        this.callResourceID = resourceID;
    }

    public String getCallResourceID() {
        return callResourceID;
    }

    public void setCallInvitationConfig(ZegoUIKitPrebuiltCallInvitationConfig invitationConfig) {
        if (this.invitationConfig == null) {
            this.invitationConfig = invitationConfig;
            initRingtoneManager(application, invitationConfig);
            // offline channel need ringtone
            callNotificationManager.createCallNotificationChannel(application);
        } else {
            this.invitationConfig = invitationConfig;
        }
    }

    public ZegoUIKitPrebuiltCallInvitationConfig getCallInvitationConfig() {
        return invitationConfig;
    }

    public void setCallConfig(ZegoUIKitPrebuiltCallConfig callConfig) {
        this.callConfig = callConfig;
    }

    public ZegoUIKitPrebuiltCallConfig getCallConfig() {
        return callConfig;
    }

    public Activity getTopActivity() {
        if (appActivityManager == null) {
            return null;
        }
        return appActivityManager.getTopActivity();
    }

    public void loginUser(String userID, String userName) {
        Timber.d("loginUser() called with: userID = [" + userID + "], userName = [" + userName + "],alreadyLogin:"
            + alreadyLogin);
        if (alreadyLogin) {
            return;
        }
        alreadyLogin = true;
        ZegoUIKit.login(userID, userName);
        ZegoUIKit.getSignalingPlugin().login(userID, userName, new ZegoUIKitPluginCallback() {
            @Override
            public void onResult(int errorCode, String message) {
                if (errorCode != 0) {
                    alreadyLogin = false;
                } else {
                    ZegoUIKit.getSignalingPlugin().enableNotifyWhenAppRunningInBackgroundOrQuit(true);
                }
            }
        });
        MMKV mmkv = MMKV.defaultMMKV(MMKV.SINGLE_PROCESS_MODE, getClass().getName());
        mmkv.putString("userID", userID);
        mmkv.putString("userName", userName);
    }

    public void initBeautyPlugin() {
        ZegoUIKit.getBeautyPlugin().setZegoBeautyPluginConfig(callConfig.beautyConfig);
        ZegoUIKit.getBeautyPlugin().init(application, appID, appSign);
    }

    public void unInitToReceiveOffline() {
        Timber.d("unInitToReceiveOffline() called");
        ZegoUIKit.getSignalingPlugin().removeInvitationListener(invitationListener);
        ZegoUIKit.getSignalingPlugin().destroy();

        setCallState(NONE);
        clearInvitationData();
        clearPushMessage();
        if (callStateListeners != null) {
            callStateListeners.clear();
        }
        zegoUIKitPrebuiltCallFragment = null;

        alreadyInit = false;
        alreadyLogin = false;
        inRoom = false;
        appID = 0;
        appSign = null;
        invitationConfig = null;
        callConfig = null;
        elapsedTime = 0;
        startTimeLocal = 0;
    }


    public void unInit() {
        Timber.d("unInit() called");
        leaveRoom();
        ZegoUIKitPrebuiltCallFragment callFragment = ZegoUIKitPrebuiltCallInvitationService.getPrebuiltCallFragment();
        if (callFragment != null) {
            callFragment.requireActivity().finish();
        }
        ZegoUIKit.removeEventHandler(expressEventHandler);
        ZegoSignalingPlugin.getInstance().unregisterZIMEventHandler(zimEventHandler);
        if (invitationConfig != null) {
            ZegoUIKit.logout();
            ZegoUIKit.getSignalingPlugin().logout();
        }
        unInitToReceiveOffline();

    }

    public boolean canShowFullOnLockScreen() {
        if (Build.MANUFACTURER.equalsIgnoreCase("xiaomi")) {
            // xiaomi
            return false;
        }
        return true;
    }

    public ZegoUIKitPrebuiltCallConfigProvider getProvider() {
        if (invitationConfig == null) {
            return null;
        } else {
            return invitationConfig.provider;
        }
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
        Timber.d("setCallState() called with: before = [" + before + "],after:" + callState);
        if (before != callState && callStateListeners != null) {
            for (CallStateListener callStateListener : callStateListeners) {
                callStateListener.onStateChanged(before, callState);
            }
        }
    }

    private void clearInvitationData() {
        RingtoneManager.stopRingTone();
        callInvitationData = null;
        if (callUserStates != null) {
            callUserStates.clear();
        }
    }

    public void addCallStateListener(CallStateListener callStateListener) {
        if (callStateListeners == null) {
            callStateListeners = new CopyOnWriteArrayList<>();
        }
        this.callStateListeners.add(callStateListener);
    }

    public void removeCallStateListener(CallStateListener callStateListener) {
        callStateListeners.remove(callStateListener);
    }

    public void showCallNotification(Context context) {
        callNotificationManager.showCallNotification(context);
    }

    public void dismissCallNotification() {
        callNotificationManager.dismissCallNotification(application);
    }

    public void dismissCallNotification(Context context) {
        callNotificationManager.dismissCallNotification(context);
    }

    public boolean isCallNotificationShowed() {
        return callNotificationManager.isCallNotificationShowed();
    }

    public String getCallNotificationMessage(boolean isVideoCall, boolean isGroup) {
        return callNotificationManager.getBackgroundNotificationMessage(isVideoCall, isGroup);
    }

    public String getCallNotificationTitle(boolean isVideoCall, boolean isGroup, String userName) {
        return callNotificationManager.getBackgroundNotificationTitle(isVideoCall, isGroup, userName);
    }

    public Notification getCallNotification(Context context) {
        return callNotificationManager.createCallNotification(context);
    }

    public boolean isInRoom() {
        return inRoom;
    }

    private void startTimeCount() {
        startTimeLocal = System.currentTimeMillis();
        handler.post(checkTimeRunnable);
    }

    public void setNotificationClickAction(String action, String invitationID) {
        this.notificationAction = action;
    }

    private void stopRoomTimeCount() {
        handler.removeCallbacks(checkTimeRunnable);
    }

    public void setDurationUpdateListener(DurationUpdateListener updateListener) {
        this.updateListener = updateListener;
    }

    public ZIMPushMessage getZIMPushMessage() {
        return pushMessage;
    }

    public void setZIMPushMessage(ZIMPushMessage pushMessage) {
        this.pushMessage = pushMessage;
    }

    public void parsePayload() {
        try {
            JSONObject jsonObject = new JSONObject(pushMessage.payLoad);
            ZegoCallInvitationData invitationData;
            if (jsonObject.has("data")) {
                invitationData = ZegoCallInvitationData.parseString(getStringFromJson(jsonObject, "data"));
                if (jsonObject.has("type")) {
                    int type = jsonObject.getInt("type");
                    invitationData.type = type;
                }
                if (jsonObject.has("inviter_name")) {
                    String inviter_name = jsonObject.getString("inviter_name");
                    invitationData.inviter = new ZegoUIKitUser(inviter_name, inviter_name);
                }
                invitationData.invitationID = pushMessage.invitationID;
                callInvitationData = invitationData;
            }

        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public long getStartTimeLocal() {
        return startTimeLocal;
    }

    public void joinRoom(String roomID, ZegoUIKitCallback callback) {
        ZegoUIKit.joinRoom(roomID, new ZegoUIKitCallback() {
            @Override
            public void onResult(int errorCode) {
                inRoom = errorCode == 0;
                if (inRoom) {
                    startTimeCount();
                    clearPushMessage();
                }
                if (callback != null) {
                    callback.onResult(errorCode);
                }
            }
        });
    }

    public void leaveRoom() {
        Timber.d("leaveRoom() called alreadyInit: roomID = [" + alreadyInit + "], callState = [" + callState + "]");
        if (alreadyInit) {
            if (callState == OUTGOING) {
                if (callInvitationData != null) {
                    List<String> waitedUserIDs = new ArrayList<>();
                    for (ZegoUIKitUser invitee : callInvitationData.invitees) {
                        if (callUserStates.get(invitee) == CallInvitationState.WAITING) {
                            waitedUserIDs.add(invitee.userID);
                        }
                    }
                    if (!waitedUserIDs.isEmpty()) {
                        CallInvitationServiceImpl.getInstance().cancelInvitation(waitedUserIDs, null);
                    }
                }
            }
        }

        if (getCallState() > 0) {
            setCallState(CallInvitationServiceImpl.NONE);
        }
        setZegoUIKitPrebuiltCallFragment(null);
        clearInvitationData();
        inRoom = false;
        stopRoomTimeCount();
        updateListener = null;
        clearPushMessage();
        ZegoUIKit.leaveRoom();
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

        if (callState > 0) {
            if (callbackListener != null) {
                Map<String, Object> map = new HashMap<>();
                map.put("code", -3);
                map.put("message", "User is not idle");
                map.put("invitationID", "");
                callbackListener.callback(map);
            }
            return;
        }

        JSONObject jsonObject = new JSONObject();
        JSONArray jsonArray = new JSONArray();

        try {
            if (TextUtils.isEmpty(callID)) {
                jsonObject.put("call_id", generateCallID());
            } else {
                jsonObject.put("call_id", callID);
            }
            for (ZegoUIKitUser invitee : invitees) {
                JSONObject tmp = new JSONObject();
                tmp.put("user_id", invitee.userID);
                tmp.put("user_name", invitee.userName);
                jsonArray.put(tmp);
            }
            jsonObject.put("invitees", jsonArray);
            jsonObject.put("custom_data", customData);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        List<String> idList = GenericUtils.map(invitees, uiKitUser -> uiKitUser.userID);
        String extraData = jsonObject.toString();

        if (notificationConfig == null) {
            notificationConfig = getSendInvitationConfig(invitees, invitationType);
            setCallResourceID("zegouikit_call");
        } else {
            boolean isVideoCall = invitationType == ZegoInvitationType.VIDEO_CALL;
            ZegoUIKitUser uiKitUser = ZegoUIKit.getLocalUser();
            if (TextUtils.isEmpty(notificationConfig.getTitle())) {
                String offlineTitle = CallInvitationServiceImpl.getInstance()
                    .getCallNotificationTitle(isVideoCall, invitees.size() > 1, uiKitUser.userName);
                notificationConfig.setTitle(offlineTitle);
            }
            if (TextUtils.isEmpty(notificationConfig.getMessage())) {
                String offlineMessage = CallInvitationServiceImpl.getInstance()
                    .getCallNotificationMessage(isVideoCall, invitees.size() > 1);
                notificationConfig.setMessage(offlineMessage);
            }
            if (TextUtils.isEmpty(notificationConfig.getResourceID())) {
                notificationConfig.setResourceID("zegouikit_call");
            } else {
                setCallResourceID(callResourceID);
            }
        }
        ZegoUIKit.getSignalingPlugin()
            .sendInvitation(idList, timeout, invitationType.getValue(), extraData, notificationConfig, result -> {
                int code = (int) result.get("code");
                List<ZegoUIKitUser> errorInvitees = (List<ZegoUIKitUser>) result.get("errorInvitees");
                String invitationID = (String) result.get("invitationID");
                if (code == 0 && errorInvitees != null && errorInvitees.size() < invitees.size()) {
                    callInvitationData = ZegoCallInvitationData.parseString(extraData);
                    if (callInvitationData != null) {
                        callInvitationData.type = invitationType.getValue();
                        callInvitationData.inviter = ZegoUIKit.getLocalUser();
                        callInvitationData.invitationID = invitationID;
                        for (ZegoUIKitUser zegoUIKitUser : callInvitationData.invitees) {
                            CallInvitationState state;
                            if (errorInvitees.contains(zegoUIKitUser)) {
                                state = CallInvitationState.ERROR;
                            } else {
                                state = CallInvitationState.WAITING;
                            }
                            changeUserState(zegoUIKitUser, state);
                        }
                        setCallState(CallInvitationServiceImpl.OUTGOING);
                        RingtoneManager.playRingTone(false);
                    }
                }
                if (callbackListener != null) {
                    callbackListener.callback(result);
                }
            });
    }

    private ZegoSignalingPluginNotificationConfig getSendInvitationConfig(List<ZegoUIKitUser> invitees,
        ZegoInvitationType invitationType) {
        boolean isVideoCall = invitationType == ZegoInvitationType.VIDEO_CALL;

        String offlineMessage = CallInvitationServiceImpl.getInstance()
            .getCallNotificationMessage(isVideoCall, invitees.size() > 1);

        ZegoUIKitUser uiKitUser = ZegoUIKit.getLocalUser();
        String offlineTitle = CallInvitationServiceImpl.getInstance()
            .getCallNotificationTitle(isVideoCall, invitees.size() > 1, uiKitUser.userName);

        String offlineResourceID = "zegouikit_call";

        ZegoSignalingPluginNotificationConfig notificationConfig = new ZegoSignalingPluginNotificationConfig();
        notificationConfig.setResourceID(offlineResourceID);
        notificationConfig.setTitle(offlineTitle);
        notificationConfig.setMessage(offlineMessage);
        return notificationConfig;
    }

    private String generateCallID() {
        String callID = null;
        String userID = ZegoUIKit.getLocalUser().userID;
        if (userID != null) {
            callID = "call_" + userID + "_" + System.currentTimeMillis();
        }
        return callID;
    }

    public void cancelInvitation(PluginCallbackListener callbackListener) {
        if (callInvitationData != null) {
            List<String> idList = GenericUtils.map(callInvitationData.invitees, uiKitUser -> uiKitUser.userID);
            cancelInvitation(idList, callInvitationData.invitationID, "", null, callbackListener);
        }
    }

    public void cancelInvitation(List<String> invitees, PluginCallbackListener callbackListener) {
        if (callInvitationData != null) {
            cancelInvitation(invitees, callInvitationData.invitationID, "", null, callbackListener);
        }
    }

    public void cancelInvitation(String data, PluginCallbackListener callbackListener) {
        if (callInvitationData != null) {
            List<String> idList = GenericUtils.map(callInvitationData.invitees, uiKitUser -> uiKitUser.userID);
            cancelInvitation(idList, callInvitationData.invitationID, data, null, callbackListener);
        }
    }

    public void cancelInvitation(List<String> invitees, String data, PluginCallbackListener callbackListener) {
        if (callInvitationData != null) {
            cancelInvitation(invitees, callInvitationData.invitationID, data, null, callbackListener);
        }
    }

    public void cancelInvitation(List<String> invitees, String invitationID, String data,
        ZegoSignalingPluginNotificationConfig pushConfig, PluginCallbackListener callbackListener) {

        if (pushConfig == null) {
            pushConfig = new ZegoSignalingPluginNotificationConfig();
            if (TextUtils.isEmpty(callResourceID)) {
                pushConfig.setResourceID("zegouikit_call");
            } else {
                pushConfig.setResourceID(callResourceID);
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
        for (Entry<ZegoUIKitUser, CallInvitationState> entry : callUserStates.entrySet()) {
            if (invitees.contains(entry.getKey().userID)) {
                entry.setValue(CallInvitationState.CANCEL);
            }
        }
        setCallState(NONE_CANCELED);
        RingtoneManager.stopRingTone();
    }

    public void rejectInvitation(PluginCallbackListener callbackListener) {
        if (callInvitationData != null) {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("reason", "decline");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            rejectInvitation(callInvitationData.invitationID, jsonObject.toString(), callbackListener);
        }
    }

    public void rejectInvitation(String data, PluginCallbackListener callbackListener) {
        if (callInvitationData != null) {
            rejectInvitation(callInvitationData.invitationID, data, callbackListener);
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
        setCallState(NONE_REJECTED);
        RingtoneManager.stopRingTone();
    }

    public void acceptInvitation(PluginCallbackListener callbackListener) {
        if (callInvitationData != null) {
            acceptInvitation(callInvitationData.invitationID, "", callbackListener);
        }
    }

    public void acceptInvitation(String data, PluginCallbackListener callbackListener) {
        if (callInvitationData != null) {
            acceptInvitation(callInvitationData.invitationID, data, callbackListener);
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
        setCallState(CONNECTED);
        RingtoneManager.stopRingTone();
    }

    public Application getApplication() {
        return application;
    }

    public void setZegoUIKitPrebuiltCallFragment(ZegoUIKitPrebuiltCallFragment zegoUIKitPrebuiltCallFragment) {
        this.zegoUIKitPrebuiltCallFragment = zegoUIKitPrebuiltCallFragment;
    }

    public ZegoUIKitPrebuiltCallFragment getZegoUIKitPrebuiltCallFragment() {
        return zegoUIKitPrebuiltCallFragment;
    }

    public void notifyIncomingCallReceived(ZegoUIKitUser inviter, int type, String extendedData) {
        ZegoInvitationCallListener invitationCallListener = ZegoUIKitPrebuiltCallService.events.invitationEvents.getInvitationListener();
        if (invitationCallListener != null) {
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

                ZegoCallType callType =
                    type == ZegoCallType.VIDEO_CALL.value() ? ZegoCallType.VIDEO_CALL : ZegoCallType.VOICE_CALL;
                ZegoCallUser inviteCaller = new ZegoCallUser(inviter.userID, inviter.userName);
                invitationCallListener.onIncomingCallReceived(callInvitationData.callID, inviteCaller, callType, list);

            } catch (JSONException e) {
                e.printStackTrace();
            }
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
            try {
                JSONObject jsonObject = new JSONObject(data);
                String reason = getStringFromJson(jsonObject, "reason");
                ZegoCallUser inviteCaller = new ZegoCallUser(uiKitUser.userID, uiKitUser.userName);
                if ("busy".equals(reason)) {
                    invitationCallListener.onOutgoingCallRejectedCauseBusy(callID, inviteCaller);
                } else {
                    invitationCallListener.onOutgoingCallDeclined(callID, inviteCaller);
                }
            } catch (JSONException e) {
                e.printStackTrace();
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

    public class AppActivityManager implements ActivityLifecycleCallbacks {

        private Activity topActivity;

        @Override
        public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {

        }

        @Override
        public void onActivityStarted(@NonNull Activity activity) {
        }

        @Override
        public void onActivityResumed(@NonNull Activity activity) {
            Timber.d("onActivityResumed() called with: activity = [" + activity + "]");
            topActivity = activity;
            boolean notificationShowed = isCallNotificationShowed();
            // if app is online and background,received a call notification
            if (notificationShowed && pushMessage == null) {
                // if app's topActivity is not CallInviteActivity.then start it
                // with incoming page
                if (!(topActivity instanceof CallInviteActivity)) {
                    Intent intent = new Intent(activity, CallInviteActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("page", "incoming");
                    intent.putExtra("bundle", bundle);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setAction(CallNotificationManager.ACTION_CLICK);
                    activity.startActivity(intent);

                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            // for some devices,if app is background and cannot start CallInviteActivity,
                            // then start a call invite dialog
                            if (!(topActivity instanceof CallInviteActivity) && topActivity != null) {
                                RingtoneManager.playRingTone(true);
                                invitationDialog = new CallInvitationDialog(topActivity, callInvitationData);
                                invitationDialog.show();
                            }
                        }
                    }, 800);
                }
            }
            // if app was at front,and topActivity is not CallInviteActivity，
            // for example,received a offline notification, not click notification
            // but directly click app icon instead
            if (!(topActivity instanceof CallInviteActivity) && notificationAction == null) {
                //clear push message will make app start normally auto sign in
                setZIMPushMessage(null);
            }
            boolean canShowFullOnLockScreen = CallInvitationServiceImpl.getInstance().canShowFullOnLockScreen();
            if (canShowFullOnLockScreen) {
                if (!(topActivity instanceof CallInviteActivity)) {
                    dismissCallNotification();
                }
            } else {
                dismissCallNotification();
            }

            ActivityManager am = (ActivityManager) topActivity.getSystemService(Context.ACTIVITY_SERVICE);
            if (am != null) {
                if (!(topActivity instanceof CallInviteActivity)) {
                    if (inRoom) {
                        // call entered the room, then switched to the background, but there is no minimize .
                        // Now, when returning to the app, it is necessary to bring the CallInviteActivity to the foreground
                        // (because the CallInviteActivity was hidden in the recent apps, it won't show up if not brought to the foreground).
                        ZegoUIKitPrebuiltCallConfig callConfig = CallInvitationServiceImpl.getInstance()
                            .getCallConfig();
                        ZegoUIKitPrebuiltCallFragment callFragment = CallInvitationServiceImpl.getInstance()
                            .getZegoUIKitPrebuiltCallFragment();
                        boolean hasMiniButton =
                            callConfig.bottomMenuBarConfig.buttons.contains(ZegoMenuBarButtonName.MINIMIZING_BUTTON)
                                || callConfig.topMenuBarConfig.buttons.contains(
                                ZegoMenuBarButtonName.MINIMIZING_BUTTON);
                        if (!hasMiniButton && callFragment != null) {
                            List<ActivityManager.AppTask> tasks = am.getAppTasks();
                            if (tasks != null && tasks.size() > 0) {
                                for (AppTask task : tasks) {
                                    RecentTaskInfo taskInfo = task.getTaskInfo();
                                    if (taskInfo.baseIntent.getComponent().toShortString()
                                        .contains(CallInviteActivity.class.getName())) {
                                        task.moveToFront();
                                        break;
                                    }
                                }
                            }
                        }
                    } else if (callInvitationData != null) {
                        boolean isCallInviteActivityStarted = false;
                        List<RunningTaskInfo> runningTasks = am.getRunningTasks(Integer.MAX_VALUE);
                        for (RunningTaskInfo runningTask : runningTasks) {
                            if (Objects.equals(runningTask.topActivity.getClassName(),
                                CallInviteActivity.class.getName())) {
                                isCallInviteActivityStarted = true;
                                break;
                            }
                        }
                        if (isCallInviteActivityStarted) {
                            List<ActivityManager.AppTask> tasks = am.getAppTasks();
                            if (tasks != null && tasks.size() > 0) {
                                for (AppTask task : tasks) {
                                    RecentTaskInfo taskInfo = task.getTaskInfo();
                                    if (taskInfo.baseIntent.getComponent().toShortString()
                                        .contains(CallInviteActivity.class.getName())) {
                                        task.moveToFront();
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        @Override
        public void onActivityPaused(@NonNull Activity activity) {
            Timber.d("onActivityPaused() called with: activity = [" + activity + "]");
        }

        @Override
        public void onActivityStopped(@NonNull Activity activity) {
        }

        @Override
        public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {

        }

        @Override
        public void onActivityDestroyed(@NonNull Activity activity) {
        }

        public Activity getTopActivity() {
            return topActivity;
        }
    }
}
