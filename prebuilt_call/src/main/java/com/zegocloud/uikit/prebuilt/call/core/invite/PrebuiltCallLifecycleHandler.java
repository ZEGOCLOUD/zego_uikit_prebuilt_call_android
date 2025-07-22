package com.zegocloud.uikit.prebuilt.call.core.invite;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.AppTask;
import android.app.ActivityManager.RecentTaskInfo;
import android.app.Application;
import android.app.Application.ActivityLifecycleCallbacks;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallFragment;
import com.zegocloud.uikit.prebuilt.call.core.CallInvitationServiceImpl;
import com.zegocloud.uikit.prebuilt.call.invite.internal.CallInviteActivity;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import timber.log.Timber;

/**
 * // onActivityCreated : save offline notification Click action.
 * <p>
 * // onActivityResumed : when app was opened or reopened from background, // // navigate to callInviteActivity class
 */
public class PrebuiltCallLifecycleHandler {

    private Activity topActivity;
    private ActivityLifecycleCallbacks lifecycleCallbacks = new ActivityLifecycleCallbacks() {
        @Override
        public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
            Timber.d("onActivityCreated() called with: activity = [" + activity + "], savedInstanceState = ["
                + savedInstanceState + "]");
        }

        @Override
        public void onActivityStarted(@NonNull Activity activity) {
            Timber.d("onActivityStarted() called with: activity = [" + activity + "]");

        }

        @Override
        public void onActivityResumed(@NonNull Activity activity) {
            Timber.d("onActivityResumed() called with: activity = [" + activity + "]");
            setTopActivity(activity);

            CallInvitationServiceImpl.getInstance().dismissCallNotification();

            AppTask callInviteActivityTask = getCallInviteActivityTask(topActivity);
            // if CallInviteActivity has already started
            if (callInviteActivityTask != null) {
                ZegoUIKitPrebuiltCallFragment callFragment = CallInvitationServiceImpl.getInstance()
                    .getZegoUIKitPrebuiltCallFragment();
                // no need to moveToFront when show miniView
                if (callFragment == null || !(callFragment.isMiniVideoShown()
                    || callFragment.isPendingShowMiniWindow())) {
                    callInviteActivityTask.moveToFront();
                }
            } else {
                ZegoCallInvitationData invitationData = CallInvitationServiceImpl.getInstance().getCallInvitationData();
                // receive call and is from background to front(for example,click app icon or click notification)
                if (invitationData == null) {
                    CallInvitationServiceImpl.getInstance().hideIncomingCallDialog();
                } else {
                    if (!(topActivity instanceof CallInviteActivity)) {
                        CallInvitationServiceImpl.getInstance().showIncomingCallDialog(invitationData);
                    }
                }
            }
        }

        @Override
        public void onActivityPaused(@NonNull Activity activity) {
        }

        @Override
        public void onActivityStopped(@NonNull Activity activity) {
            Timber.d("onActivityStopped() called with: activity = [" + activity + "]");
        }

        @Override
        public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {

        }

        @Override
        public void onActivityDestroyed(@NonNull Activity activity) {
            Timber.d("onActivityDestroyed() called with: activity = [" + activity + "]");
            if (activity == topActivity) {
                clearTopActivity();
            }
        }
    };

    private static @Nullable AppTask getCallInviteActivityTask(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (am == null) {
            return null;
        }
        List<AppTask> appTasks = am.getAppTasks();
        if (appTasks == null) {
            return null;
        }
        Optional<AppTask> taskOptional = appTasks.stream().filter(appTask -> {
            RecentTaskInfo taskInfo = appTask.getTaskInfo();
            boolean isBaseActivity = false;
            if (taskInfo.baseActivity != null) {
                String className = taskInfo.baseActivity.getClassName();
                isBaseActivity = Objects.equals(CallInviteActivity.class.getName(), className);
            }
            boolean isTopActivity = false;
            if (taskInfo.topActivity != null) {
                String className = taskInfo.topActivity.getClassName();
                isTopActivity = Objects.equals(CallInviteActivity.class.getName(), className);
            }
            return isBaseActivity || isTopActivity;
        }).findAny();
        return taskOptional.orElse(null);
    }

    public void setupCallbacks(Application application) {
        application.registerActivityLifecycleCallbacks(lifecycleCallbacks);
    }

    public void removeCallbacks(Application application) {
        application.unregisterActivityLifecycleCallbacks(lifecycleCallbacks);
    }

    public Activity getTopActivity() {
        return topActivity;
    }


    void setTopActivity(Activity activity) {
        topActivity = activity;
    }

    private void clearTopActivity() {
        topActivity = null;
    }
}
