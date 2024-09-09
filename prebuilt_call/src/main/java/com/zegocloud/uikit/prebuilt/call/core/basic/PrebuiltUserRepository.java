package com.zegocloud.uikit.prebuilt.call.core.basic;

import com.tencent.mmkv.MMKV;
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallService;
import com.zegocloud.uikit.prebuilt.call.core.CallInvitationServiceImpl;
import com.zegocloud.uikit.prebuilt.call.core.PrebuiltCallExpressBridge;
import com.zegocloud.uikit.prebuilt.call.core.PrebuiltCallZIMBridge;
import com.zegocloud.uikit.prebuilt.call.event.SignalPluginConnectListener;
import com.zegocloud.uikit.service.defines.ZegoUIKitPluginCallback;
import com.zegocloud.uikit.service.defines.ZegoUIKitUser;
import im.zego.zim.ZIM;
import im.zego.zim.callback.ZIMEventHandler;
import im.zego.zim.entity.ZIMUserInfo;
import im.zego.zim.enums.ZIMConnectionEvent;
import im.zego.zim.enums.ZIMConnectionState;
import org.json.JSONObject;
import timber.log.Timber;

public class PrebuiltUserRepository {

    private PrebuiltCallExpressBridge expressBridge;
    private PrebuiltCallZIMBridge zimBridge;
    private boolean isLoginIng = false;
    private ZIMUserInfo zimUserInfo;

    private ZIMEventHandler zimEventHandler = new ZIMEventHandler() {
        @Override
        public void onConnectionStateChanged(ZIM zim, ZIMConnectionState state, ZIMConnectionEvent event,
            JSONObject extendedData) {
            super.onConnectionStateChanged(zim, state, event, extendedData);
            Timber.d("onConnectionStateChanged() called with: zim = [" + zim + "], state = [" + state + "], event = ["
                + event + "], extendedData = [" + extendedData + "]");
            if (state == ZIMConnectionState.DISCONNECTED && event == ZIMConnectionEvent.SUCCESS) {

                CallInvitationServiceImpl.getInstance().onPrebuiltCallUserLogout(zimUserInfo.userID, zimUserInfo.userName);
                zimUserInfo = null;

                SignalPluginConnectListener pluginConnectListener = ZegoUIKitPrebuiltCallService.events.invitationEvents.getPluginConnectListener();
                if (pluginConnectListener != null) {
                    pluginConnectListener.onSignalPluginConnectionStateChanged(state, event, extendedData);
                }
                removeCallbacks();
            }

            if (state == ZIMConnectionState.CONNECTED && event == ZIMConnectionEvent.ACTIVE_LOGIN) {
                zimUserInfo = zimBridge.getLocalUser();
                CallInvitationServiceImpl.getInstance().onPrebuiltCallUserLogin(zimUserInfo.userID, zimUserInfo.userName);
            }

        }
    };

    public PrebuiltUserRepository(PrebuiltCallExpressBridge expressBridge, PrebuiltCallZIMBridge zimBridge) {
        this.expressBridge = expressBridge;
        this.zimBridge = zimBridge;
    }

    public void initAndLoginUserByLastRecord(ZegoUIKitPluginCallback callback) {
        Timber.d("initAndLoginUserByLastRecord() called with: callback = [" + callback + "]");
        MMKV mmkv = MMKV.defaultMMKV(MMKV.SINGLE_PROCESS_MODE, getClass().getName());
        String preUserID = mmkv.getString("userID", "");
        String preUserName = mmkv.getString("userName", "");
        loginUser(preUserID, preUserName, callback);
    }

    public void loginUser(String userID, String userName, ZegoUIKitPluginCallback callback) {
        Timber.d("loginUser() called with: userID = [" + userID + "], userName = [" + userName + "],isLoginIng:"
            + isLoginIng);
        if (isLoginIng) {
            return;
        }
        setupCallbacks();
        isLoginIng = true;
        expressBridge.loginUser(userID, userName);
        zimBridge.loginUser(userID, userName, new ZegoUIKitPluginCallback() {
            @Override
            public void onResult(int errorCode, String message) {
                isLoginIng = false;
                Timber.d("loginUser ZIM onResult() called with: errorCode = [" + errorCode + "], message = [" + message + "]");
                if (callback != null) {
                    callback.onResult(errorCode, message);
                }
            }
        });
        MMKV mmkv = MMKV.defaultMMKV(MMKV.SINGLE_PROCESS_MODE, getClass().getName());
        mmkv.putString("userID", userID);
        mmkv.putString("userName", userName);
    }

    public void logoutUser() {
        Timber.d("logoutUser() called");
        expressBridge.logoutUser();
        zimBridge.logout();
    }

    private void setupCallbacks() {
        zimBridge.registerZIMEventHandler(zimEventHandler);
    }

    //If call logout immediately followed by unregisterZIMEventHandler, will not receive the onConnectionStateChanged callback.
    private void removeCallbacks() {
        zimBridge.unregisterZIMEventHandler(zimEventHandler);
    }

    public ZegoUIKitUser getLocalUser() {
        return expressBridge.getLocalUser();
    }
}
