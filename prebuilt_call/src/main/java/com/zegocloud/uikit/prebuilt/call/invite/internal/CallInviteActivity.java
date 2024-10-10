package com.zegocloud.uikit.prebuilt.call.invite.internal;

import android.app.ActivityManager;
import android.app.ActivityManager.AppTask;
import android.app.ActivityManager.RecentTaskInfo;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.Window;
import android.view.WindowManager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.tencent.mmkv.MMKV;
import com.zegocloud.uikit.ZegoUIKit;
import com.zegocloud.uikit.prebuilt.call.R;
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallConfig;
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallFragment;
import com.zegocloud.uikit.prebuilt.call.config.ZegoMenuBarButtonName;
import com.zegocloud.uikit.prebuilt.call.core.CallInvitationServiceImpl;
import com.zegocloud.uikit.prebuilt.call.core.invite.PrebuiltCallRepository;
import com.zegocloud.uikit.prebuilt.call.core.invite.ZegoCallInvitationData;
import com.zegocloud.uikit.prebuilt.call.core.invite.ui.ZegoPrebuiltCallInComingFragment;
import com.zegocloud.uikit.prebuilt.call.core.invite.ui.ZegoPrebuiltCallOffLineLockScreenFragment;
import com.zegocloud.uikit.prebuilt.call.core.invite.ui.ZegoPrebuiltCallOutGoingFragment;
import com.zegocloud.uikit.prebuilt.call.core.notification.RingtoneManager;
import com.zegocloud.uikit.prebuilt.call.core.push.ZIMPushMessage;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import timber.log.Timber;

/**
 * show waiting page and call page for call-invite
 */
public class CallInviteActivity extends AppCompatActivity {

    public static final String PAGE_INCOMING = "page_incoming";
    public static final String PAGE_OUTGOING = "page_outgoing";
    public static final String PAGE_LOCKSCREEN = "page_lockscreen";
    public static final String PAGE_CALL = "page_call";
    public static final String KEY_PAGE = "page";
    public static final String KEY_ACTION = "action";

    /**
     * @param context
     * @param page    one of PAGE_INCOMING,PAGE_OUTGOING,PAGE_LOCKSCREEN,PAGE_CALL
     * @param action
     * @return
     */
    public static Intent getPageIntent(Context context, String page, String action) {
        Intent intent = new Intent(context, CallInviteActivity.class);
        intent.putExtra(KEY_PAGE, page);
        if (!TextUtils.isEmpty(action)) {
            intent.putExtra(KEY_ACTION, action);
            intent.setAction(action); // be sure to set action
        }
        addFlagsOrNot(intent, context);
        return intent;
    }

    private static void addFlagsOrNot(Intent intent, Context context) {
        ZegoUIKitPrebuiltCallConfig callConfig = CallInvitationServiceImpl.getInstance().getCallConfig();
        if (callConfig == null) {
            return;
        }
        boolean hasMiniButton = callConfig.bottomMenuBarConfig.buttons.contains(ZegoMenuBarButtonName.MINIMIZING_BUTTON)
            || callConfig.topMenuBarConfig.buttons.contains(ZegoMenuBarButtonName.MINIMIZING_BUTTON);
        boolean hasSystemOverlayPermission = VERSION.SDK_INT <= VERSION_CODES.M || Settings.canDrawOverlays(context);
        if (hasMiniButton && hasSystemOverlayPermission) {
            // call invite and has miniButton，and hasSystemOverlayPermission, excludeFromRecent + singleTask
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        } else {
            // call invite but no miniButton，normal launchMode.
        }
    }

    public static void startOutgoingPage(Context context) {
        context.startActivity(getPageIntent(context, PAGE_OUTGOING, null));
    }

    public static void startIncomingPage(Context context) {
        context.startActivity(getPageIntent(context, PAGE_INCOMING, null));
    }

    public static void startLockScreenPage(Context context) {
        context.startActivity(getPageIntent(context, PAGE_LOCKSCREEN, null));
    }

    public static void startCallPage(Context context) {
        context.startActivity(getPageIntent(context, PAGE_CALL, null));
    }

    private CallStateListener callStateListener = new CallStateListener() {
        @Override
        public void onStateChanged(int before, int after) {
            Timber.d("onStateChanged() called with: before = [" + before + "], after = [" + after + "]");
            if (after == PrebuiltCallRepository.CONNECTED) {
                showCallFragment();
            } else {
                CallInvitationServiceImpl.getInstance().openCamera(false);
                CallInvitationServiceImpl.getInstance().removeCallStateListener(callStateListener);
                if (after == PrebuiltCallRepository.NONE_REJECTED_BUSY) {
                    Fragment fragmentById = getSupportFragmentManager().findFragmentById(R.id.call_fragment_container);
                    if (fragmentById instanceof ZegoPrebuiltCallOutGoingFragment) {
                        ((ZegoPrebuiltCallOutGoingFragment) fragmentById).setBusy();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                finishCallActivityAndMoveToFront();
                            }
                        }, 3000);
                    }
                } else {
                    finishCallActivityAndMoveToFront();
                }
            }
        }
    };
    private Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Window window = getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
        } else {
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        }
        setContentView(R.layout.call_activity_prebuilt);

        String action = getIntent().getStringExtra(KEY_ACTION);
        String page = getIntent().getStringExtra(KEY_PAGE);
        ZegoCallInvitationData callInvitationData = CallInvitationServiceImpl.getInstance().getCallInvitationData();
        ZIMPushMessage pushMessage = CallInvitationServiceImpl.getInstance().getZIMPushMessage();

        Timber.d("CallInviteActivity onCreate() called with: page = [" + page + "],action:" + action
            + ",\n callInvitationData:" + callInvitationData + "\n pushMessage:" + pushMessage);

        if (pushMessage != null) {
            if (Objects.equals(PAGE_LOCKSCREEN, page)) {
                showLockScreenOffLineFragment();
            }
        } else if (callInvitationData != null) {
            CallInvitationServiceImpl.getInstance().dismissCallNotification();
            CallInvitationServiceImpl.getInstance().addCallStateListener(callStateListener);

            // normal coming
            boolean oneOnOneCall = callInvitationData.invitees.size() == 1;
            if (Objects.equals(PAGE_CALL, page)) {
                showCallFragment();
            } else if (PAGE_INCOMING.equals(page)) {
                showInComingFragment();
            } else if (PAGE_OUTGOING.equals(page)) {
                if (oneOnOneCall) {
                    showOutgoingFragment();
                } else {
                    showCallFragment();
                }
            } else if (Objects.equals(PAGE_LOCKSCREEN, page)) {
                // cannot happen
            } else {

            }
        } else {
            Timber.e("CallInviteActivity start Error");
            CallInvitationServiceImpl.getInstance().leaveRoom();
            finishCallActivityAndMoveToFront();
        }
    }

    private void finishCallActivity(boolean moveToFront) {
        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        if (am == null) {
            return;
        }
        List<AppTask> appTasks = am.getAppTasks();
        if (appTasks == null) {
            return;
        }

        Optional<AppTask> taskOptional = appTasks.stream().filter(appTask -> {
            RecentTaskInfo taskInfo = appTask.getTaskInfo();
            if (taskInfo.baseActivity != null) {
                String className = taskInfo.baseActivity.getClassName();
                return Objects.equals(CallInviteActivity.class.getName(), className);
            }
            return false;
        }).findAny();

        if (taskOptional.isPresent()) {
            taskOptional.get().finishAndRemoveTask();
            if (moveToFront) {
                appTasks.remove(taskOptional.get());
                if (!appTasks.isEmpty()) {
                    AppTask appTask = appTasks.get(appTasks.size() - 1);
                    appTask.moveToFront();
                }
            }
        } else {
            finish();
        }
    }

    public void finishCallActivity() {
        finishCallActivity(false);
    }

    public void finishCallActivityAndMoveToFront() {
        finishCallActivity(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CallInvitationServiceImpl.getInstance().removeCallStateListener(callStateListener);
    }

    private void showCallFragment() {
        RingtoneManager.stopRingTone();

        Fragment fragmentById = getSupportFragmentManager().findFragmentById(R.id.call_fragment_container);
        if (fragmentById instanceof ZegoUIKitPrebuiltCallFragment) {
            return;
        }
        ZegoCallInvitationData invitationData = CallInvitationServiceImpl.getInstance().getCallInvitationData();
        ZegoUIKitPrebuiltCallFragment fragment = ZegoUIKitPrebuiltCallFragment.newInstance(invitationData.callID);
        CallInvitationServiceImpl.getInstance().setPrebuiltCallFragment(fragment);
        CallInvitationServiceImpl.getInstance().setLeaveWhenOnlySelf(invitationData.invitees.size() <= 1);

        getSupportFragmentManager().beginTransaction().replace(R.id.call_fragment_container, fragment).commitNow();
    }

    private void showInComingFragment() {
        RingtoneManager.playRingTone(true);
        ZegoPrebuiltCallInComingFragment fragment = new ZegoPrebuiltCallInComingFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.call_fragment_container, fragment).commitNow();
    }

    private void showOutgoingFragment() {
        RingtoneManager.playRingTone(false);
        ZegoPrebuiltCallOutGoingFragment fragment = new ZegoPrebuiltCallOutGoingFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.call_fragment_container, fragment).commitNow();
    }

    private void showLockScreenOffLineFragment() {
        String string = MMKV.defaultMMKV().getString("ringtone", null);
        if (!TextUtils.isEmpty(string)) {
            Uri parse = Uri.parse(string);
            if (parse != null) {
                RingtoneManager.setIncomingUri(parse);
            }
        }
        RingtoneManager.playRingTone(true);
        ZegoPrebuiltCallOffLineLockScreenFragment fragment = new ZegoPrebuiltCallOffLineLockScreenFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.call_fragment_container, fragment).commitNow();
    }
}