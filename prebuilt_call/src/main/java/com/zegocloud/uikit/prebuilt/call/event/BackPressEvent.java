package com.zegocloud.uikit.prebuilt.call.event;

public interface BackPressEvent {

    /**
     * The default back pressed event is finish call immediately,override this event to change the default event.
     *
     * <br>if you are using minimize feature,you can return true and call ZegoUIKitPrebuiltCallInvitationService.minimizeCall() in the
     * callback to minimize the call.
     * <br>Or you can just return true in the callback to prevent user end call by back button.
     *
     * @return returns false,it means default action will be done,if returns true,it means you will do something when
     * back button is pressed,the default action will be stopped
     */
    boolean onBackPressed();
}
