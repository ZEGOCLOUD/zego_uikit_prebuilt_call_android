package com.zegocloud.uikit.prebuilt.call.invite.internal;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
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
import com.zegocloud.uikit.plugin.common.PluginCallbackListener;
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallConfig;
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallFragment;
import com.zegocloud.uikit.prebuilt.call.config.DurationUpdateListener;
import com.zegocloud.uikit.prebuilt.call.invite.ZegoCallInvitationData;
import com.zegocloud.uikit.prebuilt.call.invite.ZegoUIKitPrebuiltCallConfigProvider;
import com.zegocloud.uikit.prebuilt.call.invite.ZegoUIKitPrebuiltCallInvitationConfig;
import com.zegocloud.uikit.service.defines.ZegoScenario;
import com.zegocloud.uikit.service.defines.ZegoUIKitCallback;
import com.zegocloud.uikit.service.defines.ZegoUIKitSignalingPluginInvitationListener;
import com.zegocloud.uikit.service.defines.ZegoUIKitUser;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
    private ZegoInvitationCallListener invitationCallListener;
    private OutgoingCallButtonListener outgoingCallButtonListener;
    private IncomingCallButtonListener incomingCallButtonListener;
    private ZegoUIKitPrebuiltCallFragment zegoUIKitPrebuiltCallFragment;

    private boolean alreadyInit = false;
    private boolean alreadyLogin = false;
    private boolean inRoom = false;
    private Application application;
    private long appID;
    private String appSign;
    private String userID;
    private String userName;
    private ZegoUIKitPrebuiltCallInvitationConfig invitationConfig;
    private ZegoUIKitPrebuiltCallConfig callConfig;
    private LeaveRoomListener leaveRoomListener;
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

    private ZegoUIKitSignalingPluginInvitationListener invitationListener = new ZegoUIKitSignalingPluginInvitationListener() {
        @Override
        public void onInvitationReceived(ZegoUIKitUser inviter, int type, String data) {
            Timber.d("onInvitationReceived() called with: inviter = [" + inviter + "], type = [" + type + "], data = ["
                + data + "] ");
            JSONObject jsonObject = new JSONObject();
            String invitationID = null;
            try {
                JSONObject dataJson = new JSONObject(data);
                jsonObject.put("reason", "busy");
                invitationID = getStringFromJson(dataJson, "invitationID");
                jsonObject.put("invitationID", invitationID);

                //                if (pushMessage != null && pushMessage.invitationID.equals(invitationID)
                //
                //                ) {
                //                    // receive both zim call-receive callback and zpns offline message
                //                    return;
                //                }
                if (callState > 0) {
                    Timber.d("onInvitationReceived auto refuseInvitation because callState = " + callState);
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

            Activity topActivity = appActivityManager.getTopActivity();
            Timber.d("onInvitationReceived topActivity = [" + topActivity + "], pushMessage = [" + pushMessage
                + "], notificationAction = [" + notificationAction + "]");
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
                hideDialog();
                dismissCallNotification();
                setCallState(NONE_RECEIVE_MISSED);
                notifyIncomingCallTimeout(inviter);
            }
        }

        @Override
        public void onInvitationResponseTimeout(List<ZegoUIKitUser> invitees, String data) {
            String invitationID = getStringFromJson(getJsonObjectFromString(data), "invitationID");
            if (callInvitationData != null && callInvitationData.invitationID.equals(invitationID)) {
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
                notifyOutgoingCallTimeout(invitees);
                clearPushMessage();
            }
        }

        @Override
        public void onInvitationAccepted(ZegoUIKitUser invitee, String data) {
            String invitationID = getStringFromJson(getJsonObjectFromString(data), "invitationID");
            if (callInvitationData != null && callInvitationData.invitationID.equals(invitationID)) {
                changeUserState(invitee, CallInvitationState.ACCEPT);
                setCallState(CONNECTED);
                RingtoneManager.stopRingTone();
                notifyOutgoingCallAccepted(invitee);
            }
        }

        @Override
        public void onInvitationRefused(ZegoUIKitUser invitee, String data) {
            String invitationID = getStringFromJson(getJsonObjectFromString(data), "invitationID");
            if (callInvitationData != null && callInvitationData.invitationID.equals(invitationID)) {
                changeUserState(invitee, CallInvitationState.REFUSE);
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
                notifyOutgoingCallRejected0rDeclined(invitee, data);
            }
        }

        @Override
        public void onInvitationCanceled(ZegoUIKitUser inviter, String data) {
            String invitationID = getStringFromJson(getJsonObjectFromString(data), "invitationID");
            if (callInvitationData != null && callInvitationData.invitationID.equals(invitationID)) {
                hideDialog();
                dismissCallNotification();
                clearPushMessage();
                if (callState == CONNECTED) {
                    return;
                }
                setCallState(NONE_CANCELED);
                notifyIncomingCallCanceled(inviter);
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
            initAndLoginUser(application, preAppID, preAppSign, preUserID, preUserName);

            ZegoUIKit.getSignalingPlugin().enableNotifyWhenAppRunningInBackgroundOrQuit(true);
        }
    }

    public void initAndLoginUser(Application application, long appID, String appSign, String userID, String userName) {
        if (alreadyInit) {
            // we assume that user not changed his appID and appSign
            return;
        }
        alreadyInit = true;
        this.application = application;
        this.appID = appID;
        this.appSign = appSign;
        this.userID = userID;
        this.userName = userName;

        MMKV.initialize(application);

        MMKV mmkv = MMKV.defaultMMKV(MMKV.SINGLE_PROCESS_MODE, getClass().getName());

        mmkv.putLong("appID", appID);
        mmkv.putString("appSign", appSign);

        ZegoUIKit.init(application, appID, appSign, ZegoScenario.DEFAULT);
        ZegoUIKit.getSignalingPlugin().addInvitationListener(invitationListener);
        if (appActivityManager == null) {
            appActivityManager = new AppActivityManager();
            this.application.registerActivityLifecycleCallbacks(appActivityManager);
        }
        loginUser(userID, userName);
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
        if (invitationConfig != null) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    ZegoUIKit.getSignalingPlugin().enableNotifyWhenAppRunningInBackgroundOrQuit(true);
                }
            }, 500);
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
        ZegoUIKit.getSignalingPlugin().login(userID, userName, null);

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
        if (application != null) {
            this.application.unregisterActivityLifecycleCallbacks(appActivityManager);
            this.application = null;
            this.appActivityManager = null;
        }
        ZegoUIKit.getSignalingPlugin().removeInvitationListener(invitationListener);
        ZegoUIKit.getSignalingPlugin().destroy();

        setCallState(NONE);
        clearInvitationData();
        clearPushMessage();
        if (callStateListeners != null) {
            callStateListeners.clear();
        }
        invitationCallListener = null;
        outgoingCallButtonListener = null;
        incomingCallButtonListener = null;
        zegoUIKitPrebuiltCallFragment = null;

        alreadyInit = false;
        alreadyLogin = false;
        inRoom = false;
        application = null;
        appID = 0;
        appSign = null;
        userID = null;
        userName = null;
        invitationConfig = null;
        callConfig = null;
        leaveRoomListener = null;
        elapsedTime = 0;
        startTimeLocal = 0;
    }


    public void unInit() {
        Timber.d("unInit() called");
        leaveRoom();
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

    public boolean isNotificationShowed() {
        return callNotificationManager.isNotificationShowed();
    }

    public String getNotificationMessage(boolean isVideoCall, boolean isGroup) {
        return callNotificationManager.getBackgroundNotificationMessage(isVideoCall, isGroup);
    }

    public String getNotificationTitle(boolean isVideoCall, boolean isGroup, String userName) {
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
                invitationData = ZegoCallInvitationData.parseString(jsonObject.getString("data"));
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
        Timber.d("joinRoom() called with: roomID = [" + roomID + "], callback = [" + callback + "]");
        ZegoUIKit.joinRoom(roomID, new ZegoUIKitCallback() {
            @Override
            public void onResult(int errorCode) {
                Timber.d("joinRoom onResult:" + errorCode);
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
                        cancelInvitation(waitedUserIDs, "", null, null);
                    }
                }
            }
        }

        if (CallInvitationServiceImpl.getInstance().getCallState() > 0) {
            CallInvitationServiceImpl.getInstance().setCallState(CallInvitationServiceImpl.NONE);
        }
        clearInvitationData();
        inRoom = false;
        stopRoomTimeCount();
        updateListener = null;
        clearPushMessage();
        ZegoUIKit.leaveRoom();

        if (leaveRoomListener != null) {
            leaveRoomListener.onLeaveRoom();
            leaveRoomListener = null;
        }
    }

    public void setLeaveRoomListener(LeaveRoomListener leaveRoomListener) {
        this.leaveRoomListener = leaveRoomListener;
    }

    public void sendInvitation(List<String> invitees, int timeout, int type, String data,
        ZegoSignalingPluginNotificationConfig notificationConfig, PluginCallbackListener callbackListener) {
        if (callState > 0) {
            return;
        }
        ZegoUIKit.getSignalingPlugin()
            .sendInvitation(invitees, timeout, type, data, notificationConfig, new PluginCallbackListener() {
                @Override
                public void callback(Map<String, Object> result) {
                    int code = (int) result.get("code");
                    List<ZegoUIKitUser> errorInvitees = (List<ZegoUIKitUser>) result.get("errorInvitees");
                    String invitationID = (String) result.get("invitationID");
                    if (code == 0 && errorInvitees != null && errorInvitees.size() < invitees.size()) {
                        callInvitationData = ZegoCallInvitationData.parseString(data);
                        if (callInvitationData != null) {
                            callInvitationData.type = type;
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
                }
            });

    }

    public void cancelInvitation(List<String> invitees, String data, ZegoSignalingPluginNotificationConfig pushConfig,
        PluginCallbackListener callbackListener) {
        ZegoUIKit.getSignalingPlugin().cancelInvitation(invitees, data, pushConfig, new PluginCallbackListener() {
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

    public void callCancel(List<String> invitees, String invitationID, String data,
        ZegoSignalingPluginNotificationConfig pushConfig, PluginCallbackListener callbackListener) {
        ZegoUIKit.getSignalingPlugin().callCancel(invitees, invitationID, data, pushConfig, callbackListener);
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

    public void callReject(String invitationID, String data, PluginCallbackListener callbackListener) {
        ZegoUIKit.getSignalingPlugin().callReject(invitationID, data, callbackListener);
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
        RingtoneManager.stopRingTone();
    }

    public void callAccept(String invitationID, String data, PluginCallbackListener callbackListener) {
        ZegoUIKit.getSignalingPlugin().callAccept(invitationID, data, callbackListener);
        setCallState(CONNECTED);
        RingtoneManager.stopRingTone();
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
        this.invitationCallListener = listener;
    }

    public void setZegoUIKitPrebuiltCallFragment(ZegoUIKitPrebuiltCallFragment zegoUIKitPrebuiltCallFragment) {
        this.zegoUIKitPrebuiltCallFragment = zegoUIKitPrebuiltCallFragment;
    }

    public ZegoUIKitPrebuiltCallFragment getZegoUIKitPrebuiltCallFragment() {
        return zegoUIKitPrebuiltCallFragment;
    }

    public void removeInvitationCallListener() {
        this.invitationCallListener = null;
    }

    public void notifyIncomingCallReceived(ZegoUIKitUser inviter, int type, String extendedData) {
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

    public void notifyIncomingCallCanceled(ZegoUIKitUser inviter) {
        if (invitationCallListener != null) {
            ZegoCallUser inviteCaller = new ZegoCallUser(inviter.userID, inviter.userName);
            invitationCallListener.onIncomingCallCanceled(callInvitationData.callID, inviteCaller);
        }
    }

    public void notifyIncomingCallTimeout(ZegoUIKitUser inviter) {
        if (invitationCallListener != null) {
            ZegoCallUser inviteCaller = new ZegoCallUser(inviter.userID, inviter.userName);
            invitationCallListener.onIncomingCallTimeout(callInvitationData.callID, inviteCaller);
        }
    }

    public void notifyOutgoingCallAccepted(ZegoUIKitUser uiKitUser) {
        if (invitationCallListener != null) {
            ZegoCallUser inviteCaller = new ZegoCallUser(uiKitUser.userID, uiKitUser.userName);
            invitationCallListener.onOutgoingCallAccepted(callInvitationData.callID, inviteCaller);
        }
    }

    public void notifyOutgoingCallRejected0rDeclined(ZegoUIKitUser uiKitUser, String data) {
        if (invitationCallListener != null) {
            try {
                JSONObject jsonObject = new JSONObject(data);
                String reason = getStringFromJson(jsonObject, "reason");
                ZegoCallUser inviteCaller = new ZegoCallUser(uiKitUser.userID, uiKitUser.userName);
                if ("busy".equals(reason)) {
                    invitationCallListener.onOutgoingCallRejectedCauseBusy(callInvitationData.callID, inviteCaller);
                } else {
                    invitationCallListener.onOutgoingCallDeclined(callInvitationData.callID, inviteCaller);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void notifyOutgoingCallTimeout(List<ZegoUIKitUser> invitees) {
        if (invitationCallListener != null) {
            List<ZegoCallUser> callees = new ArrayList<>();
            for (ZegoUIKitUser user : invitees) {
                callees.add(new ZegoCallUser(user.userID, user.userName));
            }
            invitationCallListener.onOutgoingCallTimeout(callInvitationData.callID, callees);
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
            topActivity = activity;
            boolean notificationShowed = isNotificationShowed();
            if (notificationShowed && pushMessage == null) {
                if (!(topActivity instanceof CallInviteActivity)) {
                    Intent intent = new Intent(activity, CallInviteActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("page", "incoming");
                    intent.putExtra("bundle", bundle);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setAction(CallNotificationManager.ACTION_CLICK);
                    activity.startActivity(intent);
                }
            }
            boolean canShowFullOnLockScreen = CallInvitationServiceImpl.getInstance().canShowFullOnLockScreen();
            if (!(topActivity instanceof CallInviteActivity)) {
                //if click app directly
                if (notificationAction == null) {
                    setZIMPushMessage(null);
                }
            }
            if (canShowFullOnLockScreen) {
                if (!(topActivity instanceof CallInviteActivity)) {
                    dismissCallNotification();
                }
            } else {
                dismissCallNotification();
            }
        }

        @Override
        public void onActivityPaused(@NonNull Activity activity) {
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

        private boolean isActivityAlive(final Activity activity) {
            return activity != null && !activity.isFinishing() && (
                Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1 || !activity.isDestroyed());
        }

        public Activity getTopActivity() {
            return topActivity;
        }
    }
}
