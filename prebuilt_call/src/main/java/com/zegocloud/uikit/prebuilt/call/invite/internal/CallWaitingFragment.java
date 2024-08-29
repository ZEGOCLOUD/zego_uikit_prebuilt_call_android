package com.zegocloud.uikit.prebuilt.call.invite.internal;

import android.Manifest.permission;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.permissionx.guolindev.PermissionX;
import com.permissionx.guolindev.callback.RequestCallback;
import com.zegocloud.uikit.ZegoUIKit;
import com.zegocloud.uikit.components.audiovideo.ZegoAvatarViewProvider;
import com.zegocloud.uikit.internal.ZegoUIKitLanguage;
import com.zegocloud.uikit.plugin.adapter.utils.GenericUtils;
import com.zegocloud.uikit.plugin.invitation.ZegoInvitationType;
import com.zegocloud.uikit.prebuilt.call.R;
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallConfig;
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallService;
import com.zegocloud.uikit.prebuilt.call.databinding.CallLayoutWaitingBinding;
import com.zegocloud.uikit.prebuilt.call.invite.ZegoUIKitPrebuiltCallInvitationConfig;
import com.zegocloud.uikit.service.defines.ZegoUIKitUser;
import im.zego.zegoexpress.constants.ZegoViewMode;
import im.zego.zegoexpress.entity.ZegoCanvas;
import java.util.ArrayList;
import java.util.List;

public class CallWaitingFragment extends Fragment {

    private CallLayoutWaitingBinding binding;
    private OnBackPressedCallback onBackPressedCallback;
    private Drawable backgroundDrawable;
    private ZegoAvatarViewProvider zegoAvatarViewProvider;
    private ZegoCanvas zegoCanvas;

    public CallWaitingFragment() {
    }

    public static CallWaitingFragment newInstance(Bundle bundle) {
        CallWaitingFragment fragment = new CallWaitingFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = CallLayoutWaitingBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        hideSystemNavigationBar();
        if (backgroundDrawable != null) {
            binding.getRoot().setBackground(backgroundDrawable);
        } else {
            binding.getRoot().setBackgroundResource(R.drawable.call_img_bg);
        }

        String page = getArguments().getString("page");

        ZegoCallInvitationData invitationData = CallInvitationServiceImpl.getInstance().getCallInvitationData();
        int type = invitationData.type;
        String userID = ZegoUIKit.getLocalUser().userID;
        ZegoUIKitUser inviter = invitationData.inviter;
        List<ZegoUIKitUser> invitees = invitationData.invitees;

        zegoCanvas = new ZegoCanvas(binding.audioVideoView);
        zegoCanvas.viewMode = ZegoViewMode.ASPECT_FILL;

        ZegoUIKitUser showUser = null;
        if ("incoming".equals(page)) {
            showUser = inviter;
            binding.audioVideoView.setVisibility(View.GONE);
            binding.cameraSwitch.setVisibility(View.GONE);
            binding.callWaitingCancel.setVisibility(View.GONE);
            binding.callWaitingAcceptText.setVisibility(View.VISIBLE);
            binding.callWaitingAccept.setVisibility(View.VISIBLE);

            if (CallInvitationServiceImpl.getInstance().getCallInvitationConfig() != null) {
                boolean showDeclineButton = CallInvitationServiceImpl.getInstance()
                    .getCallInvitationConfig().showDeclineButton;
                binding.callWaitingRefuse.setVisibility(showDeclineButton ? View.VISIBLE : View.GONE);
                binding.callWaitingRefuseText.setVisibility(showDeclineButton ? View.VISIBLE : View.GONE);
            }
        } else if ("outgoing".equals(page)) {
            if (invitees.size() > 0) {
                showUser = invitees.get(0);
            }
            if (type == ZegoInvitationType.VOICE_CALL.getValue()) {
                binding.audioVideoView.setVisibility(View.GONE);
                binding.cameraSwitch.setVisibility(View.GONE);
            } else {
                ZegoUIKit.startPreview(zegoCanvas);
                binding.cameraSwitch.setVisibility(View.VISIBLE);
                binding.audioVideoView.setVisibility(View.VISIBLE);
//                binding.audioVideoView.setUserID(userID);
            }
            binding.callWaitingCancel.setVisibility(View.VISIBLE);
            binding.callWaitingRefuse.setVisibility(View.GONE);
            binding.callWaitingAcceptText.setVisibility(View.GONE);
            binding.callWaitingRefuseText.setVisibility(View.GONE);
            binding.callWaitingAccept.setVisibility(View.GONE);
        } else {
            requireActivity().finish();
        }
        if (showUser != null) {
            binding.callUserIcon.setText(showUser.userName, false);
            binding.callUserName.setText(showUser.userName);
            if (zegoAvatarViewProvider != null) {
                View customIcon = zegoAvatarViewProvider.onUserIDUpdated(binding.callUserCustomIcon, showUser);
                binding.callUserCustomIcon.removeAllViews();
                binding.callUserCustomIcon.addView(customIcon);
            }
        }

        ZegoTranslationText translationText = CallInvitationServiceImpl.getInstance()
            .getCallInvitationConfig().translationText;
        boolean isGroup = invitees != null && invitees.size() > 1;
        if (type == ZegoInvitationType.VOICE_CALL.getValue()) {
            binding.callWaitingAccept.setBackgroundResource(R.drawable.call_selector_dialog_voice_accept);
            if ("incoming".equals(page)) {
                if (translationText != null) {
                    String callStateTextVoice = isGroup ? translationText.incomingGroupVoiceCallPageMessage
                        : translationText.incomingVoiceCallPageMessage;
                    binding.callStateText.setText(callStateTextVoice);
                }
            }
        } else {
            binding.callWaitingAccept.setBackgroundResource(R.drawable.call_selector_dialog_video_accept);
            if ("incoming".equals(page)) {
                if (translationText != null) {
                    String callStateTextVideo = isGroup ? translationText.incomingGroupVideoCallDialogMessage
                        : translationText.incomingVideoCallPageMessage;
                    binding.callStateText.setText(callStateTextVideo);
                }
            }
        }
        binding.callWaitingAccept.setInviterID(inviter.userID);
        binding.callWaitingRefuse.setInviterID(inviter.userID);
        binding.callWaitingCancel.setInvitees(GenericUtils.map(invitees, uiKitUser -> uiKitUser.userID));
        binding.callWaitingCancel.setOnClickListener(v -> {
            OutgoingCallButtonListener outgoingCallButtonListener = ZegoUIKitPrebuiltCallService.events.invitationEvents.getOutgoingCallButtonListener();
            if (outgoingCallButtonListener != null) {
                outgoingCallButtonListener.onOutgoingCallCancelButtonPressed();
            }
            requireActivity().finish();
        });
        binding.callWaitingRefuse.setOnClickListener(v -> {
            IncomingCallButtonListener incomingCallButtonListener = ZegoUIKitPrebuiltCallService.events.invitationEvents.getIncomingCallButtonListener();
            if (incomingCallButtonListener != null) {
                incomingCallButtonListener.onIncomingCallDeclineButtonPressed();
            }
            requireActivity().finish();
            CallInvitationServiceImpl.getInstance().dismissCallNotification();
            CallInvitationServiceImpl.getInstance().clearPushMessage();
        });
        binding.callWaitingAccept.setOnClickListener(v -> {
            IncomingCallButtonListener incomingCallButtonListener = ZegoUIKitPrebuiltCallService.events.invitationEvents.getIncomingCallButtonListener();
            if (incomingCallButtonListener != null) {
                incomingCallButtonListener.onIncomingCallAcceptButtonPressed();
            }
            CallInvitationServiceImpl.getInstance().setCallState(CallInvitationServiceImpl.CONNECTED);
            CallInvitationServiceImpl.getInstance().dismissCallNotification();
            CallInvitationServiceImpl.getInstance().clearPushMessage();
        });

        binding.getRoot().post(() -> {
            List<String> permissions = new ArrayList<>();
            if (type == ZegoInvitationType.VOICE_CALL.getValue()) {
                permissions.add(permission.RECORD_AUDIO);
            } else {
                permissions.add(permission.CAMERA);
                permissions.add(permission.RECORD_AUDIO);
            }
            requestPermissionIfNeeded(permissions, (allGranted, grantedList, deniedList) -> {
                if (grantedList.contains(permission.CAMERA)) {
                    if (type == ZegoInvitationType.VIDEO_CALL.getValue()) {
                        ZegoUIKit.turnCameraOn(userID, true);
                    } else if (type == ZegoInvitationType.VOICE_CALL.getValue()) {
                        ZegoUIKit.turnCameraOn(userID, false);
                    }
                }
            });
        });

        setInnerText(page, type, showUser, invitees);
    }

    private void requestPermissionIfNeeded(List<String> permissions, RequestCallback requestCallback) {
        boolean allGranted = true;
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(requireActivity(), permission) != PackageManager.PERMISSION_GRANTED) {
                allGranted = false;
            }
        }
        if (allGranted) {
            requestCallback.onResult(true, permissions, new ArrayList<>());
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
                if (requestCallback != null) {
                    requestCallback.onResult(allGranted, grantedList, deniedList);
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

    private void setInnerText(String page, int type, ZegoUIKitUser showUser, List<ZegoUIKitUser> invitees) {
        ZegoUIKitPrebuiltCallInvitationConfig callInvitationConfig = CallInvitationServiceImpl.getInstance()
            .getCallInvitationConfig();
        if (callInvitationConfig == null) {
            return;
        }
        ZegoTranslationText innerText = callInvitationConfig.translationText;
        if (innerText == null) {
            return;
        }

        if (!TextUtils.isEmpty(innerText.incomingCallPageAcceptButton)) {
            binding.callWaitingAcceptText.setText(innerText.incomingCallPageAcceptButton);
        }

        if (!TextUtils.isEmpty(innerText.incomingCallPageDeclineButton)) {
            binding.callWaitingRefuseText.setText(innerText.incomingCallPageDeclineButton);
        }

        boolean isVideoCall = type == ZegoInvitationType.VIDEO_CALL.getValue();
        boolean isGroup = invitees != null && invitees.size() > 1;

        if (isVideoCall) {
            setVideoInnerText(isGroup, page, showUser, innerText);
        } else {
            setVoiceInnerText(isGroup, page, showUser, innerText);
        }
    }

    private void setVoiceInnerText(boolean isGroup, String page, ZegoUIKitUser showUser,
        ZegoTranslationText innerText) {
        if (isGroup) {
            if ("incoming".equals(page)) {
                if (!TextUtils.isEmpty(innerText.incomingGroupVoiceCallPageTitle)) {
                    if (showUser != null) {
                        binding.callUserIcon.setText(
                            String.format(innerText.incomingGroupVoiceCallPageTitle, showUser.userName), false);
                        binding.callUserName.setText(
                            String.format(innerText.incomingGroupVoiceCallPageTitle, showUser.userName));
                    }
                }
                if (!TextUtils.isEmpty(innerText.incomingGroupVoiceCallPageMessage)) {
                    binding.callStateText.setText(innerText.incomingGroupVoiceCallPageMessage);
                }
            } else if ("outgoing".equals(page)) {
                if (!TextUtils.isEmpty(innerText.outgoingGroupVoiceCallPageTitle)) {
                    if (showUser != null) {
                        binding.callUserIcon.setText(
                            String.format(innerText.outgoingGroupVoiceCallPageTitle, showUser.userName), false);
                        binding.callUserName.setText(
                            String.format(innerText.outgoingGroupVoiceCallPageTitle, showUser.userName));
                    }
                }
                if (!TextUtils.isEmpty(innerText.outgoingGroupVoiceCallPageMessage)) {
                    binding.callStateText.setText(innerText.outgoingGroupVoiceCallPageMessage);
                }
            }
        } else {
            if ("incoming".equals(page)) {
                if (!TextUtils.isEmpty(innerText.incomingVoiceCallPageTitle)) {
                    if (showUser != null) {
                        binding.callUserIcon.setText(
                            String.format(innerText.incomingVoiceCallPageTitle, showUser.userName), false);
                        binding.callUserName.setText(
                            String.format(innerText.incomingVoiceCallPageTitle, showUser.userName));
                    }
                }
                if (!TextUtils.isEmpty(innerText.incomingVoiceCallPageMessage)) {
                    binding.callStateText.setText(innerText.incomingVoiceCallPageMessage);
                }
            } else if ("outgoing".equals(page)) {
                if (!TextUtils.isEmpty(innerText.outgoingVoiceCallPageTitle)) {
                    if (showUser != null) {
                        binding.callUserIcon.setText(
                            String.format(innerText.outgoingVoiceCallPageTitle, showUser.userName), false);
                        binding.callUserName.setText(
                            String.format(innerText.outgoingVoiceCallPageTitle, showUser.userName));
                    }
                }
                if (!TextUtils.isEmpty(innerText.outgoingVoiceCallPageMessage)) {
                    binding.callStateText.setText(innerText.outgoingVoiceCallPageMessage);
                }
                if (!TextUtils.isEmpty(innerText.outgoingVoiceCallPageSmallMessage)) {
                    binding.callStateText2.setText(innerText.outgoingVoiceCallPageSmallMessage);
                }
            }
        }

    }

    private void setVideoInnerText(boolean isGroup, String page, ZegoUIKitUser showUser,
        ZegoTranslationText innerText) {
        if (isGroup) {
            if ("incoming".equals(page)) {
                if (!TextUtils.isEmpty(innerText.incomingGroupVideoCallPageTitle)) {
                    if (showUser != null) {
                        binding.callUserIcon.setText(
                            String.format(innerText.incomingGroupVideoCallPageTitle, showUser.userName), false);
                        binding.callUserName.setText(
                            String.format(innerText.incomingGroupVideoCallPageTitle, showUser.userName));
                    }
                }
                if (!TextUtils.isEmpty(innerText.incomingGroupVideoCallPageMessage)) {
                    binding.callStateText.setText(innerText.incomingGroupVideoCallPageMessage);
                }
            } else if ("outgoing".equals(page)) {
                if (!TextUtils.isEmpty(innerText.outgoingGroupVideoCallPageTitle)) {
                    if (showUser != null) {
                        binding.callUserIcon.setText(
                            String.format(innerText.outgoingGroupVideoCallPageTitle, showUser.userName), false);
                        binding.callUserName.setText(
                            String.format(innerText.outgoingGroupVideoCallPageTitle, showUser.userName));
                    }
                }
                if (!TextUtils.isEmpty(innerText.outgoingGroupVideoCallPageMessage)) {
                    binding.callStateText.setText(innerText.outgoingGroupVideoCallPageMessage);
                }
            }
        } else {
            if ("incoming".equals(page)) {
                if (!TextUtils.isEmpty(innerText.incomingVideoCallPageTitle)) {
                    if (showUser != null) {
                        binding.callUserIcon.setText(
                            String.format(innerText.incomingVideoCallPageTitle, showUser.userName), false);
                        binding.callUserName.setText(
                            String.format(innerText.incomingVideoCallPageTitle, showUser.userName));
                    }
                }
                if (!TextUtils.isEmpty(innerText.incomingVideoCallPageMessage)) {
                    binding.callStateText.setText(innerText.incomingVideoCallPageMessage);
                }
            } else if ("outgoing".equals(page)) {
                if (!TextUtils.isEmpty(innerText.outgoingVideoCallPageTitle)) {
                    if (showUser != null) {
                        binding.callUserIcon.setText(
                            String.format(innerText.outgoingVideoCallPageTitle, showUser.userName), false);
                        binding.callUserName.setText(
                            String.format(innerText.outgoingVideoCallPageTitle, showUser.userName));
                    }
                }
                if (!TextUtils.isEmpty(innerText.outgoingVideoCallPageMessage)) {
                    binding.callStateText.setText(innerText.outgoingVideoCallPageMessage);
                }
            }
        }

    }


    public void setBackground(Drawable drawable) {
        this.backgroundDrawable = drawable;
    }

    public void setAvatarViewProvider(ZegoAvatarViewProvider zegoAvatarViewProvider) {
        this.zegoAvatarViewProvider = zegoAvatarViewProvider;
    }
}