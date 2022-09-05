package com.zegocloud.uikit.prebuilt.call;

import java.util.Arrays;
import java.util.List;

public class ZegoBottomMenuBarConfig {

    public List<ZegoMenuBarButtonName> menuBarButtons = Arrays.asList(
        ZegoMenuBarButtonName.TOGGLE_CAMERA_BUTTON,
        ZegoMenuBarButtonName.TOGGLE_MICROPHONE_BUTTON,
        ZegoMenuBarButtonName.HANG_UP_BUTTON,
        ZegoMenuBarButtonName.SWITCH_AUDIO_OUTPUT_BUTTON,
        ZegoMenuBarButtonName.SWITCH_CAMERA_BUTTON);
    public int maxCount = 5;
    public boolean hideAutomatically = true;
    public boolean hideByClick = true;
}
