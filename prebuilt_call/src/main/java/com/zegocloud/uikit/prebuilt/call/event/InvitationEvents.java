package com.zegocloud.uikit.prebuilt.call.event;

import com.zegocloud.uikit.prebuilt.call.invite.internal.IncomingCallButtonListener;
import com.zegocloud.uikit.prebuilt.call.invite.internal.OutgoingCallButtonListener;
import com.zegocloud.uikit.prebuilt.call.invite.internal.ZegoInvitationCallListener;

public class InvitationEvents {

    private SignalPluginConnectListener pluginConnectListener;
    private ZegoInvitationCallListener invitationListener;
    private OutgoingCallButtonListener outgoingCallButtonListener;
    private IncomingCallButtonListener incomingCallButtonListener;

    public SignalPluginConnectListener getPluginConnectListener() {
        return pluginConnectListener;
    }

    public void setPluginConnectListener(SignalPluginConnectListener pluginConnectListener) {
        this.pluginConnectListener = pluginConnectListener;
    }

    public ZegoInvitationCallListener getInvitationListener() {
        return invitationListener;
    }

    public void setInvitationListener(ZegoInvitationCallListener listener) {
        this.invitationListener = listener;
    }

    public OutgoingCallButtonListener getOutgoingCallButtonListener() {
        return outgoingCallButtonListener;
    }

    public void setOutgoingCallButtonListener(
        OutgoingCallButtonListener outgoingCallButtonListener) {
        this.outgoingCallButtonListener = outgoingCallButtonListener;
    }

    public IncomingCallButtonListener getIncomingCallButtonListener() {
        return incomingCallButtonListener;
    }

    public void setIncomingCallButtonListener(
        IncomingCallButtonListener incomingCallButtonListener) {
        this.incomingCallButtonListener = incomingCallButtonListener;
    }
}
