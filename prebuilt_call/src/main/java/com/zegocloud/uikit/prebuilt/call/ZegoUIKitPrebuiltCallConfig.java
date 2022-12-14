package com.zegocloud.uikit.prebuilt.call;

import com.zegocloud.uikit.components.audiovideocontainer.ZegoLayout;
import com.zegocloud.uikit.components.audiovideocontainer.ZegoLayoutGalleryConfig;
import com.zegocloud.uikit.components.audiovideocontainer.ZegoLayoutMode;
import com.zegocloud.uikit.components.audiovideocontainer.ZegoLayoutPictureInPictureConfig;
import com.zegocloud.uikit.prebuilt.call.config.ZegoBottomMenuBarConfig;
import com.zegocloud.uikit.prebuilt.call.config.ZegoHangUpConfirmDialogInfo;
import com.zegocloud.uikit.prebuilt.call.config.ZegoMemberListConfig;
import com.zegocloud.uikit.prebuilt.call.config.ZegoMenuBarButtonName;
import com.zegocloud.uikit.prebuilt.call.config.ZegoMenuBarStyle;
import com.zegocloud.uikit.prebuilt.call.config.ZegoPrebuiltAudioVideoViewConfig;
import com.zegocloud.uikit.prebuilt.call.config.ZegoTopMenuBarConfig;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class ZegoUIKitPrebuiltCallConfig implements Serializable {

    public boolean turnOnCameraWhenJoining = true;
    public boolean turnOnMicrophoneWhenJoining = true;
    public boolean useSpeakerWhenJoining = true;
    public ZegoPrebuiltAudioVideoViewConfig audioVideoViewConfig = new ZegoPrebuiltAudioVideoViewConfig();
    public ZegoLayout layout = new ZegoLayout();
    public ZegoBottomMenuBarConfig bottomMenuBarConfig = new ZegoBottomMenuBarConfig();
    public ZegoTopMenuBarConfig topMenuBarConfig = new ZegoTopMenuBarConfig();
    public ZegoHangUpConfirmDialogInfo hangUpConfirmDialogInfo;
    public ZegoMemberListConfig memberListConfig = new ZegoMemberListConfig();

    public ZegoUIKitPrebuiltCallConfig() {

    }

    public static ZegoUIKitPrebuiltCallConfig groupVideoCall() {
        ZegoUIKitPrebuiltCallConfig config = new ZegoUIKitPrebuiltCallConfig();
        config.turnOnCameraWhenJoining = true;
        config.turnOnMicrophoneWhenJoining = true;
        config.useSpeakerWhenJoining = true;
        config.layout.mode = ZegoLayoutMode.GALLERY;
        config.layout.config = new ZegoLayoutGalleryConfig();
        config.bottomMenuBarConfig.buttons = Arrays.asList(ZegoMenuBarButtonName.TOGGLE_CAMERA_BUTTON,
            ZegoMenuBarButtonName.SWITCH_CAMERA_BUTTON, ZegoMenuBarButtonName.HANG_UP_BUTTON,
            ZegoMenuBarButtonName.TOGGLE_MICROPHONE_BUTTON, ZegoMenuBarButtonName.SWITCH_AUDIO_OUTPUT_BUTTON);
        config.topMenuBarConfig.buttons = Collections.singletonList(ZegoMenuBarButtonName.SHOW_MEMBER_LIST_BUTTON);
        config.topMenuBarConfig.isVisible = true;
        config.bottomMenuBarConfig.style = ZegoMenuBarStyle.DARK;
        config.audioVideoViewConfig.useVideoViewAspectFill = false;
        return config;
    }

    public static ZegoUIKitPrebuiltCallConfig groupVoiceCall() {
        ZegoUIKitPrebuiltCallConfig config = new ZegoUIKitPrebuiltCallConfig();
        config.turnOnCameraWhenJoining = false;
        config.turnOnMicrophoneWhenJoining = true;
        config.useSpeakerWhenJoining = true;
        config.layout.mode = ZegoLayoutMode.GALLERY;
        config.layout.config = new ZegoLayoutGalleryConfig();
        config.bottomMenuBarConfig.buttons = Arrays.asList(ZegoMenuBarButtonName.TOGGLE_MICROPHONE_BUTTON,
            ZegoMenuBarButtonName.HANG_UP_BUTTON, ZegoMenuBarButtonName.SWITCH_AUDIO_OUTPUT_BUTTON);
        config.topMenuBarConfig.buttons = Collections.singletonList(ZegoMenuBarButtonName.SHOW_MEMBER_LIST_BUTTON);
        config.topMenuBarConfig.isVisible = true;
        config.bottomMenuBarConfig.style = ZegoMenuBarStyle.DARK;
        config.audioVideoViewConfig.useVideoViewAspectFill = false;
        return config;
    }

    public static ZegoUIKitPrebuiltCallConfig oneOnOneVideoCall() {
        ZegoUIKitPrebuiltCallConfig config = new ZegoUIKitPrebuiltCallConfig();
        config.turnOnCameraWhenJoining = true;
        config.turnOnMicrophoneWhenJoining = true;
        config.useSpeakerWhenJoining = true;
        config.layout.mode = ZegoLayoutMode.PICTURE_IN_PICTURE;
        config.layout.config = new ZegoLayoutPictureInPictureConfig();
        config.bottomMenuBarConfig.buttons = Arrays.asList(ZegoMenuBarButtonName.TOGGLE_CAMERA_BUTTON,
            ZegoMenuBarButtonName.SWITCH_CAMERA_BUTTON, ZegoMenuBarButtonName.HANG_UP_BUTTON,
            ZegoMenuBarButtonName.TOGGLE_MICROPHONE_BUTTON, ZegoMenuBarButtonName.SWITCH_AUDIO_OUTPUT_BUTTON);
        config.bottomMenuBarConfig.style = ZegoMenuBarStyle.LIGHT;
        config.topMenuBarConfig.buttons = new ArrayList<>();
        config.topMenuBarConfig.isVisible = false;
        config.audioVideoViewConfig.useVideoViewAspectFill = true;
        return config;
    }

    public static ZegoUIKitPrebuiltCallConfig oneOnOneVoiceCall() {
        ZegoUIKitPrebuiltCallConfig config = new ZegoUIKitPrebuiltCallConfig();
        config.turnOnCameraWhenJoining = false;
        config.turnOnMicrophoneWhenJoining = true;
        config.useSpeakerWhenJoining = false;
        config.layout.mode = ZegoLayoutMode.PICTURE_IN_PICTURE;
        config.layout.config = new ZegoLayoutPictureInPictureConfig();
        config.bottomMenuBarConfig.buttons = Arrays.asList(ZegoMenuBarButtonName.TOGGLE_MICROPHONE_BUTTON,
            ZegoMenuBarButtonName.HANG_UP_BUTTON, ZegoMenuBarButtonName.SWITCH_AUDIO_OUTPUT_BUTTON);
        config.bottomMenuBarConfig.style = ZegoMenuBarStyle.LIGHT;
        config.topMenuBarConfig.buttons = new ArrayList<>();
        config.topMenuBarConfig.isVisible = false;
        config.audioVideoViewConfig.useVideoViewAspectFill = true;
        return config;
    }
}
