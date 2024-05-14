package com.zegocloud.uikit.prebuilt.call;

import com.zegocloud.uikit.components.audiovideo.ZegoAvatarViewProvider;
import com.zegocloud.uikit.components.audiovideocontainer.ZegoLayout;
import com.zegocloud.uikit.components.audiovideocontainer.ZegoLayoutGalleryConfig;
import com.zegocloud.uikit.components.audiovideocontainer.ZegoLayoutMode;
import com.zegocloud.uikit.components.audiovideocontainer.ZegoLayoutPictureInPictureConfig;
import com.zegocloud.uikit.components.common.ZegoPresetResolution;
import com.zegocloud.uikit.plugin.adapter.plugins.beauty.ZegoBeautyPluginConfig;
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallFragment.LeaveCallListener;
import com.zegocloud.uikit.prebuilt.call.config.ZegoBottomMenuBarConfig;
import com.zegocloud.uikit.prebuilt.call.config.ZegoCallDurationConfig;
import com.zegocloud.uikit.prebuilt.call.config.ZegoHangUpConfirmDialogInfo;
import com.zegocloud.uikit.prebuilt.call.config.ZegoInRoomChatConfig;
import com.zegocloud.uikit.prebuilt.call.config.ZegoMemberListConfig;
import com.zegocloud.uikit.prebuilt.call.config.ZegoMenuBarButtonName;
import com.zegocloud.uikit.prebuilt.call.config.ZegoMenuBarStyle;
import com.zegocloud.uikit.prebuilt.call.config.ZegoPrebuiltAudioVideoViewConfig;
import com.zegocloud.uikit.prebuilt.call.config.ZegoPrebuiltVideoConfig;
import com.zegocloud.uikit.prebuilt.call.config.ZegoTopMenuBarConfig;
import com.zegocloud.uikit.prebuilt.call.event.BackPressEvent;
import com.zegocloud.uikit.prebuilt.call.event.CallEndListener;
import com.zegocloud.uikit.prebuilt.call.event.CallEvents;
import com.zegocloud.uikit.prebuilt.call.internal.ZegoMiniVideoConfig;
import com.zegocloud.uikit.prebuilt.call.invite.internal.ZegoCallText;
import com.zegocloud.uikit.service.defines.ZegoMeRemovedFromRoomListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class ZegoUIKitPrebuiltCallConfig {

    public boolean turnOnCameraWhenJoining = true;
    public boolean turnOnMicrophoneWhenJoining = true;
    public boolean useSpeakerWhenJoining = true;
    public ZegoPrebuiltAudioVideoViewConfig audioVideoViewConfig = new ZegoPrebuiltAudioVideoViewConfig();
    public ZegoLayout layout = new ZegoLayout();
    public ZegoBottomMenuBarConfig bottomMenuBarConfig = new ZegoBottomMenuBarConfig();
    public ZegoTopMenuBarConfig topMenuBarConfig = new ZegoTopMenuBarConfig();

    /**
     * if you want to custom back button events,you can use {@link CallEvents#setBackPressEvent(BackPressEvent)}
     * </br>
     * Or if hangUpConfirmDialogInfo is not null,a confirm dialog will show when leave button is clicked or Android back button is
     * pressed. Please use {@link ZegoCallText }  to custom dialog texts.
     * </br>
     * if hangUpConfirmDialogInfo is null,click leave button or Android back button will end call directly.
     */
    @Deprecated
    public ZegoHangUpConfirmDialogInfo hangUpConfirmDialogInfo;

    public ZegoMemberListConfig memberListConfig = new ZegoMemberListConfig();
    public ZegoPrebuiltVideoConfig screenSharingVideoConfig = new ZegoPrebuiltVideoConfig(
        ZegoPresetResolution.PRESET_540P);
    public ZegoPrebuiltVideoConfig videoConfig = new ZegoPrebuiltVideoConfig(ZegoPresetResolution.PRESET_360P);
    public ZegoCallDurationConfig durationConfig;
    public LeaveCallListener leaveCallListener;
    public ZegoAvatarViewProvider avatarViewProvider;
    public ZegoBeautyPluginConfig beautyConfig = new ZegoBeautyPluginConfig();
    public ZegoInRoomChatConfig inRoomChatConfig = new ZegoInRoomChatConfig();
    public ZegoMiniVideoConfig miniVideoConfig = new ZegoMiniVideoConfig();
    public ZegoCallText zegoCallText = new ZegoCallText();

    /**
     * use ZegoUIKitPrebuiltCallService.events.callEvents.setCallEndListener() instead;
     *
     * @deprecated use {@link CallEvents#setCallEndListener(CallEndListener)} instead.
     */
    public transient ZegoMeRemovedFromRoomListener removedFromRoomListener;

    public ZegoUIKitPrebuiltCallConfig() {

    }

    public static ZegoUIKitPrebuiltCallConfig groupVideoCall() {
        ZegoUIKitPrebuiltCallConfig config = new ZegoUIKitPrebuiltCallConfig();
        config.turnOnCameraWhenJoining = true;
        config.turnOnMicrophoneWhenJoining = true;
        config.useSpeakerWhenJoining = true;
        config.layout.mode = ZegoLayoutMode.GALLERY;
        config.layout.config = new ZegoLayoutGalleryConfig();
        config.bottomMenuBarConfig.buttons = new ArrayList<>(
            Arrays.asList(ZegoMenuBarButtonName.TOGGLE_CAMERA_BUTTON, ZegoMenuBarButtonName.SWITCH_CAMERA_BUTTON,
                ZegoMenuBarButtonName.HANG_UP_BUTTON, ZegoMenuBarButtonName.TOGGLE_MICROPHONE_BUTTON,
                ZegoMenuBarButtonName.SWITCH_AUDIO_OUTPUT_BUTTON));
        config.topMenuBarConfig.buttons = new ArrayList<>(
            Collections.singletonList(ZegoMenuBarButtonName.SHOW_MEMBER_LIST_BUTTON));
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
        config.bottomMenuBarConfig.buttons = new ArrayList<>(
            Arrays.asList(ZegoMenuBarButtonName.TOGGLE_MICROPHONE_BUTTON, ZegoMenuBarButtonName.HANG_UP_BUTTON,
                ZegoMenuBarButtonName.SWITCH_AUDIO_OUTPUT_BUTTON));
        config.topMenuBarConfig.buttons = new ArrayList<>(
            Collections.singletonList(ZegoMenuBarButtonName.SHOW_MEMBER_LIST_BUTTON));
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
        config.bottomMenuBarConfig.buttons = new ArrayList<>(
            Arrays.asList(ZegoMenuBarButtonName.TOGGLE_CAMERA_BUTTON, ZegoMenuBarButtonName.SWITCH_CAMERA_BUTTON,
                ZegoMenuBarButtonName.HANG_UP_BUTTON, ZegoMenuBarButtonName.TOGGLE_MICROPHONE_BUTTON,
                ZegoMenuBarButtonName.SWITCH_AUDIO_OUTPUT_BUTTON));
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
        config.bottomMenuBarConfig.buttons = new ArrayList<>(
            Arrays.asList(ZegoMenuBarButtonName.TOGGLE_MICROPHONE_BUTTON, ZegoMenuBarButtonName.HANG_UP_BUTTON,
                ZegoMenuBarButtonName.SWITCH_AUDIO_OUTPUT_BUTTON));
        config.bottomMenuBarConfig.style = ZegoMenuBarStyle.LIGHT;
        config.topMenuBarConfig.buttons = new ArrayList<>();
        config.topMenuBarConfig.isVisible = false;
        config.audioVideoViewConfig.useVideoViewAspectFill = true;
        return config;
    }
}
