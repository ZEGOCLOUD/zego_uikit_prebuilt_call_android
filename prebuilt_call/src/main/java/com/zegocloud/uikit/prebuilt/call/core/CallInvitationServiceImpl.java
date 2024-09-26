package com.zegocloud.uikit.prebuilt.call.core;

import android.app.Activity;
import android.app.Application;
import android.app.Notification;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import com.tencent.mmkv.MMKV;
import com.zegocloud.uikit.ZegoUIKit;
import com.zegocloud.uikit.plugin.adapter.plugins.signaling.ZegoSignalingPluginNotificationConfig;
import com.zegocloud.uikit.plugin.common.PluginCallbackListener;
import com.zegocloud.uikit.plugin.invitation.ZegoInvitationType;
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallConfig;
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallFragment;
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallService;
import com.zegocloud.uikit.prebuilt.call.config.DurationUpdateListener;
import com.zegocloud.uikit.prebuilt.call.core.basic.PrebuiltRoomRepository;
import com.zegocloud.uikit.prebuilt.call.core.basic.PrebuiltUserRepository;
import com.zegocloud.uikit.prebuilt.call.core.beauty.PrebuiltBeautyRepository;
import com.zegocloud.uikit.prebuilt.call.core.invite.PrebuiltCallLifecycleHandler;
import com.zegocloud.uikit.prebuilt.call.core.invite.PrebuiltCallRepository;
import com.zegocloud.uikit.prebuilt.call.core.invite.ZegoCallInvitationData;
import com.zegocloud.uikit.prebuilt.call.core.notification.PrebuiltCallNotificationManager;
import com.zegocloud.uikit.prebuilt.call.core.push.ZIMPushMessage;
import com.zegocloud.uikit.prebuilt.call.event.ErrorEventsListener;
import com.zegocloud.uikit.prebuilt.call.invite.ZegoUIKitPrebuiltCallInvitationConfig;
import com.zegocloud.uikit.prebuilt.call.invite.internal.CallInvitationDialog;
import com.zegocloud.uikit.prebuilt.call.invite.internal.CallStateListener;
import com.zegocloud.uikit.prebuilt.call.invite.internal.RingtoneManager;
import com.zegocloud.uikit.prebuilt.call.invite.internal.ZegoTranslationText;
import com.zegocloud.uikit.prebuilt.call.invite.internal.ZegoUIKitPrebuiltCallConfigProvider;
import com.zegocloud.uikit.service.defines.ZegoAudioOutputDevice;
import com.zegocloud.uikit.service.defines.ZegoScenario;
import com.zegocloud.uikit.service.defines.ZegoUIKitCallback;
import com.zegocloud.uikit.service.defines.ZegoUIKitUser;
import im.zego.zegoexpress.entity.ZegoUser;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import timber.log.Timber;

public class CallInvitationServiceImpl {

    private CallInvitationServiceImpl() {
    }

    private static final class SingletonHolder {

        private static final CallInvitationServiceImpl INSTANCE = new CallInvitationServiceImpl();
    }

    public static CallInvitationServiceImpl getInstance() {
        return SingletonHolder.INSTANCE;
    }

    private PrebuiltCallZIMBridge zimBridge = new PrebuiltCallZIMBridge();
    private PrebuiltCallExpressBridge expressBridge = new PrebuiltCallExpressBridge();
    private PrebuiltBeautyRepository beautyRepository = new PrebuiltBeautyRepository();
    private PrebuiltCallNotificationManager notificationManager = new PrebuiltCallNotificationManager();

    private PrebuiltRoomRepository roomRepository = new PrebuiltRoomRepository(expressBridge);
    private PrebuiltUserRepository userRepository = new PrebuiltUserRepository(expressBridge, zimBridge);
    private PrebuiltCallRepository callRepository = new PrebuiltCallRepository(zimBridge, expressBridge,
        notificationManager);
    private PrebuiltCallLifecycleHandler lifecycleHandler = new PrebuiltCallLifecycleHandler();

    private CallInvitationDialog invitationDialog;

    private boolean alreadyInit = false;
    private Application application;
    private ZegoUIKitPrebuiltCallInvitationConfig invitationConfig;
    private ZegoUIKitPrebuiltCallConfig callConfig;

    public void showIncomingCallDialog(ZegoCallInvitationData callInvitationData) {
        playIncomingRingTone();

        Activity topActivity = CallInvitationServiceImpl.getInstance().getTopActivity();
        invitationDialog = new CallInvitationDialog(topActivity, callInvitationData);
        invitationDialog.show();
    }

    public void hideIncomingCallDialog() {
        if (invitationDialog != null) {
            invitationDialog.hide();
        }
        invitationDialog = null;
    }

    public void showCallBackgroundNotification() {
        Activity topActivity = CallInvitationServiceImpl.getInstance().getTopActivity();
        notificationManager.showCallBackgroundNotification(topActivity);
    }

    public void dismissIncomingCallNotification() {
        Activity topActivity = CallInvitationServiceImpl.getInstance().getTopActivity();
        if (topActivity != null) {
            notificationManager.dismissCallNotification(topActivity);
        }
    }

    public void playIncomingRingTone() {
        RingtoneManager.playRingTone(true);
    }

    public void playOutgoingRingTone() {
        RingtoneManager.playRingTone(false);
    }

    public void stopRingTone() {
        RingtoneManager.stopRingTone();
    }

    public void openCamera(boolean open) {
        expressBridge.openCamera(open);
    }

    public void openMicrophone(boolean open) {
        expressBridge.openMicrophone(open);
    }

    public boolean isMicrophoneOn(String userID) {
        return expressBridge.isMicrophoneOn(userID);
    }

    public boolean isMicrophoneOn() {
        return expressBridge.isMicrophoneOn();
    }

    public boolean isCameraOn() {
        return expressBridge.isCameraOn();
    }

    public void setAudioOutputToSpeaker(boolean outputToSpeaker) {
        expressBridge.setAudioOutputToSpeaker(outputToSpeaker);
    }

    public ZegoAudioOutputDevice getAudioRouteType() {
        return expressBridge.getAudioRouteType();
    }


    public void setUpCallbacksOnAppStart(Application application) {
        lifecycleHandler.setupCallbacks(application);
    }

    public void endCall() {
        Timber.d("endCall() called");
        roomRepository.leaveSDKRoom();
        onPrebuiltCallRoomLeft(null);
    }

    public ZegoUIKitUser getLocalUser() {
        return userRepository.getLocalUser();
    }

    public ZegoCallInvitationData getCallInvitationData() {
        return callRepository.getCallInvitationData();
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

    public void initAndLoginUserByLastRecord(Application application) {
        MMKV mmkv = MMKV.defaultMMKV(MMKV.SINGLE_PROCESS_MODE, getClass().getName());
        if (mmkv.contains("appID")) {
            long preAppID = mmkv.getLong("appID", 0);
            String preAppSign = mmkv.getString("appSign", "");
            String token = mmkv.getString("appToken", "");
            initSDK(application, preAppID, preAppSign, token);
            userRepository.initAndLoginUserByLastRecord(null);
        }
    }

    public boolean init(Application application, long appID, String appSign, String token, String userID,
        String userName, ZegoUIKitPrebuiltCallInvitationConfig config) {

        boolean result = initSDK(application, appID, appSign, token);
        if (result) {
            loginUser(userID, userName);
            setCallInvitationConfig(config);
        }
        return result;
    }

    public boolean initSDK(Application application, long appID, String appSign, String token) {
        Timber.d("Call initSDK() called with: application = [" + application + "], appID = [" + appID
            + "], appSign.isEmpty() = [" + TextUtils.isEmpty(appSign) + "], token.isEmpty() = [" + TextUtils.isEmpty(
            token) + "],alreadyInit: " + alreadyInit);
        if (alreadyInit) {
            // we assume that user not changed his appID and appSign
            ErrorEventsListener errorEvents = ZegoUIKitPrebuiltCallService.events.getErrorEventsListener();
            if (errorEvents != null) {
                errorEvents.onError(ErrorEventsListener.INIT_ALREADY,
                    "ZEGO Express Engine is already initialized, do not initialize again");
            }
            return true;
        }

        boolean result = ZegoUIKit.init(application, appID, appSign, ZegoScenario.STANDARD_VIDEO_CALL);
        if (result) {
            alreadyInit = true;
            this.application = application;

            MMKV mmkv = MMKV.defaultMMKV(MMKV.SINGLE_PROCESS_MODE, getClass().getName());
            mmkv.putLong("appID", appID);
            mmkv.putString("appSign", appSign);
            mmkv.putString("appToken", token);

            if (!TextUtils.isEmpty(token)) {
                expressBridge.renewToken(token);
            }
        }
        if (!result) {
            String errorMessage = "Create engine error,please check if your AppID and AppSign is correct";
            Timber.e(errorMessage);
            ErrorEventsListener errorEvents = ZegoUIKitPrebuiltCallService.events.getErrorEventsListener();
            if (errorEvents != null) {
                errorEvents.onError(ErrorEventsListener.INIT_PARAM_ERROR, errorMessage);
            }
        }
        return result;
    }

    public void setCallResourceID(String resourceID) {
        callRepository.setCallResourceID(resourceID);
    }

    public void clearPushMessage() {
        callRepository.clearPushMessage();
    }

    public void setCallInvitationConfig(ZegoUIKitPrebuiltCallInvitationConfig invitationConfig) {
        this.invitationConfig = invitationConfig;

        initRingtoneManager(application, invitationConfig);

        notificationManager.createCallNotificationChannel(application, invitationConfig);

        if (invitationConfig.translationText != null) {
            invitationConfig.translationText.copyFromInnerTextIfNotCustomized(invitationConfig.innerText);
        } else {
            invitationConfig.translationText = new ZegoTranslationText();
        }
    }

    public void setLeaveWhenOnlySelf(boolean leave) {
        roomRepository.setLeaveWhenOnlySelf(leave);
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
        return lifecycleHandler.getTopActivity();
    }

    public void loginUser(String userID, String userName) {
        userRepository.loginUser(userID, userName, null);
    }

    public void initBeautyPlugin() {
        if (callConfig != null) {
            beautyRepository.updateLanguageSettingsAndApply(callConfig);
            MMKV mmkv = MMKV.defaultMMKV(MMKV.SINGLE_PROCESS_MODE, getClass().getName());
            long appID = mmkv.getLong("appID", 0);
            String appSign = mmkv.getString("appSign", "");
            beautyRepository.init(application, appID, appSign);
        }
    }

    public void resetAllBeautiesToDefault() {
        beautyRepository.resetBeautyValueToDefault(null);
    }


    public void logoutUser() {
        Timber.d("logoutUser() called");
        userRepository.logoutUser();
    }

    public void unInit() {
        Timber.d("unInit() called");
        endCall();
        logoutUser();
        unInitSDK();
    }

    public void unInitSDK() {
        Timber.d("unInitSDK() called");
        onPrebuiltCallRoomLeft(null);
        onPrebuiltCallUserLogout(null, null);
        alreadyInit = false;
        invitationConfig = null;
        callConfig = null;

        // when receive offline calls,no logout ,and just destroy,will keep receive
        // offline calls.
        zimBridge.destroy();
    }

    public boolean canShowFullOnLockScreen() {
        if (Build.MANUFACTURER.equalsIgnoreCase("xiaomi")) {
            // xiaomi
            return false;
        }
        return true;
    }

    public ZegoUIKitPrebuiltCallConfigProvider getPrebuiltCallConfigProvider() {
        if (invitationConfig == null) {
            return null;
        } else {
            return invitationConfig.provider;
        }
    }

    public void setCallState(int callState) {
        callRepository.setCallState(callState);
    }

    public void addCallStateListener(CallStateListener callStateListener) {
        callRepository.addCallStateListener(callStateListener);
    }

    public void removeCallStateListener(CallStateListener callStateListener) {
        callRepository.removeCallStateListener(callStateListener);
    }

    public void showCallNotification(Context context) {
        notificationManager.showCallNotification(context);
    }

    public void dismissCallNotification() {
        if (application != null) {
            notificationManager.dismissCallNotification(application);
        }
    }

    public void dismissCallNotification(Context context) {
        notificationManager.dismissCallNotification(context);
    }

    public boolean isCallNotificationShowed() {
        return notificationManager.isCallNotificationShowed();
    }

    public String getCallNotificationMessage(boolean isVideoCall, boolean isGroup) {
        return notificationManager.getBackgroundNotificationMessage(isVideoCall, isGroup);
    }

    public String getCallNotificationTitle(boolean isVideoCall, boolean isGroup, String userName) {
        return notificationManager.getBackgroundNotificationTitle(isVideoCall, isGroup, userName);
    }

    public Notification getCallNotification(Context context) {
        return notificationManager.createCallNotification(context);
    }

    public boolean isInCallRoom() {
        return roomRepository.isInCallRoom();
    }

    public void setDurationUpdateListener(DurationUpdateListener updateListener) {
        roomRepository.setDurationUpdateListener(updateListener);
    }

    public long getStartTimeLocal() {
        return roomRepository.getStartTimeLocal();
    }

    public void joinRoom(String roomID, ZegoUIKitCallback callback) {
        roomRepository.joinRoom(roomID, callback);
    }

    public void leaveRoom() {
        roomRepository.leaveSDKRoom();
        onPrebuiltCallRoomLeft(null);
    }

    public void setNotificationClickAction(String action) {
        callRepository.setNotificationAction(action);
    }

    public String getNotificationAction() {
        return callRepository.getNotificationAction();
    }

    public ZIMPushMessage getZIMPushMessage() {
        return callRepository.getPushMessage();
    }

    public void setZIMPushMessage(ZIMPushMessage pushMessage) {
        callRepository.setPushMessage(pushMessage);
    }

    public void parsePayload() {
        callRepository.parsePayload();
    }

    public void enableFCMPush() {
        callRepository.enableFCMPush();
    }

    public void disableFCMPush() {
        callRepository.disableFCMPush();
    }

    public void enableHWPush(String hwAppID) {
        callRepository.enableHWPush(hwAppID);
    }

    public void enableMiPush(String miAppID, String miAppKey) {
        callRepository.enableMiPush(miAppID, miAppKey);
    }


    public void enableVivoPush(String vivoAppID, String vivoAppKey) {
        callRepository.enableVivoPush(vivoAppID, vivoAppKey);
    }

    public void enableOppoPush(String oppoAppID, String oppoAppKey, String oppoAppSecret) {
        callRepository.enableOppoPush(oppoAppID, oppoAppKey, oppoAppSecret);
    }

    public void setAppType(int appType) {
        callRepository.setAppType(appType);
    }

    public void sendInvitationWithUIChange(Activity activity, List<ZegoUIKitUser> invitees,
        ZegoInvitationType invitationType, String customData, int timeout, String callID,
        ZegoSignalingPluginNotificationConfig notificationConfig, PluginCallbackListener callbackListener) {
        callRepository.sendInvitationWithUIChange(activity, invitees, invitationType, customData, timeout, callID,
            notificationConfig, callbackListener);
    }

    public void sendInvitation(List<ZegoUIKitUser> invitees, ZegoInvitationType invitationType, String customData,
        int timeout, String callID, ZegoSignalingPluginNotificationConfig notificationConfig,
        PluginCallbackListener callbackListener) {

        callRepository.sendInvitation(invitees, invitationType, customData, timeout, callID, notificationConfig,
            callbackListener);
    }

    public void cancelInvitation(PluginCallbackListener callbackListener) {
        callRepository.cancelInvitation(callbackListener);
    }

    public void rejectInvitation(PluginCallbackListener callbackListener) {
        callRepository.rejectInvitation(callbackListener);
    }

    public void acceptInvitation(PluginCallbackListener callbackListener) {
        callRepository.acceptInvitation(callbackListener);
    }

    public void setPrebuiltCallFragment(ZegoUIKitPrebuiltCallFragment zegoUIKitPrebuiltCallFragment) {
        roomRepository.setPrebuiltCallFragment(zegoUIKitPrebuiltCallFragment);
    }

    public ZegoUIKitPrebuiltCallFragment getZegoUIKitPrebuiltCallFragment() {
        return roomRepository.getCallFragment();
    }

    public void onPrebuiltCallRoomJoined(String roomID) {
        Timber.d("onPrebuiltCallRoomJoined() called with: roomID = [" + roomID + "]");
        clearPushMessage();
    }

    public void onPrebuiltCallRoomLeft(String roomID) {
        Timber.d("onPrebuiltCallRoomLeft() called with: roomID = [" + roomID + "]");
        callRepository.onPrebuiltRoomLeft();
    }

    public void onPrebuiltCallUserLogin(String userID, String userName) {
        Timber.d("onPrebuiltCallUserLogin() called with: userID = [" + userID + "], userName = [" + userName + "]");
        callRepository.onPrebuiltUserLogin();
    }

    public void onPrebuiltCallUserLogout(String userID, String userName) {
        Timber.d("onPrebuiltCallUserLogout() called with: userID = [" + userID + "], userName = [" + userName + "]");

        callRepository.onPrebuiltUserLogout();
    }

    public void onPrebuiltCallRoomUserLeft(ArrayList<ZegoUser> userList, String roomID) {
        List<String> leaveUsers = userList.stream().map(zegoUser -> zegoUser.userID).collect(Collectors.toList());
        Timber.d(
            "onPrebuiltCallRoomUserLeft() called with: userList = [" + leaveUsers + "], roomID = [" + roomID + "]");
        callRepository.onPrebuiltCallRoomUserLeft(leaveUsers, roomID);
    }

    public String getPrebuiltCallRoom() {
        return roomRepository.getPrebuiltRoomID();
    }
}
