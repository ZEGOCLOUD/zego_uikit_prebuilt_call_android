package com.zegocloud.uikit.prebuilt.callinvite.internal;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.WindowManager.LayoutParams;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AlertDialog.Builder;
import com.zegocloud.uikit.components.invite.ZegoInvitationType;
import com.zegocloud.uikit.prebuilt.call.R;
import com.zegocloud.uikit.prebuilt.call.databinding.ActivityZegoCallInvitationDialogBinding;
import com.zegocloud.uikit.prebuilt.callinvite.ZegoCallInvitationData;

public class ZegoCallInvitationDialog {

    private Context context;
    private ZegoCallInvitationData invitationData;
    private ActivityZegoCallInvitationDialogBinding binding;
    private AlertDialog alertDialog;

    public ZegoCallInvitationDialog(Context context, ZegoCallInvitationData invitationData) {
        this.context = context;
        this.invitationData = invitationData;
        binding = ActivityZegoCallInvitationDialogBinding.inflate(LayoutInflater.from(context));
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
            CallInvitation invitation = CallInvitation.getFromZegoCallInvitationData(invitationData);
            UIKitPrebuiltCallInviteActivity.startActivity(context, invitation, false);
            RingtoneManager.stopRingTone();
            InvitationServiceImpl.getInstance().setCallState(InvitationServiceImpl.CONNECTED);
        });
        if (invitationData.type == ZegoInvitationType.VOICE_CALL.getValue()) {
            binding.dialogCallAccept.setBackgroundResource(R.drawable.selector_dialog_voice_accept);
            binding.dialogCallType.setText(R.string.zego_voice_call);
        } else {
            binding.dialogCallAccept.setBackgroundResource(R.drawable.selector_dialog_video_accept);
            binding.dialogCallType.setText(R.string.zego_video_call);
        }
        binding.dialogCallDecline.setInviterID(invitationData.inviter.userID);
        binding.dialogCallDecline.setOnClickListener(v -> {
            hide();
            RingtoneManager.stopRingTone();
            InvitationServiceImpl.getInstance().setCallState(InvitationServiceImpl.NONE_REJECTED);
        });
    }

    public void show() {
        alertDialog.show();
        alertDialog.getWindow().setDimAmount(0.1f);
    }

    public void hide() {
        alertDialog.hide();
    }
}
