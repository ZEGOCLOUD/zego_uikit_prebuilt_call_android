package com.zegocloud.uikit.prebuilt.call;

import android.Manifest.permission;
import android.app.Application;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.permissionx.guolindev.PermissionX;
import com.permissionx.guolindev.callback.RequestCallback;
import com.zegocloud.uikit.ZegoUIKit;
import com.zegocloud.uikit.components.audiovideo.ZegoForegroundViewProvider;
import com.zegocloud.uikit.components.audiovideocontainer.ZegoAudioVideoComparator;
import com.zegocloud.uikit.components.audiovideocontainer.ZegoAudioVideoViewConfig;
import com.zegocloud.uikit.components.audiovideocontainer.ZegoLayoutMode;
import com.zegocloud.uikit.components.memberlist.ZegoMemberListItemViewProvider;
import com.zegocloud.uikit.prebuilt.call.config.ZegoHangUpConfirmDialogInfo;
import com.zegocloud.uikit.prebuilt.call.databinding.CallFragmentCallBinding;
import com.zegocloud.uikit.prebuilt.call.internal.CallConfigGlobal;
import com.zegocloud.uikit.prebuilt.call.internal.ZegoVideoForegroundView;
import com.zegocloud.uikit.prebuilt.call.invite.ZegoCallInvitationData;
import com.zegocloud.uikit.prebuilt.call.invite.internal.CallInvitationServiceImpl;
import com.zegocloud.uikit.service.defines.ZegoOnlySelfInRoomListener;
import com.zegocloud.uikit.service.defines.ZegoScenario;
import com.zegocloud.uikit.service.defines.ZegoUIKitCallback;
import com.zegocloud.uikit.service.defines.ZegoUIKitUser;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ZegoUIKitPrebuiltCallFragment extends Fragment {

    private CallFragmentCallBinding binding;
    private List<View> bottomMenuBarBtns = new ArrayList<>();
    private List<View> topMenuBarBtns = new ArrayList<>();
    private OnBackPressedCallback onBackPressedCallback;
    private LeaveCallListener leaveCallListener;
    private ZegoOnlySelfInRoomListener onlySelfInRoomListener;

    public static ZegoUIKitPrebuiltCallFragment newInstance(ZegoCallInvitationData data,
        ZegoUIKitPrebuiltCallConfig config) {
        ZegoUIKitPrebuiltCallFragment fragment = new ZegoUIKitPrebuiltCallFragment();
        Bundle bundle = new Bundle();
        bundle.putString("callID", data.callID);
        bundle.putSerializable("config", config);
        fragment.setArguments(bundle);
        CallInvitationServiceImpl.getInstance().addZegoUIKitPrebuiltCallFragment(fragment);
        return fragment;
    }

    public static ZegoUIKitPrebuiltCallFragment newInstance(long appID, @NonNull String appSign, @NonNull String userID,
        @NonNull String userName, @NonNull String callID, @NonNull ZegoUIKitPrebuiltCallConfig config) {
        ZegoUIKitPrebuiltCallFragment fragment = new ZegoUIKitPrebuiltCallFragment();
        Bundle bundle = new Bundle();
        bundle.putLong("appID", appID);
        bundle.putString("appSign", appSign);
        bundle.putString("userID", userID);
        bundle.putString("userName", userName);
        bundle.putString("callID", callID);
        bundle.putSerializable("config", config);
        fragment.setArguments(bundle);
        CallInvitationServiceImpl.getInstance().addZegoUIKitPrebuiltCallFragment(fragment);
        return fragment;
    }

    public static ZegoUIKitPrebuiltCallFragment newInstance(long appID, @NonNull String appSign, @NonNull String userID,
        @NonNull String userName, @NonNull String callID) {
        return newInstance(appID, appSign, userID, userName, callID, new ZegoUIKitPrebuiltCallConfig());
    }

    public ZegoUIKitPrebuiltCallFragment() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arguments = getArguments();
        ZegoUIKitPrebuiltCallConfig config = (ZegoUIKitPrebuiltCallConfig) arguments.getSerializable("config");
        CallConfigGlobal.getInstance().setConfig(config);
        Application application = requireActivity().getApplication();
        long appID = arguments.getLong("appID");
        String appSign = arguments.getString("appSign");
        String userID = arguments.getString("userID");
        String userName = arguments.getString("userName");
        if (appID != 0) {
            ZegoUIKit.init(application, appID, appSign, ZegoScenario.GENERAL);
            ZegoUIKit.login(userID, userName);
            ZegoUIKit.getSignalingPlugin().login(userID, userName, null);
        }
        if (config.hangUpConfirmDialogInfo != null) {
            if (TextUtils.isEmpty(config.hangUpConfirmDialogInfo.title)) {
                config.hangUpConfirmDialogInfo.title = getString(R.string.call_leave_title);
            }
            if (TextUtils.isEmpty(config.hangUpConfirmDialogInfo.message)) {
                config.hangUpConfirmDialogInfo.message = getString(R.string.call_leave_message);
            }
            if (TextUtils.isEmpty(config.hangUpConfirmDialogInfo.cancelButtonName)) {
                config.hangUpConfirmDialogInfo.cancelButtonName = getString(R.string.call_leave_cancel);
            }
            if (TextUtils.isEmpty(config.hangUpConfirmDialogInfo.confirmButtonName)) {
                config.hangUpConfirmDialogInfo.confirmButtonName = getString(R.string.call_leave_confirm);
            }
        }
        onBackPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (config.hangUpConfirmDialogInfo != null) {
                    handleFragmentBackPressed(config.hangUpConfirmDialogInfo);
                } else {
                    leaveRoom();
                    setEnabled(false);
                    requireActivity().onBackPressed();
                }
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        leaveRoom();
        CallConfigGlobal.getInstance().clear();
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
            ZegoUIKit.joinRoom(callID, new ZegoUIKitCallback() {
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
        ZegoUIKitPrebuiltCallConfig config = (ZegoUIKitPrebuiltCallConfig) getArguments().getSerializable("config");

        applyMenuBarConfig(config);

        ZegoUIKit.setAudioOutputToSpeaker(config.useSpeakerWhenJoining);

        applyAudioVideoViewConfig(config);

        requestPermissionIfNeeded((allGranted, grantedList, deniedList) -> {
            if (grantedList.contains(permission.CAMERA)) {
                if (config.turnOnCameraWhenJoining) {
                    ZegoUIKit.turnCameraOn(userID, true);
                }
            }
            if (grantedList.contains(permission.RECORD_AUDIO)) {
                if (config.turnOnMicrophoneWhenJoining) {
                    ZegoUIKit.turnMicrophoneOn(userID, true);
                }
            }
        });

        ZegoUIKit.addOnOnlySelfInRoomListener(() -> {
            if (onlySelfInRoomListener != null) {
                onlySelfInRoomListener.onOnlySelfInRoom();
            } else {
                leaveRoom();
                requireActivity().finish();
            }
        });
    }

    private void applyAudioVideoViewConfig(ZegoUIKitPrebuiltCallConfig config) {
        ZegoForegroundViewProvider provider = CallConfigGlobal.getInstance().getVideoViewForegroundViewProvider();
        if (provider == null) {
            binding.avcontainer.setForegroundViewProvider(new ZegoForegroundViewProvider() {
                @Override
                public View getForegroundView(ViewGroup parent, ZegoUIKitUser uiKitUser) {
                    ZegoVideoForegroundView foregroundView = new ZegoVideoForegroundView(getContext(), uiKitUser);
                    foregroundView.showMicrophone(config.audioVideoViewConfig.showMicrophoneStateOnView);
                    foregroundView.showCamera(config.audioVideoViewConfig.showCameraStateOnView);
                    foregroundView.showUserName(config.audioVideoViewConfig.showUserNameOnView);
                    return foregroundView;
                }
            });
        } else {
            binding.avcontainer.setForegroundViewProvider(provider);
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

        ZegoAudioVideoViewConfig audioVideoViewConfig = new ZegoAudioVideoViewConfig();
        audioVideoViewConfig.showSoundWavesInAudioMode = config.audioVideoViewConfig.showSoundWavesInAudioMode;
        audioVideoViewConfig.useVideoViewAspectFill = config.audioVideoViewConfig.useVideoViewAspectFill;
        binding.avcontainer.setAudioVideoConfig(audioVideoViewConfig);
    }

    private void requestPermissionIfNeeded(RequestCallback requestCallback) {
        List<String> permissions = Arrays.asList(permission.CAMERA, permission.RECORD_AUDIO);

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
            scope.showRequestReasonDialog(deniedList, message, getString(R.string.call_ok));
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
            scope.showForwardToSettingsDialog(deniedList, message, getString(R.string.call_settings),
                getString(R.string.call_cancel));
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
        CallConfigGlobal.getInstance().setLeaveCallListener(new LeaveCallListener() {
            @Override
            public void onLeaveCall() {
                CallInvitationServiceImpl.getInstance().leaveRoom();
                if (leaveCallListener != null) {
                    leaveCallListener.onLeaveCall();
                } else {
                    ZegoUIKit.leaveRoom();
                    requireActivity().finish();
                }
            }
        });
        binding.bottomMenuBar.setConfig(config.bottomMenuBarConfig);
        binding.topMenuBar.setConfig(config.topMenuBarConfig);
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
        CallInvitationServiceImpl.getInstance().leaveRoom();
        ZegoUIKit.leaveRoom();
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

    public void setForegroundViewProvider(ZegoForegroundViewProvider provider) {
        CallConfigGlobal.getInstance().setVideoViewForegroundViewProvider(provider);
    }

    public void setLeaveCallListener(LeaveCallListener listener) {
        this.leaveCallListener = listener;
    }

    public void setMemberListItemViewProvider(ZegoMemberListItemViewProvider memberListItemProvider) {
        CallConfigGlobal.getInstance().setMemberListItemProvider(memberListItemProvider);
    }

    public void setOnOnlySelfInRoomListener(ZegoOnlySelfInRoomListener listener) {
        this.onlySelfInRoomListener = listener;
    }

    public interface LeaveCallListener {

        void onLeaveCall();
    }
}