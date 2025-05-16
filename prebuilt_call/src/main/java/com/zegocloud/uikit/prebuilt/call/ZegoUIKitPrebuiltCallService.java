package com.zegocloud.uikit.prebuilt.call;

import android.Manifest.permission;
import android.app.Activity;
import android.app.Application;
import android.provider.Settings;
import com.zegocloud.uikit.plugin.adapter.plugins.signaling.ZegoSignalingPluginNotificationConfig;
import com.zegocloud.uikit.plugin.common.PluginCallbackListener;
import com.zegocloud.uikit.plugin.invitation.ZegoInvitationType;
import com.zegocloud.uikit.prebuilt.call.config.ZegoBottomMenuBarConfig;
import com.zegocloud.uikit.prebuilt.call.config.ZegoMenuBarButtonName;
import com.zegocloud.uikit.prebuilt.call.config.ZegoTopMenuBarConfig;
import com.zegocloud.uikit.prebuilt.call.core.CallInvitationServiceImpl;
import com.zegocloud.uikit.prebuilt.call.event.CallEvents;
import com.zegocloud.uikit.prebuilt.call.event.Events;
import com.zegocloud.uikit.prebuilt.call.invite.ZegoUIKitPrebuiltCallInvitationConfig;
import com.zegocloud.uikit.service.defines.ZegoAudioOutputDevice;
import com.zegocloud.uikit.service.defines.ZegoInRoomCommandListener;
import com.zegocloud.uikit.service.defines.ZegoSendInRoomCommandCallback;
import com.zegocloud.uikit.service.defines.ZegoUIKitUser;
import java.util.ArrayList;
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
        CallInvitationServiceImpl.getInstance().api_init(application, appID, appSign, null, userID, userName, config);
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
        CallInvitationServiceImpl.getInstance().api_init(application, appID, null, token, userID, userName, config);
    }

    /**
     * Should call this method as soon as the user logout from app if you don't need call-invite feature,please don't
     * use this method.
     */
    public static void unInit() {
        CallInvitationServiceImpl.getInstance().api_unInit();
    }

    /**
     * used with zim_kit,not for usual use.
     */
    public static void logoutUser() {
        CallInvitationServiceImpl.getInstance().api_logoutUser();
    }

    /**
     * End and leave current call.Current activity will be finished.
     */
    public static void endCall() {
        CallInvitationServiceImpl.getInstance().api_endCall();
    }

    /**
     * This function can be used to get `ZegoUIKitPrebuiltCallFragment` when in a call.If not in a call,and null will be
     * returned.
     *
     * @return ZegoUIKitPrebuiltCallFragment or null.
     */
    public static ZegoUIKitPrebuiltCallFragment getPrebuiltCallFragment() {
        return CallInvitationServiceImpl.getInstance().api_getZegoUIKitPrebuiltCallFragment();
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
        ZegoUIKitPrebuiltCallFragment callFragment = getPrebuiltCallFragment();
        if (callFragment != null) {
            callFragment.api_minimizeCall();
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
            .api_sendInvitationWithUIChange(activity, invitees, type, "", 60, null, null, callbackListener);
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
            .api_sendInvitationWithUIChange(activity, invitees, type, "", 60, null, notificationConfig,
                callbackListener);
    }

    public static void sendInvitationWithUIChange(Activity activity, List<ZegoUIKitUser> invitees,
        ZegoInvitationType type, String customData, ZegoSignalingPluginNotificationConfig notificationConfig,
        PluginCallbackListener callbackListener) {
        CallInvitationServiceImpl.getInstance()
            .api_sendInvitationWithUIChange(activity, invitees, type, customData, 60, null, notificationConfig,
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
            .api_sendInvitationWithUIChange(activity, invitees, type, customData, 60, callID, notificationConfig,
                callbackListener);
    }

    public static void sendInvitation(List<ZegoUIKitUser> invitees, ZegoInvitationType invitationType,
        String customData, int timeout, String callID, ZegoSignalingPluginNotificationConfig notificationConfig,
        PluginCallbackListener callbackListener) {
        CallInvitationServiceImpl.getInstance()
            .api_sendInvitation(invitees, invitationType, customData, timeout, callID, notificationConfig,
                callbackListener);
    }

    public static void openCamera(boolean enable) {
        CallInvitationServiceImpl.getInstance().api_openCamera(enable);
    }

    public static void openMicrophone(boolean enable) {
        CallInvitationServiceImpl.getInstance().api_openMicrophone(enable);
    }

    public static boolean isMicrophoneOn() {
        return CallInvitationServiceImpl.getInstance().api_isMicrophoneOn();
    }

    public static boolean isCameraOn() {
        return CallInvitationServiceImpl.getInstance().api_isCameraOn();
    }

    public static void enableFCMPush() {
        CallInvitationServiceImpl.getInstance().api_enableFCMPush();
    }

    public static void enableHWPush(String hwAppID) {
        CallInvitationServiceImpl.getInstance().api_enableHWPush(hwAppID);
    }

    public static void enableMiPush(String miAppID, String miAppKey) {
        CallInvitationServiceImpl.getInstance().api_enableMiPush(miAppID, miAppKey);
    }

    public static void enableVivoPush(String vivoAppID, String vivoAppKey) {
        CallInvitationServiceImpl.getInstance().api_enableVivoPush(vivoAppID, vivoAppKey);
    }

    public static void enableOppoPush(String oppoAppID, String oppoAppKey, String oppoAppSecret) {
        CallInvitationServiceImpl.getInstance().api_enableOppoPush(oppoAppID, oppoAppKey, oppoAppSecret);
    }

    /**
     * Details Description: Whether to use the speaker to play audio. When choosing not to use the built-in speaker for
     * sound output, the SDK will select the audio output device with the highest priority according to the system
     * scheduling to play the sound. Common audio routing includes: earpiece, headphones, Bluetooth devices, etc.
     * <br>
     * Related Interface: Get the current audio routing {@link ZegoUIKitPrebuiltCallService#getAudioRouteType}.
     * <br>
     * Usage Restrictions: Only supports switching between the earpiece and the speaker. If it is a Bluetooth headset or
     * wired headphones, it does not support routing to the speaker through this interface.
     *
     * @param outputToSpeaker Whether to use the speaker to play audio
     */
    public static void setAudioOutputToSpeaker(boolean outputToSpeaker) {
        CallInvitationServiceImpl.getInstance().api_setAudioOutputToSpeaker(outputToSpeaker);
    }

    /**
     * Details Description: Audio routing refers to the audio output device used by the App when playing audio. Common
     * audio routing options include: speaker, earpiece, headphones, Bluetooth devices, etc.
     * <br>
     * Related Interface: Set the audio routing to the speaker
     * {@link ZegoUIKitPrebuiltCallService#setAudioOutputToSpeaker(boolean)}.
     *
     * @return current audio routing
     */
    public static ZegoAudioOutputDevice getAudioRouteType() {
        return CallInvitationServiceImpl.getInstance().api_getAudioRouteType();
    }

    /**
     * reset all beauty values to default.
     */
    public static void resetAllBeautiesToDefault() {
        CallInvitationServiceImpl.getInstance().api_resetAllBeautiesToDefault();
    }

    /**
     * used for offline calls,
     *
     * @param appType
     */
    public static void setAppType(int appType) {
        CallInvitationServiceImpl.getInstance().api_setAppType(appType);
    }

    /**
     * interface to send custom command in room.
     * Other users in the room can listen to this by {@link  CallEvents#addInRoomCommandListener(ZegoInRoomCommandListener)}
     * @param command command content, max 1024 bytes.To protect privacy, please do not include any sensitive user information in this interface, including but not limited to phone numbers, ID card numbers, passport numbers, real names, etc.
     * @param toUserList  command recipient list. Note: When it is [null], the SDK will send custom command to all users in the room.
     * @param callback send command result.
     */
    public static void sendInRoomCommand(String command, ArrayList<String> toUserList, ZegoSendInRoomCommandCallback callback) {
        CallInvitationServiceImpl.getInstance().api_sendInRoomCommand(command, toUserList, callback);
    }

    public String getVersion() {
        return CallInvitationServiceImpl.getInstance().getVersion();
    }
}
