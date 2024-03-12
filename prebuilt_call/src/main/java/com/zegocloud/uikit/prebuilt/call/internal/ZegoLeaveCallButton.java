package com.zegocloud.uikit.prebuilt.call.internal;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.util.AttributeSet;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.zegocloud.uikit.components.audiovideo.ZegoLeaveButton;
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallConfig;
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallFragment;
import com.zegocloud.uikit.prebuilt.call.config.ZegoHangUpConfirmDialogInfo;
import com.zegocloud.uikit.prebuilt.call.event.CallEndListener;
import com.zegocloud.uikit.prebuilt.call.event.ZegoCallEndReason;
import com.zegocloud.uikit.prebuilt.call.invite.ZegoUIKitPrebuiltCallInvitationService;
import com.zegocloud.uikit.prebuilt.call.invite.internal.CallInvitationServiceImpl;

public class ZegoLeaveCallButton extends ZegoLeaveButton {

    public ZegoLeaveCallButton(@NonNull Context context) {
        super(context);
    }

    public ZegoLeaveCallButton(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void invokedWhenClick() {
        boolean isActivity = getContext() instanceof Activity;
        ZegoUIKitPrebuiltCallConfig callConfig = CallInvitationServiceImpl.getInstance().getCallConfig();
        if (isActivity && callConfig.hangUpConfirmDialogInfo != null) {
            showQuitDialog(callConfig.hangUpConfirmDialogInfo);
        } else {
            if (callConfig.leaveCallListener != null) {
                callConfig.leaveCallListener.onLeaveCall();
            } else {
                ZegoUIKitPrebuiltCallFragment callFragment = CallInvitationServiceImpl.getInstance().getZegoUIKitPrebuiltCallFragment();
                if (callFragment != null) {
                    callFragment.endCall();
                }
                CallInvitationServiceImpl.getInstance().leaveRoom();
                CallEndListener callEndListener = ZegoUIKitPrebuiltCallInvitationService.events.callEvents.getCallEndListener();
                if (callEndListener != null) {
                    callEndListener.onCallEnd(ZegoCallEndReason.LOCAL_HANGUP, null);
                }
            }
        }
    }

    private void showQuitDialog(ZegoHangUpConfirmDialogInfo dialogInfo) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getContext());
        builder.setTitle(dialogInfo.title);
        builder.setMessage(dialogInfo.message);
        builder.setPositiveButton(dialogInfo.confirmButtonName, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ZegoUIKitPrebuiltCallConfig callConfig = CallInvitationServiceImpl.getInstance().getCallConfig();
                if (callConfig.leaveCallListener != null) {
                    callConfig.leaveCallListener.onLeaveCall();
                } else {
                    ZegoUIKitPrebuiltCallFragment callFragment = CallInvitationServiceImpl.getInstance().getZegoUIKitPrebuiltCallFragment();
                    if (callFragment != null) {
                        callFragment.endCall();
                    }
                    CallInvitationServiceImpl.getInstance().leaveRoom();
                    CallEndListener callEndListener = ZegoUIKitPrebuiltCallInvitationService.events.callEvents.getCallEndListener();
                    if (callEndListener != null) {
                        callEndListener.onCallEnd(ZegoCallEndReason.LOCAL_HANGUP, null);
                    }
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
