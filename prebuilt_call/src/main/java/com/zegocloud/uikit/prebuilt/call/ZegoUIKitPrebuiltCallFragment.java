package com.zegocloud.uikit.prebuilt.call;

import android.Manifest.permission;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.permissionx.guolindev.PermissionX;
import com.permissionx.guolindev.callback.RequestCallback;
import com.zegocloud.uikit.ZegoUIKit;
import com.zegocloud.uikit.components.audiovideo.ZegoBaseAudioVideoForegroundView;
import com.zegocloud.uikit.components.audiovideo.ZegoForegroundViewProvider;
import com.zegocloud.uikit.components.audiovideocontainer.ZegoAudioVideoComparator;
import com.zegocloud.uikit.components.audiovideocontainer.ZegoAudioVideoViewConfig;
import com.zegocloud.uikit.components.audiovideocontainer.ZegoLayoutGalleryConfig;
import com.zegocloud.uikit.components.audiovideocontainer.ZegoLayoutMode;
import com.zegocloud.uikit.prebuilt.call.config.DurationUpdateListener;
import com.zegocloud.uikit.prebuilt.call.config.ZegoHangUpConfirmDialogInfo;
import com.zegocloud.uikit.prebuilt.call.config.ZegoMenuBarButtonName;
import com.zegocloud.uikit.prebuilt.call.databinding.CallFragmentCallBinding;
import com.zegocloud.uikit.prebuilt.call.internal.MiniVideoView;
import com.zegocloud.uikit.prebuilt.call.internal.MiniVideoWindow;
import com.zegocloud.uikit.prebuilt.call.internal.ZegoAudioVideoForegroundView;
import com.zegocloud.uikit.prebuilt.call.internal.ZegoScreenShareForegroundView;
import com.zegocloud.uikit.prebuilt.call.invite.BackPressEvent;
import com.zegocloud.uikit.prebuilt.call.invite.ZegoUIKitPrebuiltCallInvitationService;
import com.zegocloud.uikit.prebuilt.call.invite.internal.CallInvitationServiceImpl;
import com.zegocloud.uikit.prebuilt.call.invite.internal.LeaveRoomListener;
import com.zegocloud.uikit.service.defines.ZegoMeRemovedFromRoomListener;
import com.zegocloud.uikit.service.defines.ZegoOnlySelfInRoomListener;
import com.zegocloud.uikit.service.defines.ZegoUIKitCallback;
import com.zegocloud.uikit.service.defines.ZegoUIKitUser;
import im.zego.zegoexpress.constants.ZegoOrientation;
import im.zego.zegoexpress.constants.ZegoVideoConfigPreset;
import im.zego.zegoexpress.entity.ZegoVideoConfig;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import timber.log.Timber;

public class ZegoUIKitPrebuiltCallFragment extends Fragment {

    private CallFragmentCallBinding binding;
    private List<View> bottomMenuBarBtns = new ArrayList<>();
    private List<View> topMenuBarBtns = new ArrayList<>();
    private OnBackPressedCallback onBackPressedCallback;
    private IntentFilter configurationChangeFilter;
    private BroadcastReceiver configurationChangeReceiver;
    private MiniVideoWindow miniVideoWindow;
    private MiniVideoView contentView;

    /**
     * start by call-invite，is already init first,only need callID to join room.
     *
     * @param callID the roomID received from call-invite to join
     * @param config get by ZegoUIKitPrebuiltCallInvitationConfig.provider passed by user
     * @return
     */
    public static ZegoUIKitPrebuiltCallFragment newInstance(Context context, String callID,
        ZegoUIKitPrebuiltCallConfig config) {

        ZegoUIKitPrebuiltCallFragment fragment = new ZegoUIKitPrebuiltCallFragment();
        Bundle bundle = new Bundle();
        bundle.putString("callID", callID);
        fragment.setArguments(bundle);
        CallInvitationServiceImpl.getInstance().setCallConfig(config);
        CallInvitationServiceImpl.getInstance().setZegoUIKitPrebuiltCallFragment(fragment);
        return fragment;
    }

    /**
     * start by no-invite join,there is no application,so can't init here,just set config
     *
     * @param appID
     * @param appSign
     * @param userID
     * @param userName
     * @param callID
     * @param config
     * @return
     */
    public static ZegoUIKitPrebuiltCallFragment newInstance(long appID, @NonNull String appSign, @NonNull String userID,
        @NonNull String userName, @NonNull String callID, @NonNull ZegoUIKitPrebuiltCallConfig config) {
        ZegoUIKitPrebuiltCallFragment fragment = new ZegoUIKitPrebuiltCallFragment();
        Bundle bundle = new Bundle();
        bundle.putLong("appID", appID);
        bundle.putString("appSign", appSign);
        bundle.putString("userID", userID);
        bundle.putString("userName", userName);
        bundle.putString("callID", callID);
        fragment.setArguments(bundle);

        CallInvitationServiceImpl.getInstance().setCallConfig(config);
        CallInvitationServiceImpl.getInstance().setZegoUIKitPrebuiltCallFragment(fragment);
        return fragment;
    }

    public ZegoUIKitPrebuiltCallFragment() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arguments = getArguments();
        Application application = requireActivity().getApplication();
        long appID = arguments.getLong("appID");
        String appSign = arguments.getString("appSign");
        String userID = arguments.getString("userID");
        String userName = arguments.getString("userName");
        if (appID != 0) {
            CallInvitationServiceImpl.getInstance().initAndLoginUser(application, appID, appSign, userID, userName);
        }

        ZegoUIKitPrebuiltCallConfig callConfig = CallInvitationServiceImpl.getInstance().getCallConfig();

        if (callConfig.bottomMenuBarConfig.buttons.contains(ZegoMenuBarButtonName.BEAUTY_BUTTON)) {
            CallInvitationServiceImpl.getInstance().initBeautyPlugin();
        }
        if (callConfig.hangUpConfirmDialogInfo != null) {
            if (TextUtils.isEmpty(callConfig.hangUpConfirmDialogInfo.title)) {
                callConfig.hangUpConfirmDialogInfo.title = getString(R.string.call_leave_title);
            }
            if (TextUtils.isEmpty(callConfig.hangUpConfirmDialogInfo.message)) {
                callConfig.hangUpConfirmDialogInfo.message = getString(R.string.call_leave_message);
            }
            if (TextUtils.isEmpty(callConfig.hangUpConfirmDialogInfo.cancelButtonName)) {
                callConfig.hangUpConfirmDialogInfo.cancelButtonName = getString(R.string.call_leave_cancel);
            }
            if (TextUtils.isEmpty(callConfig.hangUpConfirmDialogInfo.confirmButtonName)) {
                callConfig.hangUpConfirmDialogInfo.confirmButtonName = getString(R.string.call_leave_confirm);
            }
        }

        configurationChangeFilter = new IntentFilter();
        configurationChangeFilter.addAction("android.intent.action.CONFIGURATION_CHANGED");

        configurationChangeReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                ZegoOrientation orientation = ZegoOrientation.ORIENTATION_0;

                if (Surface.ROTATION_0 == requireActivity().getWindowManager().getDefaultDisplay().getRotation()) {
                    orientation = ZegoOrientation.ORIENTATION_0;
                } else if (Surface.ROTATION_180 == requireActivity().getWindowManager().getDefaultDisplay()
                    .getRotation()) {
                    orientation = ZegoOrientation.ORIENTATION_180;
                } else if (Surface.ROTATION_270 == requireActivity().getWindowManager().getDefaultDisplay()
                    .getRotation()) {
                    orientation = ZegoOrientation.ORIENTATION_270;
                } else if (Surface.ROTATION_90 == requireActivity().getWindowManager().getDefaultDisplay()
                    .getRotation()) {
                    orientation = ZegoOrientation.ORIENTATION_90;
                }
                ZegoUIKit.setAppOrientation(orientation);
            }
        };
        requireActivity().registerReceiver(configurationChangeReceiver, configurationChangeFilter);

        onBackPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                BackPressEvent backPressEvent = ZegoUIKitPrebuiltCallInvitationService.events.getBackPressEvent();
                if (backPressEvent != null && backPressEvent.onBackPressed()) {

                } else {
                    if (callConfig.hangUpConfirmDialogInfo != null) {
                        handleFragmentBackPressed(callConfig.hangUpConfirmDialogInfo);
                    } else {
                        setEnabled(false);
                        leaveRoom();
                        requireActivity().onBackPressed();
                    }
                }

            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);
    }

    @Override
    public void onResume() {
        super.onResume();

        boolean isInRoom = CallInvitationServiceImpl.getInstance().isInRoom();
        if (isInRoom) {
            long startTimeLocal = CallInvitationServiceImpl.getInstance().getStartTimeLocal();
            long elapsedTime = (System.currentTimeMillis() - startTimeLocal) / 1000;
            if (binding != null) {
                binding.timeElapsed.setText(getElapsedTimeString(elapsedTime));
                binding.avcontainer.updateLayout();
            }
            CallInvitationServiceImpl.getInstance().setDurationUpdateListener(new DurationUpdateListener() {
                @Override
                public void onDurationUpdate(long seconds) {
                    if (binding != null) {
                        binding.timeElapsed.setText(getElapsedTimeString(seconds));
                    }
                }
            });
            dismissMiniVideoWindow();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (requireActivity().isFinishing()) {
            if (configurationChangeReceiver != null) {
                requireActivity().unregisterReceiver(configurationChangeReceiver);
                configurationChangeReceiver = null;
            }
            leaveRoom();
            CallInvitationServiceImpl.getInstance().setLeaveRoomListener(null);
            CallInvitationServiceImpl.getInstance().setZegoUIKitPrebuiltCallFragment(null);
        }

    }

    @Override
    public void onStop() {
        super.onStop();
        if (!requireActivity().isFinishing()) {
            if (canShowMiniWindow()) {
                showMiniVideoWindow();
            }
        }
    }

    private boolean canShowMiniWindow() {
        boolean isCallInvite = CallInvitationServiceImpl.getInstance().getCallInvitationConfig() != null;
        boolean isInRoom = CallInvitationServiceImpl.getInstance().isInRoom();
        ZegoUIKitPrebuiltCallConfig callConfig = CallInvitationServiceImpl.getInstance().getCallConfig();
        boolean hasMiniButton =
            callConfig.bottomMenuBarConfig.buttons.contains(ZegoMenuBarButtonName.MINIMIZING_BUTTON)
                || callConfig.topMenuBarConfig.buttons.contains(ZegoMenuBarButtonName.MINIMIZING_BUTTON);
        return isInRoom && isCallInvite && checkAlertWindowPermission() && hasMiniButton;
    }

    public void minimizeCall(){
        if(canShowMiniWindow()){
            requireActivity().moveTaskToBack(true);
        }
    }

    private void handleFragmentBackPressed(ZegoHangUpConfirmDialogInfo quitInfo) {
        showQuitDialog(quitInfo.title, quitInfo.message, quitInfo.confirmButtonName, quitInfo.cancelButtonName);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
        @Nullable Bundle savedInstanceState) {
        binding = CallFragmentCallBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        String callID = getArguments().getString("callID");
        if (!TextUtils.isEmpty(callID)) {
            CallInvitationServiceImpl.getInstance().joinRoom(callID, new ZegoUIKitCallback() {
                @Override
                public void onResult(int errorCode) {
                    if (errorCode == 0) {
                        onRoomJoinSucceed();
                    } else {
                        onRoomJoinFailed();
                    }
                }
            });
        }
    }

    private void onRoomJoinFailed() {

    }

    private void onRoomJoinSucceed() {
        String userID = ZegoUIKit.getLocalUser().userID;

        ZegoUIKitPrebuiltCallConfig callConfig = CallInvitationServiceImpl.getInstance().getCallConfig();

        applyMenuBarConfig(callConfig);

        applyAudioVideoViewConfig(callConfig);

        ZegoUIKit.setAudioOutputToSpeaker(callConfig.useSpeakerWhenJoining);

        List<String> permissions = new ArrayList<>();
        if (callConfig.turnOnCameraWhenJoining) {
            permissions.add(permission.CAMERA);
        }
        if (callConfig.turnOnMicrophoneWhenJoining) {
            permissions.add(permission.RECORD_AUDIO);
        }
        requestPermissionIfNeeded(permissions, (allGranted, grantedList, deniedList) -> {
            if (callConfig.turnOnCameraWhenJoining) {
                if (grantedList.contains(permission.CAMERA)) {
                    ZegoUIKit.turnCameraOn(userID, true);
                }
            } else {
                ZegoUIKit.turnCameraOn(userID, false);
            }
            if (callConfig.turnOnMicrophoneWhenJoining) {
                if (grantedList.contains(permission.RECORD_AUDIO)) {
                    ZegoUIKit.turnMicrophoneOn(userID, true);
                }
            } else {
                ZegoUIKit.turnMicrophoneOn(userID, false);
            }
        });

        ZegoUIKit.addOnOnlySelfInRoomListener(() -> {
            ZegoOnlySelfInRoomListener selfInRoomListener = ZegoUIKitPrebuiltCallInvitationService.events.getOnlySelfInRoomListener();
            if (selfInRoomListener != null) {
                selfInRoomListener.onOnlySelfInRoom();
            } else {
                leaveRoom();
                requireActivity().finish();
            }
        });
        CallInvitationServiceImpl.getInstance().setLeaveRoomListener(new LeaveRoomListener() {
            @Override
            public void onLeaveRoom() {
                Timber.d("onLeaveRoom() called");
                dismissMiniVideoWindow();
                requireActivity().finish();
            }
        });

        ZegoUIKit.addOnMeRemovedFromRoomListener(new ZegoMeRemovedFromRoomListener() {
            @Override
            public void onMeRemovedFromRoom() {
                if (callConfig.removedFromRoomListener == null) {
                    leaveRoom();
                    requireActivity().finish();
                }
            }
        });
    }

    private void dismissMiniVideoWindow() {
        if (miniVideoWindow != null && miniVideoWindow.isShown()) {
            miniVideoWindow.dismissMinimalWindow();
        }
    }

    private boolean checkAlertWindowPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Settings.canDrawOverlays(getContext());
        } else {
            return true;
        }
    }

    private void showMiniVideoWindow() {
        FragmentActivity context = requireActivity();
        if (miniVideoWindow == null) {
            miniVideoWindow = new MiniVideoWindow(context);
            contentView = new MiniVideoView(context);
            //            contentView.setOnClickListener(v -> {
            //                //                // 获取当前任务的ID
            //                //                ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            //                //                List<AppTask> appTasks = activityManager.getAppTasks();
            //
            //                //                if (VERSION.SDK_INT >= VERSION_CODES.M) {
            //                //                    for (int index = appTasks.size(); index > 0; index--) {
            //                //                        RecentTaskInfo taskInfo = appTasks.get(index).getTaskInfo();
            //                //                        if (!Objects.equals(taskInfo.topActivity, requireActivity().getComponentName())) {
            //                //                            activityManager.moveTaskToFront(taskInfo.taskId, ActivityManager.MOVE_TASK_WITH_HOME);
            //                //                        }
            //                //                    }
            //                //                }
            //
            //                Intent intent2 = new Intent(context, context.getClass());
            //                intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            //                context.startActivity(intent2);
            //            });
        }

        long startTimeLocal = CallInvitationServiceImpl.getInstance().getStartTimeLocal();
        long elapsedTime = (System.currentTimeMillis() - startTimeLocal) / 1000;
        contentView.setText(getElapsedTimeString(elapsedTime));
        contentView.updateVideo();
        CallInvitationServiceImpl.getInstance().setDurationUpdateListener(new DurationUpdateListener() {
            @Override
            public void onDurationUpdate(long seconds) {
                contentView.setText(getElapsedTimeString(seconds));
            }
        });
        if (!miniVideoWindow.isShown()) {
            miniVideoWindow.showMinimalWindow(contentView);
        }
    }

    private void applyAudioVideoViewConfig(ZegoUIKitPrebuiltCallConfig config) {
        if (config.audioVideoViewConfig == null
            || config.audioVideoViewConfig.videoViewForegroundViewProvider == null) {
            binding.avcontainer.setAudioVideoForegroundViewProvider(new ZegoForegroundViewProvider() {
                @Override
                public ZegoBaseAudioVideoForegroundView getForegroundView(ViewGroup parent, ZegoUIKitUser uiKitUser) {
                    ZegoAudioVideoForegroundView foregroundView = new ZegoAudioVideoForegroundView(parent.getContext(),
                        uiKitUser.userID);
                    foregroundView.showMicrophoneView(config.audioVideoViewConfig.showMicrophoneStateOnView);
                    foregroundView.showCameraView(config.audioVideoViewConfig.showCameraStateOnView);
                    foregroundView.showUserNameView(config.audioVideoViewConfig.showUserNameOnView);
                    return foregroundView;
                }
            });
        } else {
            binding.avcontainer.setAudioVideoForegroundViewProvider(
                config.audioVideoViewConfig.videoViewForegroundViewProvider);
        }
        if (config.avatarViewProvider != null) {
            binding.avcontainer.setAvatarViewProvider(config.avatarViewProvider);
        }
        binding.avcontainer.setLayout(config.layout);
        binding.avcontainer.setAudioVideoComparator(new ZegoAudioVideoComparator() {
            @Override
            public List<ZegoUIKitUser> sortAudioVideo(List<ZegoUIKitUser> userList) {
                if (config.layout.mode == ZegoLayoutMode.PICTURE_IN_PICTURE) {
                    if (userList.size() > 1) {
                        ZegoUIKitUser localUser = ZegoUIKit.getLocalUser();
                        int index = userList.indexOf(localUser);
                        if (index >= 0) {
                            userList.remove(localUser);
                            userList.add(1, localUser);
                        }
                    }
                    return userList;
                } else {
                    List<ZegoUIKitUser> sortUsers = new ArrayList<>();
                    ZegoUIKitUser self = ZegoUIKit.getLocalUser();
                    userList.remove(self);
                    Collections.reverse(userList);
                    sortUsers.add(self);
                    sortUsers.addAll(userList);
                    return sortUsers;
                }
            }
        });

        binding.avcontainer.setScreenShareForegroundViewProvider((parent, uiKitUser) -> {
            ZegoScreenShareForegroundView foregroundView = new ZegoScreenShareForegroundView(parent, uiKitUser.userID);
            foregroundView.setParentContainer(binding.avcontainer);

            if (config.layout.config instanceof ZegoLayoutGalleryConfig) {
                ZegoLayoutGalleryConfig galleryConfig = (ZegoLayoutGalleryConfig) config.layout.config;
                foregroundView.setToggleButtonRules(galleryConfig.showScreenSharingFullscreenModeToggleButtonRules);
            }

            return foregroundView;
        });

        ZegoAudioVideoViewConfig audioVideoViewConfig = new ZegoAudioVideoViewConfig();
        audioVideoViewConfig.showSoundWavesInAudioMode = config.audioVideoViewConfig.showSoundWavesInAudioMode;
        audioVideoViewConfig.useVideoViewAspectFill = config.audioVideoViewConfig.useVideoViewAspectFill;

        binding.avcontainer.setAudioVideoConfig(audioVideoViewConfig);
        if (config.videoConfig != null) {
            ZegoVideoConfigPreset zegoVideoConfigPreset = ZegoVideoConfigPreset.getZegoVideoConfigPreset(
                config.videoConfig.resolution.value());
            ZegoUIKit.setVideoConfig(new ZegoVideoConfig(zegoVideoConfigPreset));
        }
    }

    private void requestPermissionIfNeeded(List<String> permissions, RequestCallback requestCallback) {
        boolean allGranted = true;
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(getContext(), permission) != PackageManager.PERMISSION_GRANTED) {
                allGranted = false;
            }
        }
        if (allGranted) {
            requestCallback.onResult(true, permissions, new ArrayList<>());
            return;
        }
        PermissionX.init(requireActivity()).permissions(permissions).onExplainRequestReason((scope, deniedList) -> {
            String message = "";
            if (deniedList.size() == 1) {
                if (deniedList.contains(permission.CAMERA)) {
                    message = getContext().getString(R.string.call_permission_explain_camera);
                } else if (deniedList.contains(permission.RECORD_AUDIO)) {
                    message = getContext().getString(R.string.call_permission_explain_mic);
                }
            } else {
                message = getContext().getString(R.string.call_permission_explain_camera_mic);
            }
            scope.showRequestReasonDialog(deniedList, message, getContext().getString(R.string.call_ok));
        }).onForwardToSettings((scope, deniedList) -> {
            String message = "";
            if (deniedList.size() == 1) {
                if (deniedList.contains(permission.CAMERA)) {
                    message = getContext().getString(R.string.call_settings_camera);
                } else if (deniedList.contains(permission.RECORD_AUDIO)) {
                    message = getContext().getString(R.string.call_settings_mic);
                }
            } else {
                message = getContext().getString(R.string.call_settings_camera_mic);
            }
            scope.showForwardToSettingsDialog(deniedList, message, getContext().getString(R.string.call_settings),
                getContext().getString(R.string.call_cancel));
        }).request(new RequestCallback() {
            @Override
            public void onResult(boolean allGranted, @NonNull List<String> grantedList,
                @NonNull List<String> deniedList) {
                if (requestCallback != null) {
                    requestCallback.onResult(allGranted, grantedList, deniedList);
                }
            }
        });
    }

    private void applyMenuBarConfig(ZegoUIKitPrebuiltCallConfig config) {
        binding.topMenuBar.setInRoomChatConfig(config.inRoomChatConfig);
        binding.bottomMenuBar.setInRoomChatConfig(config.inRoomChatConfig);

        binding.topMenuBar.setConfig(config.topMenuBarConfig);
        binding.bottomMenuBar.setConfig(config.bottomMenuBarConfig);

        binding.bottomMenuBar.setMemberListConfig(config.memberListConfig);
        binding.topMenuBar.setMemberListConfig(config.memberListConfig);

        binding.topMenuBar.setScreenShareVideoConfig(config.screenSharingVideoConfig);
        binding.bottomMenuBar.setScreenShareVideoConfig(config.screenSharingVideoConfig);

        binding.topMenuBar.setHangUpConfirmDialogInfo(config.hangUpConfirmDialogInfo);
        binding.bottomMenuBar.setHangUpConfirmDialogInfo(config.hangUpConfirmDialogInfo);

        binding.bottomMenuBar.setLeaveCallListener(new LeaveCallListener() {
            @Override
            public void onLeaveCall() {
                if (config.leaveCallListener != null) {
                    config.leaveCallListener.onLeaveCall();
                } else {
                    leaveRoom();
                    requireActivity().finish();
                }
            }
        });
        binding.topMenuBar.setLeaveCallListener(new LeaveCallListener() {
            @Override
            public void onLeaveCall() {
                if (config.leaveCallListener != null) {
                    config.leaveCallListener.onLeaveCall();
                } else {
                    leaveRoom();
                    requireActivity().finish();
                }
            }
        });

        if (bottomMenuBarBtns.size() > 0) {
            binding.bottomMenuBar.addButtons(bottomMenuBarBtns);
        }
        if (topMenuBarBtns.size() > 0) {
            binding.topMenuBar.addButtons(topMenuBarBtns);
        }
        binding.getRoot().setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.bottomMenuBar.setOutSideClicked();
                binding.topMenuBar.setOutSideClicked();
            }
        });
        binding.topMenuBar.setTitleText(config.topMenuBarConfig.title);

        CallInvitationServiceImpl.getInstance().setDurationUpdateListener(new DurationUpdateListener() {
            @Override
            public void onDurationUpdate(long seconds) {
                binding.timeElapsed.setText(getElapsedTimeString(seconds));
            }
        });
        if (config.durationConfig != null) {
            if (config.durationConfig.isVisible) {
                binding.timeElapsed.setVisibility(View.VISIBLE);
            } else {
                binding.timeElapsed.setVisibility(View.GONE);
            }
        } else {
            binding.timeElapsed.setVisibility(View.GONE);
        }
    }

    private String getElapsedTimeString(long elapsedTime) {
        String time;
        if (elapsedTime >= 60 * 60) {
            int hour = (int) (elapsedTime / (60 * 60));
            int minutes = (int) ((elapsedTime - hour * (60 * 60)) / (60));
            int seconds = (int) ((elapsedTime - hour * (60 * 60) - minutes * (60)));
            time = String.format(Locale.getDefault(), "%d:%02d:%02d", hour, minutes, seconds);
        } else {
            int minutes = (int) (elapsedTime / (60));
            int seconds = (int) ((elapsedTime - minutes * (60)));
            time = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        }
        return time;
    }

    private void showQuitDialog(String title, String message, String positiveText, String negativeText) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getContext());
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(positiveText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (onBackPressedCallback != null) {
                    onBackPressedCallback.setEnabled(false);
                }
                leaveRoom();
                requireActivity().onBackPressed();
            }
        });
        builder.setNegativeButton(negativeText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setCancelable(false);
        builder.show();
    }

    private void leaveRoom() {
        dismissMiniVideoWindow();
        CallInvitationServiceImpl.getInstance().leaveRoom();
    }

    public void addButtonToBottomMenuBar(List<View> viewList) {
        bottomMenuBarBtns.addAll(viewList);
        if (binding != null) {
            binding.bottomMenuBar.addButtons(viewList);
        }
    }

    public void addButtonToTopMenuBar(List<View> viewList) {
        topMenuBarBtns.addAll(viewList);
        if (binding != null) {
            binding.topMenuBar.addButtons(viewList);
        }
    }

    public void setOnOnlySelfInRoomListener(ZegoOnlySelfInRoomListener listener) {
        ZegoUIKitPrebuiltCallInvitationService.events.setOnlySelfInRoomListener(listener);
    }

    public interface LeaveCallListener {

        void onLeaveCall();
    }
}