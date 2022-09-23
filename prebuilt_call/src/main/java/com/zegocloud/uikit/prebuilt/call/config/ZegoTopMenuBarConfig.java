package com.zegocloud.uikit.prebuilt.call.config;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ZegoTopMenuBarConfig implements Serializable {

    public String title;
    public List<ZegoMenuBarButtonName> buttons = new ArrayList<>();
    public boolean hideAutomatically = true;
    public boolean hideByClick = true;
    public ZegoMenuBarStyle style = ZegoMenuBarStyle.DARK;
    public boolean isVisible = false;
}
