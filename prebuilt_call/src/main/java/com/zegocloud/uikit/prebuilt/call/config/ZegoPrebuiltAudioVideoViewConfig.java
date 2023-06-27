package com.zegocloud.uikit.prebuilt.call.config;

import com.zegocloud.uikit.components.audiovideo.ZegoForegroundViewProvider;
import java.io.Serializable;

public class ZegoPrebuiltAudioVideoViewConfig implements Serializable {
    public boolean showMicrophoneStateOnView = true;
    public boolean showCameraStateOnView = false;
    public boolean showUserNameOnView = true;
    public boolean showSoundWavesInAudioMode = true;
    public boolean useVideoViewAspectFill = false;
    public ZegoForegroundViewProvider videoViewForegroundViewProvider;
}
