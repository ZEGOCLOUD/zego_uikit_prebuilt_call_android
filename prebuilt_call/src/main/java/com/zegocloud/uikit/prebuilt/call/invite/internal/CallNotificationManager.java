package com.zegocloud.uikit.prebuilt.call.invite.internal;

import android.Manifest.permission;
import android.app.Activity;
import android.app.Application;
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
import android.os.Handler;
import android.os.Looper;
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
    private static final int TIMEOUT_AFTER = 30000;

    private boolean isNotificationShowed;
    private final Runnable dismissNotificationRunnable = new Runnable() {
        @Override
        public void run() {
            CallInvitationServiceImpl.getInstance().dismissCallNotification();
            CallInvitationServiceImpl.getInstance().clearPushMessage();
        }
    };
    private Handler handler = new Handler(Looper.getMainLooper());

    public void showCallNotification(Context context) {
        Timber.d("showCallNotification() called with: context = [" + context + "]");
        boolean canShowNotification = checkIfAppCanShowNotification(context);
        ContextCompat.startForegroundService(context, new Intent(context, OffLineCallNotificationService.class));
        if (canShowNotification) {
            handler.postDelayed(dismissNotificationRunnable, TIMEOUT_AFTER);
            isNotificationShowed = true;
        }
    }

    private boolean checkIfAppCanShowNotification(Context context) {
        boolean hasNotificationPermission = true;
        if (Build.VERSION.SDK_INT >= 33) {
            hasNotificationPermission = ContextCompat.checkSelfPermission(context, "android.permission.POST_NOTIFICATIONS")
                == PackageManager.PERMISSION_GRANTED;
        }
        boolean notificationsEnabled = NotificationManagerCompat.from(context).areNotificationsEnabled();
        if (!hasNotificationPermission || !notificationsEnabled) {
            Timber.d("checkNotificationEnabled() called with: hasNotificationPermission = %b,notificationsEnabled = %b",
                hasNotificationPermission, notificationsEnabled);
        }
        return hasNotificationPermission && notificationsEnabled;
    }

    public void showCallBackgroundNotification(Context context) {
        Timber.d("showCallBackgroundNotification() called with: context = [" + context + "]");
        boolean canShowNotification = checkIfAppCanShowNotification(context);
        Notification callNotification = CallInvitationServiceImpl.getInstance().getCallNotification(context);
        NotificationManagerCompat.from(context).notify(callNotificationID, callNotification);
        if (canShowNotification) {
            handler.postDelayed(dismissNotificationRunnable, TIMEOUT_AFTER);
            isNotificationShowed = true;
        }
    }

    public String getBackgroundNotificationMessage(boolean isVideoCall, boolean isGroup) {
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

    public String getBackgroundNotificationTitle(boolean isVideoCall, boolean isGroup, String userName) {
        String notificationTitle = "";
        ZegoUIKitPrebuiltCallInvitationConfig callInvitationConfig = CallInvitationServiceImpl.getInstance()
            .getCallInvitationConfig();
        if (callInvitationConfig != null && callInvitationConfig.translationText != null) {
            ZegoTranslationText innerText = callInvitationConfig.translationText;
            if (isVideoCall) {
                notificationTitle = isGroup ? String.format(innerText.incomingGroupVideoCallDialogTitle, userName)
                    : String.format(innerText.incomingVideoCallDialogTitle, userName);
            } else {
                String incomingVoiceCallDialogTitle = innerText.incomingVoiceCallDialogTitle;
                notificationTitle = isGroup ? String.format(innerText.incomingGroupVoiceCallDialogTitle, userName)
                    : String.format(incomingVoiceCallDialogTitle, userName);
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
        if (isNotificationShowed) {
            isNotificationShowed = false;
            Intent intent = new Intent(context, OffLineCallNotificationService.class);
            context.stopService(intent);

            NotificationManagerCompat.from(context).cancel(callNotificationID);

            handler.removeCallbacks(dismissNotificationRunnable);
        }
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

        Activity topActivity = CallInvitationServiceImpl.getInstance().getTopActivity();
        boolean canShowFullOnLockScreen = CallInvitationServiceImpl.getInstance().canShowFullOnLockScreen();
        boolean offlineNotification = zimPushMessage != null;
        boolean backgroundNotification = zimPushMessage == null && topActivity != null;

        if (zimPushMessage != null || topActivity == null) {
            // offline service
            Application application = (Application) context.getApplicationContext();
            CallInvitationServiceImpl.getInstance().registerLifeCycleCallback(application);
        }

        PendingIntent clickIntent = getClickIntent(context);
        PendingIntent acceptIntent = getAcceptIntent(context);
        PendingIntent declineIntent = getDeclineIntent(context);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {

            Notification.Builder builder = new Builder(context, channelID).setSmallIcon(
                    R.drawable.call_icon_chat_normal) //// A small icon that will be displayed in the status bar
                .setContentTitle(title)   // Notification text, usually the caller’s name
                .setContentText(body).setContentIntent(clickIntent).setVisibility(Notification.VISIBILITY_PUBLIC)
                .setCategory(NotificationCompat.CATEGORY_CALL).setOngoing(true).setAutoCancel(true);

            // offline call can only start foregroundService and show notification,cannot show full screen on
            // lock screen.
            if (backgroundNotification && canShowFullOnLockScreen) {
                PendingIntent lockScreenIntent = getLockScreenIntent(context);
                builder.setFullScreenIntent(lockScreenIntent, true);
            }

            if (offlineNotification || canShowFullOnLockScreen) {
                //callStyle need foreground service or fullscreen intent
                android.app.Person caller = new android.app.Person.Builder().setName(title).setImportant(true).build();
                Notification.CallStyle callStyle = Notification.CallStyle.forIncomingCall(caller, declineIntent,
                    acceptIntent);
                builder.setStyle(callStyle);
            } else {

                String accept = context.getString(R.string.call_page_action_accept);

                String decline = context.getString(R.string.call_page_action_decline);

                Notification.Action.Builder acceptAction = new Notification.Action.Builder(
                    // The icon that will be displayed on the button (or not, depends on the Android version)
                    Icon.createWithResource(context, R.drawable.call_selector_dialog_voice_accept),
                    // The text on the button
                    accept, acceptIntent);

                Notification.Action.Builder declineAction = new Notification.Action.Builder(
                    // The icon that will be displayed on the button (or not, depends on the Android version)
                    Icon.createWithResource(context,
                        com.zegocloud.uikit.R.drawable.zego_uikit_icon_dialog_voice_decline),
                    // The text on the button
                    decline, declineIntent);

                builder.addAction(acceptAction.build());
                builder.addAction(declineAction.build());
            }
            builder.setTimeoutAfter(TIMEOUT_AFTER + 1000);
            return builder.build();
        } else {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelID).setSmallIcon(
                    R.drawable.call_icon_chat_normal) //// A small icon that will be displayed in the status bar
                .setContentTitle(title)   // Notification text, usually the caller’s name
                .setContentText(body).setContentIntent(clickIntent).setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC).setCategory(NotificationCompat.CATEGORY_CALL)
                .setOngoing(true).setAutoCancel(true);
            if (backgroundNotification && canShowFullOnLockScreen) {
                PendingIntent lockScreenIntent = getLockScreenIntent(context);
                builder.setFullScreenIntent(lockScreenIntent, true);
            }

            String accept = context.getString(R.string.call_page_action_accept);

            String decline = context.getString(R.string.call_page_action_decline);

            NotificationCompat.Action.Builder acceptAction = new Action.Builder(
                // The icon that will be displayed on the button (or not, depends on the Android version)
                IconCompat.createWithResource(context, R.drawable.call_selector_dialog_voice_accept),
                // The text on the button
                accept, acceptIntent);

            NotificationCompat.Action.Builder declineAction = new Action.Builder(
                // The icon that will be displayed on the button (or not, depends on the Android version)
                IconCompat.createWithResource(context,
                    com.zegocloud.uikit.R.drawable.zego_uikit_icon_dialog_voice_decline),
                // The text on the button
                decline, declineIntent);

            builder.addAction(acceptAction.build());
            builder.addAction(declineAction.build());
            builder.setTimeoutAfter(TIMEOUT_AFTER + 1000);
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
            Intent intent = null;
            try {
                intent = new Intent(context, Class.forName(getLauncherActivity(context)));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.setAction(ACTION_ACCEPT_CALL);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
            PendingIntent pendingIntent;
            if (Build.VERSION.SDK_INT >= VERSION_CODES.M) {
                pendingIntent = PendingIntent.getActivity(context, 0, intent,
                    PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
            } else {
                pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            }
            return pendingIntent;

            // Android 12 trampoline limit
            //            // remember action and start app, auto accept and start callInviteActivity
            //            Intent intent = new Intent(context, OffLineCallNotificationService.class);
            //            intent.setAction(ACTION_ACCEPT_CALL);
            //            PendingIntent pendingIntent;
            //            if (Build.VERSION.SDK_INT >= VERSION_CODES.M) {
            //                pendingIntent = PendingIntent.getService(context, 0, intent,
            //                    PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
            //            } else {
            //                pendingIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
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
            Intent intent = null;
            try {
                intent = new Intent(context, Class.forName(getLauncherActivity(context)));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.setAction(ACTION_CLICK);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
            PendingIntent pendingIntent;
            if (Build.VERSION.SDK_INT >= VERSION_CODES.M) {
                pendingIntent = PendingIntent.getActivity(context, 0, intent,
                    PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
            } else {
                pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            }

            // Android 12 trampoline limit
            //            Intent intent = new Intent(context, OffLineCallNotificationService.class);
            //            intent.setAction(ACTION_CLICK);
            //            PendingIntent openIntent;
            //            if (Build.VERSION.SDK_INT >= VERSION_CODES.M) {
            //                pendingIntent = PendingIntent.getService(context, 0, intent,
            //                    PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
            //            } else {
            //                pendingIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            //            }
            return pendingIntent;
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
