package com.zegocloud.uikit.prebuilt.call.invite;

import com.zegocloud.uikit.plugin.common.IZegoUIKitPlugin;
import java.util.Collections;
import java.util.List;

public class ZegoUIKitPrebuiltCallInvitationConfig {

    public String incomingCallRingtone;
    public String outgoingCallRingtone;
    public List<IZegoUIKitPlugin> plugins;
    public ZegoUIKitPrebuiltCallConfigProvider provider;

    public ZegoUIKitPrebuiltCallInvitationConfig(IZegoUIKitPlugin plugin) {
        this.plugins = Collections.singletonList(plugin);
    }

    public ZegoUIKitPrebuiltCallInvitationConfig(List<IZegoUIKitPlugin> plugins) {
        this.plugins = plugins;
    }

    public ZegoUIKitPrebuiltCallInvitationConfig(List<IZegoUIKitPlugin> plugins,
        ZegoUIKitPrebuiltCallConfigProvider provider) {
        this.plugins = plugins;
        this.provider = provider;
    }
}
