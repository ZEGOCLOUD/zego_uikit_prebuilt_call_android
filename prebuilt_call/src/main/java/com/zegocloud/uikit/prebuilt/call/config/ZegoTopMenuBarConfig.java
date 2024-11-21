package com.zegocloud.uikit.prebuilt.call.config;

import android.view.View;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ZegoTopMenuBarConfig implements Serializable {

    public String title;
    public List<ZegoMenuBarButtonName> buttons = new ArrayList<>();
    public boolean hideAutomatically = true;
    public boolean hideByClick = true;
    public boolean isVisible = false;

    public int maxCount = 5;

    public List<View> extendedButtons = new ArrayList<>();

    public ZegoMenuBarStyle style = ZegoMenuBarStyle.DARK;

    public ZegoMenuBarButtonConfig buttonConfig = new ZegoMenuBarButtonConfig();
}
