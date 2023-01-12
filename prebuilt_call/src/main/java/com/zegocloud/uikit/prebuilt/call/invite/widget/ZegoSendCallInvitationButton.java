package com.zegocloud.uikit.prebuilt.call.invite.widget;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.zegocloud.uikit.ZegoUIKit;
import com.zegocloud.uikit.plugin.invitation.ZegoInvitationType;
import com.zegocloud.uikit.plugin.invitation.components.ZegoStartInvitationButton;
import com.zegocloud.uikit.prebuilt.call.R;
import com.zegocloud.uikit.prebuilt.call.invite.internal.CallInvitationServiceImpl;
import com.zegocloud.uikit.prebuilt.call.invite.internal.CallInviteActivity;
import com.zegocloud.uikit.prebuilt.call.invite.internal.ClickListener;
import com.zegocloud.uikit.prebuilt.call.invite.internal.ZegoCallUser;
import com.zegocloud.uikit.service.defines.ZegoSignalingPluginNotificationConfig;
import com.zegocloud.uikit.service.defines.ZegoUIKitUser;
import com.zegocloud.uikit.utils.GenericUtils;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ZegoSendCallInvitationButton extends ZegoStartInvitationButton {

    private boolean isVideoCall = false;
    private String customData = "";

    /**
     * resourceID can be used to specify the ringtone of an offline call invitation,
     * which must be set to the same value as the Push Resource ID in ZEGOCLOUD Admin Console.
     * This only takes effect when the notifyWhenAppRunningInBackgroundOrQuit is true.
     */
    private String resourceID = "";

    private ClickListener sendInvitationListener;

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
        if (invitees.isEmpty()) {
            return;
        }
        JSONObject jsonObject = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        String callID = generateCallID();

        try {
            jsonObject.put("call_id", callID);
            for (ZegoUIKitUser invitee : invitees) {
                JSONObject tmp = new JSONObject();
                tmp.put("user_id", invitee.userID);
                tmp.put("user_name", invitee.userName);
                jsonArray.put(tmp);
            }
            jsonObject.put("invitees", jsonArray);
            jsonObject.put("custom_data", customData);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ZegoSignalingPluginNotificationConfig notificationConfig = getSendInvitationConfig();
        data = jsonObject.toString();
        List<String> idList = GenericUtils.map(invitees, uiKitUser -> uiKitUser.userID);
        CallInvitationServiceImpl.getInstance().sendInvitation(callID,idList, timeout, type, data, notificationConfig, result -> {
            int code = (int) result.get("code");
            String message = (String) result.get("message");
            List<ZegoUIKitUser> errorInvitees = (List<ZegoUIKitUser>) result.get("errorInvitees");
            if (code == 0) {
                if (errorInvitees.isEmpty() || errorInvitees.size() != invitees.size()) {
                    ZegoUIKitUser uiKitUser = ZegoUIKit.getLocalUser();
                    if (uiKitUser != null) {
                        CallInviteActivity.startOutgoingPage(getContext(), uiKitUser, invitees, callID, type, timeout);
                    } else {
                        Toast.makeText(getContext(), "please call ZegoUIKit.login(String,String) first",
                                Toast.LENGTH_SHORT).show();
                    }
                }
                if (!errorInvitees.isEmpty()) {
                    StringBuilder sb = new StringBuilder(getContext().getString(R.string.invite_error_offline));
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
                    Toast.makeText(getContext(), sb.toString(), Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getContext(), getContext().getString(R.string.invite_error, code, message),
                        Toast.LENGTH_SHORT).show();
            }

            if (sendInvitationListener != null) {
                List<ZegoCallUser> callbackErrorInvitees = new ArrayList<>();
                if (errorInvitees != null && errorInvitees.size() > 0) {
                    for (ZegoUIKitUser errorInvitee : errorInvitees) {
                        ZegoCallUser zegoCallUser = new ZegoCallUser(errorInvitee.userID, errorInvitee.userName);
                        callbackErrorInvitees.add(zegoCallUser);
                    }
                }
                sendInvitationListener.onClick(code, message, callbackErrorInvitees);
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
    }

    private ZegoSignalingPluginNotificationConfig getSendInvitationConfig() {

        String offlineResourceID;
        String offlineMessage = CallInvitationServiceImpl.getInstance().getNotificationMessage(isVideoCall, invitees.size() > 1);

        ZegoUIKitUser uiKitUser = ZegoUIKit.getLocalUser();
        String offlineTitle = CallInvitationServiceImpl.getInstance().getNotificationTitle(isVideoCall, invitees.size() > 1, uiKitUser.userName);

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


}