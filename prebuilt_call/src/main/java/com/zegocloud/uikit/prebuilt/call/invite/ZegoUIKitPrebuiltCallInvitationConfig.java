package com.zegocloud.uikit.prebuilt.call.invite;

import android.graphics.drawable.Drawable;
import com.zegocloud.uikit.prebuilt.call.config.ZegoNotificationConfig;
import com.zegocloud.uikit.prebuilt.call.invite.internal.ZegoInnerText;

public class ZegoUIKitPrebuiltCallInvitationConfig {

    public String incomingCallRingtone;
    public String outgoingCallRingtone;
    public ZegoUIKitPrebuiltCallConfigProvider provider;
    public Drawable incomingCallBackground;
    public Drawable outgoingCallBackground;

    //Indicates if the reject button is displayed. Default is true
    public boolean showDeclineButton = true;
    /**
     * This property needs to be set when you are building an Android app and when the notifyWhenAppRunningInBackgroundOrQuit is true.
     * notificationConfig.channelID must be the same as the FCM Channel ID in [ZEGOCLOUD Admin Console|_blank]https://console.zegocloud.com),
     * and the notificationConfig.channelName can be an arbitrary value.The notificationConfig.sound must be the same as the FCM sound in Admin Console either.
     */
    public ZegoNotificationConfig notificationConfig;
    public ZegoInnerText innerText = new ZegoInnerText();
}
