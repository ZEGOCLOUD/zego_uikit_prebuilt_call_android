package com.zegocloud.uikit.prebuilt.call.config;

/**
 * This property needs to be set when you are building an Android app and when the
 * notifyWhenAppRunningInBackgroundOrQuit is true. notificationConfig.channelID must be the same as the FCM Channel
 * ID in [ZEGOCLOUD Admin Console|_blank]https://console.zegocloud.com), and the notificationConfig.channelName can
 * be an arbitrary value.The notificationConfig.sound must be the same as the FCM sound in Admin Console either.
 */
public class ZegoNotificationConfig {

    public String channelID;
    public String channelName;
    public String channelDesc;
    public String sound;

}
