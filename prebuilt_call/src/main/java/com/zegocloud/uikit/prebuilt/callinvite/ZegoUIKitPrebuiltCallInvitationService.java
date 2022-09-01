package com.zegocloud.uikit.prebuilt.callinvite;

import android.app.Application;
import com.zegocloud.uikit.prebuilt.callinvite.internal.InvitationServiceImpl;

public class ZegoUIKitPrebuiltCallInvitationService {

    public static void init(Application application, long appID, String appSign, String userID, String userName) {
        InvitationServiceImpl.getInstance().init(application, appID, appSign, userID, userName);
    }

    public static void logout() {
        InvitationServiceImpl.getInstance().unInit();
    }

    public static void setPrebuiltCallConfigProvider(ZegoUIKitPrebuiltCallConfigProvider provider) {
        InvitationServiceImpl.getInstance().setPrebuiltCallConfigProvider(provider);
    }
}
