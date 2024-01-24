package com.zegocloud.uikit.prebuilt.call.invite;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallFragment;
import com.zegocloud.uikit.prebuilt.call.invite.internal.CallInvitationServiceImpl;
import com.zegocloud.uikit.prebuilt.call.invite.internal.IncomingCallButtonListener;
import com.zegocloud.uikit.prebuilt.call.invite.internal.OutgoingCallButtonListener;
import com.zegocloud.uikit.prebuilt.call.invite.internal.ZegoInvitationCallListener;

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
        callFragment.requireActivity().moveTaskToBack(false);
    }
}
