package com.zegocloud.uikit.prebuilt.call;

import android.Manifest;
import android.Manifest.permission;
import android.app.Application;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.permissionx.guolindev.PermissionX;
import com.zegocloud.uikit.ZegoUIKit;
import com.zegocloud.uikit.components.audiovideo.ZegoViewProvider;
import com.zegocloud.uikit.components.audiovideocontainer.ZegoAudioVideoViewConfig;
import com.zegocloud.uikit.prebuilt.call.databinding.FragmentCallBinding;
import com.zegocloud.uikit.prebuilt.call.internal.CallViewModel;
import com.zegocloud.uikit.prebuilt.call.internal.ZegoVideoForegroundView;
import com.zegocloud.uikit.prebuilt.callinvite.ZegoCallInvitationData;
import com.zegocloud.uikit.prebuilt.callinvite.internal.InvitationServiceImpl;
import com.zegocloud.uikit.service.defines.OnlySelfInRoomListener;
import com.zegocloud.uikit.service.defines.ZegoScenario;
import com.zegocloud.uikit.service.defines.ZegoUIKitCallback;
import com.zegocloud.uikit.service.defines.ZegoUIKitUser;
import java.util.ArrayList;
import java.util.List;

public class ZegoUIKitPrebuiltCallFragment extends Fragment {

    private static final String TAG = "CallFragment";
    private FragmentCallBinding binding;
    private static final long HIDE_DELAY_TIME = 5000;
    private final Handler handler = new Handler(Looper.myLooper());
    private final Runnable hideBarRunnable = this::hideBarInner;
    private CallViewModel mViewModel;
    private ZegoViewProvider provider;
    private List<View> menuBarExtendedButtons = new ArrayList<>();
    private OnBackPressedCallback onBackPressedCallback;
    private OnlySelfInRoomListener onlySelfInRoomListener;
    private HangUpListener hangupListener;

    public static ZegoUIKitPrebuiltCallFragment newInstance(ZegoCallInvitationData data,
        ZegoUIKitPrebuiltCallConfig config) {
        ZegoUIKitPrebuiltCallFragment fragment = new ZegoUIKitPrebuiltCallFragment();
        Bundle bundle = new Bundle();
        bundle.putString("callID", data.roomID);
        bundle.putString("userID", InvitationServiceImpl.getInstance().userID);
        bundle.putSerializable("config", config);
        fragment.setArguments(bundle);
        return fragment;
    }

    public static ZegoUIKitPrebuiltCallFragment newInstance(long appID, @NonNull String appSign, @NonNull String callID,
        @NonNull String userID, @NonNull String userName, @NonNull ZegoUIKitPrebuiltCallConfig config) {
        ZegoUIKitPrebuiltCallFragment fragment = new ZegoUIKitPrebuiltCallFragment();
        Bundle bundle = new Bundle();
        bundle.putLong("appID", appID);
        bundle.putString("appSign", appSign);
        bundle.putString("callID", callID);
        bundle.putString("userID", userID);
        bundle.putString("userName", userName);
        bundle.putSerializable("config", config);
        fragment.setArguments(bundle);
        return fragment;
    }

    public static ZegoUIKitPrebuiltCallFragment newInstance(long appID, @NonNull String appSign,
        @NonNull String callID, @NonNull String userID, @NonNull String userName) {
        return newInstance(appID, appSign, callID, userID, userName, new ZegoUIKitPrebuiltCallConfig());
    }

    public ZegoUIKitPrebuiltCallFragment() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arguments = getArguments();
        ZegoUIKitPrebuiltCallConfig config = (ZegoUIKitPrebuiltCallConfig) arguments.getSerializable("config");
        Application application = requireActivity().getApplication();
        long appID = arguments.getLong("appID");
        String appSign = arguments.getString("appSign");
        String userID = arguments.getString("userID");
        String userName = arguments.getString("userName");
        if (appID != 0) {
            ZegoUIKit.init(application, appID, appSign, ZegoScenario.GENERAL);
            ZegoUIKit.login(userID, userName);
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
        mViewModel = new ViewModelProvider(this).get(CallViewModel.class);
        binding.avcontainer.setForegroundViewProvider(provider);
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
        String userID = getArguments().getString("userID");
        ZegoUIKitPrebuiltCallConfig config = (ZegoUIKitPrebuiltCallConfig) getArguments().getSerializable("config");
        mViewModel.getConfigLiveData().setValue(config);
        mViewModel.getConfigLiveData().observe(getViewLifecycleOwner(), new Observer<ZegoUIKitPrebuiltCallConfig>() {
            @Override
            public void onChanged(ZegoUIKitPrebuiltCallConfig config) {
                if (config.bottomMenuBarConfig.maxCount != 0) {
                    binding.bottomMenuBar.setLimitedCount(config.bottomMenuBarConfig.maxCount);
                }
                binding.bottomMenuBar.setButtons(config.bottomMenuBarConfig.buttons);
                binding.bottomMenuBar.setHangUpConfirmDialogInfo(config.hangUpConfirmDialogInfo);
                binding.bottomMenuBar.setHangUpListener(() -> {
                    InvitationServiceImpl.getInstance().setCallState(InvitationServiceImpl.NONE_HANG_UP);
                    if (hangupListener != null) {
                        hangupListener.onHangUp();
                    } else {
                        ZegoUIKit.leaveRoom();
                        requireActivity().finish();
                    }
                });

                ZegoUIKit.turnCameraOn(userID, config.turnOnCameraWhenJoining);
                ZegoUIKit.turnMicrophoneOn(userID, config.turnOnMicrophoneWhenJoining);
                ZegoUIKit.setAudioOutputToSpeaker(config.useSpeakerWhenJoining);
                showBars(config.bottomMenuBarConfig.hideAutomatically);
                setForegroundViewProvider(new ZegoViewProvider() {
                    @Override
                    public View getForegroundView(ZegoUIKitUser userInfo) {
                        ZegoVideoForegroundView foregroundView = new ZegoVideoForegroundView(getContext(),
                            userInfo);
                        foregroundView.showMicrophone(config.audioVideoViewConfig.showMicrophoneStateOnView);
                        foregroundView.showCamera(config.audioVideoViewConfig.showCameraStateOnView);
                        foregroundView.showUserName(config.audioVideoViewConfig.showUserNameOnView);
                        return foregroundView;
                    }
                });
                if (menuBarExtendedButtons.size() > 0) {
                    binding.bottomMenuBar.addButtons(menuBarExtendedButtons);
                }
                binding.avcontainer.setLayout(config.layout);
                ZegoAudioVideoViewConfig audioVideoViewConfig = new ZegoAudioVideoViewConfig();
                audioVideoViewConfig.showSoundWavesInAudioMode = config.audioVideoViewConfig.showSoundWavesInAudioMode;
                audioVideoViewConfig.useVideoViewAspectFill = config.audioVideoViewConfig.useVideoViewAspectFill;
                binding.avcontainer.setAudioVideoConfig(audioVideoViewConfig);
            }
        });
        binding.getRoot().setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (binding.bottomMenuBar.getVisibility() == View.VISIBLE) {
                    if (config.bottomMenuBarConfig.hideByClick) {
                        hideBarInner();
                    }
                } else {
                    showBars(config.bottomMenuBarConfig.hideAutomatically);
                }
            }
        });
        ZegoUIKit.addOnOnlySelfInRoomListener(() -> {
            if (onlySelfInRoomListener != null) {
                onlySelfInRoomListener.onOnlySelfInRoom();
            } else {
                requireActivity().finish();
            }
        });
        boolean permissionGranted =
            PermissionX.isGranted(getContext(), permission.CAMERA)
                && PermissionX.isGranted(getContext(), permission.RECORD_AUDIO);
        if (!permissionGranted) {
            PermissionX.init(requireActivity())
                .permissions(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
                .onExplainRequestReason((scope, deniedList) -> {
                    scope.showRequestReasonDialog(deniedList, "We require camera&microphone access to connect a call",
                        "OK", "Cancel");
                })
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

    private void
    showQuitDialog(String title, String message, String positiveText, String negativeText) {
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
        if (InvitationServiceImpl.getInstance().getCallState() > 0) {
            InvitationServiceImpl.getInstance().setCallState(
                InvitationServiceImpl.NONE);
        }
        ZegoUIKit.leaveRoom();
    }

    private void showBars(boolean hideDelay) {
        binding.bottomMenuBar.setVisibility(View.VISIBLE);
        binding.topMenuBar.setVisibility(View.VISIBLE);
        if (hideDelay) {
            handler.removeCallbacks(hideBarRunnable);
            handler.postDelayed(hideBarRunnable, HIDE_DELAY_TIME);
        }
    }

    private void hideBarInner() {
        binding.bottomMenuBar.setVisibility(View.GONE);
        binding.topMenuBar.setVisibility(View.GONE);
    }

    public void setForegroundViewProvider(ZegoViewProvider provider) {
        this.provider = provider;
        if (binding != null) {
            binding.avcontainer.setForegroundViewProvider(provider);
        }
    }

    public void setOnOnlySelfInRoomListener(OnlySelfInRoomListener listener) {
        this.onlySelfInRoomListener = listener;
    }

    public void setOnHangUpListener(HangUpListener listener) {
        this.hangupListener = listener;
    }

    public void addButtonToBottomMenuBar(List<View> viewList) {
        menuBarExtendedButtons.addAll(viewList);
        if (binding != null) {
            binding.bottomMenuBar.addButtons(viewList);
        }
    }

    public interface HangUpListener {

        void onHangUp();
    }
}