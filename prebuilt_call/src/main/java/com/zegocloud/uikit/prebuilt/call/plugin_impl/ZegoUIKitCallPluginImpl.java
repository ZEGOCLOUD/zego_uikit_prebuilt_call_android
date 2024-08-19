package com.zegocloud.uikit.prebuilt.call.plugin_impl;

import android.app.Activity;
import android.app.Application;
import android.util.Log;
import com.zegocloud.uikit.plugin.adapter.plugins.ZegoPluginType;
import com.zegocloud.uikit.plugin.adapter.plugins.call.PluginCallType;
import com.zegocloud.uikit.plugin.adapter.plugins.call.PluginCallUser;
import com.zegocloud.uikit.plugin.adapter.plugins.call.ZegoCallPluginConfig;
import com.zegocloud.uikit.plugin.adapter.plugins.call.ZegoCallPluginProtocol;
import com.zegocloud.uikit.plugin.adapter.plugins.common.CallbackNullChecker;
import com.zegocloud.uikit.plugin.adapter.plugins.common.ZegoPluginCallback;
import com.zegocloud.uikit.plugin.adapter.plugins.signaling.ZegoSignalingPluginNotificationConfig;
import com.zegocloud.uikit.plugin.common.PluginCallbackListener;
import com.zegocloud.uikit.plugin.invitation.ZegoInvitationType;
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallService;
import com.zegocloud.uikit.prebuilt.call.invite.ZegoUIKitPrebuiltCallInvitationConfig;
import com.zegocloud.uikit.prebuilt.call.invite.internal.CallInvitationServiceImpl;
import com.zegocloud.uikit.service.defines.ZegoUIKitUser;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ZegoUIKitCallPluginImpl implements ZegoCallPluginProtocol {

    private static ZegoUIKitCallPluginImpl sInstance;

    private ZegoUIKitCallPluginImpl() {
    }

    public static ZegoUIKitCallPluginImpl getInstance() {
        synchronized (ZegoUIKitCallPluginImpl.class) {
            if (sInstance == null) {
                sInstance = new ZegoUIKitCallPluginImpl();
            }
            return sInstance;
        }
    }

    @Override
    public ZegoPluginType getPluginType() {
        return ZegoPluginType.CALLKIT;
    }

    @Override
    public String getVersion() {
        return "3.0.0";
    }

    private static final String TAG = "ZegoUIKitCallPluginImpl";

    @Override
    public void init(Application application, long appID, String appSign, String userID, String userName,
        ZegoCallPluginConfig callPluginConfig) {
        ZegoUIKitPrebuiltCallInvitationConfig invitationConfig;
        if (callPluginConfig.invitationConfig instanceof ZegoUIKitPrebuiltCallInvitationConfig) {
            invitationConfig = (ZegoUIKitPrebuiltCallInvitationConfig) callPluginConfig.invitationConfig;
        } else {
            invitationConfig = new ZegoUIKitPrebuiltCallInvitationConfig();
        }
        ZegoUIKitPrebuiltCallService.init(application, appID, appSign, userID, userName, invitationConfig);
    }

    @Override
    public void initWithToken(Application application, long appID, String token, String userID, String userName,
        ZegoCallPluginConfig callPluginConfig) {
        ZegoUIKitPrebuiltCallInvitationConfig invitationConfig;
        if (callPluginConfig.invitationConfig instanceof ZegoUIKitPrebuiltCallInvitationConfig) {
            invitationConfig = (ZegoUIKitPrebuiltCallInvitationConfig) callPluginConfig.invitationConfig;
        } else {
            invitationConfig = new ZegoUIKitPrebuiltCallInvitationConfig();
        }
        ZegoUIKitPrebuiltCallService.initWithToken(application, appID, token, userID, userName, invitationConfig);
    }

    @Override
    public void unInit() {
        ZegoUIKitPrebuiltCallService.unInit();
    }

    @Override
    public void logoutUser() {
        ZegoUIKitPrebuiltCallService.logoutUser();
    }

    @Override
    public void sendInvitationWithUIChange(Activity activity, List<PluginCallUser> invitees,
        PluginCallType invitationType, String customData, int timeout, String callID,
        ZegoSignalingPluginNotificationConfig notificationConfig, ZegoPluginCallback pluginCallback) {
        List<ZegoUIKitUser> uiKitUsers = new ArrayList<>();
        for (PluginCallUser pluginCallUser : invitees) {
            ZegoUIKitUser uiKitUser = new ZegoUIKitUser(pluginCallUser.userID, pluginCallUser.userName);
            uiKitUser.avatar = pluginCallUser.avatar;
            uiKitUsers.add(uiKitUser);
        }
        ZegoInvitationType type = ZegoInvitationType.getZegoInvitationType(invitationType.getValue());
        CallInvitationServiceImpl.getInstance()
            .sendInvitationWithUIChange(activity, uiKitUsers, type, customData, timeout, callID, notificationConfig,
                new PluginCallbackListener() {
                    @Override
                    public void callback(Map<String, Object> result) {
                        int code = (int) result.get("code");
                        String message = (String) result.get("message");
                        List<ZegoUIKitUser> errorInvitees = (List<ZegoUIKitUser>) result.get("errorInvitees");
                        if (pluginCallback != null) {
                            if (code == 0) {
                                CallbackNullChecker.onSuccess(pluginCallback);
                            } else {
                                CallbackNullChecker.onError(pluginCallback, code, message);
                            }
                        }
                    }
                });
    }
}
