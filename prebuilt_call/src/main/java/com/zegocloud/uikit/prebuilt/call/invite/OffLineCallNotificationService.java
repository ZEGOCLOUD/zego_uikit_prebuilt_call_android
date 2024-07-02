package com.zegocloud.uikit.prebuilt.call.invite;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import androidx.core.app.NotificationManagerCompat;
import com.zegocloud.uikit.prebuilt.call.invite.internal.CallInvitationServiceImpl;
import com.zegocloud.uikit.prebuilt.call.invite.internal.CallNotificationManager;
import com.zegocloud.uikit.prebuilt.call.invite.internal.ZIMPushMessage;
import com.zegocloud.uikit.prebuilt.call.invite.internal.ZegoCallInvitationData;
import timber.log.Timber;

/**
 * foreground service, when receive fcm data,use this service to show a call-style Notification.
 */
public class OffLineCallNotificationService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            return super.onStartCommand(intent, flags, startId);
        }

        Timber.d("onStartCommand() called with: intent.getAction() = [" + intent.getAction() + "]");

        ZIMPushMessage zimPushMessage = CallInvitationServiceImpl.getInstance().getZIMPushMessage();
        if (CallNotificationManager.ACTION_DECLINE_CALL.equals(intent.getAction())) {
            if (zimPushMessage == null) {
                ZegoCallInvitationData callInvitationData = CallInvitationServiceImpl.getInstance()
                    .getCallInvitationData();
                if (callInvitationData != null) {
                    CallInvitationServiceImpl.getInstance().rejectInvitation(null);
                }
            } else {
                //after init will receive onInvitationReceived,and ACTION_DECLINE_CALL will cause rejectInvitation
                // and unInitToReceiveOffline to ready for offline invite again.
                CallInvitationServiceImpl.getInstance()
                    .setNotificationClickAction(intent.getAction(), zimPushMessage.invitationID);
                CallInvitationServiceImpl.getInstance().autoInitAndLoginUser(getApplication());
                CallInvitationServiceImpl.getInstance().parsePayload();
            }
            CallInvitationServiceImpl.getInstance().dismissCallNotification(getApplicationContext());
        } else if (CallNotificationManager.ACTION_CLICK.equals(intent.getAction())) {
            // click will start service ,then start app,inject action here
            // while click accept button will start app directly
            //            CallInvitationServiceImpl.getInstance().setNotificationClickAction(intent.getAction(), zimPushMessage.invitationID);
            //            startApp();
            //            CallInvitationServiceImpl.getInstance().dismissCallNotification(getApplicationContext());
        } else if (CallNotificationManager.ACTION_ACCEPT_CALL.equals(intent.getAction())) {
            //            CallInvitationServiceImpl.getInstance().setNotificationClickAction(intent.getAction(), zimPushMessage.invitationID);
            //            startApp();
            //            CallInvitationServiceImpl.getInstance().dismissCallNotification(getApplicationContext());
        } else {
            Notification callNotification = CallInvitationServiceImpl.getInstance().getCallNotification(this);
            if (callNotification != null) {
//                if (VERSION.SDK_INT >= VERSION_CODES.Q) {
//                    startForeground(CallNotificationManager.callNotificationID, callNotification,
//                        ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC);
//                } else {
//                    startForeground(CallNotificationManager.callNotificationID, callNotification);
//                }
                NotificationManagerCompat.from(this).notify(CallNotificationManager.callNotificationID, callNotification);
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }

//    @Override
    //    public void onDestroy() {
    //        super.onDestroy();
    //        stopForeground(true);
    //    }
}