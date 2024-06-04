package com.zegocloud.uikit.prebuilt.call;

import android.Manifest.permission;
import android.app.Activity;
import android.app.Application;
import android.provider.Settings;
import com.zegocloud.uikit.ZegoUIKit;
import com.zegocloud.uikit.plugin.adapter.plugins.signaling.ZegoSignalingPluginNotificationConfig;
import com.zegocloud.uikit.plugin.common.PluginCallbackListener;
import com.zegocloud.uikit.plugin.invitation.ZegoInvitationType;
import com.zegocloud.uikit.prebuilt.call.config.ZegoBottomMenuBarConfig;
import com.zegocloud.uikit.prebuilt.call.config.ZegoMenuBarButtonName;
import com.zegocloud.uikit.prebuilt.call.config.ZegoTopMenuBarConfig;
import com.zegocloud.uikit.prebuilt.call.event.Events;
import com.zegocloud.uikit.prebuilt.call.invite.ZegoUIKitPrebuiltCallInvitationConfig;
import com.zegocloud.uikit.prebuilt.call.invite.ZegoUIKitPrebuiltCallInvitationService;
import com.zegocloud.uikit.prebuilt.call.invite.internal.CallInvitationServiceImpl;
import com.zegocloud.uikit.service.defines.ZegoUIKitUser;
import java.util.List;

public class ZegoUIKitPrebuiltCallService {

    public static Events events = new Events();

    /**
     * ZEGO SDK will init and login.Only after init and login,SDK can send/receive call to/from others. Please make sure
     * SDK init success and login success.This method should be called as soon as user login your own business.If you
     * wan\'t to custom {@link ZegoUIKitPrebuiltCallConfig} in call-invite,you can use
     * {@link ZegoUIKitPrebuiltCallInvitationConfig#provider}.
     * <p>
     * if you don't need call-invite feature,please don't use this method.
     *
     * @param application
     * @param appID
     * @param appSign
     * @param userID
     * @param userName
     * @param config
     */
    public static void init(Application application, long appID, String appSign, String userID, String userName,
        ZegoUIKitPrebuiltCallInvitationConfig config) {
        CallInvitationServiceImpl.getInstance().init(application, appID, appSign, null);
        CallInvitationServiceImpl.getInstance().loginUser(userID, userName);
        CallInvitationServiceImpl.getInstance().setCallInvitationConfig(config);
    }

    /**
     * ZEGO SDK will init and login.Only after init and login,SDK can send/receive call to/from others. Please make sure
     * SDK init success and login success.This method should be called as soon as user login this own business.If you
     * wan\'t to custom {@link ZegoUIKitPrebuiltCallConfig} in call-invite,you can use
     * {@link ZegoUIKitPrebuiltCallInvitationConfig#provider}.
     * <p>
     * if you don't need call-invite feature,please don't use this method.
     *
     * @param application
     * @param appID
     * @param token
     * @param userID
     * @param userName
     * @param config
     */
    public static void initWithToken(Application application, long appID, String token, String userID, String userName,
        ZegoUIKitPrebuiltCallInvitationConfig config) {
        CallInvitationServiceImpl.getInstance().init(application, appID, null, token);
        CallInvitationServiceImpl.getInstance().loginUser(userID, userName);
        CallInvitationServiceImpl.getInstance().setCallInvitationConfig(config);
    }

    /**
     * Should call this method as soon as the user logout from app if you don't need call-invite feature,please don't
     * use this method.
     */
    public static void unInit() {
        CallInvitationServiceImpl.getInstance().unInit();
    }

    /**
     * End and leave current call.Current activity will be finished.
     */
    public static void endCall() {
        CallInvitationServiceImpl.getInstance().endCallAndInvokeCallback();
    }

    /**
     * This function can be used to get `ZegoUIKitPrebuiltCallFragment` when in a call.If not in a call,and null will be
     * returned.
     *
     * @return ZegoUIKitPrebuiltCallFragment or null.
     */
    public static ZegoUIKitPrebuiltCallFragment getPrebuiltCallFragment() {
        return CallInvitationServiceImpl.getInstance().getZegoUIKitPrebuiltCallFragment();
    }

    /**
     * This function is used to minimize the current call.usually it can be used with backpressed Listener to minimize
     * call when press back button.To make it work,you should :
     * <br>
     * 1.Have declares {@link permission#SYSTEM_ALERT_WINDOW} permission in manifest.
     * <br>
     * 2.User specifically grants the app this capability.{@link Settings#ACTION_MANAGE_OVERLAY_PERMISSION}
     * <br>
     * 3.Have {@link ZegoMenuBarButtonName#MINIMIZING_BUTTON} in {@link ZegoBottomMenuBarConfig#buttons} or
     * {@link ZegoTopMenuBarConfig#buttons}
     * <br>
     * 4.Use ZEGO call invite service.
     */
    public static void minimizeCall() {
        ZegoUIKitPrebuiltCallFragment callFragment = ZegoUIKitPrebuiltCallInvitationService.getPrebuiltCallFragment();
        if (callFragment != null) {
            callFragment.minimizeCall();
        }
    }

    /**
     * use this method to sendInvitation to other users and auto navigate to call waiting page.
     *
     * @param activity
     * @param invitees
     * @param type
     * @param callbackListener
     */
    public static void sendInvitationWithUIChange(Activity activity, List<ZegoUIKitUser> invitees,
        ZegoInvitationType type, PluginCallbackListener callbackListener) {
        CallInvitationServiceImpl.getInstance()
            .sendInvitationWithUIChange(activity, invitees, type, "", 60, null, null, callbackListener);
    }

    /**
     * use this method to sendInvitation to other users and auto navigate to call waiting page.
     *
     * @param activity
     * @param invitees
     * @param type
     * @param resourceID
     * @param callbackListener
     */
    public static void sendInvitationWithUIChange(Activity activity, List<ZegoUIKitUser> invitees,
        ZegoInvitationType type, String resourceID, PluginCallbackListener callbackListener) {
        ZegoSignalingPluginNotificationConfig notificationConfig = new ZegoSignalingPluginNotificationConfig();
        notificationConfig.setResourceID(resourceID);
        CallInvitationServiceImpl.getInstance()
            .sendInvitationWithUIChange(activity, invitees, type, "", 60, null, notificationConfig, callbackListener);
    }

    public static void sendInvitationWithUIChange(Activity activity, List<ZegoUIKitUser> invitees,
        ZegoInvitationType type, String customData, ZegoSignalingPluginNotificationConfig notificationConfig,
        PluginCallbackListener callbackListener) {
        CallInvitationServiceImpl.getInstance()
            .sendInvitationWithUIChange(activity, invitees, type, "", 60, null, notificationConfig,
                callbackListener);
    }

    /**
     * use this method to sendInvitation to other users and auto navigate to call waiting page.
     *
     * @param activity
     * @param invitees
     * @param type
     * @param customData
     * @param callID
     * @param notificationConfig
     * @param callbackListener
     */
    public static void sendInvitationWithUIChange(Activity activity, List<ZegoUIKitUser> invitees,
        ZegoInvitationType type, String customData, String callID,
        ZegoSignalingPluginNotificationConfig notificationConfig, PluginCallbackListener callbackListener) {
        CallInvitationServiceImpl.getInstance()
            .sendInvitationWithUIChange(activity, invitees, type, customData, 60, callID, notificationConfig,
                callbackListener);
    }

    public static void openCamera(boolean enable) {
        CallInvitationServiceImpl.getInstance().openCamera(enable);
    }

    public static void openMicrophone(boolean enable) {
        CallInvitationServiceImpl.getInstance().openMicrophone(enable);
    }

    public static boolean isMicrophoneOn() {
        return CallInvitationServiceImpl.getInstance().isMicrophoneOn();
    }

    public static boolean isCameraOn() {
        return CallInvitationServiceImpl.getInstance().isCameraOn();
    }

    public static void enableFCMPush() {
        CallInvitationServiceImpl.getInstance().enableFCMPush();
    }

    public static void enableHWPush(String hwAppID) {
        CallInvitationServiceImpl.getInstance().enableHWPush(hwAppID);
    }

    public static void enableMiPush(String miAppID, String miAppKey) {
        CallInvitationServiceImpl.getInstance().enableMiPush(miAppID, miAppKey);
    }

    public static void enableVivoPush(String vivoAppID, String vivoAppKey) {
        CallInvitationServiceImpl.getInstance().enableVivoPush(vivoAppID, vivoAppKey);
    }

    public static void enableOppoPush(String oppoAppID, String oppoAppKey, String oppoAppSecret) {
        CallInvitationServiceImpl.getInstance().enableOppoPush(oppoAppID, oppoAppKey, oppoAppSecret);
    }
}
