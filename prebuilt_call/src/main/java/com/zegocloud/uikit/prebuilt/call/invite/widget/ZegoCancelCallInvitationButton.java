package com.zegocloud.uikit.prebuilt.call.invite.widget;

import android.content.Context;
import android.util.AttributeSet;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.zegocloud.uikit.plugin.invitation.components.ZegoCancelInvitationButton;
import com.zegocloud.uikit.prebuilt.call.invite.internal.CallInvitationServiceImpl;

public class ZegoCancelCallInvitationButton extends ZegoCancelInvitationButton {

    public ZegoCancelCallInvitationButton(@NonNull Context context) {
        super(context);
    }

    public ZegoCancelCallInvitationButton(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void invokedWhenClick() {
        CallInvitationServiceImpl.getInstance().cancelInvitation(invitees, "",null);
    }
}
