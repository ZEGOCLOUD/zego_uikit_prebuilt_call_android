package com.zegocloud.uikit.prebuilt.call.internal;

import android.content.Context;
import android.text.TextUtils;
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
import com.zegocloud.uikit.components.audiovideo.ZegoBaseAudioVideoForegroundView;
import com.zegocloud.uikit.service.defines.ZegoUIKitUser;
import com.zegocloud.uikit.utils.Utils;

public class ZegoAudioVideoForegroundView extends ZegoBaseAudioVideoForegroundView {

    private TextView textView;
    private ZegoMicrophoneStateView micStatusView;
    private ZegoCameraStateView cameraStatusView;

    public ZegoAudioVideoForegroundView(@NonNull Context context, String userID) {
        super(context, userID);
    }

    public ZegoAudioVideoForegroundView(@NonNull Context context, @Nullable AttributeSet attrs, String userID) {
        super(context, attrs, userID);
    }

    @Override
    protected void onForegroundViewCreated(ZegoUIKitUser uiKitUser) {
        super.onForegroundViewCreated(uiKitUser);
        ViewGroup viewGroup = (ViewGroup) LayoutInflater.from(getContext())
            .inflate(R.layout.call_layout_video_foreground, null, false);
        textView = viewGroup.findViewById(R.id.foreground_textview);
        micStatusView = viewGroup.findViewById(R.id.foreground_mic);
        cameraStatusView = viewGroup.findViewById(R.id.foreground_camera);

        if (uiKitUser != null) {
            textView.setText(uiKitUser.userName);
        }
        if (!TextUtils.isEmpty(userID)) {
            micStatusView.setUserID(userID);
            cameraStatusView.setUserID(userID);
        }

        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(-2, -2);
        layoutParams.gravity = Gravity.BOTTOM | Gravity.END;
        int margin = Utils.dp2px(5f, getResources().getDisplayMetrics());
        layoutParams.setMargins(margin, margin, margin, margin);
        layoutParams.bottomMargin = margin;
        addView(viewGroup, layoutParams);
    }

    public void showCameraView(boolean showMicStatusOnView) {
        cameraStatusView.setVisibility(showMicStatusOnView ? View.VISIBLE : View.GONE);
    }

    public void showMicrophoneView(boolean showMicStatusOnView) {
        micStatusView.setVisibility(showMicStatusOnView ? View.VISIBLE : View.GONE);
    }

    public void showUserNameView(boolean showUserName) {
        textView.setVisibility(showUserName ? View.VISIBLE : View.GONE);
    }
}
