package com.zegocloud.uikit.prebuilt.call.invite.widget;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.zegocloud.uikit.plugin.adapter.plugins.signaling.ZegoSignalingPluginNotificationConfig;
import com.zegocloud.uikit.plugin.invitation.components.ZegoCancelInvitationButton;
import com.zegocloud.uikit.prebuilt.call.invite.internal.CallInvitationServiceImpl;

public class ZegoCancelCallInvitationButton extends ZegoCancelInvitationButton {

    private String resourceID;

    public ZegoCancelCallInvitationButton(@NonNull Context context) {
        super(context);
    }

    public ZegoCancelCallInvitationButton(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void invokedWhenClick() {
        String offlineResourceID;

        String callResourceID = CallInvitationServiceImpl.getInstance().getCallResourceID();
        if (TextUtils.isEmpty(callResourceID)) {
            offlineResourceID = "zegouikit_call";
        } else {
            offlineResourceID = resourceID;
        }

        ZegoSignalingPluginNotificationConfig notificationConfig = new ZegoSignalingPluginNotificationConfig();
        notificationConfig.setResourceID(resourceID);

        CallInvitationServiceImpl.getInstance().cancelInvitation(invitees, "", notificationConfig, null);
    }
}
