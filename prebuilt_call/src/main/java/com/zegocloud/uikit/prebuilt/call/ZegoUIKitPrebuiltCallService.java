package com.zegocloud.uikit.prebuilt.call;

import android.app.Activity;
import android.app.Application;
import com.zegocloud.uikit.plugin.adapter.plugins.signaling.ZegoSignalingPluginNotificationConfig;
import com.zegocloud.uikit.plugin.common.PluginCallbackListener;
import com.zegocloud.uikit.plugin.invitation.ZegoInvitationType;
import com.zegocloud.uikit.prebuilt.call.event.Events;
import com.zegocloud.uikit.prebuilt.call.invite.ZegoUIKitPrebuiltCallInvitationConfig;
import com.zegocloud.uikit.prebuilt.call.invite.ZegoUIKitPrebuiltCallInvitationService;
import com.zegocloud.uikit.prebuilt.call.invite.internal.CallInvitationServiceImpl;
import com.zegocloud.uikit.service.defines.ZegoUIKitUser;
import java.util.List;

public class ZegoUIKitPrebuiltCallService {

    public static Events events = new Events();

    /**
     * ZEGO SDK will init and login,only after init and login,SDK can send/receive call to/from others.Please make sure
     * SDK init success and login success.
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
        CallInvitationServiceImpl.getInstance().init(application, appID, appSign);
        CallInvitationServiceImpl.getInstance().loginUser(userID, userName);
        CallInvitationServiceImpl.getInstance().setCallInvitationConfig(config);
    }

    public static void unInit() {
        CallInvitationServiceImpl.getInstance().unInit();
    }

    /**
     * End and leave current call.Current activity will be finished.
     */
    public static void endCall() {
        ZegoUIKitPrebuiltCallFragment prebuiltCallFragment = getPrebuiltCallFragment();
        if (prebuiltCallFragment != null) {
            prebuiltCallFragment.endCall();
        }
        CallInvitationServiceImpl.getInstance().leaveRoom();
    }

    /**
     * if you are in a call ,then ZegoUIKitPrebuiltCallFragment will be returned else null will be returned.
     *
     * @return ZegoUIKitPrebuiltCallFragment or null.
     */
    public static ZegoUIKitPrebuiltCallFragment getPrebuiltCallFragment() {
        return CallInvitationServiceImpl.getInstance().getZegoUIKitPrebuiltCallFragment();
    }

    /**
     * if you have configured Minimize button in top or bottom bars,and have granted related permissions,you can use
     * this method to minimize the current call activity to a float window.
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
}
