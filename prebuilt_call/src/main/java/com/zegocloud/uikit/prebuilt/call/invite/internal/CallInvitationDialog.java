package com.zegocloud.uikit.prebuilt.call.invite.internal;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.WindowManager.LayoutParams;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AlertDialog.Builder;
import com.zegocloud.uikit.plugin.invitation.ZegoInvitationType;
import com.zegocloud.uikit.prebuilt.call.R;
import com.zegocloud.uikit.prebuilt.call.databinding.DialogCallInvitationBinding;
import com.zegocloud.uikit.prebuilt.call.invite.ZegoCallInvitationData;

public class CallInvitationDialog {

    private Context context;
    private ZegoCallInvitationData invitationData;
    private DialogCallInvitationBinding binding;
    private AlertDialog alertDialog;

    public CallInvitationDialog(Context context, ZegoCallInvitationData invitationData) {
        this.context = context;
        this.invitationData = invitationData;
        binding = DialogCallInvitationBinding.inflate(LayoutInflater.from(context));
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

        binding.dialogCallIcon.setText(invitationData.inviter.userName, false);
        binding.dialogCallName.setText(invitationData.inviter.userName);
        binding.dialogCallAccept.setInviterID(invitationData.inviter.userID);
        binding.dialogCallAccept.setOnClickListener(v -> {
            hide();
            CallInviteActivity.startCallPage(context, invitationData.inviter, invitationData.invitees,
                invitationData.callID, invitationData.type);
        });
        if (invitationData.type == ZegoInvitationType.VOICE_CALL.getValue()) {
            binding.dialogCallAccept.setBackgroundResource(R.drawable.selector_dialog_voice_accept);
            if (invitationData.invitees.size() > 1) {
                binding.dialogCallType.setText(R.string.group_voice_call);
            } else {
                binding.dialogCallType.setText(R.string.voice_call);
            }
        } else {
            binding.dialogCallAccept.setBackgroundResource(R.drawable.selector_dialog_video_accept);
            if (invitationData.invitees.size() > 1) {
                binding.dialogCallType.setText(R.string.group_video_call);
            } else {
                binding.dialogCallType.setText(R.string.video_call);
            }
        }
        binding.dialogCallDecline.setInviterID(invitationData.inviter.userID);
        binding.dialogCallDecline.setOnClickListener(v -> {
            hide();
        });
        binding.getRoot().setOnClickListener(v -> {
            hide();
            CallInviteActivity.startIncomingPage(context, invitationData.inviter, invitationData.invitees,
                invitationData.callID, invitationData.type);
        });
    }

    public void show() {
        alertDialog.show();
        alertDialog.getWindow().setDimAmount(0.1f);
    }

    public void hide() {
        alertDialog.dismiss();
    }
}
