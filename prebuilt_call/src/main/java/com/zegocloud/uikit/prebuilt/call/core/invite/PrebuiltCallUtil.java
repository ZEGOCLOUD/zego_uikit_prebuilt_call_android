package com.zegocloud.uikit.prebuilt.call.core.invite;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import com.google.gson.Gson;
import com.zegocloud.uikit.ZegoUIKit;
import com.zegocloud.uikit.prebuilt.call.core.push.ZIMPushMessage;
import com.zegocloud.uikit.service.defines.ZegoUIKitUser;
import java.util.List;
import java.util.stream.Collectors;
import org.json.JSONException;
import org.json.JSONObject;

public class PrebuiltCallUtil {

    public static String generatePrebuiltCallRoomID() {
        String callID = null;
        String userID;
        if (ZegoUIKit.getLocalUser() == null) {
            userID = "";
        } else {
            userID = ZegoUIKit.getLocalUser().userID;
        }
        if (userID != null) {
            callID = "call_" + userID + "_" + System.currentTimeMillis();
        }
        return callID;
    }

    public static String getAutoRejectJsonString(String zimCallID) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("reason", "busy");
            jsonObject.put("invitationID", zimCallID);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }

    public static String getManualRejectJsonString(String zimCallID) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("reason", "decline");
            jsonObject.put("invitationID", zimCallID);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }

    public static boolean isAppBackground(Context context) {
        if (context == null) {
            return true;
        }
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.processName.equals(context.getPackageName())) {
                if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    return false;
                } else {
                    return true;
                }
            }
        }
        return false;
    }

    public static ZegoCallInvitationData parsePushMessage(Gson gson, ZIMPushMessage zimPushMessage) {
        PrebuiltCallInviteExtendedData extendedData = gson.fromJson(zimPushMessage.payLoad,
            PrebuiltCallInviteExtendedData.class);
        PrebuiltCallInviteExtendedData.Data data = gson.fromJson(extendedData.getData(),
            PrebuiltCallInviteExtendedData.Data.class);
        ZegoCallInvitationData invitationData = new ZegoCallInvitationData();
        invitationData.invitationID = zimPushMessage.invitationID;
        invitationData.callID = data.getCallId();
        invitationData.customData = data.getCustomData();
        invitationData.invitees = data.getInvitees().stream()
            .map(invitees -> new ZegoUIKitUser(invitees.getUserId(), invitees.getUserName()))
            .collect(Collectors.toList());
        invitationData.type = extendedData.getType();
        invitationData.inviter = new ZegoUIKitUser(extendedData.getInviterName(), extendedData.getInviterName());
        invitationData.caller = new ZegoUIKitUser(extendedData.getInviterName(), extendedData.getInviterName());
        return invitationData;
    }
}
