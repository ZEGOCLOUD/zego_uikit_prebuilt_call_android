package com.zegocloud.uikit.prebuilt.call.invite.internal;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AlertDialog.Builder;
import com.zegocloud.uikit.plugin.invitation.ZegoInvitationType;
import com.zegocloud.uikit.prebuilt.call.R;
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallConfig;
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallService;
import com.zegocloud.uikit.prebuilt.call.core.CallInvitationServiceImpl;
import com.zegocloud.uikit.prebuilt.call.core.invite.ZegoCallInvitationData;
import com.zegocloud.uikit.prebuilt.call.databinding.CallDialogInvitationBinding;

public class CallInvitationDialog {

    private Context context;
    private ZegoCallInvitationData invitationData;
    private CallDialogInvitationBinding binding;
    private AlertDialog alertDialog;

    public CallInvitationDialog(Context context, ZegoCallInvitationData invitationData) {
        this.context = context;
        this.invitationData = invitationData;
        binding = CallDialogInvitationBinding.inflate(LayoutInflater.from(context));
        AlertDialog.Builder builder = new Builder(context);
        builder.setView(binding.getRoot());
        builder.setCancelable(false);
        alertDialog = builder.create();

        initDialogView();
    }

    private void initDialogView() {
        LayoutParams attributes = alertDialog.getWindow().getAttributes();
        attributes.width = LayoutParams.MATCH_PARENT;
        attributes.height = LayoutParams.WRAP_CONTENT;
        attributes.gravity = Gravity.TOP;
        alertDialog.getWindow().setAttributes(attributes);
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable());

        if (CallInvitationServiceImpl.getInstance().getCallInvitationConfig() != null) {
            boolean showDeclineButton = CallInvitationServiceImpl.getInstance()
                .getCallInvitationConfig().showDeclineButton;
            binding.dialogCallDecline.setVisibility(showDeclineButton ? View.VISIBLE : View.GONE);
        }

        binding.dialogCallIcon.setText(invitationData.inviter.userName, false);
        binding.dialogCallName.setText(invitationData.inviter.userName);
        binding.dialogCallAccept.setInviterID(invitationData.inviter.userID);
        binding.dialogCallAccept.setOnClickListener(v -> {
            IncomingCallButtonListener incomingCallButtonListener = ZegoUIKitPrebuiltCallService.events.invitationEvents.getIncomingCallButtonListener();
            if (incomingCallButtonListener != null) {
                incomingCallButtonListener.onIncomingCallAcceptButtonPressed();
            }
            hide();
            CallInviteActivity.startCallPage(context);
        });
        ZegoTranslationText translationText = CallInvitationServiceImpl.getInstance()
            .getCallInvitationConfig().translationText;
        if (invitationData.type == ZegoInvitationType.VOICE_CALL.getValue()) {
            binding.dialogCallAccept.setBackgroundResource(R.drawable.call_selector_dialog_voice_accept);
            if (translationText != null) {
                if (invitationData.invitees.size() > 1) {
                    binding.dialogCallType.setText(translationText.incomingGroupVoiceCallDialogMessage);
                } else {
                    binding.dialogCallType.setText(translationText.incomingVoiceCallDialogMessage);
                }
            }
        } else {
            binding.dialogCallAccept.setBackgroundResource(R.drawable.call_selector_dialog_video_accept);
            if (translationText != null) {
                if (invitationData.invitees.size() > 1) {
                    binding.dialogCallType.setText(translationText.incomingGroupVideoCallDialogMessage);
                } else {
                    binding.dialogCallType.setText(translationText.incomingVideoCallDialogMessage);
                }
            }
        }
        binding.dialogCallDecline.setInviterID(invitationData.inviter.userID);
        binding.dialogCallDecline.setOnClickListener(v -> {
            IncomingCallButtonListener incomingCallButtonListener = ZegoUIKitPrebuiltCallService.events.invitationEvents.getIncomingCallButtonListener();
            if (incomingCallButtonListener != null) {
                incomingCallButtonListener.onIncomingCallDeclineButtonPressed();
            }
            hide();
        });
        binding.getRoot().setOnClickListener(v -> {
            hide();
            CallInviteActivity.startIncomingPage(context);
        });

        ZegoUIKitPrebuiltCallConfig prebuiltCallConfig = CallInvitationServiceImpl.getInstance().getCallConfig();
        if (prebuiltCallConfig.audioVideoViewConfig != null && prebuiltCallConfig.avatarViewProvider != null) {
            View view = prebuiltCallConfig.avatarViewProvider.onUserIDUpdated(binding.dialogCallCustomIcon, invitationData.inviter);
            binding.dialogCallCustomIcon.removeAllViews();
            binding.dialogCallCustomIcon.addView(view);
        }
    }

    public void show() {
        alertDialog.show();
        alertDialog.getWindow().setDimAmount(0.1f);
    }

    public boolean isShowing() {
        return alertDialog.isShowing();
    }

    public void hide() {
        alertDialog.dismiss();
    }
}
