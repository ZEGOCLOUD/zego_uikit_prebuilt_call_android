package com.zegocloud.uikit.prebuilt.call;

import android.Manifest.permission;
import android.app.Application;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.permissionx.guolindev.PermissionX;
import com.zegocloud.uikit.ZegoUIKit;
import com.zegocloud.uikit.components.audiovideo.ZegoForegroundViewProvider;
import com.zegocloud.uikit.components.audiovideocontainer.ZegoAudioVideoViewConfig;
import com.zegocloud.uikit.components.common.ZegoMemberListItemViewProvider;
import com.zegocloud.uikit.prebuilt.call.config.ZegoHangUpConfirmDialogInfo;
import com.zegocloud.uikit.prebuilt.call.databinding.FragmentCallBinding;
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
import java.util.List;

public class ZegoUIKitPrebuiltCallFragment extends Fragment {

    private static final String TAG = "CallFragment";
    private FragmentCallBinding binding;
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
        }
        if (config.hangUpConfirmDialogInfo != null) {
            if (TextUtils.isEmpty(config.hangUpConfirmDialogInfo.title)) {
                config.hangUpConfirmDialogInfo.title = getString(R.string.leave_title);
            }
            if (TextUtils.isEmpty(config.hangUpConfirmDialogInfo.message)) {
                config.hangUpConfirmDialogInfo.message = getString(R.string.leave_message);
            }
            if (TextUtils.isEmpty(config.hangUpConfirmDialogInfo.cancelButtonName)) {
                config.hangUpConfirmDialogInfo.cancelButtonName = getString(R.string.leava_cancel);
            }
            if (TextUtils.isEmpty(config.hangUpConfirmDialogInfo.confirmButtonName)) {
                config.hangUpConfirmDialogInfo.confirmButtonName = getString(R.string.leave_confirm);
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
    }

    private void handleFragmentBackPressed(ZegoHangUpConfirmDialogInfo quitInfo) {
        showQuitDialog(quitInfo.title, quitInfo.message, quitInfo.confirmButtonName, quitInfo.cancelButtonName);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
        @Nullable Bundle savedInstanceState) {
        binding = FragmentCallBinding.inflate(inflater, container, false);
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

        ZegoUIKit.turnCameraOn(userID, config.turnOnCameraWhenJoining);
        ZegoUIKit.turnMicrophoneOn(userID, config.turnOnMicrophoneWhenJoining);
        ZegoUIKit.setAudioOutputToSpeaker(config.useSpeakerWhenJoining);

        applyAudioVideoViewConfig(config);

        requestPermissionIfNeeded();

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
                public View getForegroundView(ViewGroup parent, ZegoUIKitUser userInfo) {
                    ZegoVideoForegroundView foregroundView = new ZegoVideoForegroundView(getContext(), userInfo);
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
        ZegoAudioVideoViewConfig audioVideoViewConfig = new ZegoAudioVideoViewConfig();
        audioVideoViewConfig.showSoundWavesInAudioMode = config.audioVideoViewConfig.showSoundWavesInAudioMode;
        audioVideoViewConfig.useVideoViewAspectFill = config.audioVideoViewConfig.useVideoViewAspectFill;
        binding.avcontainer.setAudioVideoConfig(audioVideoViewConfig);
    }

    private void requestPermissionIfNeeded() {
        String userID = getArguments().getString("userID");
        ZegoUIKitPrebuiltCallConfig config = (ZegoUIKitPrebuiltCallConfig) getArguments().getSerializable("config");

        List<String> permissions = Arrays.asList(permission.CAMERA, permission.RECORD_AUDIO);
        boolean permissionGranted = true;
        for (String permission : permissions) {
            if (!PermissionX.isGranted(getContext(), permission)) {
                permissionGranted = false;
            }
            break;
        }
        if (!permissionGranted) {
            PermissionX.init(requireActivity())
                .permissions(permissions)
                .onExplainRequestReason((scope, deniedList) ->
                    scope.showRequestReasonDialog(deniedList, getString(R.string.permission_explain),
                        getString(R.string.ok)))
                .onForwardToSettings((scope, deniedList) ->
                    scope.showForwardToSettingsDialog(deniedList, getString(R.string.permission_explain),
                        getString(R.string.ok),getString(R.string.cancel)))
                .request((allGranted, grantedList, deniedList) -> {
                    if (allGranted) {
                        if (config.turnOnCameraWhenJoining) {
                            ZegoUIKit.turnCameraOn(userID, false);
                            ZegoUIKit.turnCameraOn(userID, true);
                        }
                        if (config.turnOnMicrophoneWhenJoining) {
                            ZegoUIKit.turnMicrophoneOn(userID, false);
                            ZegoUIKit.turnMicrophoneOn(userID, true);
                        }
                    }
                });
        }
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