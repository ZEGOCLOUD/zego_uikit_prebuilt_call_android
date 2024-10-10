package com.zegocloud.uikit.prebuilt.call.core.invite.ui;

import android.Manifest.permission;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import com.permissionx.guolindev.PermissionX;
import com.permissionx.guolindev.callback.RequestCallback;
import com.zegocloud.uikit.ZegoUIKit;
import com.zegocloud.uikit.components.audiovideo.ZegoAvatarViewProvider;
import com.zegocloud.uikit.internal.ZegoUIKitLanguage;
import com.zegocloud.uikit.plugin.invitation.ZegoInvitationType;
import com.zegocloud.uikit.prebuilt.call.R;
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallConfig;
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallService;
import com.zegocloud.uikit.prebuilt.call.core.CallInvitationServiceImpl;
import com.zegocloud.uikit.prebuilt.call.core.invite.ZegoCallInvitationData;
import com.zegocloud.uikit.prebuilt.call.databinding.CallLayoutOutgoingBinding;
import com.zegocloud.uikit.prebuilt.call.invite.ZegoUIKitPrebuiltCallInvitationConfig;
import com.zegocloud.uikit.prebuilt.call.invite.internal.CallInviteActivity;
import com.zegocloud.uikit.prebuilt.call.invite.internal.InvitationTextEnglish;
import com.zegocloud.uikit.prebuilt.call.invite.internal.OutgoingCallButtonListener;
import com.zegocloud.uikit.prebuilt.call.invite.internal.ZegoCallText;
import com.zegocloud.uikit.prebuilt.call.invite.internal.ZegoTranslationText;
import com.zegocloud.uikit.service.defines.ZegoUIKitUser;
import im.zego.zegoexpress.constants.ZegoViewMode;
import im.zego.zegoexpress.entity.ZegoCanvas;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ZegoPrebuiltCallOutGoingFragment extends Fragment {

    private CallLayoutOutgoingBinding binding;
    private OnBackPressedCallback onBackPressedCallback;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        onBackPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                //                ZegoUIKit.leaveRoom();
                //                setEnabled(false);
                //                requireActivity().onBackPressed();
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(onBackPressedCallback);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = CallLayoutOutgoingBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        hideSystemNavigationBar();

        applyBackground();

        applyUser();

        applyCancelButton();

        applyCameraPreview();

        applyTranslationText();

        requestPermission();
    }

    private void applyCameraPreview() {
        CallInvitationServiceImpl serviceImpl = CallInvitationServiceImpl.getInstance();
        ZegoCallInvitationData invitationData = serviceImpl.getCallInvitationData();

        ZegoCanvas zegoCanvas = new ZegoCanvas(binding.textureView);
        zegoCanvas.viewMode = ZegoViewMode.ASPECT_FILL;

        if (invitationData.type == ZegoInvitationType.VOICE_CALL.getValue()) {
            binding.textureView.setVisibility(View.GONE);
            binding.cameraSwitch.setVisibility(View.GONE);
        } else {
            ZegoUIKit.startPreview(zegoCanvas);
            binding.cameraSwitch.setVisibility(View.VISIBLE);
            binding.textureView.setVisibility(View.VISIBLE);
        }
    }

    private void applyCancelButton() {
        CallInvitationServiceImpl serviceImpl = CallInvitationServiceImpl.getInstance();
        ZegoCallInvitationData invitationData = serviceImpl.getCallInvitationData();

        List<String> userIDList = invitationData.invitees.stream().map(uiKitUser -> uiKitUser.userID)
            .collect(Collectors.toList());
        binding.callWaitingCancel.setInvitees(userIDList);
        binding.callWaitingCancel.setOnClickListener(v -> {
            OutgoingCallButtonListener outgoingCallButtonListener = ZegoUIKitPrebuiltCallService.events.invitationEvents.getOutgoingCallButtonListener();
            if (outgoingCallButtonListener != null) {
                outgoingCallButtonListener.onOutgoingCallCancelButtonPressed();
            }
            ((CallInviteActivity) requireActivity()).finishCallActivityAndMoveToFront();
        });
    }

    private void applyBackground() {
        ZegoUIKitPrebuiltCallInvitationConfig callInvitationConfig = CallInvitationServiceImpl.getInstance()
            .getCallInvitationConfig();
        if (callInvitationConfig != null && callInvitationConfig.outgoingCallBackground != null) {
            binding.getRoot().setBackground(callInvitationConfig.outgoingCallBackground);
        } else {
            binding.getRoot().setBackgroundResource(R.drawable.call_img_bg);
        }
    }

    private void applyUser() {
        CallInvitationServiceImpl serviceImpl = CallInvitationServiceImpl.getInstance();
        ZegoCallInvitationData invitationData = serviceImpl.getCallInvitationData();
        ZegoAvatarViewProvider zegoAvatarViewProvider = null;
        ZegoUIKitPrebuiltCallConfig callConfig = serviceImpl.getCallConfig();
        if (callConfig != null && callConfig.avatarViewProvider != null) {
            zegoAvatarViewProvider = callConfig.avatarViewProvider;
        }

        binding.callUserIcon.setText(invitationData.inviter.userName, false);
        binding.callUserName.setText(invitationData.inviter.userName);

        if (zegoAvatarViewProvider != null) {
            View customIcon = zegoAvatarViewProvider.onUserIDUpdated(binding.customIcon, invitationData.inviter);
            binding.customIcon.removeAllViews();
            binding.customIcon.addView(customIcon);
        }
    }

    private void requestPermission() {
        ZegoCallInvitationData invitationData = CallInvitationServiceImpl.getInstance().getCallInvitationData();
        binding.getRoot().post(() -> {
            List<String> permissions = new ArrayList<>();
            if (invitationData.type == ZegoInvitationType.VOICE_CALL.getValue()) {
                permissions.add(permission.RECORD_AUDIO);
            } else {
                permissions.add(permission.CAMERA);
                permissions.add(permission.RECORD_AUDIO);
            }
            requestPermissionIfNeeded(requireActivity(), permissions, (allGranted, grantedList, deniedList) -> {
                if (grantedList.contains(permission.CAMERA)) {
                    if (invitationData.type == ZegoInvitationType.VIDEO_CALL.getValue()) {
                        ZegoUIKit.turnCameraOn(ZegoUIKit.getLocalUser().userID, true);
                    } else if (invitationData.type == ZegoInvitationType.VOICE_CALL.getValue()) {
                        ZegoUIKit.turnCameraOn(ZegoUIKit.getLocalUser().userID, false);
                    }
                }
            });
        });
    }

    private void requestPermissionIfNeeded(FragmentActivity activity, List<String> permissions,
        RequestCallback callback) {
        List<String> granted = new ArrayList<>();
        List<String> notGranted = new ArrayList<>();
        for (String permission : permissions) {
            int checkedSelfPermission = ContextCompat.checkSelfPermission(activity, permission);
            if (checkedSelfPermission != PackageManager.PERMISSION_GRANTED) {
                notGranted.add(permission);
            } else {
                granted.add(permission);
            }
        }
        if (notGranted.isEmpty() && callback != null) {
            callback.onResult(true, granted, notGranted);
            return;
        }

        ZegoUIKitPrebuiltCallInvitationConfig callInvitationConfig = CallInvitationServiceImpl.getInstance()
            .getCallInvitationConfig();
        if (callInvitationConfig == null || callInvitationConfig.translationText == null) {
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
            ZegoCallText zegoCallText;
            if (callInvitationConfig.translationText.getInvitationBaseText() instanceof InvitationTextEnglish) {
                zegoCallText = new ZegoCallText(ZegoUIKitLanguage.ENGLISH);
            } else {
                zegoCallText = new ZegoCallText(ZegoUIKitLanguage.CHS);
            }
            camera = zegoCallText.permissionExplainCamera;
            mic = zegoCallText.permissionExplainMic;
            micAndCamera = zegoCallText.permissionExplainMicAndCamera;
            settings = zegoCallText.settings;
            cancel = zegoCallText.cancel;
            settingsCamera = zegoCallText.settingCamera;
            settingsMic = zegoCallText.settingMic;
            settingsMicAndCamera = zegoCallText.settingMicAndCamera;
            ok = zegoCallText.ok;
            if (deniedList.size() == 1) {
                if (deniedList.contains(permission.CAMERA)) {
                    message = camera;
                } else if (deniedList.contains(permission.RECORD_AUDIO)) {
                    message = mic;
                }
            } else {
                message = settingsMicAndCamera;
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
            ZegoCallText zegoCallText;
            if (callInvitationConfig.translationText.getInvitationBaseText() instanceof InvitationTextEnglish) {
                zegoCallText = new ZegoCallText(ZegoUIKitLanguage.ENGLISH);
            } else {
                zegoCallText = new ZegoCallText(ZegoUIKitLanguage.CHS);
            }
            camera = zegoCallText.permissionExplainCamera;
            mic = zegoCallText.permissionExplainMic;
            micAndCamera = zegoCallText.permissionExplainMicAndCamera;
            settings = zegoCallText.settings;
            cancel = zegoCallText.cancel;
            settingsCamera = zegoCallText.settingCamera;
            settingsMic = zegoCallText.settingMic;
            settingsMicAndCamera = zegoCallText.settingMicAndCamera;
            ok = zegoCallText.ok;
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
                if (callback != null) {
                    callback.onResult(allGranted, grantedList, deniedList);
                }
            }
        });
    }

    private void hideSystemNavigationBar() {
        int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        binding.getRoot().setSystemUiVisibility(uiOptions);

        int statusBarHeight = getInternalDimensionSize(getContext(), "status_bar_height");
        binding.getRoot().setPadding(0, statusBarHeight, 0, 0);
    }


    static int getInternalDimensionSize(Context context, String key) {
        int result = 0;
        try {
            int resourceId = Resources.getSystem().getIdentifier(key, "dimen", "android");
            if (resourceId > 0) {
                int sizeOne = context.getResources().getDimensionPixelSize(resourceId);
                int sizeTwo = Resources.getSystem().getDimensionPixelSize(resourceId);

                if (sizeTwo >= sizeOne && !(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && !key.equals(
                    "status_bar_height"))) {
                    return sizeTwo;
                } else {
                    float densityOne = context.getResources().getDisplayMetrics().density;
                    float densityTwo = Resources.getSystem().getDisplayMetrics().density;
                    float f = sizeOne * densityTwo / densityOne;
                    return (int) ((f >= 0) ? (f + 0.5f) : (f - 0.5f));
                }
            }
        } catch (Resources.NotFoundException ignored) {
            return 0;
        }
        return result;
    }

    private void applyTranslationText() {
        ZegoCallInvitationData invitationData = CallInvitationServiceImpl.getInstance().getCallInvitationData();
        ZegoUIKitPrebuiltCallInvitationConfig callInvitationConfig = CallInvitationServiceImpl.getInstance()
            .getCallInvitationConfig();
        if (callInvitationConfig == null || invitationData == null) {
            return;
        }
        ZegoTranslationText translationText = callInvitationConfig.translationText;
        if (translationText == null) {
            return;
        }

        if (invitationData.invitees == null || invitationData.invitees.isEmpty()) {
            return;
        }

        boolean isVideoCall = invitationData.type == ZegoInvitationType.VIDEO_CALL.getValue();
        boolean isGroup = invitationData.invitees != null && invitationData.invitees.size() > 1; //single

        ZegoUIKitUser uiKitUser = invitationData.invitees.get(0);

        if (isVideoCall) {
            if (isGroup) {
                if (!TextUtils.isEmpty(translationText.outgoingGroupVideoCallPageTitle)) {
                    String format = String.format(translationText.outgoingGroupVideoCallPageTitle, uiKitUser.userName);
                    binding.callUserIcon.setText(format, false);
                    binding.callUserName.setText(format);
                }
                setTextIfNotEmpty(binding.callStateText, translationText.outgoingGroupVideoCallPageMessage);
            } else {
                if (!TextUtils.isEmpty(translationText.outgoingVideoCallPageTitle)) {
                    String format = String.format(translationText.outgoingVideoCallPageTitle, uiKitUser.userName);
                    binding.callUserIcon.setText(format, false);
                    binding.callUserName.setText(format);
                }
                setTextIfNotEmpty(binding.callStateText, translationText.outgoingVideoCallPageMessage);
            }
        } else {
            if (isGroup) {
                if (!TextUtils.isEmpty(translationText.outgoingGroupVoiceCallPageTitle)) {
                    if (uiKitUser != null) {
                        String format = String.format(translationText.outgoingGroupVoiceCallPageTitle,
                            uiKitUser.userName);
                        binding.callUserIcon.setText(format, false);
                        binding.callUserName.setText(format);
                    }
                }
                setTextIfNotEmpty(binding.callStateText, translationText.outgoingGroupVoiceCallPageMessage);
            } else {
                if (!TextUtils.isEmpty(translationText.outgoingVoiceCallPageTitle)) {
                    if (uiKitUser != null) {
                        String format = String.format(translationText.outgoingVoiceCallPageTitle, uiKitUser.userName);
                        binding.callUserIcon.setText(format, false);
                        binding.callUserName.setText(format);
                    }
                }
                setTextIfNotEmpty(binding.callStateText, translationText.outgoingVoiceCallPageMessage);
                setTextIfNotEmpty(binding.callStateText2, translationText.outgoingVoiceCallPageSmallMessage);
            }
        }
    }

    public void setBusy() {
        CallInvitationServiceImpl.getInstance().openCamera(false);
        ZegoUIKitPrebuiltCallInvitationConfig invitationConfig = CallInvitationServiceImpl.getInstance()
            .getCallInvitationConfig();
        if (invitationConfig != null && invitationConfig.translationText != null
            && invitationConfig.translationText.outgoingCallPageBusyMessage != null) {
            binding.callStateText.setText(invitationConfig.translationText.outgoingCallPageBusyMessage);
        }
    }

    private static void setTextIfNotEmpty(TextView textView, String text) {
        if (!TextUtils.isEmpty(text)) {
            textView.setText(text);
        }
    }
}