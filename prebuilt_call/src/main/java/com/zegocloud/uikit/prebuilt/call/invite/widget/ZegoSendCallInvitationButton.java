package com.zegocloud.uikit.prebuilt.call.invite.widget;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.zegocloud.uikit.ZegoUIKit;
import com.zegocloud.uikit.plugin.adapter.plugins.signaling.ZegoSignalingPluginNotificationConfig;
import com.zegocloud.uikit.plugin.common.PluginCallbackListener;
import com.zegocloud.uikit.plugin.invitation.ZegoInvitationType;
import com.zegocloud.uikit.plugin.invitation.components.ZegoStartInvitationButton;
import com.zegocloud.uikit.prebuilt.call.core.CallInvitationServiceImpl;
import com.zegocloud.uikit.prebuilt.call.invite.internal.CallInviteActivity;
import com.zegocloud.uikit.prebuilt.call.invite.internal.ClickListener;
import com.zegocloud.uikit.prebuilt.call.invite.internal.ZegoCallUser;
import com.zegocloud.uikit.prebuilt.call.invite.internal.ZegoTranslationText;
import com.zegocloud.uikit.service.defines.ZegoUIKitUser;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ZegoSendCallInvitationButton extends ZegoStartInvitationButton {

    private boolean isVideoCall = false;
    private String customData = "";

    /**
     * resourceID can be used to specify the ringtone of an offline call invitation, which must be set to the same value
     * as the Push Resource ID in ZEGOCLOUD Admin Console. This only takes effect when the
     * notifyWhenAppRunningInBackgroundOrQuit is true.
     */
    private String resourceID = "";
    private ClickListener sendInvitationListener;
    private boolean showErrorToast = true;

    public ZegoSendCallInvitationButton(@NonNull Context context) {
        super(context);
    }

    public ZegoSendCallInvitationButton(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void initView() {
        super.initView();
    }

    public String generateCallID() {
        String callID = null;
        String userID = ZegoUIKit.getLocalUser().userID;
        if (userID != null) {
            callID = "call_" + userID + "_" + System.currentTimeMillis();
        }
        return callID;
    }

    public void setIsVideoCall(boolean isVideoCall) {
        this.isVideoCall = isVideoCall;
        if (isVideoCall) {
            setType(ZegoInvitationType.VIDEO_CALL);
        } else {
            setType(ZegoInvitationType.VOICE_CALL);
        }
    }

    @Override
    public void setType(int type) {
        throw new UnsupportedOperationException("unSupport operation");
    }

    public void setCustomData(String customData) {
        this.customData = customData;
    }

    @Override
    protected void invokedWhenClick() {
        ZegoTranslationText translationText = CallInvitationServiceImpl.getInstance()
            .getCallInvitationConfig().translationText;
        ZegoInvitationType invitationType = ZegoInvitationType.getZegoInvitationType(type);
        CallInvitationServiceImpl.getInstance().sendInvitation(invitees, invitationType, customData, timeout, this.callID, getSendInvitationConfig(),
                new PluginCallbackListener() {
                    @Override
                    public void callback(Map<String, Object> result) {
                        ZegoUIKitUser uiKitUser = ZegoUIKit.getLocalUser();
                        int code = (int) result.get("code");
                        String message = (String) result.get("message");
                        List<ZegoUIKitUser> errorInvitees = (List<ZegoUIKitUser>) result.get("errorInvitees");
                        if (code == 0) {
                            if (errorInvitees.isEmpty() || errorInvitees.size() != invitees.size()) {
                                CallInviteActivity.startOutgoingPage(getContext());
                            }
                            if (!errorInvitees.isEmpty()) {
                                String error = "";
                                if (translationText != null) {
                                    error = translationText.sendCallButtonErrorOffLine;
                                }
                                StringBuilder sb = new StringBuilder(error);
                                int count = 0;
                                for (ZegoUIKitUser errorInvitee : errorInvitees) {
                                    sb.append(errorInvitee.userID);
                                    sb.append(" ");
                                    count += 1;
                                    if (count == 5) {
                                        sb.append("...");
                                        break;
                                    }
                                }
                                showError(-5, sb.toString());
                            }
                        } else {
                            String error = "";
                            if (translationText != null) {
                                error = translationText.sendCallButtonError;
                            }
                            showError(code, String.format(error, code, message));
                        }
                        if (sendInvitationListener != null) {
                            List<ZegoCallUser> callbackErrorInvitees = new ArrayList<>();
                            if (errorInvitees != null && errorInvitees.size() > 0) {
                                for (ZegoUIKitUser errorInvitee : errorInvitees) {
                                    ZegoCallUser zegoCallUser = new ZegoCallUser(errorInvitee.userID,
                                        errorInvitee.userName);
                                    callbackErrorInvitees.add(zegoCallUser);
                                }
                            }
                            sendInvitationListener.onClick(code, message, callbackErrorInvitees);
                        }
                    }
                });
    }

    public void setOnClickListener(ClickListener listener) {
        this.sendInvitationListener = listener;
    }

    public String getResourceID() {
        return resourceID;
    }

    public void setResourceID(String resourceID) {
        this.resourceID = resourceID;
        CallInvitationServiceImpl.getInstance().setCallResourceID(resourceID);
    }

    private ZegoSignalingPluginNotificationConfig getSendInvitationConfig() {

        String offlineResourceID;
        String offlineMessage = CallInvitationServiceImpl.getInstance()
            .getCallNotificationMessage(isVideoCall, invitees.size() > 1);

        ZegoUIKitUser uiKitUser = ZegoUIKit.getLocalUser();
        String offlineTitle = CallInvitationServiceImpl.getInstance()
            .getCallNotificationTitle(isVideoCall, invitees.size() > 1, uiKitUser.userName);

        if (TextUtils.isEmpty(resourceID)) {
            offlineResourceID = "zegouikit_call";
        } else {
            offlineResourceID = resourceID;
        }

        ZegoSignalingPluginNotificationConfig notificationConfig = new ZegoSignalingPluginNotificationConfig();
        notificationConfig.setResourceID(offlineResourceID);
        notificationConfig.setTitle(offlineTitle);
        notificationConfig.setMessage(offlineMessage);
        return notificationConfig;
    }

    protected void showError(int errorCode, String errorMessage) {
        if (showErrorToast) {
            Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
        }
    }

    public void showErrorToast(boolean showErrorToast) {
        this.showErrorToast = showErrorToast;
    }
}
