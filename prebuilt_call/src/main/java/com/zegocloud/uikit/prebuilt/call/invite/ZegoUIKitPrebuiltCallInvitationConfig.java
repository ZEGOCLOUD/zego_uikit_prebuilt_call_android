package com.zegocloud.uikit.prebuilt.call.invite;

import android.graphics.drawable.Drawable;
import com.zegocloud.uikit.plugin.invitation.ZegoInvitationType;
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallConfig;
import com.zegocloud.uikit.prebuilt.call.config.ZegoNotificationConfig;
import com.zegocloud.uikit.prebuilt.call.invite.internal.ZegoCallInvitationData;
import com.zegocloud.uikit.prebuilt.call.invite.internal.ZegoInnerText;
import com.zegocloud.uikit.prebuilt.call.invite.internal.ZegoTranslationText;
import com.zegocloud.uikit.prebuilt.call.invite.internal.ZegoUIKitPrebuiltCallConfigProvider;

public class ZegoUIKitPrebuiltCallInvitationConfig {

    public String incomingCallRingtone;
    public String outgoingCallRingtone;
    public ZegoUIKitPrebuiltCallConfigProvider provider;
    public Drawable incomingCallBackground;
    public Drawable outgoingCallBackground;

    //Indicates if the reject button is displayed. Default is true
    public boolean showDeclineButton = true;

    public ZegoNotificationConfig notificationConfig;

    public ZegoInnerText innerText = new ZegoInnerText();
    public ZegoTranslationText translationText = new ZegoTranslationText();

    public static ZegoUIKitPrebuiltCallConfig generateDefaultConfig(ZegoCallInvitationData invitationData) {
        ZegoUIKitPrebuiltCallConfig config = null;
        boolean isVideoCall = invitationData.type == ZegoInvitationType.VIDEO_CALL.getValue();
        boolean isGroupCall = invitationData.invitees.size() > 1;
        if (isVideoCall && isGroupCall) {
            config = ZegoUIKitPrebuiltCallConfig.groupVideoCall();
        } else if (!isVideoCall && isGroupCall) {
            config = ZegoUIKitPrebuiltCallConfig.groupVoiceCall();
        } else if (!isVideoCall) {
            config = ZegoUIKitPrebuiltCallConfig.oneOnOneVoiceCall();
        } else {
            config = ZegoUIKitPrebuiltCallConfig.oneOnOneVideoCall();
        }
        return config;
    }
}
