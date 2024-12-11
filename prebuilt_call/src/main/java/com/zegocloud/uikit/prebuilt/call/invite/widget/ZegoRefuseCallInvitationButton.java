package com.zegocloud.uikit.prebuilt.call.invite.widget;

import android.content.Context;
import android.util.AttributeSet;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.zegocloud.uikit.plugin.common.PluginCallbackListener;
import com.zegocloud.uikit.plugin.invitation.components.ZegoRefuseInvitationButton;
import com.zegocloud.uikit.prebuilt.call.core.CallInvitationServiceImpl;
import com.zegocloud.uikit.prebuilt.call.core.invite.ZegoCallInvitationData;
import im.zego.uikit.libuikitreport.ReportUtil;
import java.util.HashMap;
import java.util.Map;

public class ZegoRefuseCallInvitationButton extends ZegoRefuseInvitationButton {

    public ZegoRefuseCallInvitationButton(@NonNull Context context) {
        super(context);
    }

    public ZegoRefuseCallInvitationButton(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void invokedWhenClick() {
        CallInvitationServiceImpl.getInstance().rejectInvitation(new PluginCallbackListener() {
            @Override
            public void callback(Map<String, Object> result) {
                HashMap<String, Object> hashMap = new HashMap<>();
                ZegoCallInvitationData invitationData = CallInvitationServiceImpl.getInstance().getCallInvitationData();
                if (invitationData != null) {
                    hashMap.put("call_id", invitationData.invitationID);
                } else {
                    hashMap.put("call_id", "");
                }
                hashMap.put("app_state", "active");
                hashMap.put("action", "refuse");
                ReportUtil.reportEvent("call/respondInvitation", hashMap);

            }
        });
    }
}
