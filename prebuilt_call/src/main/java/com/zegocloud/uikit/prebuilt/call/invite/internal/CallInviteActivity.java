package com.zegocloud.uikit.prebuilt.call.invite.internal;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.Window;
import android.view.WindowManager;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.zegocloud.uikit.ZegoUIKit;
import com.zegocloud.uikit.internal.ZegoUIKitLanguage;
import com.zegocloud.uikit.plugin.common.PluginCallbackListener;
import com.zegocloud.uikit.prebuilt.call.R;
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallConfig;
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallFragment;
import com.zegocloud.uikit.prebuilt.call.config.ZegoMenuBarButtonName;
import com.zegocloud.uikit.prebuilt.call.invite.ZegoUIKitPrebuiltCallInvitationConfig;
import com.zegocloud.uikit.service.defines.ZegoOnlySelfInRoomListener;
import java.util.Map;
import timber.log.Timber;

/**
 * show waiting page and call page for call-invite
 */
public class CallInviteActivity extends AppCompatActivity {

    private CallStateListener callStateListener;

    public static void startOutgoingPage(Context context) {
        Intent intent = new Intent(context, CallInviteActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("page", "outgoing");
        addFlags(intent, context);
        intent.putExtra("bundle", bundle);
        context.startActivity(intent);
    }

    private static void addFlags(Intent intent, Context context) {
        ZegoUIKitPrebuiltCallConfig callConfig = getCustomCallConfig();
        if (callConfig != null) {
            boolean hasMiniButton =
                callConfig.bottomMenuBarConfig.buttons.contains(ZegoMenuBarButtonName.MINIMIZING_BUTTON)
                    || callConfig.topMenuBarConfig.buttons.contains(ZegoMenuBarButtonName.MINIMIZING_BUTTON);
            boolean hasSystemOverlayPermission =
                VERSION.SDK_INT <= VERSION_CODES.M || Settings.canDrawOverlays(context);
            if (hasMiniButton && hasSystemOverlayPermission) {
                // call invite and has miniButton，and hasSystemOverlayPermission, excludeFromRecent + singleTask
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            } else {
                // call invite but no miniButton，normal launchMode.
            }
        }
    }

    public static void startIncomingPage(Context context) {
        Intent intent = new Intent(context, CallInviteActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("page", "incoming");
        intent.putExtra("bundle", bundle);
        addFlags(intent, context);
        context.startActivity(intent);
    }


    public static void startCallPage(Context context) {
        Intent intent = new Intent(context, CallInviteActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("page", "call");
        intent.putExtra("bundle", bundle);
        addFlags(intent, context);
        context.startActivity(intent);
    }


    public static Intent getStartCallPageIntent(Context context, String action) {
        Intent intent = new Intent(context, CallInviteActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("page", "call");
        intent.putExtra("bundle", bundle);
        addFlags(intent, context);
        intent.setAction(action);
        return intent;
    }


    public static Intent getIncomingPageIntent(Context context, String action) {
        Intent intent = new Intent(context, CallInviteActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("page", "incoming");
        intent.putExtra("bundle", bundle);
        addFlags(intent, context);
        intent.setAction(action);
        return intent;
    }

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

        callStateListener = new CallStateListener() {
            @Override
            public void onStateChanged(int before, int after) {
                if (after == CallInvitationServiceImpl.CONNECTED) {
                    showCallFragment();
                } else {
                    String userID = ZegoUIKit.getLocalUser().userID;
                    if (!TextUtils.isEmpty(userID)) {
                        if (ZegoUIKit.isCameraOn(userID)) {
                            ZegoUIKit.turnCameraOn(userID, false);
                        }
                    }
                    CallInvitationServiceImpl.getInstance().removeCallStateListener(callStateListener);
                    finish();
                }
            }
        };
        CallInvitationServiceImpl.getInstance().addCallStateListener(callStateListener);

        ZIMPushMessage pushMessage = CallInvitationServiceImpl.getInstance().getZIMPushMessage();

        ZegoCallInvitationData callInvitationData = CallInvitationServiceImpl.getInstance().getCallInvitationData();
        Timber.d("onCreate() called with: callInvitationData = [" + callInvitationData + "]");
        if (callInvitationData == null) {
            CallInvitationServiceImpl.getInstance().leaveRoomInternal();
            finish();
            return;
        }

        String intentAction = getIntent().getAction();
        Timber.d("onCreate() called with: intentAction = [" + intentAction + "]");
        if (CallNotificationManager.ACTION_ACCEPT_CALL.equals(intentAction)) {
            if (pushMessage != null) {
                ZegoUIKit.getSignalingPlugin().callAccept(pushMessage.invitationID, "", new PluginCallbackListener() {
                    @Override
                    public void callback(Map<String, Object> result) {
                        showCallFragment();
                    }
                });
            } else {
                CallInvitationServiceImpl.getInstance().acceptInvitation(new PluginCallbackListener() {
                    @Override
                    public void callback(Map<String, Object> result) {
                        showCallFragment();
                    }
                });
            }
        } else if (CallNotificationManager.ACTION_DECLINE_CALL.equals(intentAction)) {

        } else if (CallNotificationManager.ACTION_CLICK.equals(intentAction)) {
            // background,receive call, click notification
            RingtoneManager.playRingTone(true);
            showWaitingFragment(true);
        } else if (CallNotificationManager.SHOW_FULL_ON_LOCK_SCREEN.equals(intentAction)) {
            showWaitingFragment(true);
        } else {
            Bundle bundle = getIntent().getParcelableExtra("bundle");
            String page = bundle.getString("page");

            boolean isOneOnOneOnGoingCall = "outgoing".equals(page) && callInvitationData.invitees.size() == 1;
            boolean isIncoming = "incoming".equals(page);
            if (isIncoming || isOneOnOneOnGoingCall) {
                showWaitingFragment(isIncoming);
            } else {
                showCallFragment();
            }
        }
        // if activity is show on lock screen,keep notification until user clicked
        // accept or reject.
        if (!CallNotificationManager.SHOW_FULL_ON_LOCK_SCREEN.equals(intentAction)) {
            CallInvitationServiceImpl.getInstance().dismissCallNotification(this);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Timber.d("onDestroy() called");

        CallInvitationServiceImpl.getInstance().dismissCallNotification(this);
        CallInvitationServiceImpl.getInstance().removeCallStateListener(callStateListener);
    }

    private void showCallFragment() {
        Fragment fragmentById = getSupportFragmentManager().findFragmentById(R.id.call_fragment_container);
        if (fragmentById instanceof ZegoUIKitPrebuiltCallFragment) {
            return;
        }
        ZegoCallInvitationData invitationData = CallInvitationServiceImpl.getInstance().getCallInvitationData();
        ZegoUIKitPrebuiltCallConfig callConfig = getCustomCallConfig();
        if (callConfig == null) {
            callConfig = ZegoUIKitPrebuiltCallInvitationConfig.generateDefaultConfig(invitationData);
        }
        ZegoUIKitPrebuiltCallInvitationConfig callInvitationConfig = CallInvitationServiceImpl.getInstance()
            .getCallInvitationConfig();
        if (callInvitationConfig != null && callInvitationConfig.translationText != null
            && callInvitationConfig.translationText.getInvitationBaseText() instanceof InvitationTextCHS) {
            callConfig.zegoCallText = new ZegoCallText(ZegoUIKitLanguage.CHS);
        } else {
            callConfig.zegoCallText = new ZegoCallText(ZegoUIKitLanguage.ENGLISH);
        }

        ZegoUIKitPrebuiltCallFragment fragment = ZegoUIKitPrebuiltCallFragment.newInstance(this, invitationData.callID,
            callConfig);

        if (invitationData.invitees.size() > 1) {
            fragment.setOnOnlySelfInRoomListener(new ZegoOnlySelfInRoomListener() {
                @Override
                public void onOnlySelfInRoom() {

                }
            });
        }
        getSupportFragmentManager().beginTransaction().replace(R.id.call_fragment_container, fragment).commitNow();
    }

    private static @Nullable ZegoUIKitPrebuiltCallConfig getCustomCallConfig() {
        ZegoCallInvitationData invitationData = CallInvitationServiceImpl.getInstance().getCallInvitationData();
        ZegoUIKitPrebuiltCallConfig config = null;
        ZegoUIKitPrebuiltCallConfigProvider provider = CallInvitationServiceImpl.getInstance().getProvider();
        if (provider != null) {
            config = provider.requireConfig(invitationData);
        }
        return config;
    }

    private void showWaitingFragment(boolean isIncoming) {
        Bundle bundle = getIntent().getParcelableExtra("bundle");
        CallWaitingFragment fragment = CallWaitingFragment.newInstance(bundle);

        ZegoCallInvitationData invitationData = CallInvitationServiceImpl.getInstance().getCallInvitationData();
        CallInvitationServiceImpl service = CallInvitationServiceImpl.getInstance();
        if (service.getProvider() != null) {
            ZegoUIKitPrebuiltCallConfig prebuiltCallConfig = service.getProvider().requireConfig(invitationData);
            if (prebuiltCallConfig.audioVideoViewConfig != null && prebuiltCallConfig.avatarViewProvider != null) {
                fragment.setAvatarViewProvider(prebuiltCallConfig.avatarViewProvider);
            }
        }
        ZegoUIKitPrebuiltCallInvitationConfig invitationConfig = CallInvitationServiceImpl.getInstance()
            .getCallInvitationConfig();
        if (isIncoming) {
            if (invitationConfig.incomingCallBackground != null) {
                fragment.setBackground(invitationConfig.incomingCallBackground);
            }
        } else {
            if (invitationConfig.outgoingCallBackground != null) {
                fragment.setBackground(invitationConfig.outgoingCallBackground);
            }
        }
        getSupportFragmentManager().beginTransaction().replace(R.id.call_fragment_container, fragment).commitNow();
    }
}