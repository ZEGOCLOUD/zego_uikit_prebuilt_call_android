package com.zegocloud.uikit.prebuilt.call.invite.widget;

import android.content.Context;
import android.util.AttributeSet;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.zegocloud.uikit.plugin.invitation.components.ZegoCancelInvitationButton;
import com.zegocloud.uikit.prebuilt.call.invite.ZegoUIKitPrebuiltCallInvitationService;

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
        ZegoUIKitPrebuiltCallInvitationService.cancelInvitation(null);
    }
}
