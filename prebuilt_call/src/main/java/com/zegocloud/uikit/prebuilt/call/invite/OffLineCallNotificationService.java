package com.zegocloud.uikit.prebuilt.call.invite;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import com.zegocloud.uikit.prebuilt.call.invite.internal.CallInvitationServiceImpl;
import com.zegocloud.uikit.prebuilt.call.invite.internal.CallNotificationManager;
import com.zegocloud.uikit.prebuilt.call.invite.internal.ZIMPushMessage;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;
import timber.log.Timber;

/**
 * foreground service, only used to keep process foreground to receive messages
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
                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put("reason", "decline");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    CallInvitationServiceImpl.getInstance()
                        .refuseInvitation(callInvitationData.inviter.userID, jsonObject.toString(), null);
                }
            } else {
                CallInvitationServiceImpl.getInstance()
                    .setNotificationClickAction(intent.getAction(), zimPushMessage.invitationID);
                CallInvitationServiceImpl.getInstance().autoInitAndLoginUser(getApplication());
                CallInvitationServiceImpl.getInstance().parsePayload();
            }
            CallInvitationServiceImpl.getInstance().dismissCallNotification(getApplicationContext());
        } else if (CallNotificationManager.ACTION_CLICK.equals(intent.getAction())) {
            // click will start service ,then start app,inject action here
            // while click accept button will start app directly
            CallInvitationServiceImpl.getInstance()
                .setNotificationClickAction(intent.getAction(), zimPushMessage.invitationID);
            startApp();
            CallInvitationServiceImpl.getInstance().dismissCallNotification(getApplicationContext());
        } else if (CallNotificationManager.ACTION_ACCEPT_CALL.equals(intent.getAction())) {
            CallInvitationServiceImpl.getInstance()
                .setNotificationClickAction(intent.getAction(), zimPushMessage.invitationID);
            startApp();
            CallInvitationServiceImpl.getInstance().dismissCallNotification(getApplicationContext());
        } else {
            Notification callNotification = CallInvitationServiceImpl.getInstance().getCallNotification(this);
            if (callNotification != null) {
                startForeground(CallNotificationManager.callNotificationID, callNotification);
            }
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    CallInvitationServiceImpl.getInstance().dismissCallNotification(getApplicationContext());
                    CallInvitationServiceImpl.getInstance().clearPushMessage();
                }
            }, 30000);
        }

        return super.onStartCommand(intent, flags, startId);
    }

    public void startApp() {
        Intent intent = null;
        try {
            intent = new Intent(this, Class.forName(getLauncherActivity()));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public String getLauncherActivity() {
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setPackage(getApplication().getPackageName());
        PackageManager pm = getApplication().getPackageManager();
        List<ResolveInfo> info = pm.queryIntentActivities(intent, 0);
        if (info == null || info.size() == 0) {
            return "";
        }
        return info.get(0).activityInfo.name;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);
    }
}