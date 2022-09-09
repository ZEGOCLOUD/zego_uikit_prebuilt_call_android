package com.zegocloud.uikit.prebuilt.call.internal;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.util.AttributeSet;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.zegocloud.uikit.ZegoUIKit;
import com.zegocloud.uikit.components.audiovideo.ZegoLeaveButton;
import com.zegocloud.uikit.prebuilt.call.ZegoHangUpConfirmDialogInfo;
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallFragment.HangUpListener;

public class ZegoHangUpButton extends ZegoLeaveButton {

    private ZegoHangUpConfirmDialogInfo hangUpConfirmDialogInfo;
    private HangUpListener hangUpListener;

    public ZegoHangUpButton(@NonNull Context context) {
        super(context);
    }

    public ZegoHangUpButton(@NonNull Context context, @Nullable AttributeSet attrs) {
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
            if (hangUpListener != null) {
                hangUpListener.onHangUp();
            }
        }
    }

    public void setHangUpListener(HangUpListener hangupListener) {
        this.hangUpListener = hangupListener;
    }

    private void showQuitDialog(ZegoHangUpConfirmDialogInfo dialogInfo) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getContext());
        builder.setTitle(dialogInfo.title);
        builder.setMessage(dialogInfo.message);
        builder.setPositiveButton(dialogInfo.confirmButtonName, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (hangUpListener != null) {
                    hangUpListener.onHangUp();
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
