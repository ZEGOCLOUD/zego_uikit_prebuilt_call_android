package com.zegocloud.uikit.prebuilt.call.invite.internal;

import java.util.List;

public interface ClickListener {

    void onClick(int errorCode, String errorMessage, List<ZegoCallUser> errorInvitees);

}
