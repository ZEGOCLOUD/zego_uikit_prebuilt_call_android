package com.zegocloud.uikit.prebuilt.call.event;

import im.zego.zim.enums.ZIMConnectionEvent;
import im.zego.zim.enums.ZIMConnectionState;
import org.json.JSONObject;

public interface SignalPluginConnectListener {

    /**
     * SignalPlugin is used to send call invitations,if your ZIMConnectionState is DISCONNECTED,you can't send or
     * receive call invitations,you can connect to signal plugin by 'ZegoUIKitPrebuiltCallInvitationService.init()'.
     *
     * @param state
     * @param event
     * @param extendedData
     */
    void onSignalPluginConnectionStateChanged(ZIMConnectionState state, ZIMConnectionEvent event,
        JSONObject extendedData);
}
