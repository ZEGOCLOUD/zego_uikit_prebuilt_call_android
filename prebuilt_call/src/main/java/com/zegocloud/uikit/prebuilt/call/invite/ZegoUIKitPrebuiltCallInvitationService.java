package com.zegocloud.uikit.prebuilt.call.invite;

import android.app.Application;
import com.zegocloud.uikit.plugin.adapter.plugins.signaling.ZegoSignalingPluginNotificationConfig;
import com.zegocloud.uikit.plugin.common.PluginCallbackListener;
import com.zegocloud.uikit.plugin.invitation.ZegoInvitationType;
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallFragment;
import com.zegocloud.uikit.prebuilt.call.invite.internal.CallInvitationServiceImpl;
import com.zegocloud.uikit.prebuilt.call.invite.internal.IncomingCallButtonListener;
import com.zegocloud.uikit.prebuilt.call.invite.internal.OutgoingCallButtonListener;
import com.zegocloud.uikit.prebuilt.call.invite.internal.ZegoInvitationCallListener;
import com.zegocloud.uikit.service.defines.ZegoUIKitUser;
import java.util.List;

public class ZegoUIKitPrebuiltCallInvitationService {

    public static Events events = new Events();

    public static void init(Application application, long appID, String appSign, String userID, String userName,
        ZegoUIKitPrebuiltCallInvitationConfig config) {
        CallInvitationServiceImpl.getInstance().initAndLoginUser(application, appID, appSign, userID, userName);
        CallInvitationServiceImpl.getInstance().setCallInvitationConfig(config);
    }

    public static void unInit() {
        CallInvitationServiceImpl.getInstance().unInit();
    }

    public static void addIncomingCallButtonListener(IncomingCallButtonListener listener) {
        CallInvitationServiceImpl.getInstance().addIncomingCallButtonListener(listener);
    }

    public static void addOutgoingCallButtonListener(OutgoingCallButtonListener listener) {
        CallInvitationServiceImpl.getInstance().addOutgoingCallButtonListener(listener);
    }

    public static void addInvitationCallListener(ZegoInvitationCallListener listener) {
        CallInvitationServiceImpl.getInstance().addInvitationCallListener(listener);
    }

    public static void removeInvitationCallListener() {
        CallInvitationServiceImpl.getInstance().removeInvitationCallListener();
    }

    public static ZegoUIKitPrebuiltCallFragment getPrebuiltCallFragment() {
        return CallInvitationServiceImpl.getInstance().getZegoUIKitPrebuiltCallFragment();
    }

    public static void endCall() {
        ZegoUIKitPrebuiltCallFragment prebuiltCallFragment = getPrebuiltCallFragment();
        if (prebuiltCallFragment != null) {
            prebuiltCallFragment.requireActivity().finish();
        }
    }

    public static void minimizeCall() {
        ZegoUIKitPrebuiltCallFragment callFragment = ZegoUIKitPrebuiltCallInvitationService.getPrebuiltCallFragment();
        if (callFragment != null) {
            callFragment.minimizeCall();
        }
    }

    public static void sendInvitation(List<ZegoUIKitUser> invitees, ZegoInvitationType type, String customData,
        PluginCallbackListener callbackListener) {
        CallInvitationServiceImpl.getInstance()
            .sendInvitation(invitees, type, customData, 60, null, null, callbackListener);
    }

    public static void sendInvitation(List<ZegoUIKitUser> invitees, ZegoInvitationType type, String customData,
        int timeout, PluginCallbackListener callbackListener) {
        CallInvitationServiceImpl.getInstance()
            .sendInvitation(invitees, type, customData, timeout, null, null, callbackListener);
    }

    public static void sendInvitation(List<ZegoUIKitUser> invitees, ZegoInvitationType type, String customData,
        int timeout, String callID, PluginCallbackListener callbackListener) {
        CallInvitationServiceImpl.getInstance()
            .sendInvitation(invitees, type, customData, timeout, callID, null, callbackListener);
    }

    public static void sendInvitation(List<ZegoUIKitUser> invitees, ZegoInvitationType type, String customData,
        int timeout, String callID, String resourceID, PluginCallbackListener callbackListener) {
        ZegoSignalingPluginNotificationConfig pluginNotificationConfig = new ZegoSignalingPluginNotificationConfig();
        pluginNotificationConfig.setResourceID(resourceID);

        CallInvitationServiceImpl.getInstance()
            .sendInvitation(invitees, type, customData, timeout, callID, pluginNotificationConfig, callbackListener);
    }

    public static void sendInvitation(List<ZegoUIKitUser> invitees, ZegoInvitationType type, String customData,
        int timeout, String callID, ZegoSignalingPluginNotificationConfig pluginNotificationConfig,
        PluginCallbackListener callbackListener) {
        CallInvitationServiceImpl.getInstance()
            .sendInvitation(invitees, type, customData, timeout, callID, pluginNotificationConfig, callbackListener);
    }

    public static void acceptInvitation(PluginCallbackListener pluginCallbackListener) {
        CallInvitationServiceImpl.getInstance().acceptInvitation(pluginCallbackListener);
    }

    public static void acceptInvitation(String data, PluginCallbackListener callbackListener) {
        CallInvitationServiceImpl.getInstance().acceptInvitation(data, callbackListener);
    }

    public static void acceptInvitation(String invitationID, String data, PluginCallbackListener callbackListener) {
        CallInvitationServiceImpl.getInstance().acceptInvitation(invitationID, data, callbackListener);
    }

    public static void rejectInvitation(PluginCallbackListener callbackListener) {
        CallInvitationServiceImpl.getInstance().rejectInvitation(callbackListener);
    }

    public static void rejectInvitation(String data, PluginCallbackListener callbackListener) {
        CallInvitationServiceImpl.getInstance().rejectInvitation(data, callbackListener);
    }

    public static void rejectInvitation(String invitationID, String data, PluginCallbackListener callbackListener) {
        CallInvitationServiceImpl.getInstance().rejectInvitation(invitationID, data, callbackListener);
    }

    public static void cancelInvitation(PluginCallbackListener callbackListener) {
        CallInvitationServiceImpl.getInstance().cancelInvitation(callbackListener);
    }

    public static void cancelInvitation(String data, PluginCallbackListener callbackListener) {
        CallInvitationServiceImpl.getInstance().cancelInvitation(data, callbackListener);
    }

    public static void cancelInvitation(List<String> invitees, PluginCallbackListener callbackListener) {
        CallInvitationServiceImpl.getInstance().cancelInvitation(invitees, callbackListener);
    }

    public static void cancelInvitation(List<String> invitees, String data, PluginCallbackListener callbackListener) {
        CallInvitationServiceImpl.getInstance().cancelInvitation(invitees, data, callbackListener);
    }

    public static void cancelInvitation(List<String> invitees, String invitationID, String data,
        ZegoSignalingPluginNotificationConfig pushConfig, PluginCallbackListener callbackListener) {
        CallInvitationServiceImpl.getInstance()
            .cancelInvitation(invitees, invitationID, data, pushConfig, callbackListener);
    }
}
