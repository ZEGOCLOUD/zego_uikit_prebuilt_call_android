package com.zegocloud.uikit.prebuilt.call.core.notification;

import android.app.Notification;
import android.app.Notification.Action;
import android.app.Notification.Builder;
import android.app.Notification.CallStyle;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Icon;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.widget.RemoteViews;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.IconCompat;
import com.zegocloud.uikit.prebuilt.call.R;
import java.util.Arrays;
import java.util.Optional;

public class NotificationUtil {

    private static final String KEY_ACTION_PRIORITY = "ACTION";
    private static final String ANDROID_PERMISSION_USE_FULL_SCREEN_INTENT = "android.permission.USE_FULL_SCREEN_INTENT";
    private static final String FOREGROUND_SERVICE = "android.permission.FOREGROUND_SERVICE";
    private static final String FOREGROUND_SERVICE_MEDIA_PROJECTION = "android.permission.FOREGROUND_SERVICE_MEDIA_PROJECTION";

    public static NotificationChannel generateCallChannel(String channelID, String channelName, String channelDesc,
        Uri sound) {
        NotificationChannel channel = new NotificationChannel(channelID, channelName,
            NotificationManager.IMPORTANCE_HIGH);
        channel.setDescription(channelDesc);
        if (sound != null) {
            AudioAttributes attribution = new AudioAttributes.Builder().setContentType(
                    AudioAttributes.CONTENT_TYPE_SONIFICATION).setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                .setLegacyStreamType(AudioManager.STREAM_RING).build();
            channel.setSound(sound, attribution);
        } else {
            channel.setSound(null, null);
        }
        channel.enableLights(true);
        channel.enableVibration(true);
        channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        return channel;
    }


    static Action makeNegativeAction(Context context, PendingIntent declineIntent, int icon) {
        //        int icon = R.drawable.google_ic_call_decline_low;
        //        if (Build.VERSION.SDK_INT >= 21) {
        //            icon = R.drawable.google_ic_call_decline;
        //        }
        //        if (declineIntent == null) {
        //            return makeAction(context, icon, R.string.call_notification_hang_up_action, declineButtonColor,
        //                R.color.call_notification_decline_color, mHangUpIntent);
        //        }
        //        else {
        return makeAction(context, icon, R.string.call_notification_decline_action, null,
            R.color.call_notification_decline_color, declineIntent);
        //        }
    }

    static Action makeAnswerAction(Context context, PendingIntent answerIntent, int icon) {
        //        int videoIcon = R.drawable.google_ic_call_answer_video_low;
        //        int icon = R.drawable.google_ic_call_answer_low;
        //        if (VERSION.SDK_INT >= 21) {
        //            videoIcon = R.drawable.google_ic_call_answer_video;
        //            icon = R.drawable.google_ic_call_answer;
        //        }

        return answerIntent == null ? null : makeAction(context, icon, R.string.call_notification_answer_action, null,
            R.color.call_notification_answer_color, answerIntent);
    }

    static Action makeAction(Context context, int icon, int title, Integer colorInt, int defaultColorRes,
        PendingIntent intent) {
        if (colorInt == null) {
            colorInt = ContextCompat.getColor(context, defaultColorRes);
        }
        SpannableStringBuilder stringBuilder = new SpannableStringBuilder();
        stringBuilder.append(context.getResources().getString(title));
        stringBuilder.setSpan(new ForegroundColorSpan(colorInt), 0, stringBuilder.length(),
            SpannableStringBuilder.SPAN_INCLUSIVE_INCLUSIVE);

        Action action = new Action.Builder(Icon.createWithResource(context, icon), stringBuilder, intent).build();
        action.getExtras().putBoolean(KEY_ACTION_PRIORITY, true);
        return action;
    }

    static NotificationCompat.Action makeNegativeCompatAction(Context context, PendingIntent declineIntent, int icon) {
        return makeCompatAction(context, icon, R.string.call_notification_decline_action, null,
            R.color.call_notification_decline_color, declineIntent);
    }

    static NotificationCompat.Action makeCompatAnswerAction(Context context, PendingIntent answerIntent, int icon) {
        return answerIntent == null ? null
            : makeCompatAction(context, icon, R.string.call_notification_answer_action, null,
                R.color.call_notification_answer_color, answerIntent);
    }

    static NotificationCompat.Action makeCompatAction(Context context, int icon, int title, Integer colorInt,
        int defaultColorRes, PendingIntent intent) {
        if (colorInt == null) {
            colorInt = ContextCompat.getColor(context, defaultColorRes);
        }
        SpannableStringBuilder stringBuilder = new SpannableStringBuilder();
        stringBuilder.append(context.getResources().getString(title));
        stringBuilder.setSpan(new ForegroundColorSpan(colorInt), 0, stringBuilder.length(),
            SpannableStringBuilder.SPAN_INCLUSIVE_INCLUSIVE);

        NotificationCompat.Action action = new NotificationCompat.Action.Builder(
            IconCompat.createWithResource(context, icon), stringBuilder, intent).build();
        action.getExtras().putBoolean(KEY_ACTION_PRIORITY, true);
        return action;
    }

    public static boolean hasFullScreenIntentPermissionDeclared(Context context) {
        String[] permissions = getManifestPermissions(context);
        Optional<String> optionalString = Arrays.stream(permissions)
            .filter(ANDROID_PERMISSION_USE_FULL_SCREEN_INTENT::equals).findAny();
        return optionalString.isPresent();
    }

    public static boolean hasForegroundServicePermissionDeclared(Context context) {
        String[] permissions = getManifestPermissions(context);
        Optional<String> optionalString = Arrays.stream(permissions).filter(FOREGROUND_SERVICE::equals).findAny();
        return optionalString.isPresent();
    }

    public static boolean hasMediaProjectionPermissionDeclared(Context context) {
        String[] permissions = getManifestPermissions(context);
        Optional<String> optionalString = Arrays.stream(permissions).filter(FOREGROUND_SERVICE_MEDIA_PROJECTION::equals)
            .findAny();
        return optionalString.isPresent();
    }

    private static String[] getManifestPermissions(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                .getPackageInfo(context.getPackageName(), PackageManager.GET_PERMISSIONS);
            if (packageInfo != null) {
                return packageInfo.requestedPermissions;
            }
        } catch (NameNotFoundException e) {
        }
        return null;
    }

    static PendingIntent getPendingServiceIntent(Context context, Intent intent) {
        PendingIntent openIntent;
        if (Build.VERSION.SDK_INT >= VERSION_CODES.M) {
            openIntent = PendingIntent.getService(context, 0, intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        } else {
            openIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }
        return openIntent;
    }

    static PendingIntent getPendingBroadcastIntent(Context context, Intent intent) {
        PendingIntent openIntent;
        if (Build.VERSION.SDK_INT >= VERSION_CODES.M) {
            openIntent = PendingIntent.getBroadcast(context, 0, intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        } else {
            openIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }
        return openIntent;
    }


    static PendingIntent getPendingActivityIntent(Context context, Intent intent) {
        PendingIntent openIntent;
        if (Build.VERSION.SDK_INT >= VERSION_CODES.M) {
            openIntent = PendingIntent.getActivity(context, 0, intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        } else {
            openIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }
        return openIntent;
    }

    public static Notification generateNotification(Context context, String channelID, String title, String body,
        boolean isVideoCall, long timeout, PendingIntent declineIntent, PendingIntent acceptIntent,
        PendingIntent clickIntent, PendingIntent deleteIntent, PendingIntent lockScreenIntent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if ("samsung".equalsIgnoreCase(Build.MANUFACTURER)) {
                return CustomImpl.generateNotification(context, channelID, title, body, isVideoCall, timeout,
                    declineIntent, acceptIntent, clickIntent, deleteIntent, lockScreenIntent);
            } else {
                return Api31Impl.generateNotification(context, channelID, title, body, isVideoCall, timeout,
                    declineIntent, acceptIntent, clickIntent, deleteIntent, lockScreenIntent);
            }
        } else {
            return CustomImpl.generateNotification(context, channelID, title, body, isVideoCall, timeout, declineIntent,
                acceptIntent, clickIntent, deleteIntent, lockScreenIntent);
        }
    }


    @RequiresApi(31)
    static class Api31Impl {

        static Notification generateNotification(Context context, String channelID, String title, String content,
            boolean isVideoCall, long timeout, PendingIntent declineIntent, PendingIntent answerIntent,
            PendingIntent contentIntent, PendingIntent deleteIntent, PendingIntent lockScreenIntent) {
            Notification.Builder builder = new Builder(context, channelID).setSmallIcon(
                    R.drawable.call_icon_chat_normal)
                .setContentTitle(title)   // Notification text, usually the caller’s name
                .setContentText(content).setContentIntent(contentIntent).setVisibility(Notification.VISIBILITY_PUBLIC)
                .setCategory(Notification.CATEGORY_CALL).setOngoing(true).setAutoCancel(true)
                .setFlag(Notification.FLAG_INSISTENT, true) // keep sound repeat,should be last.
                .setPriority(Notification.PRIORITY_MAX);

            if (timeout > 0) {
                builder.setTimeoutAfter(timeout);
            }
            if (deleteIntent != null) {
                builder.setDeleteIntent(deleteIntent);
            }

            //callStyle need foreground service or fullscreen intent
            boolean fullScreenIntentPermission = NotificationUtil.hasFullScreenIntentPermissionDeclared(context);
            if (fullScreenIntentPermission) {
                builder.setFullScreenIntent(lockScreenIntent, true);
                android.app.Person caller = new android.app.Person.Builder().setName(title).setImportant(true).build();
                CallStyle callStyle = CallStyle.forIncomingCall(caller, declineIntent, answerIntent);
                builder.setStyle(callStyle);
            } else {
                Notification.Action answerAction = NotificationUtil.makeAnswerAction(context, answerIntent,
                    R.drawable.call_selector_dialog_voice_accept);
                Notification.Action negativeAction = NotificationUtil.makeNegativeAction(context, declineIntent,
                    R.drawable.zego_uikit_icon_dialog_voice_decline);
                builder.addAction(negativeAction);
                builder.addAction(answerAction);
            }
            return builder.build();
        }
    }

    static class CompatImpl {

        static Notification generateNotification(Context context, String channelID, String title, String content,
            boolean isVideoCall, long timeout, PendingIntent declineIntent, PendingIntent answerIntent,
            PendingIntent contentIntent, PendingIntent deleteIntent, PendingIntent lockScreenIntent) {

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelID).setSmallIcon(
                    R.drawable.call_icon_chat_normal)
                .setContentTitle(title)   // Notification text, usually the caller’s name
                .setContentText(content).setContentIntent(contentIntent)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC).setCategory(NotificationCompat.CATEGORY_CALL)
                .setOngoing(true).setAutoCancel(true).setPriority(NotificationCompat.PRIORITY_MAX);

            if (timeout > 0) {
                builder.setTimeoutAfter(timeout);
            }
            if (deleteIntent != null) {
                builder.setDeleteIntent(deleteIntent);
            }
            boolean fullScreenIntentPermission = NotificationUtil.hasFullScreenIntentPermissionDeclared(context);
            if (fullScreenIntentPermission) {
                builder.setFullScreenIntent(lockScreenIntent, true);
            }
            NotificationCompat.Action answerAction = NotificationUtil.makeCompatAnswerAction(context, answerIntent,
                R.drawable.call_selector_dialog_voice_accept);
            NotificationCompat.Action negativeAction = NotificationUtil.makeNegativeCompatAction(context, declineIntent,
                R.drawable.zego_uikit_icon_dialog_voice_decline);
            builder.addAction(negativeAction);
            builder.addAction(answerAction);
            Notification notification = builder.build();
            notification.flags = notification.flags | Notification.FLAG_INSISTENT;  // keep sound repeat
            return notification;
        }
    }

    static class CustomImpl {

        static Notification generateNotification(Context context, String channelID, String title, String content,
            boolean isVideoCall, long timeout, PendingIntent declineIntent, PendingIntent answerIntent,
            PendingIntent contentIntent, PendingIntent deleteIntent, PendingIntent lockScreenIntent) {

            RemoteViews smallViews = new RemoteViews(context.getPackageName(), R.layout.call_layout_notification_small);
            smallViews.setTextViewText(R.id.dialog_call_name, title);
            smallViews.setTextViewText(R.id.dialog_call_type, content);
            smallViews.setOnClickPendingIntent(R.id.dialog_call_accept, answerIntent);
            smallViews.setOnClickPendingIntent(R.id.dialog_call_decline, declineIntent);
            if (isVideoCall) {
                smallViews.setImageViewResource(R.id.dialog_call_accept, R.drawable.call_icon_dialog_video_accept);
            } else {
                smallViews.setImageViewResource(R.id.dialog_call_accept, R.drawable.call_icon_dialog_voice_accept);
            }

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelID).setSmallIcon(
                    R.drawable.call_icon_chat_normal)
                .setContentTitle(title)   // Notification text, usually the caller’s name
                .setContentText(content).setContentIntent(contentIntent)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC).setCategory(NotificationCompat.CATEGORY_CALL)
                .setOngoing(true).setAutoCancel(true).setPriority(NotificationCompat.PRIORITY_MAX)
                .setStyle(new NotificationCompat.DecoratedCustomViewStyle()).setCustomHeadsUpContentView(smallViews)
                .setCustomContentView(smallViews);
            if (timeout > 0) {
                builder.setTimeoutAfter(timeout);
            }
            if (deleteIntent != null) {
                builder.setDeleteIntent(deleteIntent);
            }
            boolean fullScreenIntentPermission = NotificationUtil.hasFullScreenIntentPermissionDeclared(context);
            if (fullScreenIntentPermission) {
                builder.setFullScreenIntent(lockScreenIntent, true);
            }
            Notification notification = builder.build();
            notification.flags = notification.flags | Notification.FLAG_INSISTENT; // keep sound repeat
            return notification;
        }
    }
}
