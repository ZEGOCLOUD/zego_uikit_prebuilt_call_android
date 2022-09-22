package com.zegocloud.uikit.prebuilt.call.invite;

import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallConfig;

public interface ZegoUIKitPrebuiltCallConfigProvider {

    ZegoUIKitPrebuiltCallConfig requireConfig(ZegoCallInvitationData invitationData);
}