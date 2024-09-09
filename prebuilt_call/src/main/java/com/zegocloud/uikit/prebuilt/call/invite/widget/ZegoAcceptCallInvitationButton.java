package com.zegocloud.uikit.prebuilt.call.invite.widget;

import android.content.Context;
import android.util.AttributeSet;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.zegocloud.uikit.plugin.invitation.components.ZegoAcceptInvitationButton;
import com.zegocloud.uikit.prebuilt.call.core.CallInvitationServiceImpl;

public class ZegoAcceptCallInvitationButton extends ZegoAcceptInvitationButton {

    public ZegoAcceptCallInvitationButton(@NonNull Context context) {
        super(context);
    }

    public ZegoAcceptCallInvitationButton(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void invokedWhenClick() {
        CallInvitationServiceImpl.getInstance().acceptInvitation(null);
    }
}
