package com.zegocloud.uikit.prebuilt.call.core.invite.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.zegocloud.uikit.plugin.common.PluginCallbackListener;
import com.zegocloud.uikit.prebuilt.call.core.CallInvitationServiceImpl;
import com.zegocloud.uikit.prebuilt.call.core.invite.ZegoCallInvitationData;
import com.zegocloud.uikit.prebuilt.call.core.notification.PrebuiltCallNotificationManager;
import com.zegocloud.uikit.prebuilt.call.core.push.ZIMPushMessage;
import com.zegocloud.uikit.prebuilt.call.invite.internal.CallInviteActivity;
import java.util.Map;
import java.util.Objects;
import timber.log.Timber;

public class CallRouteActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String action = getIntent().getAction();

        Timber.d("CallRouteActivity onCreate() called with: action = [" + action + "]");

        String actionAccept = getPackageName() + "." + PrebuiltCallNotificationManager.ACTION_ACCEPT_CALL;
        String actionClick = getPackageName() + "." + PrebuiltCallNotificationManager.ACTION_CLICK;
        String actionDecline = getPackageName() + "." + PrebuiltCallNotificationManager.ACTION_DECLINE_CALL;
        String actionLockScreen = getPackageName() + "." + PrebuiltCallNotificationManager.SHOW_ON_LOCK_SCREEN;

        ZegoCallInvitationData invitationData = CallInvitationServiceImpl.getInstance().getCallInvitationData();
        ZIMPushMessage zimPushMessage = CallInvitationServiceImpl.getInstance().getZIMPushMessage();
        CallInvitationServiceImpl.getInstance().dismissCallNotification();

        if (Objects.equals(action, actionAccept)) {
            if (invitationData != null) {
                Context context = CallRouteActivity.this;
                CallInvitationServiceImpl.getInstance().acceptInvitation(new PluginCallbackListener() {
                    @Override
                    public void callback(Map<String, Object> result) {
                        CallInviteActivity.startCallPage(context);
                    }
                });
            } else {
                if (zimPushMessage != null) {
                    CallInvitationServiceImpl.getInstance().setNotificationClickAction(action);

                    Intent intent = getPackageManager().getLaunchIntentForPackage(getPackageName());
                    startActivity(intent);
                }
            }
        } else if (Objects.equals(action, actionClick)) {
            if (invitationData != null) {
                CallInviteActivity.startIncomingPage(this);
            } else {
                if (zimPushMessage != null) {
                    CallInvitationServiceImpl.getInstance().setZIMPushMessage(null);

                    Intent intent = getPackageManager().getLaunchIntentForPackage(getPackageName());
                    startActivity(intent);
                }
            }
        } else if (Objects.equals(action, actionDecline)) {
            if (invitationData != null) {
                CallInvitationServiceImpl.getInstance().rejectInvitation(null);
            } else {
                if (zimPushMessage != null) {
                    CallInvitationServiceImpl.getInstance().setNotificationClickAction(action);
                    CallInvitationServiceImpl.getInstance().initAndLoginUserByLastRecord(this.getApplication());
                }
            }
        } else if (Objects.equals(action, actionLockScreen)) {
            if (invitationData != null) {
                CallInviteActivity.startIncomingPage(this);
            } else {
                if (zimPushMessage != null) {
                    CallInviteActivity.startLockScreenPage(this);
                }
            }
        }
        finish();

    }

    public static Intent getAcceptIntent(Context context) {
        Intent intent = new Intent(context, CallRouteActivity.class);
        intent.setPackage(context.getPackageName());
        String actionAccept = context.getPackageName() + "." + PrebuiltCallNotificationManager.ACTION_ACCEPT_CALL;
        intent.setAction(actionAccept);
        return intent;
    }

    public static Intent getContentIntent(Context context) {
        Intent intent = new Intent(context, CallRouteActivity.class);
        intent.setPackage(context.getPackageName());
        String actionClick = context.getPackageName() + "." + PrebuiltCallNotificationManager.ACTION_CLICK;
        intent.setAction(actionClick);
        return intent;
    }

    public static Intent getDeclineIntent(Context context) {
        Intent intent = new Intent(context, CallRouteActivity.class);
        intent.setPackage(context.getPackageName());
        String actionDecline = context.getPackageName() + "." + PrebuiltCallNotificationManager.ACTION_DECLINE_CALL;
        intent.setAction(actionDecline);
        return intent;
    }

    public static Intent getLockScreenIntent(Context context) {
        Intent intent = new Intent(context, CallRouteActivity.class);
        intent.setPackage(context.getPackageName());
        String actionLockScreen = context.getPackageName() + "." + PrebuiltCallNotificationManager.SHOW_ON_LOCK_SCREEN;
        intent.setAction(actionLockScreen);
        return intent;
    }


}