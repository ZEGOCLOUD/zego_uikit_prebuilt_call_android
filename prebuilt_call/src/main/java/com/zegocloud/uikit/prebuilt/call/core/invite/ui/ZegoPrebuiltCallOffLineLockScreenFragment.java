package com.zegocloud.uikit.prebuilt.call.core.invite.ui;

import android.content.Context;
import android.content.Intent;
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
import androidx.fragment.app.Fragment;
import com.zegocloud.uikit.plugin.invitation.ZegoInvitationType;
import com.zegocloud.uikit.plugin.signaling.ZegoSignalingPlugin;
import com.zegocloud.uikit.prebuilt.call.R;
import com.zegocloud.uikit.prebuilt.call.core.CallInvitationServiceImpl;
import com.zegocloud.uikit.prebuilt.call.core.push.ZIMPushMessage;
import com.zegocloud.uikit.prebuilt.call.databinding.CallLayoutIncomingBinding;
import com.zegocloud.uikit.prebuilt.call.invite.internal.CallInviteActivity;
import com.zegocloud.uikit.prebuilt.call.core.notification.RingtoneManager;
import com.zegocloud.uikit.prebuilt.call.invite.internal.ZegoTranslationText;
import im.zego.zim.callback.ZIMUsersInfoQueriedCallback;
import im.zego.zim.entity.ZIMError;
import im.zego.zim.entity.ZIMErrorUserInfo;
import im.zego.zim.entity.ZIMUserFullInfo;
import im.zego.zim.entity.ZIMUsersInfoQueryConfig;
import im.zego.zim.enums.ZIMErrorCode;
import java.util.ArrayList;
import java.util.Collections;
import timber.log.Timber;

public class ZegoPrebuiltCallOffLineLockScreenFragment extends Fragment {

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
        Timber.d(
            "onViewCreated() called with: view = [" + view + "], savedInstanceState = [" + savedInstanceState + "]");

        applyBackgroundWhenOffLine();

        applyAcceptButtonWhenOffLine();

        applyRejectButtonWhenOffLine();

        ZIMPushMessage pushMessage = CallInvitationServiceImpl.getInstance().getZIMPushMessage();
        String inviterId = pushMessage.zimExtendedData.getInviterId();

        ZegoSignalingPlugin.getInstance()
            .queryUserInfo(Collections.singletonList(inviterId), new ZIMUsersInfoQueryConfig(),
                new ZIMUsersInfoQueriedCallback() {
                    @Override
                    public void onUsersInfoQueried(ArrayList<ZIMUserFullInfo> userList,
                        ArrayList<ZIMErrorUserInfo> errorUserList, ZIMError errorInfo) {
                        if (errorInfo.code == ZIMErrorCode.SUCCESS && !userList.isEmpty()) {
                            ZIMUserFullInfo userFullInfo = userList.get(0);
                            applyUserWhenOffLine(userFullInfo.baseInfo.userName);
                            applyTranslationTextWhenOffLine(userFullInfo.baseInfo.userName);
                        }
                    }
                });

        applyUserWhenOffLine(inviterId);

        applyTranslationTextWhenOffLine(inviterId);
    }

    private void applyBackgroundWhenOffLine() {
        binding.getRoot().setBackgroundResource(R.drawable.call_img_bg);
    }

    private void applyRejectButtonWhenOffLine() {
        binding.callWaitingRefuse.setBackgroundResource(com.zegocloud.uikit.R.drawable.zego_uikit_icon_dialog_voice_decline);
        binding.callWaitingRefuse.setOnClickListener(v -> {
            CallInvitationServiceImpl.getInstance().dismissCallNotification();
            RingtoneManager.stopRingTone();
            Intent declineIntent = CallRouteActivity.getDeclineIntent(getContext());
            startActivity(declineIntent);
            ((CallInviteActivity) requireActivity()).finishCallActivity();
        });
    }

    private void applyAcceptButtonWhenOffLine() {
        ZIMPushMessage pushMessage = CallInvitationServiceImpl.getInstance().getZIMPushMessage();

        if (pushMessage.zimExtendedData.getType() == ZegoInvitationType.VOICE_CALL.getValue()) {
            binding.callWaitingAccept.setBackgroundResource(R.drawable.call_selector_dialog_voice_accept);
        } else {
            binding.callWaitingAccept.setBackgroundResource(R.drawable.call_selector_dialog_video_accept);
        }
        binding.callWaitingAccept.setOnClickListener(v -> {
            RingtoneManager.stopRingTone();
            CallInvitationServiceImpl.getInstance().dismissCallNotification();
            Intent acceptIntent = CallRouteActivity.getAcceptIntent(getContext());
            startActivity(acceptIntent);
            ((CallInviteActivity) requireActivity()).finishCallActivity();
        });
    }

    private void applyUserWhenOffLine(String userName) {
        binding.callUserIcon.setText(userName, false);
        binding.callUserName.setText(userName);
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

    private void applyTranslationTextWhenOffLine(String userName) {
        ZegoTranslationText translationText = new ZegoTranslationText();

        ZIMPushMessage pushMessage = CallInvitationServiceImpl.getInstance().getZIMPushMessage();
        boolean isVideoCall = pushMessage.zimExtendedData.getType() == ZegoInvitationType.VIDEO_CALL.getValue();
        boolean isGroup = pushMessage.callData.getInvitees().size() > 1;

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
                    String format = String.format(translationText.incomingGroupVideoCallPageTitle, userName);
                    binding.callUserIcon.setText(format, false);
                    binding.callUserName.setText(format);
                }
                setTextIfNotEmpty(binding.callStateText, translationText.incomingGroupVideoCallPageMessage);
            } else {
                if (!TextUtils.isEmpty(translationText.incomingVideoCallPageTitle)) {
                    String format = String.format(translationText.incomingVideoCallPageTitle, userName);
                    binding.callUserIcon.setText(format, false);
                    binding.callUserName.setText(format);
                }
                setTextIfNotEmpty(binding.callStateText, translationText.incomingVideoCallPageMessage);
            }
        } else {
            if (isGroup) {
                if (!TextUtils.isEmpty(translationText.incomingGroupVoiceCallPageTitle)) {
                    String format = String.format(translationText.incomingGroupVoiceCallPageTitle, userName);
                    binding.callUserIcon.setText(format, false);
                    binding.callUserName.setText(format);
                }
                setTextIfNotEmpty(binding.callStateText, translationText.incomingGroupVoiceCallPageMessage);
            } else {
                if (!TextUtils.isEmpty(translationText.incomingVoiceCallPageTitle)) {
                    String format = String.format(translationText.incomingVoiceCallPageTitle, userName);
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