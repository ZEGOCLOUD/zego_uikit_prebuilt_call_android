package com.zegocloud.uikit.prebuilt.call.invite;

import com.zegocloud.uikit.service.defines.ZegoUIKitUser;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ZegoCallInvitationData implements Serializable {

    public String callID;
    public int type;
    public List<ZegoUIKitUser> invitees;
    public ZegoUIKitUser inviter;
    public String customData;
    public String invitationID;

    public ZegoCallInvitationData() {

    }

    public ZegoCallInvitationData(String callID, int type, List<ZegoUIKitUser> invitees, ZegoUIKitUser inviter,
        String customData) {
        this.callID = callID;
        this.type = type;
        this.invitees = invitees;
        this.inviter = inviter;
        this.customData = customData;
    }

    public static ZegoCallInvitationData parseString(String string) {
        ZegoCallInvitationData invitationData;
        try {
            JSONObject jsonObject = new JSONObject(string);
            JSONArray inviteUsers = jsonObject.getJSONArray("invitees");
            List<ZegoUIKitUser> list = new ArrayList<>();
            for (int i = 0; i < inviteUsers.length(); i++) {
                JSONObject invitee = inviteUsers.getJSONObject(i);
                String user_id = invitee.getString("user_id");
                String user_name = invitee.getString("user_name");
                list.add(new ZegoUIKitUser(user_id, user_name));
            }
            String call_id = getStringFromJson(jsonObject,"call_id");
            String customData = getStringFromJson(jsonObject,"custom_data");

            invitationData = new ZegoCallInvitationData();
            invitationData.callID = call_id;
            invitationData.invitees = list;
            invitationData.customData = customData;
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return invitationData;
    }

    private static String getStringFromJson(JSONObject jsonObject, String key) {
        try {
            if (jsonObject.has(key)) {
                return jsonObject.getString(key);
            } else {
                return null;
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }
}
