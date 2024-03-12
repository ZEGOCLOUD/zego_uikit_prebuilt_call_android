package com.zegocloud.uikit.prebuilt.call;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.text.TextUtils;
import com.tencent.mmkv.MMKV;
import com.zegocloud.uikit.ZegoUIKit;
import com.zegocloud.uikit.prebuilt.call.invite.internal.ZegoCallInvitationData;
import com.zegocloud.uikit.prebuilt.call.invite.internal.CallInvitationServiceImpl;
import com.zegocloud.uikit.prebuilt.call.invite.internal.ZIMPushMessage;
import im.zego.zpns.ZPNsMessageReceiver;
import im.zego.zpns.entity.ZPNsMessage;
import im.zego.zpns.entity.ZPNsRegisterMessage;
import im.zego.zpns.enums.ZPNsConstants.PushSource;
import java.util.List;
import timber.log.Timber;

public class MyZPNsReceiver extends ZPNsMessageReceiver {

    @Override
    protected void onThroughMessageReceived(Context context, ZPNsMessage message) {

        ZIMPushMessage pushMessage = getZIMPushMessage(message);
        ZIMPushMessage zimPushMessage = CallInvitationServiceImpl.getInstance().getZIMPushMessage();
        Activity topActivity = CallInvitationServiceImpl.getInstance().getTopActivity();

        ZegoUIKit.debugMode(context);

        Timber.d("onThroughMessageReceived() called with: topActivity = [" + topActivity + "], pushMessage = ["
            + pushMessage + "]");

        if (message.getPushSource() == PushSource.FCM) {
            if (TextUtils.isEmpty(pushMessage.invitationID)) {
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
                MMKV.initialize(context);
                // if app have background activity,we assume that app has already login in.In this
                // case,offline message is ignored.
                // else we assume that app is not started,offline message is effective
                if (topActivity == null) {
                    if (TextUtils.isEmpty(pushMessage.payLoad)) {
                        // cancel call
                        if (zimPushMessage != null) {
                            if (pushMessage.invitationID.equals(zimPushMessage.invitationID)) {
                                CallInvitationServiceImpl.getInstance().dismissCallNotification(context);
                                CallInvitationServiceImpl.getInstance().clearPushMessage();
                            }
                        }
                    } else {
                        ZegoCallInvitationData callInvitationData = CallInvitationServiceImpl.getInstance()
                            .getCallInvitationData();
                        if (callInvitationData != null) {
                            if (pushMessage.invitationID.equals(callInvitationData.invitationID)) {
                                return;
                            }
                        } else {
                            CallInvitationServiceImpl.getInstance().setZIMPushMessage(pushMessage);
                            CallInvitationServiceImpl.getInstance().showCallNotification(context);
                        }
                    }
                }
            }
        }
    }

    @Override
    protected void onNotificationClicked(Context context, ZPNsMessage message) {
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
    }

    @Override
    protected void onRegistered(Context context, ZPNsRegisterMessage message) {
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
