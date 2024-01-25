package com.zegocloud.uikit.prebuilt.call.invite;

import com.zegocloud.uikit.service.defines.ZegoOnlySelfInRoomListener;

public class Events {

    private BackPressEvent backPressEvent;
    private ZegoOnlySelfInRoomListener onlySelfInRoomListener;

    public void setBackPressEvent(BackPressEvent backPressEvent) {
        this.backPressEvent = backPressEvent;
    }

    public BackPressEvent getBackPressEvent() {
        return backPressEvent;
    }

    public void setOnlySelfInRoomListener(ZegoOnlySelfInRoomListener onlySelfInRoomListener) {
        this.onlySelfInRoomListener = onlySelfInRoomListener;
    }

    public ZegoOnlySelfInRoomListener getOnlySelfInRoomListener() {
        return onlySelfInRoomListener;
    }
}
