package com.zegocloud.uikit.prebuilt.call.internal;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.util.AttributeSet;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.zegocloud.uikit.components.audiovideo.ZegoLeaveButton;
import com.zegocloud.uikit.prebuilt.call.config.ZegoHangUpConfirmDialogInfo;
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallFragment.LeaveCallListener;

public class ZegoLeaveCallButton extends ZegoLeaveButton {

    private ZegoHangUpConfirmDialogInfo hangUpConfirmDialogInfo;
    private LeaveCallListener leaveCallListener;

    public ZegoLeaveCallButton(@NonNull Context context) {
        super(context);
    }

    public ZegoLeaveCallButton(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public void setHangUpConfirmInfo(ZegoHangUpConfirmDialogInfo info) {
        hangUpConfirmDialogInfo = info;
    }

    @Override
    public void invokedWhenClick() {
        boolean isActivity = getContext() instanceof Activity;
        if (isActivity && hangUpConfirmDialogInfo != null) {
            showQuitDialog(hangUpConfirmDialogInfo);
        } else {
            if (leaveCallListener != null) {
                leaveCallListener.onLeaveCall();
            }
        }
    }

    public void setLeaveListener(LeaveCallListener listener) {
        this.leaveCallListener = listener;
    }

    private void showQuitDialog(ZegoHangUpConfirmDialogInfo dialogInfo) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getContext());
        builder.setTitle(dialogInfo.title);
        builder.setMessage(dialogInfo.message);
        builder.setPositiveButton(dialogInfo.confirmButtonName, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (leaveCallListener != null) {
                    leaveCallListener.onLeaveCall();
                }
            }
        });
        builder.setNegativeButton(dialogInfo.cancelButtonName, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setCancelable(false);
        builder.show();
    }

}
