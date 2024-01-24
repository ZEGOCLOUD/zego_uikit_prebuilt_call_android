package com.zegocloud.uikit.prebuilt.call.invite.widget;

import android.content.Context;
import android.util.AttributeSet;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ZegoVideoCallButton extends ZegoCallButton {

    public ZegoVideoCallButton(@NonNull Context context) {
        super(context);
    }

    public ZegoVideoCallButton(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ZegoVideoCallButton(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void initView() {
        super.initView();
        setImageResource(com.zegocloud.uikit.R.drawable.zego_uikit_icon_online_video,
            com.zegocloud.uikit.R.drawable.zego_uikit_icon_online_video);
    }


}
