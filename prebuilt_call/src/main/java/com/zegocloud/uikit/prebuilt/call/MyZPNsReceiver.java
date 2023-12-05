package com.zegocloud.uikit.prebuilt.call;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import com.tencent.mmkv.MMKV;
import com.zegocloud.uikit.prebuilt.call.invite.ZegoCallInvitationData;
import com.zegocloud.uikit.prebuilt.call.invite.internal.CallInvitationServiceImpl;
import com.zegocloud.uikit.prebuilt.call.invite.internal.ZIMPushMessage;
import im.zego.zpns.ZPNsMessageReceiver;
import im.zego.zpns.entity.ZPNsMessage;
import im.zego.zpns.entity.ZPNsRegisterMessage;
import timber.log.Timber;

public class MyZPNsReceiver extends ZPNsMessageReceiver {

    @Override
    protected void onThroughMessageReceived(Context context, ZPNsMessage message) {
        MMKV.initialize(context);
        ZIMPushMessage pushMessage = getZIMPushMessage(message);

        ZIMPushMessage zimPushMessage = CallInvitationServiceImpl.getInstance().getZIMPushMessage();
        Activity topActivity = CallInvitationServiceImpl.getInstance().getTopActivity();
        Timber.d("onThroughMessageReceived() called with: topActivity = [" + topActivity + "], message = [" + message + "]");
        // if app have background activity,we assume that app has already login in.In this
        // case,offline message is ignored.
        // else we assume that app is not started,offline message is effective
        if (topActivity == null) {
            if (pushMessage != null) {
                if (TextUtils.isEmpty(pushMessage.payLoad)) {
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

    @Override
    protected void onNotificationClicked(Context context, ZPNsMessage message) {
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
