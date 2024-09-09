package com.zegocloud.uikit.prebuilt.call.core.invite;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import com.zegocloud.uikit.ZegoUIKit;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;

public class PrebuiltCallUtil {

    public static String generatePrebuiltCallRoomID() {
        String callID = null;
        String userID = ZegoUIKit.getLocalUser().userID;
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

    //    public static String generateCallInviteJsonString(List<ZegoUIKitUser> invitees, String customData, String callID) {
    //        JSONObject jsonObject = new JSONObject();
    //        JSONArray jsonArray = new JSONArray();
    //        try {
    //            jsonObject.put("call_id", callID);
    //            for (ZegoUIKitUser invitee : invitees) {
    //                JSONObject tmp = new JSONObject();
    //                tmp.put("user_id", invitee.userID);
    //                tmp.put("user_name", invitee.userName);
    //                jsonArray.put(tmp);
    //            }
    //            jsonObject.put("invitees", jsonArray);
    //            jsonObject.put("custom_data", customData);
    //        } catch (JSONException e) {
    //            e.printStackTrace();
    //        }
    //        return jsonObject.toString();
    //    }

    public static boolean isAppBackground(Context context) {
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
}
