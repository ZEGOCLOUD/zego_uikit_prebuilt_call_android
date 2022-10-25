package com.zegocloud.uikit.prebuilt.call.invite.widget;

import android.content.Context;
import android.util.AttributeSet;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.zegocloud.uikit.plugin.invitation.components.ZegoRefuseInvitationButton;
import com.zegocloud.uikit.prebuilt.call.invite.internal.CallInvitationServiceImpl;
import org.json.JSONException;
import org.json.JSONObject;

public class ZegoRefuseCallInvitationButton extends ZegoRefuseInvitationButton {

    public ZegoRefuseCallInvitationButton(@NonNull Context context) {
        super(context);
    }

    public ZegoRefuseCallInvitationButton(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void invokedWhenClick() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("reason", "decline");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        CallInvitationServiceImpl.getInstance().refuseInvitation(inviterID, jsonObject.toString(), null);
    }
}
