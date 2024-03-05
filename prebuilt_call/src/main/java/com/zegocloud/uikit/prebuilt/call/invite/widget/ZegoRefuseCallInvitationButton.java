package com.zegocloud.uikit.prebuilt.call.invite.widget;

import android.content.Context;
import android.util.AttributeSet;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.zegocloud.uikit.plugin.invitation.components.ZegoRefuseInvitationButton;
import com.zegocloud.uikit.prebuilt.call.invite.ZegoUIKitPrebuiltCallInvitationService;

public class ZegoRefuseCallInvitationButton extends ZegoRefuseInvitationButton {

    public ZegoRefuseCallInvitationButton(@NonNull Context context) {
        super(context);
    }

    public ZegoRefuseCallInvitationButton(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void invokedWhenClick() {
        ZegoUIKitPrebuiltCallInvitationService.rejectInvitation(null);
    }
}
