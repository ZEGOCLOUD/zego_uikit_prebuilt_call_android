package com.zegocloud.uikit.prebuilt.call.invite.widget;

import android.content.Context;
import android.util.AttributeSet;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.zegocloud.uikit.plugin.common.PluginCallbackListener;
import com.zegocloud.uikit.plugin.invitation.components.ZegoAcceptInvitationButton;
import com.zegocloud.uikit.prebuilt.call.core.CallInvitationServiceImpl;
import com.zegocloud.uikit.prebuilt.call.core.invite.ZegoCallInvitationData;
import im.zego.uikit.libuikitreport.ReportUtil;
import java.util.HashMap;
import java.util.Map;

public class ZegoAcceptCallInvitationButton extends ZegoAcceptInvitationButton {

    public ZegoAcceptCallInvitationButton(@NonNull Context context) {
        super(context);
    }

    public ZegoAcceptCallInvitationButton(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void invokedWhenClick() {
        CallInvitationServiceImpl.getInstance().acceptInvitation(new PluginCallbackListener() {
            @Override
            public void callback(Map<String, Object> result) {
                int code = (int) result.get("code");
                String message = (String) result.get("message");
                if (code == 0) {
                    HashMap<String, Object> hashMap = new HashMap<>();
                    ZegoCallInvitationData invitationData = CallInvitationServiceImpl.getInstance()
                        .getCallInvitationData();
                    if (invitationData != null) {
                        hashMap.put("call_id", invitationData.invitationID);
                    } else {
                        hashMap.put("call_id", "");
                    }
                    hashMap.put("app_state", "active");
                    hashMap.put("action", "accept");
                    ReportUtil.reportEvent("call/respondInvitation", hashMap);
                }

            }
        });
    }
}
