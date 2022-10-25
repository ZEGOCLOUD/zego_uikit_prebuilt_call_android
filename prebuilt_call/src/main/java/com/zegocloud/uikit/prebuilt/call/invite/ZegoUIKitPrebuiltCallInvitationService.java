package com.zegocloud.uikit.prebuilt.call.invite;

import android.app.Application;
import android.text.TextUtils;
import com.zegocloud.uikit.prebuilt.call.invite.internal.CallInvitationServiceImpl;
import com.zegocloud.uikit.prebuilt.call.invite.internal.RingtoneManager;

public class ZegoUIKitPrebuiltCallInvitationService {

    public static void init(Application application, long appID, String appSign, String userID, String userName,
        ZegoUIKitPrebuiltCallInvitationConfig config) {
        initRingtoneManager(application, config);
        CallInvitationServiceImpl.getInstance().setPrebuiltConfigProvider(config.provider);
        CallInvitationServiceImpl.getInstance().init(application, appID, appSign, userID, userName, config);
    }

    public static void unInit() {
        CallInvitationServiceImpl.getInstance().unInit();
    }

    private static void initRingtoneManager(Application application, ZegoUIKitPrebuiltCallInvitationConfig config) {
        RingtoneManager.init(application);
        String outgoing;
        if (TextUtils.isEmpty(config.outgoingCallRingtone)) {
            outgoing = "zego_outgoing";
        } else {
            outgoing = config.outgoingCallRingtone;
        }
        RingtoneManager.setOutgoingUri(RingtoneManager.getUriFromRaw(application, outgoing));
        String incoming;
        if (TextUtils.isEmpty(config.incomingCallRingtone)) {
            incoming = "zego_incoming";
        } else {
            incoming = config.incomingCallRingtone;
        }
        RingtoneManager.setIncomingUri(RingtoneManager.getUriFromRaw(application, incoming));
    }
}
