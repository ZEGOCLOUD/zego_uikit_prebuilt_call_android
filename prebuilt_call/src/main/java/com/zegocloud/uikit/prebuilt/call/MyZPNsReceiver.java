package com.zegocloud.uikit.prebuilt.call;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.text.TextUtils;
import com.zegocloud.uikit.prebuilt.call.core.CallInvitationServiceImpl;
import com.zegocloud.uikit.prebuilt.call.core.invite.ZegoCallInvitationData;
import com.zegocloud.uikit.prebuilt.call.core.push.ZIMPushMessage;
import com.zegocloud.uikit.prebuilt.call.invite.internal.CallInviteActivity;
import com.zegocloud.uikit.service.defines.ZegoUIKitUser;
import im.zego.uikit.libuikitreport.ReportUtil;
import im.zego.zpns.ZPNsMessageReceiver;
import im.zego.zpns.entity.ZPNsMessage;
import im.zego.zpns.entity.ZPNsRegisterMessage;
import im.zego.zpns.enums.ZPNsConstants.PushSource;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import timber.log.Timber;

public class MyZPNsReceiver extends ZPNsMessageReceiver {

    @Override
    protected void onThroughMessageReceived(Context context, ZPNsMessage message) {
        ZIMPushMessage pushMessage = getZIMPushMessage(message);
        Activity topActivity = CallInvitationServiceImpl.getInstance().getTopActivity();

        Timber.d(
            "onThroughMessageReceived() called with: topActivity = [" + topActivity + "], ZPNsMessage = [" + message
                + "]");
        if (message.getPushSource() == PushSource.FCM) {
            if (TextUtils.isEmpty(pushMessage.invitationID)) { // not zim push message.
                Timber.d(
                    "pushMessage.invitationID = [" + pushMessage.invitationID + "],messages from other push.");
                String action = "com.zegocloud.zegouikit.call.fcm";
                String receiver = findReceiver(context, action, context.getPackageName());
                if (!TextUtils.isEmpty(receiver)) {
                    com.google.firebase.messaging.RemoteMessage remoteMessage = (com.google.firebase.messaging.RemoteMessage) message.getPushMessage();
                    Intent intent = new Intent(action);
                    intent.putExtra("remoteMessage", remoteMessage);
                    intent.setComponent(new ComponentName(context.getPackageName(), receiver));
                    context.sendBroadcast(intent);
                }
            } else {
                Timber.d("pushMessage.payLoad = [" + pushMessage.payLoad + "]");
                // else we assume that app is not started,offline message is effective
                if (TextUtils.isEmpty(pushMessage.payLoad)) {    //Empty payLoad means cancel call
                    ZIMPushMessage zimPushMessage = CallInvitationServiceImpl.getInstance().getZIMPushMessage();

                    // cancel call
                    ZegoUIKitUser localUser = CallInvitationServiceImpl.getInstance().getLocalUser();
                    if (topActivity instanceof CallInviteActivity && localUser == null) {
                        // show offline full on lockscreen
                        topActivity.finishAndRemoveTask();
                    }
                    if (zimPushMessage != null && Objects.equals(pushMessage.invitationID,
                        zimPushMessage.invitationID)) {
                        CallInvitationServiceImpl.getInstance().dismissCallNotification();
                        CallInvitationServiceImpl.getInstance().clearPushMessage();

                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("call_id", pushMessage.invitationID);
                        hashMap.put("app_state", "restarted");
                        hashMap.put("action", "inviterCancel");
                        ReportUtil.reportEvent("call/respondInvitation", hashMap);

                    }
                } else {
                    // if app have background activity,we assume that app has already login in.In this
                    // case,offline message is ignored.
                    if (topActivity != null) {
                        Timber.d(
                            "receive offline call but topActivity is not null,ignore this time");
                        return;
                    }
                    ZegoCallInvitationData callInvitationData = CallInvitationServiceImpl.getInstance()
                        .getCallInvitationData();
                    if (callInvitationData == null) {
                        CallInvitationServiceImpl.getInstance().setZIMPushMessage(pushMessage);
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("call_id", pushMessage.invitationID);
                        hashMap.put("app_state", "restarted");
                        hashMap.put("extended_data", pushMessage.payLoad);
                        ReportUtil.reportEvent("call/invitationReceived", hashMap);
                        CallInvitationServiceImpl.getInstance().showCallNotification();
                    }
                }
            }
        }
    }

    @Override
    protected void onNotificationClicked(Context context, ZPNsMessage message) {
        Timber.d("onNotificationClicked() called with: context = [" + context + "], message = [" + message + "]");
    }

    private String findReceiver(Context context, String action, String packageName) {
        if (!TextUtils.isEmpty(action) && !TextUtils.isEmpty(packageName)) {
            Intent var4 = new Intent(action);
            var4.setPackage(packageName);
            List resolveList = context.getPackageManager().queryBroadcastReceivers(var4, 0);
            return resolveList != null && resolveList.size() > 0 ? ((ResolveInfo) resolveList.get(0)).activityInfo.name
                : null;
        } else {
            return null;
        }
    }

    @Override
    protected void onNotificationArrived(Context context, ZPNsMessage message) {
        Timber.d("onNotificationArrived() called with: context = [" + context + "], message = [" + message + "]");
    }

    @Override
    protected void onRegistered(Context context, ZPNsRegisterMessage message) {
        Timber.d("onRegistered() called with: context = [" + context + "], message = [" + message + "]");
    }

    static public ZIMPushMessage getZIMPushMessage(ZPNsMessage message) {
        ZIMPushMessage pushMessage = null;
        switch (message.getPushSource()) {
            case FCM: {
                com.google.firebase.messaging.RemoteMessage remoteMessage = (com.google.firebase.messaging.RemoteMessage) message.getPushMessage();
                String payload = remoteMessage.getData().get("payload");
                String title = remoteMessage.getData().get("title");
                String body = remoteMessage.getData().get("body");
                String invitationID = remoteMessage.getData().get("call_id");
                pushMessage = new ZIMPushMessage(invitationID, title, body, payload);
            }
            break;
        }
        return pushMessage;
    }
}
