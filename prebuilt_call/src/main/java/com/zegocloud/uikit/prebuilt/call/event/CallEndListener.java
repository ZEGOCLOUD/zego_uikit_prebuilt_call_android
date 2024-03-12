package com.zegocloud.uikit.prebuilt.call.event;

public interface CallEndListener {

    /**
     * @param callEndReason
     * @param jsonObject    when callEndReason is KICK_OUT,this value means who kick out you.if the value is empty,means
     *                      you are kicked out by server
     */
    void onCallEnd(ZegoCallEndReason callEndReason, String jsonObject);
}
