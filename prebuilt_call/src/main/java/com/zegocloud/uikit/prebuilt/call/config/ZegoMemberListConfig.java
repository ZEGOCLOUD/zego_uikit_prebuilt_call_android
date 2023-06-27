package com.zegocloud.uikit.prebuilt.call.config;

import com.zegocloud.uikit.components.memberlist.ZegoMemberListItemViewProvider;
import java.io.Serializable;

public class ZegoMemberListConfig implements Serializable {

    public boolean showMicrophoneState = true;
    public boolean showCameraState = true;
    public ZegoMemberListItemViewProvider memberListItemProvider;
}
