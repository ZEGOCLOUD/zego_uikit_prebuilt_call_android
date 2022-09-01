package com.zegocloud.uikit.prebuilt.call;

import com.zegocloud.uikit.components.audiovideocontainer.ZegoLayout;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

public class ZegoUIKitPrebuiltCallConfig implements Serializable {

    public boolean showMicrophoneStateOnView = true;
    public boolean showCameraStateOnView = false;
    public boolean showUserNameOnView = true;
    public boolean turnOnCameraWhenJoining = true;
    public boolean turnOnMicrophoneWhenJoining = true;
    public boolean useSpeakerWhenJoining = true;
    public ZegoLayout layout = new ZegoLayout();
    public List<ZegoMenuBarButtonName> menuBarButtons = Arrays.asList(
        ZegoMenuBarButtonName.TOGGLE_CAMERA_BUTTON,
        ZegoMenuBarButtonName.TOGGLE_MICROPHONE_BUTTON,
        ZegoMenuBarButtonName.SWITCH_AUDIO_OUTPUT_BUTTON, ZegoMenuBarButtonName.HANG_UP_BUTTON);
    public int menuBarButtonsMaxCount = 5;
    public boolean hideMenuBarAutomatically = true;
    public boolean hideMenuBardByClick = true;
    public ZegoHangUpConfirmDialogInfo hangUpConfirmDialogInfo;
}
