package com.zegocloud.uikit.prebuilt.call.event;

import com.zegocloud.uikit.ZegoUIKit;
import com.zegocloud.uikit.service.defines.ZegoAudioOutputDeviceChangedListener;
import com.zegocloud.uikit.service.defines.ZegoOnlySelfInRoomListener;
import com.zegocloud.uikit.service.express.IExpressEngineEventHandler;

public class CallEvents {

    private IExpressEngineEventHandler expressEngineEventHandler;
    private BackPressEvent backPressEvent;
    private ZegoOnlySelfInRoomListener onlySelfInRoomListener;
    private CallEndListener callEndListener;
    private ZegoMenuBarButtonClickListener buttonClickListener;

    /**
     * The default back pressed event is finish call immediately,override this event to change the default event.
     *
     * @param backPressEvent
     */
    public void setBackPressEvent(BackPressEvent backPressEvent) {
        this.backPressEvent = backPressEvent;
    }

    public BackPressEvent getBackPressEvent() {
        return backPressEvent;
    }

    public ZegoOnlySelfInRoomListener getOnlySelfInRoomListener() {
        return onlySelfInRoomListener;
    }

    /**
     * The default action is finish call immediately,override this event to change the default event.
     *
     * @param onlySelfInRoomListener
     */
    public void setOnlySelfInRoomListener(ZegoOnlySelfInRoomListener onlySelfInRoomListener) {
        this.onlySelfInRoomListener = onlySelfInRoomListener;
    }

    public CallEndListener getCallEndListener() {
        return callEndListener;
    }

    public void setCallEndListener(CallEndListener callEndListener) {
        this.callEndListener = callEndListener;
    }

    public void setExpressEngineEventHandler(IExpressEngineEventHandler eventHandler) {
        if (this.expressEngineEventHandler != null) {
            ZegoUIKit.removeEventHandler(this.expressEngineEventHandler);
        }
        this.expressEngineEventHandler = eventHandler;
        if (eventHandler != null) {
            ZegoUIKit.addEventHandler(eventHandler);
        }
    }

    public ZegoMenuBarButtonClickListener getButtonClickListener() {
        return buttonClickListener;
    }

    public void setButtonClickListener(ZegoMenuBarButtonClickListener buttonClickListener) {
        this.buttonClickListener = buttonClickListener;
    }

    public void addAudioOutputDeviceChangedListener(ZegoAudioOutputDeviceChangedListener listener) {
        ZegoUIKit.addAudioOutputDeviceChangedListener(listener);
    }

    public void removeAudioOutputDeviceChangedListener(ZegoAudioOutputDeviceChangedListener listener) {
        ZegoUIKit.removeAudioOutputDeviceChangedListener(listener);
    }
}
