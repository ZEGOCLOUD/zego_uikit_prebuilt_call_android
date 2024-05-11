package com.zegocloud.uikit.prebuilt.call.invite.internal;

import com.zegocloud.uikit.internal.ZegoUIKitLanguage;

public class ZegoCallText {

    public String memberListTitle;

    public String permissionExplainCamera;
    public String permissionExplainMic;
    public String permissionExplainMicAndCamera;
    public String settingCamera;
    public String settingMic;
    public String settingMicAndCamera;

    public String ok;

    public String cancel;

    public String settings;

    public String leaveTitle;
    public String leaveMessage;
    public String confirm;

    public String inRoomChatTitle;
    public String inRoomChatInputHint;

    public String permissionFloatWindow;
    public String agree;
    public String disagree;

    private ZegoUIKitLanguage language;

    public ZegoCallText() {
        this(ZegoUIKitLanguage.ENGLISH);
    }

    public ZegoCallText(ZegoUIKitLanguage language) {
        this.language = language;

        if (language == ZegoUIKitLanguage.ENGLISH) {
            memberListTitle = "Member";
            permissionExplainCamera = "Camera access is required to start a call";
            permissionExplainMic = "Microphone access is required to start a call";
            permissionExplainMicAndCamera = "Camera and microphone access are required to start a call.";
            settingCamera = "Please go to system settings to allow camera access.";
            settingMic = "Please go to system settings to allow microphone access.";
            settingMicAndCamera = "Please go to system settings to allow camera and microphone access.";
            ok = "OK";
            cancel = "Cancel";
            settings = "Settings";
            leaveTitle = "Leave the call";
            leaveMessage = "Are you sure to leave the call?";
            confirm = "Confirm";
            inRoomChatTitle = "Chat";
            inRoomChatInputHint = "Send a message to everyone";
            permissionFloatWindow = "We need your consent for the following permissions in order to display the video window properly";
            agree = "Agree";
            disagree = "Disagree";
        } else {
            memberListTitle = "成员";
            permissionExplainCamera = "需要摄像头访问权限才能发起通话。";
            permissionExplainMic = "需要麦克风访问权限才能开始通话。";
            permissionExplainMicAndCamera = "需要摄像头和麦克风访问权限才能开始通话。";
            settingCamera = "请前往系统设置允许访问摄像头。";
            settingMic = "请前往系统设置允许访问麦克风。";
            settingMicAndCamera = "请前往系统设置允许访问摄像头和麦克风。";
            ok = "确定";
            cancel = "取消";
            settings = "设置";
            leaveTitle = "退出通话";
            leaveMessage = "您确定要退出通话吗？";
            confirm = "确认";
            inRoomChatTitle = "聊天";
            inRoomChatInputHint = "向所有人发送一条消息";
            permissionFloatWindow = "为了正确显示视频窗口，我们需要您同意以下权限";
            agree = "同意";
            disagree = "拒绝";
        }
    }

    public ZegoUIKitLanguage getLanguage() {
        return language;
    }
}
