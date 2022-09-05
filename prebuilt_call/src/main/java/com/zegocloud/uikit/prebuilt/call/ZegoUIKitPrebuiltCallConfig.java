package com.zegocloud.uikit.prebuilt.call;

import com.zegocloud.uikit.components.audiovideocontainer.ZegoLayout;
import java.io.Serializable;

public class ZegoUIKitPrebuiltCallConfig implements Serializable {
    public boolean turnOnCameraWhenJoining = true;
    public boolean turnOnMicrophoneWhenJoining = true;
    public boolean useSpeakerWhenJoining = true;
    public ZegoPrebuiltAudioVideoViewConfig audioVideoViewConfig = new ZegoPrebuiltAudioVideoViewConfig();
    public ZegoLayout layout = new ZegoLayout();
    public ZegoBottomMenuBarConfig bottomMenuBarConfig = new ZegoBottomMenuBarConfig();
    public ZegoHangUpConfirmDialogInfo hangUpConfirmDialogInfo;
}
