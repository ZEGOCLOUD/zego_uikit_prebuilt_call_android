package com.zegocloud.uikit.prebuilt.call.invite.internal;

import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallConfig;
import com.zegocloud.uikit.prebuilt.call.invite.internal.ZegoCallInvitationData;

public interface ZegoUIKitPrebuiltCallConfigProvider {

    ZegoUIKitPrebuiltCallConfig requireConfig(ZegoCallInvitationData invitationData);
}