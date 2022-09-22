package com.zegocloud.uikit.prebuilt.call.invite;

import com.zegocloud.uikit.service.defines.ZegoUIKitUser;
import java.io.Serializable;
import java.util.List;

public class ZegoCallInvitationData implements Serializable {

    public String callID;
    public int type;
    public List<ZegoUIKitUser> invitees;
    public ZegoUIKitUser inviter;
}
