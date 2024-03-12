package com.zegocloud.uikit.prebuilt.call.invite.internal;

import android.app.Activity;
import android.app.Notification;
import android.app.Notification.Builder;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.text.TextUtils;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationCompat.Action;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.IconCompat;
import com.tencent.mmkv.MMKV;
import com.zegocloud.uikit.plugin.invitation.ZegoInvitationType;
import com.zegocloud.uikit.prebuilt.call.R;
import com.zegocloud.uikit.prebuilt.call.invite.OffLineCallNotificationService;
import com.zegocloud.uikit.prebuilt.call.invite.ZegoUIKitPrebuiltCallInvitationConfig;
import java.util.List;
import timber.log.Timber;

public class CallNotificationManager {

    public static final String ACTION_ACCEPT_CALL = "accept";
    public static final String ACTION_DECLINE_CALL = "decline";
    public static final String ACTION_CLICK = "click";
    public static final String SHOW_FULL_ON_LOCK_SCREEN = "show_full_on_lock_screen";

    public static final int callNotificationID = 23432;
    public static final String callNotificationChannelID = "call_notification_id";
    private static final String callNotificationChannelName = "call_notification_name";
    private static final String callNotificationChannelDesc = "call_notification_desc";

    private boolean isNotificationShowed;

    public void showCallNotification(Context context) {
        Timber.d("showCallNotification() called with: context = [" + context + "]");
        ContextCompat.startForegroundService(context, new Intent(context, OffLineCallNotificationService.class));
        isNotificationShowed = true;
    }

    public void showCallBackgroundNotification(Context context) {
        Timber.d("showCallBackgroundNotification() called with: context = [" + context + "]");
        Notification callNotification = CallInvitationServiceImpl.getInstance().getCallNotification(context);
        NotificationManagerCompat.from(context).notify(callNotificationID, callNotification);
        isNotificationShowed = true;
    }

    public String getBackgroundNotificationMessage(boolean isVideoCall, boolean isGroup) {
        String notificationMessage = "";
        ZegoUIKitPrebuiltCallInvitationConfig callInvitationConfig = CallInvitationServiceImpl.getInstance()
            .getCallInvitationConfig();
        if (callInvitationConfig != null && callInvitationConfig.innerText != null) {
            ZegoInnerText innerText = CallInvitationServiceImpl.getInstance().getCallInvitationConfig().innerText;
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

    public String getBackgroundNotificationTitle(boolean isVideoCall, boolean isGroup, String userName) {
        String notificationTitle = "";
        ZegoUIKitPrebuiltCallInvitationConfig callInvitationConfig = CallInvitationServiceImpl.getInstance()
            .getCallInvitationConfig();
        if (callInvitationConfig != null && callInvitationConfig.innerText != null) {
            ZegoInnerText innerText = callInvitationConfig.innerText;
            if (isVideoCall) {
                notificationTitle = isGroup ? String.format(innerText.incomingGroupVideoCallDialogTitle, userName)
                    : String.format(innerText.incomingVideoCallDialogTitle, userName);
            } else {
                notificationTitle = isGroup ? String.format(innerText.incomingGroupVoiceCallDialogTitle, userName)
                    : String.format(innerText.incomingVoiceCallDialogTitle, userName);
            }
        }

        if (TextUtils.isEmpty(notificationTitle)) {
            notificationTitle = userName;
        }

        return notificationTitle;
    }

    public void createCallNotificationChannel(Context context) {
        ZegoUIKitPrebuiltCallInvitationConfig invitationConfig = CallInvitationServiceImpl.getInstance()
            .getCallInvitationConfig();
        String channelID;
        String channelName;
        String channelDesc;
        Uri ringtone = RingtoneManager.getIncomingUri();
        if (invitationConfig == null || invitationConfig.notificationConfig == null) {
            channelID = callNotificationChannelID;
            channelName = callNotificationChannelName;
            channelDesc = callNotificationChannelDesc;
        } else {
            channelID = invitationConfig.notificationConfig.channelID;
            channelName = invitationConfig.notificationConfig.channelName;
            channelDesc = channelName;
            String soundName = invitationConfig.notificationConfig.sound;
            if (!TextUtils.isEmpty(soundName)) {
                ringtone = RingtoneManager.getUriFromRaw(context, getSoundName(soundName));
            }
        }

        if (Build.VERSION.SDK_INT >= VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelID, channelName,
                NotificationManager.IMPORTANCE_HIGH);
            channel.setSound(ringtone, null);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            channel.setDescription(channelDesc);
            NotificationManagerCompat.from(context).createNotificationChannel(channel);
        }
        MMKV.defaultMMKV().putString("channelID", channelID);
    }

    public static String getSoundName(String sound) {
        if (TextUtils.isEmpty(sound)) {
            return "zego_incoming";
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
        Intent intent = new Intent(context, OffLineCallNotificationService.class);
        isNotificationShowed = false;
        context.stopService(intent);

        NotificationManagerCompat.from(context).cancel(callNotificationID);
    }

    public boolean isCallNotificationShowed() {
        return isNotificationShowed;
    }

    private String getLauncherActivity(Context context) {
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setPackage(context.getPackageName());
        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> info = pm.queryIntentActivities(intent, 0);
        if (info == null || info.size() == 0) {
            return "";
        }
        return info.get(0).activityInfo.name;
    }

    public Notification createCallNotification(Context context) {
        String title;
        String body;
        ZIMPushMessage zimPushMessage = CallInvitationServiceImpl.getInstance().getZIMPushMessage();
        if (zimPushMessage == null) {
            ZegoCallInvitationData invitationData = CallInvitationServiceImpl.getInstance().getCallInvitationData();
            boolean isVideoCall = invitationData.type == ZegoInvitationType.VIDEO_CALL.getValue();
            boolean isGroup = invitationData.invitees.size() > 1;
            title = getBackgroundNotificationTitle(isVideoCall, isGroup, invitationData.inviter.userName);
            body = getBackgroundNotificationMessage(isVideoCall, isGroup);
        } else {
            title = zimPushMessage.title;
            body = zimPushMessage.body;
        }

        ZegoUIKitPrebuiltCallInvitationConfig invitationConfig = CallInvitationServiceImpl.getInstance()
            .getCallInvitationConfig();
        String channelID = MMKV.defaultMMKV().getString("channelID", null);
        if (channelID == null) {
            if (invitationConfig != null && invitationConfig.notificationConfig != null) {
                channelID = invitationConfig.notificationConfig.channelID;
            } else {
                channelID = callNotificationChannelID;
            }
        }

        PendingIntent clickIntent = getClickIntent(context);
        PendingIntent acceptIntent = getAcceptIntent(context);
        PendingIntent declineIntent = getDeclineIntent(context);

        boolean canShowFullOnLockScreen = CallInvitationServiceImpl.getInstance().canShowFullOnLockScreen();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {

            Notification.Builder builder = new Builder(context, channelID).setSmallIcon(
                    R.drawable.call_icon_chat_normal) //// A small icon that will be displayed in the status bar
                .setContentTitle(title)   // Notification text, usually the caller’s name
                .setContentText(body).setContentIntent(clickIntent).setVisibility(Notification.VISIBILITY_PUBLIC)
                .setCategory(NotificationCompat.CATEGORY_CALL).setOngoing(true)
                .setAutoCancel(true);
            Activity topActivity = CallInvitationServiceImpl.getInstance().getTopActivity();

            if (zimPushMessage == null && topActivity != null && canShowFullOnLockScreen) {
                PendingIntent lockScreenIntent = getLockScreenIntent(context);
                builder.setFullScreenIntent(lockScreenIntent, true);

                //callStyle need foreground service or fullscreen intent
                android.app.Person caller = new android.app.Person.Builder().setName(title).setImportant(true).build();
                Notification.CallStyle callStyle = Notification.CallStyle.forIncomingCall(caller, declineIntent,
                    acceptIntent);
                builder.setStyle(callStyle);
            }else {
                Notification.Action.Builder acceptAction = new Notification.Action.Builder(
                    // The icon that will be displayed on the button (or not, depends on the Android version)
                    Icon.createWithResource(context, R.drawable.call_selector_dialog_voice_accept),
                    // The text on the button
                    context.getString(R.string.call_page_action_accept), acceptIntent);

                Notification.Action.Builder declineAction = new Notification.Action.Builder(
                    // The icon that will be displayed on the button (or not, depends on the Android version)
                    Icon.createWithResource(context,
                        com.zegocloud.uikit.R.drawable.zego_uikit_icon_dialog_voice_decline),
                    // The text on the button
                    context.getString(R.string.call_page_action_decline), declineIntent);

                builder.addAction(acceptAction.build());
                builder.addAction(declineAction.build());
            }
            return builder.build();
        } else {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelID).setSmallIcon(
                    R.drawable.call_icon_chat_normal) //// A small icon that will be displayed in the status bar
                .setContentTitle(title)   // Notification text, usually the caller’s name
                .setContentText(body).setContentIntent(clickIntent).setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC).setCategory(NotificationCompat.CATEGORY_CALL)
                .setOngoing(true).setAutoCancel(true);
            Activity topActivity = CallInvitationServiceImpl.getInstance().getTopActivity();
            if (zimPushMessage == null && topActivity != null && canShowFullOnLockScreen) {
                PendingIntent lockScreenIntent = getLockScreenIntent(context);
                builder.setFullScreenIntent(lockScreenIntent, true);
            }
            NotificationCompat.Action.Builder acceptAction = new Action.Builder(
                // The icon that will be displayed on the button (or not, depends on the Android version)
                IconCompat.createWithResource(context, R.drawable.call_selector_dialog_voice_accept),
                // The text on the button
                context.getString(R.string.call_page_action_accept), acceptIntent);

            NotificationCompat.Action.Builder declineAction = new Action.Builder(
                // The icon that will be displayed on the button (or not, depends on the Android version)
                IconCompat.createWithResource(context,
                    com.zegocloud.uikit.R.drawable.zego_uikit_icon_dialog_voice_decline),
                // The text on the button
                context.getString(R.string.call_page_action_decline), declineIntent);

            builder.addAction(acceptAction.build());
            builder.addAction(declineAction.build());
            return builder.build();
        }
    }

    private PendingIntent getAcceptIntent(Context context) {

        ZIMPushMessage zimPushMessage = CallInvitationServiceImpl.getInstance().getZIMPushMessage();
        Activity topActivity = CallInvitationServiceImpl.getInstance().getTopActivity();
        if (zimPushMessage == null && topActivity != null) {
            Intent intent = new Intent(context, CallInviteActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString("page", "call");
            intent.putExtra("bundle", bundle);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setAction(ACTION_ACCEPT_CALL);

            PendingIntent openIntent;
            if (Build.VERSION.SDK_INT >= VERSION_CODES.M) {
                openIntent = PendingIntent.getActivity(context, 0, intent,
                    PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
            } else {
                openIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            }
            return openIntent;
        } else {

            Intent intent = new Intent(context, OffLineCallNotificationService.class);
            intent.setAction(ACTION_ACCEPT_CALL);

            PendingIntent pendingIntent;
            if (Build.VERSION.SDK_INT >= VERSION_CODES.M) {
                pendingIntent = PendingIntent.getService(context, 0, intent,
                    PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
            } else {
                pendingIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            }
            return pendingIntent;

            //             if use offline service,click notification will not dismiss notification dialog,so we
            //             go to activity directly here
            //            Intent intent = new Intent();
            //            try {
            //                intent = new Intent(context, Class.forName(getLauncherActivity(context)));
            //            } catch (ClassNotFoundException e) {
            //                e.printStackTrace();
            //            }
            //            intent.setAction(ACTION_ACCEPT_CALL);
            //            PendingIntent pendingIntent;
            //            if (Build.VERSION.SDK_INT >= VERSION_CODES.M) {
            //                pendingIntent = PendingIntent.getActivity(context, 0, intent,
            //                    PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
            //            } else {
            //                pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            //            }
            //            return pendingIntent;
        }
    }


    private PendingIntent getDeclineIntent(Context context) {
        Intent intent = new Intent(context, OffLineCallNotificationService.class);
        intent.setAction(ACTION_DECLINE_CALL);

        PendingIntent pendingIntent;
        if (Build.VERSION.SDK_INT >= VERSION_CODES.M) {
            pendingIntent = PendingIntent.getService(context, 0, intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        } else {
            pendingIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }
        return pendingIntent;
    }

    private PendingIntent getClickIntent(Context context) {
        ZIMPushMessage zimPushMessage = CallInvitationServiceImpl.getInstance().getZIMPushMessage();
        Activity topActivity = CallInvitationServiceImpl.getInstance().getTopActivity();
        if (zimPushMessage == null && topActivity != null) {
            Intent intent = new Intent(context, CallInviteActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString("page", "incoming");
            intent.putExtra("bundle", bundle);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setAction(ACTION_CLICK);
            PendingIntent openIntent;
            if (Build.VERSION.SDK_INT >= VERSION_CODES.M) {
                openIntent = PendingIntent.getActivity(context, 0, intent,
                    PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
            } else {
                openIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            }
            return openIntent;
        } else {
            Intent intent = new Intent(context, OffLineCallNotificationService.class);
            intent.setAction(ACTION_CLICK);

            PendingIntent openIntent;
            if (Build.VERSION.SDK_INT >= VERSION_CODES.M) {
                openIntent = PendingIntent.getService(context, 0, intent,
                    PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
            } else {
                openIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            }
            return openIntent;
        }

    }

    private PendingIntent getLockScreenIntent(Context context) {
        ZIMPushMessage zimPushMessage = CallInvitationServiceImpl.getInstance().getZIMPushMessage();
        Activity topActivity = CallInvitationServiceImpl.getInstance().getTopActivity();
        if (zimPushMessage == null && topActivity != null) {
            Intent intent = new Intent(context, CallInviteActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString("page", "incoming");
            intent.putExtra("bundle", bundle);
            intent.setAction(SHOW_FULL_ON_LOCK_SCREEN);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            PendingIntent openIntent;
            if (Build.VERSION.SDK_INT >= VERSION_CODES.M) {
                openIntent = PendingIntent.getActivity(context, 0, intent,
                    PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
            } else {
                openIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            }
            return openIntent;
        }
        return null;
    }
}
