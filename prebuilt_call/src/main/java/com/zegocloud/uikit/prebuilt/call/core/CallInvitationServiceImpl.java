package com.zegocloud.uikit.prebuilt.call.core;

import android.app.Activity;
import android.app.Application;
import android.app.Dialog;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import com.zegocloud.uikit.ZegoUIKit;
import com.zegocloud.uikit.internal.ZegoUIKitLanguage;
import com.zegocloud.uikit.plugin.adapter.plugins.signaling.ZegoSignalingPluginNotificationConfig;
import com.zegocloud.uikit.plugin.common.PluginCallbackListener;
import com.zegocloud.uikit.plugin.invitation.ZegoInvitationType;
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallConfig;
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallFragment;
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallService;
import com.zegocloud.uikit.prebuilt.call.config.DurationUpdateListener;
import com.zegocloud.uikit.prebuilt.call.config.ZegoMenuBarButtonName;
import com.zegocloud.uikit.prebuilt.call.core.basic.PrebuiltRoomRepository;
import com.zegocloud.uikit.prebuilt.call.core.basic.PrebuiltUserRepository;
import com.zegocloud.uikit.prebuilt.call.core.beauty.PrebuiltBeautyRepository;
import com.zegocloud.uikit.prebuilt.call.core.invite.PrebuiltCallLifecycleHandler;
import com.zegocloud.uikit.prebuilt.call.core.invite.PrebuiltCallRepository;
import com.zegocloud.uikit.prebuilt.call.core.invite.ZegoCallInvitationData;
import com.zegocloud.uikit.prebuilt.call.core.notification.PrebuiltCallNotificationManager;
import com.zegocloud.uikit.prebuilt.call.core.notification.RingtoneManager;
import com.zegocloud.uikit.prebuilt.call.core.push.ZIMPushMessage;
import com.zegocloud.uikit.prebuilt.call.core.utils.Storage;
import com.zegocloud.uikit.prebuilt.call.event.ErrorEventsListener;
import com.zegocloud.uikit.prebuilt.call.invite.ZegoUIKitPrebuiltCallInvitationConfig;
import com.zegocloud.uikit.prebuilt.call.invite.internal.CallInvitationDialog;
import com.zegocloud.uikit.prebuilt.call.invite.internal.CallStateListener;
import com.zegocloud.uikit.prebuilt.call.invite.internal.InvitationTextCHS;
import com.zegocloud.uikit.prebuilt.call.invite.internal.ZegoCallText;
import com.zegocloud.uikit.prebuilt.call.invite.internal.ZegoTranslationText;
import com.zegocloud.uikit.prebuilt.call.invite.internal.ZegoUIKitPrebuiltCallConfigProvider;
import com.zegocloud.uikit.service.defines.ZegoAudioOutputDevice;
import com.zegocloud.uikit.service.defines.ZegoAudioOutputDeviceChangedListener;
import com.zegocloud.uikit.service.defines.ZegoInRoomCommandListener;
import com.zegocloud.uikit.service.defines.ZegoScenario;
import com.zegocloud.uikit.service.defines.ZegoSendInRoomCommandCallback;
import com.zegocloud.uikit.service.defines.ZegoUIKitCallback;
import com.zegocloud.uikit.service.defines.ZegoUIKitUser;
import com.zegocloud.uikit.service.express.IExpressEngineEventHandler;
import im.zego.uikit.libuikitreport.ReportUtil;
import im.zego.zegoexpress.entity.ZegoUser;
import java.util.ArrayList;
import java.util.HashMap;
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
        Activity topActivity = CallInvitationServiceImpl.getInstance().getTopActivity();
        Timber.d(
            "showIncomingCallDialog() called with: callInvitationData = [" + callInvitationData + "],topActivity = "
                + topActivity);
        if (!isIncomingCallDialogShown()) {

            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("call_id", callInvitationData.invitationID);
            hashMap.put("app_state", "active");
            ReportUtil.reportEvent("call/displayNotification", hashMap);

            playIncomingRingTone();
            invitationDialog = new CallInvitationDialog(topActivity, callInvitationData);
            invitationDialog.show();
        }
    }

    private boolean isIncomingCallDialogShown() {
        return invitationDialog != null && invitationDialog.isShowing();
    }

    public void hideIncomingCallDialog() {
        if (isIncomingCallDialogShown()) {
            Timber.d("hideIncomingCallDialog() called");
            invitationDialog.hide();
        }
        invitationDialog = null;
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
        this.application = application;
        lifecycleHandler.setupCallbacks(application);
    }

    public Application getApplication() {
        return application;
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
        String outgoingRingToneName;
        if (config == null || TextUtils.isEmpty(config.outgoingCallRingtone)) {
            outgoingRingToneName = "zego_outgoing";
        } else {
            outgoingRingToneName = config.outgoingCallRingtone;
        }
        int outgoingIdentifier = application.getResources()
            .getIdentifier(outgoingRingToneName, "raw", application.getPackageName());
        Uri ongoingUri = RingtoneManager.getUriFromID(application, outgoingIdentifier);
        RingtoneManager.setOutgoingUri(ongoingUri);

        String incomingRingToneName;
        if (config == null || TextUtils.isEmpty(config.incomingCallRingtone)) {
            incomingRingToneName = "zego_incoming";
        } else {
            incomingRingToneName = config.incomingCallRingtone;
        }

        int incomingIdentifier = application.getResources()
            .getIdentifier(incomingRingToneName, "raw", application.getPackageName());
        Uri inComingUri = RingtoneManager.getUriFromID(application, incomingIdentifier);

        RingtoneManager.setIncomingUri(inComingUri);
    }

    public void initAndLoginUserByLastRecord(Application application) {
        boolean containsAppID = Storage.contains("appID");
        Timber.d("initAndLoginUserByLastRecord() called with: containsAppID = [" + containsAppID + "]");
        if (containsAppID) {
            long preAppID = Storage.appID();
            String preAppSign = Storage.appSign();
            String token = Storage.appToken();

            initSDK(application, preAppID, preAppSign, token);
            userRepository.initAndLoginUserByLastRecord(null);
        } else {
            Timber.d("initAndLoginUserByLastRecord,no AppID found");
        }
    }

    public boolean init(Application application, long appID, String appSign, String token, String userID,
        String userName, ZegoUIKitPrebuiltCallInvitationConfig config) {

        HashMap<String, Object> commonParams = new HashMap<>();
        commonParams.put(ReportUtil.PLATFORM, "android");
        commonParams.put(ReportUtil.PLATFORM_VERSION, android.os.Build.VERSION.SDK_INT + "");
        commonParams.put(ReportUtil.UIKIT_VERSION, ZegoUIKit.getVersion());
        commonParams.put(ReportUtil.USER_ID, userID);
        commonParams.put("call_version", getVersion());
        String verify = "";
        if (!TextUtils.isEmpty(appSign)) {
            verify = appSign;
        } else {
            if (!TextUtils.isEmpty(token)) {
                verify = token;
            }
        }
        ReportUtil.create(appID, verify, commonParams);

        boolean result = initSDK(application, appID, appSign, token);
        Timber.d("initSDK result : " + result);
        if (result) {
            loginUser(userID, userName);
            setCallInvitationConfig(config);
        }
        return result;
    }

    public boolean initSDK(Application application, long appID, String appSign, String token) {
        Timber.d("init() called with: Build.MANUFACTURER = [" + Build.MANUFACTURER + "], Build.VERSION.SDK_INT = ["
            + Build.VERSION.SDK_INT + "],targetSdkVersion:" + application.getApplicationInfo().targetSdkVersion);

        Timber.d("Call initSDK() called with: version = [" + getVersion() + "], appID = [" + appID
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

        long startTime = System.currentTimeMillis();

        ZegoUIKit.init(application, appID, appSign, ZegoScenario.STANDARD_VIDEO_CALL);

        boolean initExpress = ZegoUIKit.isExpressEngineInitSucceed();
        boolean initZIM = ZegoUIKit.getSignalingPlugin().isPluginInitSucceed();

        HashMap<String, Object> hashMap = new HashMap<>();
        if (initExpress && initZIM) {
            hashMap.put("error", 0);
        } else {
            hashMap.put("error", -1);
        }
        hashMap.put("start_time", startTime);
        ReportUtil.reportEvent("call/init", hashMap);

        if (initExpress && initZIM) {
            alreadyInit = true;
            this.application = application;

            boolean encode = Storage.set_appID(appID);
            Storage.set_appSign(appSign);
            Storage.set_appToken(token);
            Timber.d("save_appID : appID = [" + appID + "],result:" + encode);

            if (!TextUtils.isEmpty(token)) {
                expressBridge.renewToken(token);
            }
        } else {
            String errorMessage = "Create engine error,please check if your AppID|AppSign|Token is correct";
            Timber.e(errorMessage);
            ErrorEventsListener errorEvents = ZegoUIKitPrebuiltCallService.events.getErrorEventsListener();
            if (errorEvents != null) {
                errorEvents.onError(ErrorEventsListener.INIT_PARAM_ERROR, errorMessage);
            }
        }
        return initExpress && initZIM;
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

    public void generateCallConfigFromInvite(ZegoCallInvitationData invitationData) {
        setCallConfig(ZegoUIKitPrebuiltCallInvitationConfig.generateDefaultConfig(invitationData));
        if (invitationConfig != null && invitationConfig.provider != null) {
            setCallConfig(invitationConfig.provider.requireConfig(invitationData));
        }

        if (invitationConfig != null && invitationConfig.translationText != null
            && invitationConfig.translationText.getInvitationBaseText() instanceof InvitationTextCHS) {
            callConfig.zegoCallText = new ZegoCallText(ZegoUIKitLanguage.CHS);
        } else {
            callConfig.zegoCallText = new ZegoCallText(ZegoUIKitLanguage.ENGLISH);
        }

        boolean hasBeautyButton = callConfig.bottomMenuBarConfig.buttons.contains(ZegoMenuBarButtonName.BEAUTY_BUTTON)
            || callConfig.topMenuBarConfig.buttons.contains(ZegoMenuBarButtonName.BEAUTY_BUTTON);
        if (hasBeautyButton) {
            CallInvitationServiceImpl.getInstance().initBeautyPlugin();
        }
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
            boolean containsAppID = Storage.contains("appID");
            if (containsAppID) {
                long appID = Storage.appID();
                String appSign = Storage.appSign();
                beautyRepository.init(application, appID, appSign);
            } else {
                Timber.d("initBeautyPlugin,no AppID found");
            }
        }
    }

    public void resetAllBeautiesToDefault() {
        beautyRepository.resetBeautyValueToDefault(null);
    }

    public Dialog getBeautyDialog(Context context) {
        return beautyRepository.getBeautyDialog(context);
    }

    public boolean isPluginExited() {
        return beautyRepository.isPluginExited();
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

        HashMap<String, Object> hashMap = new HashMap<>();
        ReportUtil.reportEvent("call/unInit", hashMap);
    }

    public void unInitSDK() {
        Timber.d("unInitSDK() called");
        onPrebuiltCallRoomLeft(null);
        invitationDialog = null;
        // when receive offline calls,no logout ,and just destroy,will keep receive
        // offline calls. and should clear userRepo data.
        if (userRepository.getUserInfo() != null) {
            userRepository.onUserLogoutSuccess();
        } else {
            onPrebuiltCallUserLogout(null, null);
        }

        alreadyInit = false;
        invitationConfig = null;
        setCallConfig(null);

        // when receive offline calls,no logout ,and just destroy,will keep receive
        // offline calls.
        zimBridge.destroy();
        ZegoUIKit.unInitExpressEngine();
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

    public void showCallNotification() {
        if (application != null) {
            notificationManager.showCallNotification(application);
        }
    }

    public void dismissCallNotification() {
        if (application != null) {
            notificationManager.dismissCallNotification(application);
        }
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

    public ZIMPushMessage getZIMPushMessage() {
        return callRepository.getPushMessage();
    }

    public void setZIMPushMessage(ZIMPushMessage pushMessage) {
        callRepository.setPushMessage(pushMessage);
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

        if (callConfig != null && callConfig.beautyConfig != null) {
            if (!callConfig.beautyConfig.saveLastBeautyParam) {
                resetAllBeautiesToDefault();
            }
        }

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

    public void api_openMicrophone(boolean enable) {
        Timber.d("api_openMicrophone: ");
        openMicrophone(enable);
    }

    public boolean api_isMicrophoneOn() {
        Timber.d("api_isMicrophoneOn: ");
        return isMicrophoneOn();
    }

    public boolean api_isCameraOn() {
        Timber.d("api_isCameraOn: ");
        return isCameraOn();
    }

    public void api_enableFCMPush() {
        Timber.d("api_enableFCMPush: ");
        enableFCMPush();
    }

    public void api_enableHWPush(String hwAppID) {
        Timber.d("api_enableHWPush: ");
        enableHWPush(hwAppID);
    }

    public void api_enableMiPush(String miAppID, String miAppKey) {
        Timber.d("api_enableMiPush: ");
        enableMiPush(miAppID, miAppKey);
    }

    public void api_enableVivoPush(String vivoAppID, String vivoAppKey) {
        Timber.d("api_enableVivoPush: ");
        enableVivoPush(vivoAppID, vivoAppKey);
    }

    public void api_enableOppoPush(String oppoAppID, String oppoAppKey, String oppoAppSecret) {
        Timber.d("api_enableOppoPush: ");
        enableOppoPush(oppoAppID, oppoAppKey, oppoAppSecret);
    }

    public void api_setAudioOutputToSpeaker(boolean outputToSpeaker) {
        Timber.d("api_setAudioOutputToSpeaker: ");
        setAudioOutputToSpeaker(outputToSpeaker);
    }

    public ZegoAudioOutputDevice api_getAudioRouteType() {
        Timber.d("api_getAudioRouteType: ");
        return getAudioRouteType();
    }

    public void api_resetAllBeautiesToDefault() {
        Timber.d("api_resetAllBeautiesToDefault: ");
        resetAllBeautiesToDefault();
    }

    public void api_setAppType(int appType) {
        Timber.d("api_setAppType: ");
        setAppType(appType);
    }

    public void api_openCamera(boolean open) {
        Timber.d("api_openCamera: ");
        openCamera(open);
    }

    public void api_endCall() {
        Timber.d("api_endCall: ");
        endCall();
    }

    public void api_sendInRoomCommand(String command, ArrayList<String> toUserList,
        ZegoSendInRoomCommandCallback callback) {
        Timber.d("api_sendInRoomCommand: ");
        expressBridge.sendInRoomCommand(command, toUserList, callback);
    }


    public boolean api_init(Application application, long appID, String appSign, String token, String userID,
        String userName, ZegoUIKitPrebuiltCallInvitationConfig config) {
        Timber.d("api_init: ");
        return init(application, appID, appSign, token, userID, userName, config);
    }


    public void api_logoutUser() {
        Timber.d("api_logoutUser: ");
        logoutUser();
    }

    public void api_unInit() {
        Timber.d("api_unInit: ");
        unInit();
    }

    public void api_sendInvitationWithUIChange(Activity activity, List<ZegoUIKitUser> invitees,
        ZegoInvitationType invitationType, String customData, int timeout, String callID,
        ZegoSignalingPluginNotificationConfig notificationConfig, PluginCallbackListener callbackListener) {
        Timber.d("api_sendInvitationWithUIChange: ");
        sendInvitationWithUIChange(activity, invitees, invitationType, customData, timeout, callID, notificationConfig,
            callbackListener);
    }

    public void api_sendInvitation(List<ZegoUIKitUser> invitees, ZegoInvitationType invitationType, String customData,
        int timeout, String callID, ZegoSignalingPluginNotificationConfig notificationConfig,
        PluginCallbackListener callbackListener) {
        Timber.d("api_sendInvitation: ");
        sendInvitation(invitees, invitationType, customData, timeout, callID, notificationConfig, callbackListener);
    }

    public ZegoUIKitPrebuiltCallFragment api_getZegoUIKitPrebuiltCallFragment() {
        Timber.d("api_getZegoUIKitPrebuiltCallFragment: ");
        return getZegoUIKitPrebuiltCallFragment();
    }

    public void api_removeEventHandler(IExpressEngineEventHandler expressEngineEventHandler) {
        Timber.d("api_removeEventHandler: ");
        expressBridge.removeEventHandler(expressEngineEventHandler);
    }

    public void api_addEventHandler(IExpressEngineEventHandler eventHandler) {
        Timber.d("api_addEventHandler: ");
        expressBridge.addEventHandler(eventHandler);
    }

    public void api_addAudioOutputDeviceChangedListener(ZegoAudioOutputDeviceChangedListener listener) {
        Timber.d("api_addAudioOutputDeviceChangedListener: ");
        expressBridge.addAudioOutputDeviceChangedListener(listener);
    }

    public void api_removeAudioOutputDeviceChangedListener(ZegoAudioOutputDeviceChangedListener listener) {
        Timber.d("api_removeAudioOutputDeviceChangedListener: ");
        expressBridge.removeAudioOutputDeviceChangedListener(listener);
    }

    public void api_addInRoomCommandListener(ZegoInRoomCommandListener listener) {
        Timber.d("api_addInRoomCommandListener: ");
        expressBridge.addInRoomCommandListener(listener);
    }

    public void api_removeInRoomCommandListener(ZegoInRoomCommandListener listener) {
        Timber.d("api_removeInRoomCommandListener: ");
        expressBridge.removeInRoomCommandListener(listener);
    }


    public String getVersion() {
        return "Prebuilt_Call:" + "3.8.8";
    }
}
