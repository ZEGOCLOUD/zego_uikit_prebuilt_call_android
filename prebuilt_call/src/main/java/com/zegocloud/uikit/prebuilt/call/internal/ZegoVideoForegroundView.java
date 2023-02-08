package com.zegocloud.uikit.prebuilt.call.internal;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.zegocloud.uikit.components.audiovideo.ZegoCameraStateView;
import com.zegocloud.uikit.components.audiovideo.ZegoMicrophoneStateView;
import com.zegocloud.uikit.prebuilt.call.R;
import com.zegocloud.uikit.service.defines.ZegoUIKitUser;
import com.zegocloud.uikit.utils.Utils;

public class ZegoVideoForegroundView extends FrameLayout {

    private TextView textView;
    private ZegoMicrophoneStateView micStatusView;
    private ZegoUIKitUser userInfo;
    private ZegoCameraStateView cameraStatusView;

    public ZegoVideoForegroundView(@NonNull Context context, ZegoUIKitUser userInfo) {
        super(context);
        this.userInfo = userInfo;
        initView();
    }

    public ZegoVideoForegroundView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public ZegoVideoForegroundView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        ViewGroup viewGroup = (ViewGroup) LayoutInflater.from(getContext())
            .inflate(R.layout.call_layout_video_foreground, null, false);
        textView = viewGroup.findViewById(R.id.foreground_textview);
        micStatusView = viewGroup.findViewById(R.id.foreground_mic);
        cameraStatusView = viewGroup.findViewById(R.id.foreground_camera);
        setUserInfo(userInfo);

        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(-2, -2);
        layoutParams.gravity = Gravity.BOTTOM | Gravity.END;
        int margin = Utils.dp2px(5f, getResources().getDisplayMetrics());
        layoutParams.setMargins(margin, margin, margin, margin);
        layoutParams.bottomMargin = margin;
        addView(viewGroup, layoutParams);
    }

    public void setUserInfo(ZegoUIKitUser userInfo) {
        this.userInfo = userInfo;
        if (textView != null) {
            if (userInfo != null) {
                textView.setText(userInfo.userName);
            }
        }
        if (micStatusView != null) {
            if (userInfo != null) {
                micStatusView.setUserID(userInfo.userID);
            }
        }
        if (cameraStatusView != null) {
            if (userInfo != null) {
                cameraStatusView.setUserID(userInfo.userID);
            }
        }
    }

    public void showCamera(boolean showMicStatusOnView) {
        cameraStatusView.setVisibility(showMicStatusOnView ? View.VISIBLE : View.GONE);
    }

    public void showMicrophone(boolean showMicStatusOnView) {
        micStatusView.setVisibility(showMicStatusOnView ? View.VISIBLE : View.GONE);
    }

    public void showUserName(boolean showUserName) {
        textView.setVisibility(showUserName ? View.VISIBLE : View.GONE);
    }

    public ZegoMicrophoneStateView getMicStatusView() {
        return micStatusView;
    }

    public ZegoCameraStateView getCameraStatusView() {
        return cameraStatusView;
    }
}
