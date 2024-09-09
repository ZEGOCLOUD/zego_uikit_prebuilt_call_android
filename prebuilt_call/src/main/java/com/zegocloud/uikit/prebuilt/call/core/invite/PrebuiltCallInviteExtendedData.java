package com.zegocloud.uikit.prebuilt.call.core.invite;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * full extended data when use zim.callInvite.
 */
public class PrebuiltCallInviteExtendedData {

    @SerializedName("type")
    private int type;
    @SerializedName("inviter_id")
    private String inviterId;
    @SerializedName("inviter_name")
    private String inviterName;
    @SerializedName("data")
    private String data;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getInviterId() {
        return inviterId;
    }

    public void setInviterId(String inviterId) {
        this.inviterId = inviterId;
    }

    public String getInviterName() {
        return inviterName;
    }

    public void setInviterName(String inviterName) {
        this.inviterName = inviterName;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }


    public static class Data {

        @SerializedName("call_id")
        private String callId; // means roomID of express SDK
        @SerializedName("invitees")
        private List<Invitee> invitees;
        @SerializedName("custom_data")
        private String customData;

        public String getCallId() {
            return callId;
        }

        public void setCallId(String callId) {
            this.callId = callId;
        }

        public List<Invitee> getInvitees() {
            return invitees;
        }

        public void setInvitees(List<Invitee> invitees) {
            this.invitees = invitees;
        }

        public String getCustomData() {
            return customData;
        }

        public void setCustomData(String customData) {
            this.customData = customData;
        }

        public static class Invitee {

            @SerializedName("user_id")
            private String userId;
            @SerializedName("user_name")
            private String userName;

            public String getUserId() {
                return userId;
            }

            public void setUserId(String userId) {
                this.userId = userId;
            }

            public String getUserName() {
                return userName;
            }

            public void setUserName(String userName) {
                this.userName = userName;
            }
        }
    }
}
