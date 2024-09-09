package com.zegocloud.uikit.prebuilt.call.internal;

import android.Manifest.permission;
import android.content.Context;
import android.content.pm.PackageManager;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.view.GestureDetectorCompat;
import androidx.fragment.app.FragmentActivity;
import com.permissionx.guolindev.PermissionX;
import com.permissionx.guolindev.callback.RequestCallback;
import com.zegocloud.uikit.components.audiovideo.ZegoToggleMicrophoneButton;
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallService;
import com.zegocloud.uikit.prebuilt.call.config.ZegoMenuBarButtonName;
import com.zegocloud.uikit.prebuilt.call.event.ZegoMenuBarButtonClickListener;
import com.zegocloud.uikit.prebuilt.call.core.CallInvitationServiceImpl;
import com.zegocloud.uikit.prebuilt.call.invite.internal.ZegoCallText;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PermissionMicrophoneButton extends ZegoToggleMicrophoneButton {

    private GestureDetectorCompat gestureDetectorCompat;

    public PermissionMicrophoneButton(@NonNull Context context) {
        super(context);
        gestureDetectorCompat = new GestureDetectorCompat(context, new SimpleOnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                if (getContext() instanceof FragmentActivity) {
                    requestPermissionIfNeeded((allGranted, grantedList, deniedList) -> {
                        if (allGranted) {
                            callOnClick();
                            ZegoMenuBarButtonClickListener clickListener = ZegoUIKitPrebuiltCallService.events.callEvents.getButtonClickListener();
                            if (clickListener != null) {
                                clickListener.onClick(ZegoMenuBarButtonName.TOGGLE_MICROPHONE_BUTTON,
                                    PermissionMicrophoneButton.this);
                            }
                        }
                    });
                }
                return true;
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetectorCompat.onTouchEvent(event);
    }

    private void requestPermissionIfNeeded(RequestCallback requestCallback) {
        List<String> permissions = Arrays.asList(permission.RECORD_AUDIO);

        boolean allGranted = true;
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(getContext(), permission) != PackageManager.PERMISSION_GRANTED) {
                allGranted = false;
            }
        }
        if (allGranted) {
            requestCallback.onResult(true, permissions, new ArrayList<>());
            return;
        }

        PermissionX.init((FragmentActivity) getContext()).permissions(permission.RECORD_AUDIO)
            .onExplainRequestReason((scope, deniedList) -> {
                String camera = "";
                String mic = "";
                String settings = "";
                String cancel = "";
                String ok = "";
                String micAndCamera = "";
                String settingsCamera = "";
                String settingsMic = "";
                String settingsMicAndCamera = "";
                ZegoCallText zegoCallText = CallInvitationServiceImpl.getInstance().getCallConfig().zegoCallText;
                if (zegoCallText != null) {
                    camera = zegoCallText.permissionExplainCamera;
                    mic = zegoCallText.permissionExplainMic;
                    micAndCamera = zegoCallText.permissionExplainMicAndCamera;
                    settings = zegoCallText.settings;
                    cancel = zegoCallText.cancel;
                    settingsCamera = zegoCallText.settingCamera;
                    settingsMic = zegoCallText.settingMic;
                    settingsMicAndCamera = zegoCallText.settingMicAndCamera;
                    ok = zegoCallText.ok;
                }
                scope.showRequestReasonDialog(deniedList, mic, ok);
            }).onForwardToSettings((scope, deniedList) -> {
                String camera = "";
                String mic = "";
                String settings = "";
                String cancel = "";
                String ok = "";
                String micAndCamera = "";
                String settingsCamera = "";
                String settingsMic = "";
                String settingsMicAndCamera = "";
                ZegoCallText zegoCallText = CallInvitationServiceImpl.getInstance().getCallConfig().zegoCallText;
                if (zegoCallText != null) {
                    camera = zegoCallText.permissionExplainCamera;
                    mic = zegoCallText.permissionExplainMic;
                    micAndCamera = zegoCallText.permissionExplainMicAndCamera;
                    settings = zegoCallText.settings;
                    cancel = zegoCallText.cancel;
                    settingsCamera = zegoCallText.settingCamera;
                    settingsMic = zegoCallText.settingMic;
                    settingsMicAndCamera = zegoCallText.settingMicAndCamera;
                    ok = zegoCallText.ok;
                }
                scope.showForwardToSettingsDialog(deniedList, settingsMic, settings, cancel);
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
}
