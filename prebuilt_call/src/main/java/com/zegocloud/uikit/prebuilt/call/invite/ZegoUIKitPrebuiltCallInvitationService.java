package com.zegocloud.uikit.prebuilt.call.invite;

import android.app.Application;
import android.os.Handler;
import android.text.TextUtils;

import com.zegocloud.uikit.ZegoUIKit;
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallFragment;
import com.zegocloud.uikit.prebuilt.call.invite.internal.CallInvitationServiceImpl;
import com.zegocloud.uikit.prebuilt.call.invite.internal.IncomingCallButtonListener;
import com.zegocloud.uikit.prebuilt.call.invite.internal.OutgoingCallButtonListener;
import com.zegocloud.uikit.prebuilt.call.invite.internal.RingtoneManager;
import com.zegocloud.uikit.prebuilt.call.invite.internal.ZegoInvitationCallListener;

public class ZegoUIKitPrebuiltCallInvitationService {

    private static boolean alreadyInit = false;

    public static void init(Application application, long appID, String appSign, String userID, String userName,
        ZegoUIKitPrebuiltCallInvitationConfig config) {
        if (alreadyInit) {
            return;
        }
        alreadyInit = true;
        CallInvitationServiceImpl.getInstance().init(application, appID, appSign, userID, userName, config);
    }

    public static void init(Application application, long appID, String appSign, String userID, String userName) {
        init(application, appID, appSign, userID, userName, null);
    }

    public static void unInit() {
        alreadyInit = false;
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
}
