package com.zegocloud.uikit.prebuilt.call.event;

import com.zegocloud.uikit.service.defines.ZegoOnlySelfInRoomListener;

public class Events {

    public CallEvents callEvents = new CallEvents();
    public InvitationEvents invitationEvents = new InvitationEvents();
    private ErrorEventsListener errorEventsListener;

    /**
     * use ZegoUIKitPrebuiltCallService.events.callEvents.setBackPressEvent() instead;
     *
     * @deprecated use {@link CallEvents#setBackPressEvent(BackPressEvent)} instead.
     */
    @Deprecated
    public void setBackPressEvent(BackPressEvent backPressEvent) {
        callEvents.setBackPressEvent(backPressEvent);
    }

    /**
     * use ZegoUIKitPrebuiltCallService.events.callEvents.setOnlySelfInRoomListener() instead;
     *
     * @deprecated use {@link CallEvents#setOnlySelfInRoomListener(ZegoOnlySelfInRoomListener)} instead.
     */
    @Deprecated
    public void setOnlySelfInRoomListener(ZegoOnlySelfInRoomListener onlySelfInRoomListener) {
        callEvents.setOnlySelfInRoomListener(onlySelfInRoomListener);
    }

    public ErrorEventsListener getErrorEventsListener() {
        return errorEventsListener;
    }

    public void setErrorEventsListener(ErrorEventsListener errorEventsListener) {
        this.errorEventsListener = errorEventsListener;
    }
}
