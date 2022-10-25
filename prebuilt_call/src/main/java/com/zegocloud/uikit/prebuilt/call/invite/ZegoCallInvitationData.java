package com.zegocloud.uikit.prebuilt.call.invite;

import com.zegocloud.uikit.service.defines.ZegoUIKitUser;
import java.io.Serializable;
import java.util.List;

public class ZegoCallInvitationData implements Serializable {

    public String callID;
    public int type;
    public List<ZegoUIKitUser> invitees;
    public ZegoUIKitUser inviter;
    public String customData;

    public ZegoCallInvitationData(String callID, int type, List<ZegoUIKitUser> invitees, ZegoUIKitUser inviter,
        String customData) {
        this.callID = callID;
        this.type = type;
        this.invitees = invitees;
        this.inviter = inviter;
        this.customData = customData;
    }
}
