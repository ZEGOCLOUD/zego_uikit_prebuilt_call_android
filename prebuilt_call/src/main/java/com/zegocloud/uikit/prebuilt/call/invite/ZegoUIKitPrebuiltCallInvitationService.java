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

    public static void init(Application application, long appID, String appSign, String userID, String userName,
        ZegoUIKitPrebuiltCallInvitationConfig config) {
        initRingtoneManager(application, config);
        CallInvitationServiceImpl.getInstance().setPrebuiltConfigProvider(config.provider);
        CallInvitationServiceImpl.getInstance().init(application, appID, appSign, userID, userName, config);

        if (config.notifyWhenAppRunningInBackgroundOrQuit) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    ZegoUIKit.getSignalingPlugin().enableNotifyWhenAppRunningInBackgroundOrQuit(true);
                    RingtoneManager.setIncomingOfflineRing();
                }
            }, 500);
        }
    }

    public static void init(Application application, long appID, String appSign, String userID, String userName) {
        initRingtoneManager(application, null);
        CallInvitationServiceImpl.getInstance().init(application, appID, appSign, userID, userName, null);
    }

    public static void unInit() {
        CallInvitationServiceImpl.getInstance().unInit();
    }

    private static void initRingtoneManager(Application application, ZegoUIKitPrebuiltCallInvitationConfig config) {
        RingtoneManager.init(application);
        String outgoing;
        if (config == null || TextUtils.isEmpty(config.outgoingCallRingtone)) {
            outgoing = "zego_outgoing";
        } else {
            outgoing = config.outgoingCallRingtone;
        }
        RingtoneManager.setOutgoingUri(RingtoneManager.getUriFromRaw(application, outgoing));
        String incoming;
        if (config == null || TextUtils.isEmpty(config.incomingCallRingtone)) {
            incoming = "zego_incoming";
        } else {
            incoming = config.incomingCallRingtone;
        }
        RingtoneManager.setIncomingUri(RingtoneManager.getUriFromRaw(application, incoming));
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
