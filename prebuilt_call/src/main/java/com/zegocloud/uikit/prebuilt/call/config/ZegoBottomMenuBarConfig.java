package com.zegocloud.uikit.prebuilt.call.config;

import android.view.View;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ZegoBottomMenuBarConfig implements Serializable {

    public List<ZegoMenuBarButtonName> buttons = new ArrayList<>(
        Arrays.asList(ZegoMenuBarButtonName.TOGGLE_CAMERA_BUTTON, ZegoMenuBarButtonName.SWITCH_CAMERA_BUTTON,
            ZegoMenuBarButtonName.HANG_UP_BUTTON, ZegoMenuBarButtonName.TOGGLE_MICROPHONE_BUTTON,
            ZegoMenuBarButtonName.SWITCH_AUDIO_OUTPUT_BUTTON));

    public int maxCount = 5;
    public boolean hideAutomatically = true;
    public boolean hideByClick = true;
    public List<View> extendedButtons = new ArrayList<>();

    public ZegoMenuBarStyle style = ZegoMenuBarStyle.DARK;

    public ZegoMenuBarButtonConfig buttonConfig = new ZegoMenuBarButtonConfig();
}
