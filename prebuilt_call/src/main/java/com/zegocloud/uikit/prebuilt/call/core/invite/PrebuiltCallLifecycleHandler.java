package com.zegocloud.uikit.prebuilt.call.core.invite;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.AppTask;
import android.app.ActivityManager.RecentTaskInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Application;
import android.app.Application.ActivityLifecycleCallbacks;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallConfig;
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallFragment;
import com.zegocloud.uikit.prebuilt.call.config.ZegoMenuBarButtonName;
import com.zegocloud.uikit.prebuilt.call.core.CallInvitationServiceImpl;
import com.zegocloud.uikit.prebuilt.call.core.notification.PrebuiltCallNotificationManager;
import com.zegocloud.uikit.prebuilt.call.core.push.ZIMPushMessage;
import com.zegocloud.uikit.prebuilt.call.invite.ZegoUIKitPrebuiltCallInvitationConfig;
import com.zegocloud.uikit.prebuilt.call.invite.internal.CallInviteActivity;
import im.zego.internal.screencapture.ZegoScreenCaptureManager.ZegoScreenCaptureAssistantActivity;
import java.util.List;
import java.util.Objects;
import timber.log.Timber;

/**
 * // onActivityCreated : save offline notification Click action.
 * <p>
 * // onActivityResumed : when app was opened or reopened from background, // // navigate to callInviteActivity class
 */
public class PrebuiltCallLifecycleHandler {

    private String launcherActivityString;
    private Activity topActivity;
    private ActivityLifecycleCallbacks lifecycleCallbacks = new ActivityLifecycleCallbacks() {
        @Override
        public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
            if (launcherActivityString == null) {
                launcherActivityString = getLauncherActivityString(activity.getApplication());
            }

            ZIMPushMessage zimPushMessage = CallInvitationServiceImpl.getInstance().getZIMPushMessage();
            String action = activity.getIntent().getAction();
            String activityName = activity.getClass().getCanonicalName();
            Timber.d(
                "onActivityCreated() called with: activityName = [" + activityName + "], launcherActivityString = ["
                    + launcherActivityString + "],zimPushMessage = " + zimPushMessage + ",action = " + action);
            boolean isLauncherActivity = Objects.equals(activityName, launcherActivityString);
            if (zimPushMessage != null && isLauncherActivity) {
                if (!TextUtils.isEmpty(action)) {
                    CallInvitationServiceImpl.getInstance().setNotificationClickAction(action);
                }
                CallInvitationServiceImpl.getInstance().dismissCallNotification(activity.getApplication());
            }
        }

        @Override
        public void onActivityStarted(@NonNull Activity activity) {

        }

        @Override
        public void onActivityResumed(@NonNull Activity activity) {
            setTopActivity(activity);

            boolean notificationShowed = CallInvitationServiceImpl.getInstance().isCallNotificationShowed();
            ZIMPushMessage pushMessage = CallInvitationServiceImpl.getInstance().getZIMPushMessage();
            Activity topActivity = CallInvitationServiceImpl.getInstance().getTopActivity();
            String notificationAction = CallInvitationServiceImpl.getInstance().getNotificationAction();
            Timber.d("onActivityResumed() called with: topActivity = [" + topActivity + "],pushMessage:" + pushMessage
                + ",notificationAction:" + notificationAction);

            // if app is online and background,received a call notification
            if (notificationShowed && pushMessage == null) {
                // if app's topActivity is not CallInviteActivity.then start it
                // with incoming page
                if (!(topActivity instanceof CallInviteActivity)) {
                    Intent incomingPageIntent = CallInviteActivity.getIncomingPageIntent(activity,
                        PrebuiltCallNotificationManager.ACTION_CLICK);
                    activity.startActivity(incomingPageIntent);
                }
            }

            // if app was at front,and topActivity is not CallInviteActivity，
            // for example,received a offline notification, not click notification
            // but directly click app icon instead
            if (!(topActivity instanceof CallInviteActivity) && notificationAction == null) {
                //clear push message will make app start normally auto sign in
                CallInvitationServiceImpl.getInstance().setZIMPushMessage(null);
            }
            //            boolean canShowFullOnLockScreen = CallInvitationServiceImpl.getInstance().canShowFullOnLockScreen();
            //            if (canShowFullOnLockScreen) {
            //                if (!(topActivity instanceof CallInviteActivity)) {
            //                    CallInvitationServiceImpl.getInstance().dismissCallNotification(topActivity);
            //                }
            //            } else {
            CallInvitationServiceImpl.getInstance().dismissCallNotification(topActivity);
            //            }
            ActivityManager am = (ActivityManager) topActivity.getSystemService(Context.ACTIVITY_SERVICE);
            if (am == null) {
                return;
            }

            ZegoUIKitPrebuiltCallInvitationConfig callInvitationConfig = CallInvitationServiceImpl.getInstance()
                .getCallInvitationConfig();
            boolean isCallInvite = callInvitationConfig != null;
            if (!isCallInvite) {
                return;
            }
            if (!(topActivity instanceof CallInviteActivity)
                && !(topActivity instanceof ZegoScreenCaptureAssistantActivity)) {

                // call-invite,in room,hasMiniButton,but no permission, and app goes background.
                // Now, when returning to the app, it is necessary to bring the CallInviteActivity to the foreground
                // (because the CallInviteActivity was hidden in the recent apps, it won't show up if not brought to the foreground).
                boolean inCallRoom = CallInvitationServiceImpl.getInstance().isInCallRoom();
                ZegoUIKitPrebuiltCallFragment prebuiltCallFragment = CallInvitationServiceImpl.getInstance()
                    .getZegoUIKitPrebuiltCallFragment();
                ZegoUIKitPrebuiltCallConfig callConfig = CallInvitationServiceImpl.getInstance().getCallConfig();
                ZegoCallInvitationData callInvitationData = CallInvitationServiceImpl.getInstance()
                    .getCallInvitationData();
                if (inCallRoom) {
                    boolean hasMiniButton =
                        callConfig.bottomMenuBarConfig.buttons.contains(ZegoMenuBarButtonName.MINIMIZING_BUTTON)
                            || callConfig.topMenuBarConfig.buttons.contains(ZegoMenuBarButtonName.MINIMIZING_BUTTON);
                    boolean noSystemOverlayPermission =
                        VERSION.SDK_INT >= VERSION_CODES.M && !Settings.canDrawOverlays(topActivity);
                    if (hasMiniButton && noSystemOverlayPermission) {
                        if (prebuiltCallFragment != null) {
                            bringCallInviteActivityToFront(am);
                        }
                    }
                } else {
                    if (callInvitationData != null) {
                        // not in room ,but in waiting page，need to bring CallInviteActivity to front
                        boolean isCallInviteActivityStarted = false;
                        List<RunningTaskInfo> runningTasks = am.getRunningTasks(Integer.MAX_VALUE);
                        for (RunningTaskInfo runningTask : runningTasks) {
                            if (Objects.equals(runningTask.topActivity.getClassName(),
                                CallInviteActivity.class.getName())) {
                                isCallInviteActivityStarted = true;
                                break;
                            }
                        }
                        if (isCallInviteActivityStarted) {
                            bringCallInviteActivityToFront(am);
                        }
                    }
                }
            }
        }

        @Override
        public void onActivityPaused(@NonNull Activity activity) {

        }

        @Override
        public void onActivityStopped(@NonNull Activity activity) {

        }

        @Override
        public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {

        }

        @Override
        public void onActivityDestroyed(@NonNull Activity activity) {
            if (activity == topActivity) {
                clearTopActivity();
            }
        }
    };

    public void setupCallbacks(Application application) {
        application.registerActivityLifecycleCallbacks(lifecycleCallbacks);
    }

    public void removeCallbacks(Application application) {
        application.unregisterActivityLifecycleCallbacks(lifecycleCallbacks);
    }

    public Activity getTopActivity() {
        return topActivity;
    }

    public static void bringCallInviteActivityToFront(ActivityManager am) {
        List<AppTask> tasks = am.getAppTasks();
        if (tasks != null && !tasks.isEmpty()) {
            for (AppTask task : tasks) {
                RecentTaskInfo taskInfo = task.getTaskInfo();
                if (taskInfo.baseIntent.getComponent().toShortString().contains(CallInviteActivity.class.getName())) {
                    task.moveToFront();
                    break;
                }
            }
        }
    }

    public static String getLauncherActivityString(Application application) {
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setPackage(application.getPackageName());
        PackageManager pm = application.getPackageManager();
        List<ResolveInfo> info = pm.queryIntentActivities(intent, 0);
        if (info == null || info.isEmpty()) {
            return "";
        }
        return info.get(0).activityInfo.name;
    }

    void setTopActivity(Activity activity) {
        topActivity = activity;
    }

    private void clearTopActivity() {
        topActivity = null;
    }
}
