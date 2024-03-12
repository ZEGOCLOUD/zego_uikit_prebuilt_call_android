package com.zegocloud.uikit.prebuilt.call.invite;

import android.app.Activity;
import android.app.Application;
import com.zegocloud.uikit.plugin.adapter.plugins.signaling.ZegoSignalingPluginNotificationConfig;
import com.zegocloud.uikit.plugin.common.PluginCallbackListener;
import com.zegocloud.uikit.plugin.invitation.ZegoInvitationType;
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallFragment;
import com.zegocloud.uikit.prebuilt.call.event.Events;
import com.zegocloud.uikit.prebuilt.call.invite.internal.CallInvitationServiceImpl;
import com.zegocloud.uikit.prebuilt.call.invite.internal.IncomingCallButtonListener;
import com.zegocloud.uikit.prebuilt.call.invite.internal.OutgoingCallButtonListener;
import com.zegocloud.uikit.prebuilt.call.invite.internal.ZegoInvitationCallListener;
import com.zegocloud.uikit.service.defines.ZegoUIKitUser;
import java.util.List;

public class ZegoUIKitPrebuiltCallInvitationService {

    public static Events events = new Events();

    /**
     * ZEGO SDK will init and login,only after init and login,SDK can send/receive call to/from others.Please make sure SDK
     * init success and login success.
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
     * use ZegoUIKitPrebuiltCallInvitationService.events.invitationEvents.setIncomingCallButtonListener() instead;
     *
     * @deprecated use
     * {@link
     * com.zegocloud.uikit.prebuilt.call.event.InvitationEvents#setIncomingCallButtonListener(IncomingCallButtonListener)}
     * instead.
     */
    @Deprecated
    public static void addIncomingCallButtonListener(IncomingCallButtonListener listener) {
        events.invitationEvents.setIncomingCallButtonListener(listener);
    }

    /**
     * use ZegoUIKitPrebuiltCallInvitationService.events.invitationEvents.setOutgoingCallButtonListener() instead;
     *
     * @deprecated use
     * {@link
     * com.zegocloud.uikit.prebuilt.call.event.InvitationEvents#setOutgoingCallButtonListener(OutgoingCallButtonListener)}
     * instead.
     */
    @Deprecated
    public static void addOutgoingCallButtonListener(OutgoingCallButtonListener listener) {
        events.invitationEvents.setOutgoingCallButtonListener(listener);
    }

    /**
     * use ZegoUIKitPrebuiltCallInvitationService.events.invitationEvents.setInvitationListener() instead;
     *
     * @param listener
     * @deprecated use
     * {@link
     * com.zegocloud.uikit.prebuilt.call.event.InvitationEvents#setInvitationListener(ZegoInvitationCallListener)}
     * instead.
     */
    @Deprecated
    public static void addInvitationCallListener(ZegoInvitationCallListener listener) {
        events.invitationEvents.setInvitationListener(listener);
    }

    /**
     * use ZegoUIKitPrebuiltCallInvitationService.events.invitationEvents.setInvitationListener() instead;
     *
     * @deprecated use
     * {@link
     * com.zegocloud.uikit.prebuilt.call.event.InvitationEvents#setInvitationListener(ZegoInvitationCallListener)}
     * instead.
     */
    @Deprecated
    public static void removeInvitationCallListener() {
        events.invitationEvents.setInvitationListener(null);
    }

    public static ZegoUIKitPrebuiltCallFragment getPrebuiltCallFragment() {
        return CallInvitationServiceImpl.getInstance().getZegoUIKitPrebuiltCallFragment();
    }


    public static void endCall() {
        ZegoUIKitPrebuiltCallFragment prebuiltCallFragment = getPrebuiltCallFragment();
        if (prebuiltCallFragment != null) {
            prebuiltCallFragment.endCall();
        }
        CallInvitationServiceImpl.getInstance().leaveRoom();
    }

    public static void minimizeCall() {
        ZegoUIKitPrebuiltCallFragment callFragment = ZegoUIKitPrebuiltCallInvitationService.getPrebuiltCallFragment();
        if (callFragment != null) {
            callFragment.minimizeCall();
        }
    }

    public static void sendInvitationWithUIChange(Activity activity, List<ZegoUIKitUser> invitees,
        ZegoInvitationType type, PluginCallbackListener callbackListener) {
        CallInvitationServiceImpl.getInstance()
            .sendInvitationWithUIChange(activity, invitees, type, "", 60, null, null, callbackListener);
    }

    public static void sendInvitationWithUIChange(Activity activity, List<ZegoUIKitUser> invitees,
        ZegoInvitationType type, String resourceID, PluginCallbackListener callbackListener) {
        ZegoSignalingPluginNotificationConfig notificationConfig = new ZegoSignalingPluginNotificationConfig();
        notificationConfig.setResourceID(resourceID);
        CallInvitationServiceImpl.getInstance()
            .sendInvitationWithUIChange(activity, invitees, type, "", 60, null, notificationConfig, callbackListener);
    }

    public static void sendInvitationWithUIChange(Activity activity, List<ZegoUIKitUser> invitees,
        ZegoInvitationType type, String customData, String callID,
        ZegoSignalingPluginNotificationConfig notificationConfig, PluginCallbackListener callbackListener) {
        CallInvitationServiceImpl.getInstance()
            .sendInvitationWithUIChange(activity, invitees, type, customData, 60, callID, notificationConfig,
                callbackListener);
    }
}
