package com.zegocloud.uikit.prebuilt.call.core;

import com.zegocloud.uikit.ZegoUIKit;
import com.zegocloud.uikit.plugin.signaling.ZegoSignalingPlugin;
import com.zegocloud.uikit.service.defines.ZegoUIKitPluginCallback;
import im.zego.zim.callback.ZIMCallAcceptanceSentCallback;
import im.zego.zim.callback.ZIMCallCancelSentCallback;
import im.zego.zim.callback.ZIMCallEndSentCallback;
import im.zego.zim.callback.ZIMCallInvitationSentCallback;
import im.zego.zim.callback.ZIMCallJoinSentCallback;
import im.zego.zim.callback.ZIMCallQuitSentCallback;
import im.zego.zim.callback.ZIMCallRejectionSentCallback;
import im.zego.zim.callback.ZIMCallingInvitationSentCallback;
import im.zego.zim.callback.ZIMEventHandler;
import im.zego.zim.entity.ZIMCallAcceptConfig;
import im.zego.zim.entity.ZIMCallCancelConfig;
import im.zego.zim.entity.ZIMCallEndConfig;
import im.zego.zim.entity.ZIMCallInfo;
import im.zego.zim.entity.ZIMCallInviteConfig;
import im.zego.zim.entity.ZIMCallJoinConfig;
import im.zego.zim.entity.ZIMCallQuitConfig;
import im.zego.zim.entity.ZIMCallRejectConfig;
import im.zego.zim.entity.ZIMCallingInviteConfig;
import im.zego.zim.entity.ZIMError;
import im.zego.zim.entity.ZIMUserFullInfo;
import im.zego.zim.entity.ZIMUserInfo;
import java.util.List;

/**
 * wrapper prebuilt business logic to ZIM SDK and add business logic when receive ZIM callbacks
 */
public class PrebuiltCallZIMBridge {

    private static final String TAG = "PrebuiltCallZIMBridge";

    public void registerZIMEventHandler(ZIMEventHandler zimEventHandler) {
        ZegoSignalingPlugin.getInstance().registerZIMEventHandler(zimEventHandler);
    }

    public void unregisterZIMEventHandler(ZIMEventHandler zimEventHandler) {
        ZegoSignalingPlugin.getInstance().unregisterZIMEventHandler(zimEventHandler);
    }

    public void loginUser(String userID, String userName, ZegoUIKitPluginCallback zegoUIKitPluginCallback) {
        ZegoUIKit.getSignalingPlugin().login(userID, userName, zegoUIKitPluginCallback);
    }

    public void logout() {
        ZegoUIKit.getSignalingPlugin().logout();
    }

    public ZIMUserInfo getLocalUser() {
        return ZegoSignalingPlugin.getInstance().getUserInfo();
    }

    public ZIMCallInfo getZIMCallInfo(String zimCallID) {
        return ZegoSignalingPlugin.getInstance().getZIMCallInfo(zimCallID);
    }

    public ZIMUserFullInfo getMemoryUserInfo(String userID) {
        return ZegoSignalingPlugin.getInstance().getMemoryUserInfo(userID);
    }

    public void callReject(String callID, ZIMCallRejectConfig config, ZIMCallRejectionSentCallback callback) {
        ZegoSignalingPlugin.getInstance().callReject(callID, config, callback);
    }

    public void callAccept(String callID, ZIMCallAcceptConfig config, ZIMCallAcceptanceSentCallback callback) {
        ZegoSignalingPlugin.getInstance().callAccept(callID, config, callback);
    }

    public void callingInvite(List<String> invitees, String callID, ZIMCallingInviteConfig config,
        ZIMCallingInvitationSentCallback callback) {
        ZegoSignalingPlugin.getInstance().callingInvite(invitees, callID, config, callback);
    }

    public void callInvite(List<String> invitees, ZIMCallInviteConfig config, ZIMCallInvitationSentCallback callback) {
        ZegoSignalingPlugin.getInstance().callInvite(invitees, config, callback);
    }

    public void callCancel(List<String> invitees, String zimCallID, ZIMCallCancelConfig config,
        ZIMCallCancelSentCallback callback) {
        ZegoSignalingPlugin.getInstance().callCancel(invitees, zimCallID, config, callback);
    }

    public void callEnd(String zimCallID, ZIMCallEndConfig config, ZIMCallEndSentCallback callback) {
        ZegoSignalingPlugin.getInstance().callEnd(zimCallID, config, callback);
    }

    public void callQuit(String zimCallID, ZIMCallQuitConfig config, ZIMCallQuitSentCallback callback) {
        ZegoSignalingPlugin.getInstance().callQuit(zimCallID, config, callback);
    }

    public void callJoin(String zimCallID, ZIMCallJoinConfig config, ZIMCallJoinSentCallback callback) {
        ZegoSignalingPlugin.getInstance().callJoin(zimCallID, config, callback);
    }

    public void destroy() {
        ZegoSignalingPlugin.getInstance().destroy();
    }
}
