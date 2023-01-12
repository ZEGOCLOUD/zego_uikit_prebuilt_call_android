package com.zegocloud.uikit.prebuilt.call.invite.internal;

public enum ZegoCallType {

    VOICE_CALL(0), VIDEO_CALL(1);

    private int value;

    private ZegoCallType(int value) {
        this.value = value;
    }

    public int value() {
        return this.value;
    }


}
