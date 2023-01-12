package com.zegocloud.uikit.prebuilt.call.invite.internal;

import java.util.List;

public interface ZegoInvitationCallListener {

    void onIncomingCallReceived(String callID, ZegoCallUser caller, ZegoCallType callType, List<ZegoCallUser> callees);

    void onIncomingCallCanceled(String callID, ZegoCallUser caller);

    void onIncomingCallTimeout(String callID, ZegoCallUser caller);

    void onOutgoingCallAccepted(String callID, ZegoCallUser callee);

    void onOutgoingCallRejectedCauseBusy(String callID, ZegoCallUser callee);

    void onOutgoingCallDeclined(String callID, ZegoCallUser callee);

    void onOutgoingCallTimeout(String callID, List<ZegoCallUser> callees);

}
