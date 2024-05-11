package com.zegocloud.uikit.prebuilt.call.internal;

import android.Manifest.permission;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.AttributeSet;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.view.GestureDetectorCompat;
import androidx.fragment.app.FragmentActivity;
import com.permissionx.guolindev.PermissionX;
import com.permissionx.guolindev.callback.RequestCallback;
import com.zegocloud.uikit.components.audiovideo.ZegoToggleCameraButton;
import com.zegocloud.uikit.prebuilt.call.R;
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallConfig;
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallService;
import com.zegocloud.uikit.prebuilt.call.config.ZegoMenuBarButtonName;
import com.zegocloud.uikit.prebuilt.call.event.ZegoMenuBarButtonClickListener;
import com.zegocloud.uikit.prebuilt.call.invite.internal.CallInvitationServiceImpl;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PermissionCameraButton extends ZegoToggleCameraButton {

    public PermissionCameraButton(@NonNull Context context, String userID) {
        super(context, userID);
    }

    public PermissionCameraButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private GestureDetectorCompat gestureDetectorCompat;

    public PermissionCameraButton(@NonNull Context context) {
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
                                clickListener.onClick(ZegoMenuBarButtonName.TOGGLE_CAMERA_BUTTON,
                                    PermissionCameraButton.this);
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
        List<String> permissions = Collections.singletonList(permission.CAMERA);

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

        ZegoUIKitPrebuiltCallConfig callConfig = CallInvitationServiceImpl.getInstance().getCallConfig();
        PermissionX.init((FragmentActivity) getContext()).permissions(permissions)
            .onExplainRequestReason((scope, deniedList) -> {
                String message = "";
                String ok = "";
                if (callConfig.zegoCallText != null) {
                    message = callConfig.zegoCallText.permissionExplainCamera;
                    ok = callConfig.zegoCallText.ok;
                }
                scope.showRequestReasonDialog(deniedList, message, ok);
            }).onForwardToSettings((scope, deniedList) -> {
                String message = "";
                String settings = "";
                String cancel = "";
                if (callConfig.zegoCallText != null) {
                    message = callConfig.zegoCallText.permissionExplainCamera;
                    settings = callConfig.zegoCallText.settings;
                    cancel = callConfig.zegoCallText.cancel;
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
}
