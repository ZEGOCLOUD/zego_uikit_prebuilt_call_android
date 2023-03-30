package com.zegocloud.uikit.prebuilt.call.invite.internal;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.zegocloud.uikit.prebuilt.call.R;

public class NotificationsUtils {

    public static final String chatChannelId = "com.zegocloud.uikit.prebuilt.call";

    @TargetApi(Build.VERSION_CODES.O)
    public static NotificationChannel createNotificationChannel(NotificationManager manager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelName = "offline";
            NotificationChannel channel;
            channel = new NotificationChannel(chatChannelId, channelName, NotificationManager.IMPORTANCE_HIGH);
            channel.canBypassDnd();//Is it possible to bypass Do Not Disturb mode
            channel.enableLights(false);//Whether to display the notification flashing light
            channel.setLockscreenVisibility(NotificationCompat.VISIBILITY_SECRET);
            channel.getAudioAttributes();//Get to set ringtone settings
            channel.enableVibration(false);
            channel.setVibrationPattern(new long[]{0});//Set vibration mode
            channel.setSound(null,null);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
            return channel;
        }
        return null;
    }

    public static class NotifyConfig {
        public String message;
        public String title;
        public String userId;
    }

    public static void showNotification(NotifyConfig notifyConfig) {
        Context context = CallInvitationServiceImpl.getInstance().getApplication();
        if(context == null){
            return;
        }
        NotificationCompat.Builder builder;
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context
                .NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = NotificationsUtils.createNotificationChannel(notificationManager);
            builder = new NotificationCompat.Builder(context, channel.getId());
        } else {
            builder = new NotificationCompat.Builder(context);
        }

        builder.setSmallIcon(R.drawable.smallicon_notification_background)
                .setContentTitle(notifyConfig.title)
                .setContentText(notifyConfig.message)
                .setAutoCancel(true);
        Notification notification = builder.build();

        Activity topActivity = CallInvitationServiceImpl.getInstance().appActivityManager.getTopActivity();
        Intent notificationIntent = new Intent(context, topActivity.getClass());
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            pendingIntent = PendingIntent.getActivity(context, notifyConfig.userId.hashCode(), notificationIntent, PendingIntent.FLAG_IMMUTABLE);
        } else {
            pendingIntent = PendingIntent.getActivity(context, notifyConfig.userId.hashCode(), notificationIntent, PendingIntent.FLAG_ONE_SHOT);
        }
        notification.contentIntent = pendingIntent;
        int notifyId = notifyConfig.userId.hashCode();
        notificationManager.notify(notifyId, notification);
    }

    public static void clearAllNotifications() {
        if(CallInvitationServiceImpl.getInstance().getApplication() != null){
            NotificationManager notificationManager = (NotificationManager)CallInvitationServiceImpl.getInstance().getApplication().getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancelAll();
        }
    }

}
