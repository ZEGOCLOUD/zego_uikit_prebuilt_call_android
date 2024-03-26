package com.zegocloud.uikit.prebuilt.call.invite;

import android.app.Activity;
import android.app.Application;
import com.zegocloud.uikit.plugin.adapter.plugins.signaling.ZegoSignalingPluginNotificationConfig;
import com.zegocloud.uikit.plugin.common.PluginCallbackListener;
import com.zegocloud.uikit.plugin.invitation.ZegoInvitationType;
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallFragment;
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallService;
import com.zegocloud.uikit.prebuilt.call.event.Events;
import com.zegocloud.uikit.prebuilt.call.invite.internal.IncomingCallButtonListener;
import com.zegocloud.uikit.prebuilt.call.invite.internal.OutgoingCallButtonListener;
import com.zegocloud.uikit.prebuilt.call.invite.internal.ZegoInvitationCallListener;
import com.zegocloud.uikit.service.defines.ZegoUIKitUser;
import java.util.List;

/**
 * @deprecated use {@link ZegoUIKitPrebuiltCallService } instead
 */
@Deprecated
public class ZegoUIKitPrebuiltCallInvitationService {

    @Deprecated
    public static Events events = ZegoUIKitPrebuiltCallService.events;

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
     * @deprecated use
     * {@link ZegoUIKitPrebuiltCallService#init(Application, long, String, String, String,
     * ZegoUIKitPrebuiltCallInvitationConfig)}  instead.
     */
    @Deprecated
    public static void init(Application application, long appID, String appSign, String userID, String userName,
        ZegoUIKitPrebuiltCallInvitationConfig config) {
        ZegoUIKitPrebuiltCallService.init(application, appID, appSign, userID, userName, config);
    }

    /**
     * {@link ZegoUIKitPrebuiltCallService#unInit()}   instead.
     */
    @Deprecated
    public static void unInit() {
        ZegoUIKitPrebuiltCallService.unInit();
    }

    /**
     * use ZegoUIKitPrebuiltCallService.events.invitationEvents.setIncomingCallButtonListener() instead;
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
     * use ZegoUIKitPrebuiltCallService.events.invitationEvents.setOutgoingCallButtonListener() instead;
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
     * use ZegoUIKitPrebuiltCallService.events.invitationEvents.setInvitationListener() instead;
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
     * use ZegoUIKitPrebuiltCallService.events.invitationEvents.setInvitationListener() instead;
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

    /**
     * if you are in a call ,then ZegoUIKitPrebuiltCallFragment will be returned else null will be returned.
     *
     * @deprecated use {@link ZegoUIKitPrebuiltCallService#getPrebuiltCallFragment()} instead.
     */
    @Deprecated
    public static ZegoUIKitPrebuiltCallFragment getPrebuiltCallFragment() {
        return ZegoUIKitPrebuiltCallService.getPrebuiltCallFragment();
    }


    /**
     * end and leave current call.
     *
     * @deprecated use {@link ZegoUIKitPrebuiltCallService#endCall()} instead.
     */
    @Deprecated
    public static void endCall() {
        ZegoUIKitPrebuiltCallService.endCall();
    }

    /**
     * if you have configured Minimize button in top or bottom bars,and have granted related permissions,you can use
     * this method to minimize the current call activity to a float window.
     *
     * @deprecated use {@link ZegoUIKitPrebuiltCallService#minimizeCall()} instead.
     */
    @Deprecated
    public static void minimizeCall() {
        ZegoUIKitPrebuiltCallService.minimizeCall();
    }

    /**
     * use this method to sendInvitation to other users and auto navigate to call waiting page.
     *
     * @deprecated use
     * {@link ZegoUIKitPrebuiltCallService#sendInvitationWithUIChange(Activity, List, ZegoInvitationType,
     * PluginCallbackListener)} instead.
     */
    @Deprecated
    public static void sendInvitationWithUIChange(Activity activity, List<ZegoUIKitUser> invitees,
        ZegoInvitationType type, PluginCallbackListener callbackListener) {
        ZegoUIKitPrebuiltCallService.sendInvitationWithUIChange(activity, invitees, type, callbackListener);
    }

    /**
     * use this method to sendInvitation to other users and auto navigate to call waiting page.
     *
     * @deprecated use
     * {@link ZegoUIKitPrebuiltCallService#sendInvitationWithUIChange(Activity, List, ZegoInvitationType, String,
     * String, ZegoSignalingPluginNotificationConfig, PluginCallbackListener)} instead.
     */
    @Deprecated
    public static void sendInvitationWithUIChange(Activity activity, List<ZegoUIKitUser> invitees,
        ZegoInvitationType type, String resourceID, PluginCallbackListener callbackListener) {
        ZegoSignalingPluginNotificationConfig notificationConfig = new ZegoSignalingPluginNotificationConfig();
        notificationConfig.setResourceID(resourceID);
        ZegoUIKitPrebuiltCallService.sendInvitationWithUIChange(activity, invitees, type, resourceID, callbackListener);
    }

    /**
     * use this method to sendInvitation to other users and auto navigate to call waiting page.
     *
     * @deprecated use
     * {@link ZegoUIKitPrebuiltCallService#sendInvitationWithUIChange(Activity, List, ZegoInvitationType,
     * PluginCallbackListener)}  instead.
     */
    @Deprecated
    public static void sendInvitationWithUIChange(Activity activity, List<ZegoUIKitUser> invitees,
        ZegoInvitationType type, String customData, String callID,
        ZegoSignalingPluginNotificationConfig notificationConfig, PluginCallbackListener callbackListener) {
        ZegoUIKitPrebuiltCallService.sendInvitationWithUIChange(activity, invitees, type, customData, callID,
            notificationConfig, callbackListener);
    }
}
