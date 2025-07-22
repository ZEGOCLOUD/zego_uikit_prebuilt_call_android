package com.zegocloud.uikit.prebuilt.call.core.notification;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.text.TextUtils;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import com.google.gson.Gson;
import com.zegocloud.uikit.plugin.invitation.ZegoInvitationType;
import com.zegocloud.uikit.prebuilt.call.core.CallInvitationServiceImpl;
import com.zegocloud.uikit.prebuilt.call.core.invite.PrebuiltCallInviteExtendedData;
import com.zegocloud.uikit.prebuilt.call.core.invite.ZegoCallInvitationData;
import com.zegocloud.uikit.prebuilt.call.core.invite.ui.CallRouteActivity;
import com.zegocloud.uikit.prebuilt.call.core.push.ZIMPushMessage;
import com.zegocloud.uikit.prebuilt.call.core.utils.Storage;
import com.zegocloud.uikit.prebuilt.call.invite.ZegoUIKitPrebuiltCallInvitationConfig;
import com.zegocloud.uikit.prebuilt.call.invite.internal.CallInviteActivity;
import com.zegocloud.uikit.prebuilt.call.invite.internal.ZegoTranslationText;
import im.zego.uikit.libuikitreport.ReportUtil;
import java.util.HashMap;
import timber.log.Timber;

public class PrebuiltCallNotificationManager {

    public static String ACTION_ACCEPT_CALL = "notification_accept";
    public static String ACTION_DECLINE_CALL = "notification_decline";
    public static String ACTION_NOTIFICATION_CLEARED = "notification_cleared";
    public static String ACTION_CLICK = "notification_click";
    public static String SHOW_ON_LOCK_SCREEN = "show_full_on_lock_screen";

    public static final int incoming_call_notification_id = 23432;
    public static final String incoming_call_channel_id = "Incoming_Call";
    private static final String incoming_call_channel_name = "Channel for Incoming_Call";
    private static final String incoming_call_channel_desc = "Incoming_Call";
    private static final int TIMEOUT_AFTER = 30000;
    private static final String DEFAULT_INCOMING_RINGTONE = "zego_incoming";

    private boolean isNotificationShowed;
    private Handler handler = new Handler(Looper.getMainLooper());

    public void showCallNotification(Context context) {
        boolean hasNotificationPermission = true;
        if (Build.VERSION.SDK_INT >= 33) {
            hasNotificationPermission =
                ContextCompat.checkSelfPermission(context, "android.permission.POST_NOTIFICATIONS")
                    == PackageManager.PERMISSION_GRANTED;
        }

        boolean notificationsEnabled = NotificationManagerCompat.from(context).areNotificationsEnabled();
        Timber.d("showCallNotification, hasNotificationPermission = %b,notificationsEnabled = %b", hasNotificationPermission, notificationsEnabled);
        if (hasNotificationPermission && notificationsEnabled) {

            isNotificationShowed = true;
            Notification callNotification = createCallNotification(context);

            NotificationManagerCompat.from(context).notify(incoming_call_notification_id, callNotification);

            wakeLockScreen(context);
        }
    }

    public void wakeLockScreen(Context context) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WAKE_LOCK) == PackageManager.PERMISSION_GRANTED) {
            PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            PowerManager.WakeLock wakeLock = powerManager.newWakeLock(
                PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "MyApp::MyWakeLockTag");

            wakeLock.acquire(3000);

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    wakeLock.release();
                }
            }, 5000);
        }
    }

    public static String getBackgroundNotificationMessage(boolean isVideoCall, boolean isGroup) {
        String notificationMessage = "";
        ZegoUIKitPrebuiltCallInvitationConfig callInvitationConfig = CallInvitationServiceImpl.getInstance()
            .getCallInvitationConfig();
        if (callInvitationConfig != null && callInvitationConfig.translationText != null) {
            ZegoTranslationText innerText = callInvitationConfig.translationText;
            if (isVideoCall) {
                notificationMessage =
                    isGroup ? innerText.incomingGroupVideoCallDialogMessage : innerText.incomingVideoCallDialogMessage;
            } else {
                notificationMessage =
                    isGroup ? innerText.incomingGroupVoiceCallDialogMessage : innerText.incomingVoiceCallDialogMessage;
            }
        }
        return notificationMessage;
    }

    public static String getBackgroundNotificationTitle(boolean isVideoCall, boolean isGroup, String userName) {
        String notificationTitle = "";
        ZegoUIKitPrebuiltCallInvitationConfig callInvitationConfig = CallInvitationServiceImpl.getInstance()
            .getCallInvitationConfig();
        if (callInvitationConfig != null && callInvitationConfig.translationText != null) {
            ZegoTranslationText translationText = callInvitationConfig.translationText;
            if (isVideoCall) {
                notificationTitle = isGroup ? String.format(translationText.incomingGroupVideoCallDialogTitle, userName)
                    : String.format(translationText.incomingVideoCallDialogTitle, userName);
            } else {
                String incomingVoiceCallDialogTitle = translationText.incomingVoiceCallDialogTitle;
                notificationTitle = isGroup ? String.format(translationText.incomingGroupVoiceCallDialogTitle, userName)
                    : String.format(incomingVoiceCallDialogTitle, userName);
            }
        }

        if (TextUtils.isEmpty(notificationTitle)) {
            notificationTitle = userName;
        }

        return notificationTitle;
    }

    public void createCallNotificationChannel(Context context, ZegoUIKitPrebuiltCallInvitationConfig invitationConfig) {
        String channelID;
        String channelName;
        String channelDesc;
        // default is config.ringtone.
        Uri soundUri = RingtoneManager.getIncomingUri();
        if (invitationConfig == null || invitationConfig.notificationConfig == null) {
            channelID = incoming_call_channel_id;
            channelName = incoming_call_channel_name;
            channelDesc = incoming_call_channel_desc;
        } else {
            // if custom notificationConfig sound, apply it.
            channelID = invitationConfig.notificationConfig.channelID;
            channelName = invitationConfig.notificationConfig.channelName;
            channelDesc = invitationConfig.notificationConfig.channelDesc;
            String soundName = invitationConfig.notificationConfig.sound;
            String rawSoundName = getSoundName(soundName);
            int identifier = context.getResources().getIdentifier(rawSoundName, "raw", context.getPackageName());
            soundUri = RingtoneManager.getUriFromID(context, identifier);
        }

        NotificationChannel channel = NotificationUtil.generateCallChannel(channelID, channelName, channelDesc,
            soundUri);
        NotificationManagerCompat.from(context).createNotificationChannel(channel);

        Storage.set_channelID(channelID);
        Storage.set_ringtone(soundUri.toString());
    }

    public static String getSoundName(String sound) {
        if (TextUtils.isEmpty(sound)) {
            return DEFAULT_INCOMING_RINGTONE;
        }
        String[] splits = sound.split("\\.");
        String suffixStr = "";
        if (splits != null && splits.length > 1) {
            suffixStr = sound.substring(0, sound.length() - (splits[splits.length - 1].length() + 1));
        } else {
            suffixStr = sound;
        }
        return suffixStr;
    }

    public void dismissCallNotification(Context context) {
        Timber.d("dismissCallNotification() called with: isNotificationShowed = [" + isNotificationShowed + "]");
        if (isNotificationShowed) {
            isNotificationShowed = false;
            NotificationManagerCompat.from(context).cancel(incoming_call_notification_id);
        }
    }

    public Notification createCallNotification(Context context) {
        String title;
        String body;
        boolean isVideoCall;

        String app_state;
        String call_id;

        ZIMPushMessage zimPushMessage = CallInvitationServiceImpl.getInstance().getZIMPushMessage();
        if (zimPushMessage == null) {
            ZegoCallInvitationData invitationData = CallInvitationServiceImpl.getInstance().getCallInvitationData();
            isVideoCall = invitationData.type == ZegoInvitationType.VIDEO_CALL.getValue();
            boolean isGroup = invitationData.invitees.size() > 1;
            title = getBackgroundNotificationTitle(isVideoCall, isGroup, invitationData.inviter.userName);
            body = getBackgroundNotificationMessage(isVideoCall, isGroup);

            app_state = "background";
            call_id = invitationData.invitationID;
        } else {
            Gson gson = new Gson();
            PrebuiltCallInviteExtendedData extendedData = gson.fromJson(zimPushMessage.payLoad,
                PrebuiltCallInviteExtendedData.class);
            isVideoCall = extendedData.getType() == ZegoInvitationType.VIDEO_CALL.getValue();
            title = zimPushMessage.title;
            body = zimPushMessage.body;

            app_state = "restarted";
            call_id = zimPushMessage.invitationID;
        }
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("call_id", call_id);
        hashMap.put("app_state", app_state);
        ReportUtil.reportEvent("call/displayNotification", hashMap);

        ZegoUIKitPrebuiltCallInvitationConfig invitationConfig = CallInvitationServiceImpl.getInstance()
            .getCallInvitationConfig();
        String channelID = Storage.channelID();
        if (channelID == null) {
            if (invitationConfig != null && invitationConfig.notificationConfig != null) {
                channelID = invitationConfig.notificationConfig.channelID;
            } else {
                channelID = incoming_call_channel_id;
            }
        }

        PendingIntent clickIntent = getClickIntent(context);
        PendingIntent acceptIntent = getAcceptIntent(context);
        PendingIntent declineIntent = getDeclineIntent(context);
        PendingIntent lockScreenIntent = getLockScreenIntent(context);
        //        PendingIntent deleteIntent = getDeleteIntent(context);

        return NotificationUtil.generateNotification(context, channelID, title, body, isVideoCall, TIMEOUT_AFTER * 2,
            declineIntent, acceptIntent, clickIntent, null, lockScreenIntent);
    }

    private PendingIntent getAcceptIntent(Context context) {
        Intent intent = CallRouteActivity.getAcceptIntent(context);
        return NotificationUtil.getPendingActivityIntent(context, intent);
    }

    //    private PendingIntent getDeleteIntent(Context context) {
    //        Intent intent = new Intent(context, NotificationActionReceiver.class);
    //        intent.setAction(context.getPackageName() + "." + ACTION_NOTIFICATION_CLEARED);
    //        return NotificationUtil.getPendingBroadcastIntent(context, intent);
    //    }

    private PendingIntent getDeclineIntent(Context context) {
        Intent intent = CallRouteActivity.getDeclineIntent(context);
        return NotificationUtil.getPendingActivityIntent(context, intent);
    }

    private PendingIntent getClickIntent(Context context) {
        Intent intent = CallRouteActivity.getContentIntent(context);
        return NotificationUtil.getPendingActivityIntent(context, intent);
    }

    private PendingIntent getLockScreenIntent(Context context) {
        ZegoCallInvitationData invitationData = CallInvitationServiceImpl.getInstance().getCallInvitationData();
        Intent intent;
        if (invitationData == null) {
            intent = CallInviteActivity.getPageIntent(context, CallInviteActivity.PAGE_LOCKSCREEN, null);
        } else {
            intent = CallInviteActivity.getPageIntent(context, CallInviteActivity.PAGE_INCOMING, null);
        }
        //  use CallRouteActivity.getLockScreenIntent will not working
        //        Intent intent = CallRouteActivity.getLockScreenIntent(context);
        return NotificationUtil.getPendingActivityIntent(context, intent);
    }
}
