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
import com.zegocloud.uikit.prebuilt.call.event.BackPressEvent;
import com.zegocloud.uikit.prebuilt.call.event.CallEndListener;
import com.zegocloud.uikit.prebuilt.call.event.CallEvents;
import com.zegocloud.uikit.prebuilt.call.event.ZegoCallEndReason;
import com.zegocloud.uikit.prebuilt.call.internal.MiniVideoView;
import com.zegocloud.uikit.prebuilt.call.internal.MiniVideoWindow;
import com.zegocloud.uikit.prebuilt.call.internal.ZegoAudioVideoForegroundView;
import com.zegocloud.uikit.prebuilt.call.internal.ZegoScreenShareForegroundView;
import com.zegocloud.uikit.prebuilt.call.invite.internal.CallInvitationServiceImpl;
import com.zegocloud.uikit.prebuilt.call.invite.internal.ZegoCallText;
import com.zegocloud.uikit.service.defines.ZegoMeRemovedFromRoomListener;
import com.zegocloud.uikit.service.defines.ZegoOnlySelfInRoomListener;
import com.zegocloud.uikit.service.defines.ZegoUIKitCallback;
import com.zegocloud.uikit.service.defines.ZegoUIKitUser;
import im.zego.zegoexpress.ZegoExpressEngine;
import im.zego.zegoexpress.constants.ZegoOrientation;
import im.zego.zegoexpress.constants.ZegoVideoConfigPreset;
import im.zego.zegoexpress.entity.ZegoVideoConfig;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class ZegoUIKitPrebuiltCallFragment extends Fragment {

    private CallFragmentCallBinding binding;
    private List<View> bottomMenuBarBtns = new ArrayList<>();
    private List<View> topMenuBarBtns = new ArrayList<>();
    private OnBackPressedCallback onBackPressedCallback;
    private IntentFilter configurationChangeFilter;
    private BroadcastReceiver configurationChangeReceiver;
    private MiniVideoWindow miniVideoWindow;
    private MiniVideoView contentView;
    private ZegoOnlySelfInRoomListener onlySelfInRoomListener;

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

    public static ZegoUIKitPrebuiltCallFragment newInstanceWithToken(long appID, @NonNull String appToken,
        @NonNull String userID, @NonNull String userName, @NonNull String callID,
        @NonNull ZegoUIKitPrebuiltCallConfig config) {
        ZegoUIKitPrebuiltCallFragment fragment = new ZegoUIKitPrebuiltCallFragment();
        Bundle bundle = new Bundle();
        bundle.putLong("appID", appID);
        bundle.putString("appToken", appToken);
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
        String appToken = arguments.getString("appToken");
        ZegoUIKitPrebuiltCallConfig callConfig = CallInvitationServiceImpl.getInstance().getCallConfig();
        if (appID != 0) {
            if (!TextUtils.isEmpty(appToken)) {
                CallInvitationServiceImpl.getInstance().init(application, appID, null, appToken);
            } else {
                CallInvitationServiceImpl.getInstance().init(application, appID, appSign, null);
            }
            if (callConfig.zegoCallText != null) {
                ZegoUIKit.setLanguage(callConfig.zegoCallText.getLanguage());
            }
            CallInvitationServiceImpl.getInstance().loginUser(userID, userName);
        } else {
            if (callConfig.zegoCallText != null) {
                ZegoUIKit.setLanguage(callConfig.zegoCallText.getLanguage());
            }
        }

        if (callConfig.bottomMenuBarConfig.buttons.contains(ZegoMenuBarButtonName.BEAUTY_BUTTON)
            || callConfig.topMenuBarConfig.buttons.contains(ZegoMenuBarButtonName.BEAUTY_BUTTON)) {
            CallInvitationServiceImpl.getInstance().initBeautyPlugin();
        }

        ZegoCallText zegoCallText = CallInvitationServiceImpl.getInstance().getCallConfig().zegoCallText;
        if (callConfig.hangUpConfirmDialogInfo != null) {
            if (zegoCallText != null) {
                callConfig.hangUpConfirmDialogInfo.title = zegoCallText.leaveTitle;
            }
            if (zegoCallText != null) {
                callConfig.hangUpConfirmDialogInfo.message = zegoCallText.leaveMessage;
            }
            if (zegoCallText != null) {
                callConfig.hangUpConfirmDialogInfo.cancelButtonName = zegoCallText.cancel;
            }
            if (zegoCallText != null) {
                callConfig.hangUpConfirmDialogInfo.confirmButtonName = zegoCallText.confirm;
            }
        }

        configurationChangeFilter = new IntentFilter();
        configurationChangeFilter.addAction("android.intent.action.CONFIGURATION_CHANGED");

        configurationChangeReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                ZegoOrientation orientation = ZegoOrientation.ORIENTATION_0;
                ZegoVideoConfig videoConfig = ZegoExpressEngine.getEngine().getVideoConfig();
                int sEdge = Math.min(videoConfig.encodeWidth, videoConfig.encodeHeight);
                int lEdge = Math.max(videoConfig.encodeWidth, videoConfig.encodeHeight);
                if (Surface.ROTATION_0 == requireActivity().getWindowManager().getDefaultDisplay().getRotation()) {
                    orientation = ZegoOrientation.ORIENTATION_0;
                    videoConfig.setEncodeResolution(sEdge, lEdge);
                } else if (Surface.ROTATION_180 == requireActivity().getWindowManager().getDefaultDisplay()
                    .getRotation()) {
                    orientation = ZegoOrientation.ORIENTATION_180;
                    videoConfig.setEncodeResolution(sEdge, lEdge);
                } else if (Surface.ROTATION_270 == requireActivity().getWindowManager().getDefaultDisplay()
                    .getRotation()) {
                    orientation = ZegoOrientation.ORIENTATION_270;
                    videoConfig.setEncodeResolution(lEdge, sEdge);
                } else if (Surface.ROTATION_90 == requireActivity().getWindowManager().getDefaultDisplay()
                    .getRotation()) {
                    orientation = ZegoOrientation.ORIENTATION_90;
                    videoConfig.setEncodeResolution(lEdge, sEdge);
                }
                ZegoUIKit.setAppOrientation(orientation);
                ZegoUIKit.setVideoConfig(videoConfig);
            }
        };
        requireActivity().registerReceiver(configurationChangeReceiver, configurationChangeFilter);

        onBackPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                BackPressEvent backPressEvent = ZegoUIKitPrebuiltCallService.events.callEvents.getBackPressEvent();
                if (backPressEvent != null && backPressEvent.onBackPressed()) {

                } else {
                    if (callConfig.hangUpConfirmDialogInfo != null) {
                        handleFragmentBackPressed(callConfig.hangUpConfirmDialogInfo);
                    } else {
                        CallEndListener callEndListener = ZegoUIKitPrebuiltCallService.events.callEvents.getCallEndListener();
                        if (callEndListener != null) {
                            callEndListener.onCallEnd(ZegoCallEndReason.LOCAL_HANGUP, null);
                        }
                        setEnabled(false);
                        endCall();
                        CallInvitationServiceImpl.getInstance().leaveRoomInternal();
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
            dismissMiniVideoWindow();
            CallInvitationServiceImpl.getInstance().leaveRoomInternal();
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding.bottomMenuBar.dismissMoreDialog();
        if (configurationChangeReceiver != null) {
            requireActivity().unregisterReceiver(configurationChangeReceiver);
            configurationChangeReceiver = null;
            dismissMiniVideoWindow();
            CallInvitationServiceImpl.getInstance().leaveRoomInternal();
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
        boolean hasMiniButton = callConfig.bottomMenuBarConfig.buttons.contains(ZegoMenuBarButtonName.MINIMIZING_BUTTON)
            || callConfig.topMenuBarConfig.buttons.contains(ZegoMenuBarButtonName.MINIMIZING_BUTTON);
        return isInRoom && isCallInvite && checkAlertWindowPermission() && hasMiniButton;
    }

    public void minimizeCall() {
        if (canShowMiniWindow()) {
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
            ZegoOnlySelfInRoomListener selfInRoomListener = ZegoUIKitPrebuiltCallService.events.callEvents.getOnlySelfInRoomListener();
            if (onlySelfInRoomListener != null) {
                onlySelfInRoomListener.onOnlySelfInRoom();
            } else if (selfInRoomListener != null) {
                selfInRoomListener.onOnlySelfInRoom();
            } else {
                CallEndListener callEndListener = ZegoUIKitPrebuiltCallService.events.callEvents.getCallEndListener();
                if (callEndListener != null) {
                    callEndListener.onCallEnd(ZegoCallEndReason.REMOTE_HANGUP, "");
                }
                endCall();
                CallInvitationServiceImpl.getInstance().leaveRoomInternal();
            }
        });

        ZegoUIKit.addOnMeRemovedFromRoomListener(new ZegoMeRemovedFromRoomListener() {
            @Override
            public void onMeRemovedFromRoom() {
                if (callConfig.removedFromRoomListener == null) {
                    endCall();
                    //callEndListener has been invoked in callInvitationServiceImpl.onIMRecvCustomCommand();
                    CallInvitationServiceImpl.getInstance().leaveRoomInternal();
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
            String camera = "";
            String mic = "";
            String settings = "";
            String cancel = "";
            String ok = "";
            String micAndCamera = "";
            String settingsCamera = "";
            String settingsMic = "";
            String settingsMicAndCamera = "";
            ZegoCallText zegoCallText = CallInvitationServiceImpl.getInstance().getCallConfig().zegoCallText;
            if (zegoCallText != null) {
                camera = zegoCallText.permissionExplainCamera;
                mic = zegoCallText.permissionExplainMic;
                micAndCamera = zegoCallText.permissionExplainMicAndCamera;
                settings = zegoCallText.settings;
                cancel = zegoCallText.cancel;
                settingsCamera = zegoCallText.settingCamera;
                settingsMic = zegoCallText.settingMic;
                settingsMicAndCamera = zegoCallText.settingMicAndCamera;
                ok = zegoCallText.ok;
            }
            if (deniedList.size() == 1) {
                if (deniedList.contains(permission.CAMERA)) {
                    message = camera;
                } else if (deniedList.contains(permission.RECORD_AUDIO)) {
                    message = mic;
                }
            } else {
                message = micAndCamera;
            }
            scope.showRequestReasonDialog(deniedList, message, ok);
        }).onForwardToSettings((scope, deniedList) -> {
            String message = "";
            String camera = "";
            String mic = "";
            String settings = "";
            String cancel = "";
            String ok = "";
            String micAndCamera = "";
            String settingsCamera = "";
            String settingsMic = "";
            String settingsMicAndCamera = "";
            ZegoCallText zegoCallText = CallInvitationServiceImpl.getInstance().getCallConfig().zegoCallText;
            if (zegoCallText != null) {
                camera = zegoCallText.permissionExplainCamera;
                mic = zegoCallText.permissionExplainMic;
                micAndCamera = zegoCallText.permissionExplainMicAndCamera;
                settings = zegoCallText.settings;
                cancel = zegoCallText.cancel;
                settingsCamera = zegoCallText.settingCamera;
                settingsMic = zegoCallText.settingMic;
                settingsMicAndCamera = zegoCallText.settingMicAndCamera;
                ok = zegoCallText.ok;
            }
            if (deniedList.size() == 1) {
                if (deniedList.contains(permission.CAMERA)) {
                    message = settingsCamera;
                } else if (deniedList.contains(permission.RECORD_AUDIO)) {
                    message = settingsMic;
                }
            } else {
                message = settingsMicAndCamera;
            }
            scope.showForwardToSettingsDialog(deniedList, message, settings, cancel);
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
                dialog.dismiss();
                CallEndListener callEndListener = ZegoUIKitPrebuiltCallService.events.callEvents.getCallEndListener();
                if (callEndListener != null) {
                    callEndListener.onCallEnd(ZegoCallEndReason.LOCAL_HANGUP, null);
                }
                endCall();
                CallInvitationServiceImpl.getInstance().leaveRoomInternal();
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

    public void endCall() {
        dismissMiniVideoWindow();
        requireActivity().finish();
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

    /**
     * use ZegoUIKitPrebuiltCallService.events.callEvents.setOnlySelfInRoomListener() instead;
     *
     * @deprecated use {@link CallEvents#setOnlySelfInRoomListener(ZegoOnlySelfInRoomListener)} instead.
     */
    @Deprecated
    public void setOnOnlySelfInRoomListener(ZegoOnlySelfInRoomListener listener) {
        this.onlySelfInRoomListener = listener;
    }

    public interface LeaveCallListener {

        void onLeaveCall();
    }
}