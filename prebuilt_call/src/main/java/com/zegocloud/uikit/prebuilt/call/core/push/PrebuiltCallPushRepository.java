package com.zegocloud.uikit.prebuilt.call.core.push;

import com.zegocloud.uikit.ZegoUIKit;

public class PrebuiltCallPushRepository {

    private String notificationClickAction;
    private String callResourceID = DEFAULT_PUSH_RESOURCE_ID;
    private ZIMPushMessage pushMessage;
    public static final String DEFAULT_PUSH_RESOURCE_ID = "zegouikit_call";

    public String getNotificationAction() {
        return notificationClickAction;
    }

    public void setNotificationAction(String notificationAction) {
        this.notificationClickAction = notificationAction;
    }

    public void setCallResourceID(String resourceID) {
        this.callResourceID = resourceID;
    }

    public String getCallResourceID() {
        return callResourceID;
    }

    public ZIMPushMessage getPushMessage() {
        return pushMessage;
    }

    public void setPushMessage(ZIMPushMessage pushMessage) {
        this.pushMessage = pushMessage;
    }

    public void clearPushMessage() {
        setNotificationAction(null);
        setPushMessage(null);
    }

    public void enableFCMPush() {
        //        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
        //            if (!task.isSuccessful()) {
        //                return;
        //            }
        //            String token = task.getResult();
        //            if (!TextUtils.isEmpty(token)) {
        //              //  ZegoUIKit.getSignalingPlugin().enableFCMPush();
        //            }
        //        });
        ZegoUIKit.getSignalingPlugin().enableFCMPush();
    }

    public void disableFCMPush() {
        ZegoUIKit.getSignalingPlugin().disableFCMPush();
    }

    public void enableHWPush(String hwAppID) {
        ZegoUIKit.getSignalingPlugin().enableHWPush(hwAppID);
    }

    public void enableMiPush(String miAppID, String miAppKey) {
        ZegoUIKit.getSignalingPlugin().enableMiPush(miAppID, miAppKey);
    }

    public void enableVivoPush(String vivoAppID, String vivoAppKey) {
        ZegoUIKit.getSignalingPlugin().enableVivoPush(vivoAppID, vivoAppKey);
    }

    public void enableOppoPush(String oppoAppID, String oppoAppKey, String oppoAppSecret) {
        ZegoUIKit.getSignalingPlugin().enableOppoPush(oppoAppID, oppoAppKey, oppoAppSecret);
    }

    public boolean isOtherPushEnable() {
        return ZegoUIKit.getSignalingPlugin().isOtherPushEnable();
    }

    public boolean isFCMPushEnable() {
        return ZegoUIKit.getSignalingPlugin().isFCMPushEnable();
    }

    public void registerPush() {
        if (isFCMPushEnable() || isOtherPushEnable()) {
            ZegoUIKit.getSignalingPlugin().registerPush();
        }
    }

    public void unregisterPush() {
        ZegoUIKit.getSignalingPlugin().unregisterPush();
    }

    public void onPrebuiltUserLogin() {
        if (!isOtherPushEnable()) {
            enableFCMPush();
        }
        registerPush();
    }

    public void onPrebuiltUserLogout() {
        clearPushMessage();
    }
}
