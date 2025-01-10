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
import com.zegocloud.uikit.plugin.common.PluginCallbackListener;
import com.zegocloud.uikit.plugin.invitation.ZegoInvitationType;
import com.zegocloud.uikit.prebuilt.call.R;
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallConfig;
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallService;
import com.zegocloud.uikit.prebuilt.call.core.CallInvitationServiceImpl;
import com.zegocloud.uikit.prebuilt.call.core.invite.ZegoCallInvitationData;
import com.zegocloud.uikit.prebuilt.call.databinding.CallLayoutIncomingBinding;
import com.zegocloud.uikit.prebuilt.call.invite.ZegoUIKitPrebuiltCallInvitationConfig;
import com.zegocloud.uikit.prebuilt.call.invite.internal.CallInviteActivity;
import com.zegocloud.uikit.prebuilt.call.invite.internal.IncomingCallButtonListener;
import com.zegocloud.uikit.prebuilt.call.invite.internal.InvitationTextEnglish;
import com.zegocloud.uikit.prebuilt.call.invite.internal.ZegoCallText;
import com.zegocloud.uikit.prebuilt.call.invite.internal.ZegoTranslationText;
import im.zego.uikit.libuikitreport.ReportUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ZegoPrebuiltCallInComingFragment extends Fragment {

    private CallLayoutIncomingBinding binding;
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
        binding = CallLayoutIncomingBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        hideSystemNavigationBar();

        applyBackground();
        applyUser();
        applyAcceptButton();
        applyRejectButton();

        applyTranslationText();

        requestPermission();
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

    private void applyRejectButton() {
        CallInvitationServiceImpl serviceImpl = CallInvitationServiceImpl.getInstance();
        ZegoUIKitPrebuiltCallInvitationConfig callInvitationConfig = serviceImpl.getCallInvitationConfig();

        if (callInvitationConfig != null) {
            boolean showDeclineButton = CallInvitationServiceImpl.getInstance()
                .getCallInvitationConfig().showDeclineButton;
            binding.callWaitingRefuse.setVisibility(showDeclineButton ? View.VISIBLE : View.GONE);
            binding.callWaitingRefuseText.setVisibility(showDeclineButton ? View.VISIBLE : View.GONE);
        }

        binding.callWaitingRefuse.setBackgroundResource(com.zegocloud.uikit.R.drawable.zego_uikit_icon_dialog_voice_decline);
        binding.callWaitingRefuse.setOnClickListener(v -> {
            IncomingCallButtonListener incomingCallButtonListener = ZegoUIKitPrebuiltCallService.events.invitationEvents.getIncomingCallButtonListener();
            if (incomingCallButtonListener != null) {
                incomingCallButtonListener.onIncomingCallDeclineButtonPressed();
            }
            CallInvitationServiceImpl.getInstance().rejectInvitation(new PluginCallbackListener() {
                @Override
                public void callback(Map<String, Object> result) {
                    HashMap<String, Object> hashMap = new HashMap<>();
                    ZegoCallInvitationData invitationData = CallInvitationServiceImpl.getInstance().getCallInvitationData();
                    if (invitationData != null) {
                        hashMap.put("call_id", invitationData.invitationID);
                    } else {
                        hashMap.put("call_id", "");
                    }
                    hashMap.put("app_state", "active");
                    hashMap.put("action", "refuse");
                    ReportUtil.reportEvent("call/respondInvitation", hashMap);
                }
            });
            ((CallInviteActivity)requireActivity()).finishCallActivityAndMoveToFront();
            CallInvitationServiceImpl.getInstance().dismissCallNotification();
            CallInvitationServiceImpl.getInstance().clearPushMessage();
        });
    }

    private void applyAcceptButton() {
        CallInvitationServiceImpl serviceImpl = CallInvitationServiceImpl.getInstance();
        ZegoCallInvitationData invitationData = serviceImpl.getCallInvitationData();
        if (invitationData.type == ZegoInvitationType.VOICE_CALL.getValue()) {
            binding.callWaitingAccept.setBackgroundResource(R.drawable.call_selector_dialog_voice_accept);
        } else {
            binding.callWaitingAccept.setBackgroundResource(R.drawable.call_selector_dialog_video_accept);
        }
        binding.callWaitingAccept.setOnClickListener(v -> {
            IncomingCallButtonListener incomingCallButtonListener = ZegoUIKitPrebuiltCallService.events.invitationEvents.getIncomingCallButtonListener();
            if (incomingCallButtonListener != null) {
                incomingCallButtonListener.onIncomingCallAcceptButtonPressed();
            }
            CallInvitationServiceImpl.getInstance().acceptInvitation(new PluginCallbackListener() {
                @Override
                public void callback(Map<String, Object> result) {
                    int code = (int) result.get("code");
                    String message = (String) result.get("message");
                    if(code == 0){
                        HashMap<String, Object> hashMap = new HashMap<>();
                        ZegoCallInvitationData invitationData = CallInvitationServiceImpl.getInstance().getCallInvitationData();
                        if (invitationData != null ) {
                            hashMap.put("call_id", invitationData.invitationID);
                        }else {
                            hashMap.put("call_id", "");
                        }
                        hashMap.put("app_state", "active");
                        hashMap.put("action", "accept");
                        ReportUtil.reportEvent("call/respondInvitation", hashMap);
                    }

                    CallInvitationServiceImpl.getInstance().dismissCallNotification();
                    CallInvitationServiceImpl.getInstance().clearPushMessage();
                }
            });
        });
    }

    private void applyBackground() {
        ZegoUIKitPrebuiltCallInvitationConfig callInvitationConfig = CallInvitationServiceImpl.getInstance()
            .getCallInvitationConfig();
        if (callInvitationConfig != null && callInvitationConfig.incomingCallBackground != null) {
            binding.getRoot().setBackground(callInvitationConfig.incomingCallBackground);
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
        boolean isVideoCall = invitationData.type == ZegoInvitationType.VIDEO_CALL.getValue();
        boolean isGroup = invitationData.invitees != null && invitationData.invitees.size() > 1;

        String callStateText;
        if (isVideoCall) {
            callStateText = isGroup ? translationText.incomingGroupVideoCallDialogMessage
                : translationText.incomingVideoCallPageMessage;
        } else {
            callStateText = isGroup ? translationText.incomingGroupVoiceCallPageMessage
                : translationText.incomingVoiceCallPageMessage;
        }

        setTextIfNotEmpty(binding.callStateText, callStateText);
        setTextIfNotEmpty(binding.callWaitingAcceptText, translationText.incomingCallPageAcceptButton);
        setTextIfNotEmpty(binding.callWaitingRefuseText, translationText.incomingCallPageDeclineButton);

        if (isVideoCall) {
            if (isGroup) {
                if (!TextUtils.isEmpty(translationText.incomingGroupVideoCallPageTitle)) {
                    String format = String.format(translationText.incomingGroupVideoCallPageTitle,
                        invitationData.inviter.userName);
                    binding.callUserIcon.setText(format, false);
                    binding.callUserName.setText(format);
                }
                setTextIfNotEmpty(binding.callStateText, translationText.incomingGroupVideoCallPageMessage);
            } else {
                if (!TextUtils.isEmpty(translationText.incomingVideoCallPageTitle)) {
                    String format = String.format(translationText.incomingVideoCallPageTitle,
                        invitationData.inviter.userName);
                    binding.callUserIcon.setText(format, false);
                    binding.callUserName.setText(format);
                }
                setTextIfNotEmpty(binding.callStateText, translationText.incomingVideoCallPageMessage);
            }
        } else {
            if (isGroup) {
                if (!TextUtils.isEmpty(translationText.incomingGroupVoiceCallPageTitle)) {
                    String format = String.format(translationText.incomingGroupVoiceCallPageTitle,
                        invitationData.inviter.userName);
                    binding.callUserIcon.setText(format, false);
                    binding.callUserName.setText(format);
                }
                setTextIfNotEmpty(binding.callStateText, translationText.incomingGroupVoiceCallPageMessage);
            } else {
                if (!TextUtils.isEmpty(translationText.incomingVoiceCallPageTitle)) {
                    String format = String.format(translationText.incomingVoiceCallPageTitle,
                        invitationData.inviter.userName);
                    binding.callUserIcon.setText(format, false);
                    binding.callUserName.setText(format);
                }
                setTextIfNotEmpty(binding.callStateText, translationText.incomingVoiceCallPageMessage);
            }
        }
    }

    private static void setTextIfNotEmpty(TextView textView, String text) {
        if (!TextUtils.isEmpty(text)) {
            textView.setText(text);
        }
    }
}