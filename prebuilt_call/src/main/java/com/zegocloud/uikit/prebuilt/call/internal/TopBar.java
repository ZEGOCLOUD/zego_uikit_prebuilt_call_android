package com.zegocloud.uikit.prebuilt.call.internal;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

public class TopBar extends RelativeLayout {

    public TopBar(Context context) {
        super(context);
        initView();
    }

    public TopBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public TopBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
//        int childWidth = Utils.dp2px(35f, getResources().getDisplayMetrics());
//        int childHeight = Utils.dp2px(35f, getResources().getDisplayMetrics());
//        int marginEnd = Utils.dp2px(13f, getResources().getDisplayMetrics());
//        ZegoSwitchCameraFacingButton cameraFacingButton = new ZegoSwitchCameraFacingButton(getContext());
//        RelativeLayout.LayoutParams childLayoutParams = new RelativeLayout.LayoutParams(childWidth, childHeight);
//        childLayoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
//        childLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_END);
//        childLayoutParams.setMarginEnd(marginEnd);
//        addView(cameraFacingButton, childLayoutParams);
    }
}
