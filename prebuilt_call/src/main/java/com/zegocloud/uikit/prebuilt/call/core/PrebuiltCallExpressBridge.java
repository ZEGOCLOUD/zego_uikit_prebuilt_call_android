package com.zegocloud.uikit.prebuilt.call.core;

import com.zegocloud.uikit.ZegoUIKit;
import com.zegocloud.uikit.service.defines.ZegoAudioOutputDevice;
import com.zegocloud.uikit.service.defines.ZegoAudioOutputDeviceChangedListener;
import com.zegocloud.uikit.service.defines.ZegoInRoomCommandListener;
import com.zegocloud.uikit.service.defines.ZegoSendInRoomCommandCallback;
import com.zegocloud.uikit.service.defines.ZegoUIKitCallback;
import com.zegocloud.uikit.service.defines.ZegoUIKitUser;
import com.zegocloud.uikit.service.express.IExpressEngineEventHandler;
import im.zego.zegoexpress.constants.ZegoAudioRoute;
import java.util.ArrayList;

/**
 * wrapper prebuilt business logic to express SDK and add business logic when receive express callbacks
 */
public class PrebuiltCallExpressBridge {

    private static final String TAG = "PrebuiltCallExpressBrid";

    public void addEventHandler(IExpressEngineEventHandler eventHandler) {
        ZegoUIKit.addEventHandler(eventHandler, false);
    }

    public void removeEventHandler(IExpressEngineEventHandler eventHandler) {
        ZegoUIKit.removeEventHandler(eventHandler);
    }

    public void openCamera(boolean open) {
        ZegoUIKit.openCamera(open);
    }

    public void openMicrophone(boolean open) {
        ZegoUIKit.openMicrophone(open);
    }

    public boolean isMicrophoneOn(String userID) {
        return ZegoUIKit.isMicrophoneOn(userID);
    }

    public boolean isMicrophoneOn() {
        if (ZegoUIKit.getLocalUser() == null) {
            return false;
        }
        return ZegoUIKit.isMicrophoneOn(ZegoUIKit.getLocalUser().userID);
    }

    public boolean isCameraOn() {
        if (ZegoUIKit.getLocalUser() == null) {
            return false;
        }
        return ZegoUIKit.isCameraOn(ZegoUIKit.getLocalUser().userID);
    }

    public void setAudioOutputToSpeaker(boolean outputToSpeaker) {
        ZegoUIKit.setAudioOutputToSpeaker(outputToSpeaker);
    }

    public ZegoAudioOutputDevice getAudioRouteType() {
        ZegoAudioRoute audioRouteType = ZegoUIKit.getAudioRouteType();
        return ZegoAudioOutputDevice.getAudioOutputDevice(audioRouteType.value());
    }

    public void joinSDKRoom(String roomID, ZegoUIKitCallback callback) {
        ZegoUIKit.joinRoom(roomID, callback);
    }

    public void leaveSDKRoom() {
        ZegoUIKit.leaveRoom();
    }

    public void loginUser(String userID, String userName) {
        ZegoUIKit.login(userID, userName);
    }

    public void logoutUser() {
        ZegoUIKit.logout();
    }

    public ZegoUIKitUser getLocalUser() {
        return ZegoUIKit.getLocalUser();
    }

    public void renewToken(String token) {
        ZegoUIKit.renewToken(token);
    }

    public void sendInRoomCommand(String command, ArrayList<String> toUserList,
        ZegoSendInRoomCommandCallback callback) {
        ZegoUIKit.sendInRoomCommand(command, toUserList, callback);
    }

    public void addInRoomCommandListener(ZegoInRoomCommandListener listener) {
        ZegoUIKit.addInRoomCommandListener(listener);
    }

    public void removeInRoomCommandListener(ZegoInRoomCommandListener listener) {
        ZegoUIKit.removeInRoomCommandListener(listener);
    }

    public void addAudioOutputDeviceChangedListener(ZegoAudioOutputDeviceChangedListener listener) {
        ZegoUIKit.addAudioOutputDeviceChangedListener(listener);
    }

    public void removeAudioOutputDeviceChangedListener(ZegoAudioOutputDeviceChangedListener listener) {
        ZegoUIKit.removeAudioOutputDeviceChangedListener(listener);
    }
}
