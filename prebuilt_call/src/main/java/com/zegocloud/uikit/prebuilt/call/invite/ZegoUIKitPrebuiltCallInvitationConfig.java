package com.zegocloud.uikit.prebuilt.call.invite;

import com.zegocloud.uikit.plugin.common.IZegoUIKitPlugin;
import com.zegocloud.uikit.prebuilt.call.config.ZegoNotificationConfig;
import com.zegocloud.uikit.prebuilt.call.invite.internal.ZegoInnerText;

import java.util.Collections;
import java.util.List;

public class ZegoUIKitPrebuiltCallInvitationConfig {

    public String incomingCallRingtone;
    public String outgoingCallRingtone;
    public List<IZegoUIKitPlugin> plugins;
    public ZegoUIKitPrebuiltCallConfigProvider provider;

    //Indicates if the reject button is displayed. Default is true
    public boolean showDeclineButton = true;
    public boolean notifyWhenAppRunningInBackgroundOrQuit = true;

    /**
     * This property needs to be set when you are building an Android app and when the notifyWhenAppRunningInBackgroundOrQuit is true.
     * notificationConfig.channelID must be the same as the FCM Channel ID in [ZEGOCLOUD Admin Console|_blank]https://console.zegocloud.com),
     * and the notificationConfig.channelName can be an arbitrary value.The notificationConfig.sound must be the same as the FCM sound in Admin Console either.
     */
    public ZegoNotificationConfig notificationConfig;

    public ZegoInnerText innerText = new ZegoInnerText();

    public ZegoUIKitPrebuiltCallInvitationConfig(IZegoUIKitPlugin plugin) {
        this.plugins = Collections.singletonList(plugin);
    }

    public ZegoUIKitPrebuiltCallInvitationConfig(IZegoUIKitPlugin plugin, ZegoUIKitPrebuiltCallConfigProvider provider) {
        this.plugins = Collections.singletonList(plugin);
        this.provider = provider;
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
