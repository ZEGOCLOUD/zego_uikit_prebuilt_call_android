package com.zegocloud.uikit.prebuilt.call.core.invite;

import com.zegocloud.uikit.service.defines.ZegoUIKitUser;
import java.io.Serializable;
import java.util.List;

public class ZegoCallInvitationData implements Serializable {

    // express roomID
    public String callID;
    public int type;
    public List<ZegoUIKitUser> invitees;
    public ZegoUIKitUser inviter;
    public ZegoUIKitUser caller;
    public String customData;
    // zim callID
    public String invitationID;

    @Override
    public String toString() {
        return "ZegoCallInvitationData{" +
            "callID='" + callID + '\'' +
            ", type=" + type +
            ", invitees=" + invitees +
            ", inviter=" + inviter +
            ", customData='" + customData + '\'' +
            ", invitationID='" + invitationID + '\'' +
            '}';
    }
}
