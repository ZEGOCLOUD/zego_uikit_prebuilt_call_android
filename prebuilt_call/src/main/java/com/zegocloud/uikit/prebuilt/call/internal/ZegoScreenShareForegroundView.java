package com.zegocloud.uikit.prebuilt.call.internal;

import android.graphics.Color;
import android.graphics.drawable.StateListDrawable;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.core.content.ContextCompat;
import com.zegocloud.uikit.components.audiovideo.ZegoBaseAudioVideoForegroundView;
import com.zegocloud.uikit.components.audiovideo.ZegoScreenSharingView;
import com.zegocloud.uikit.components.common.ZegoShowFullscreenModeToggleButtonRules;
import com.zegocloud.uikit.components.audiovideocontainer.ZegoAudioVideoContainer;
import com.zegocloud.uikit.service.defines.ZegoUIKitUser;
import com.zegocloud.uikit.utils.Utils;

public class ZegoScreenShareForegroundView extends ZegoBaseAudioVideoForegroundView {


    private ImageView imageView;
    private ZegoAudioVideoContainer audioVideoContainer;
    private ZegoShowFullscreenModeToggleButtonRules toggleButtonRules;
    private ZegoScreenSharingView screenSharingView;
    private Handler handler = new Handler(Looper.getMainLooper());

    public ZegoScreenShareForegroundView(ViewGroup screenSharingView, String userID) {
        super(screenSharingView.getContext(), userID);
        this.screenSharingView = (ZegoScreenSharingView) screenSharingView;
        imageView.setSelected(this.screenSharingView.isFullScreen());
    }

    @Override
    protected void onForegroundViewCreated(ZegoUIKitUser uiKitUser) {
        super.onForegroundViewCreated(uiKitUser);
        imageView = new ImageView(getContext());

        StateListDrawable sld = new StateListDrawable();
        sld.addState(new int[]{android.R.attr.state_selected},
            ContextCompat.getDrawable(getContext(), com.zegocloud.uikit.R.drawable.zego_uikit_exit_full_screen));
        sld.addState(new int[]{},
            ContextCompat.getDrawable(getContext(), com.zegocloud.uikit.R.drawable.zego_uikit_icon_full_screen));
        imageView.setImageDrawable(sld);
        int size = Utils.dp2px(32, getContext().getResources().getDisplayMetrics());
        LayoutParams params = new LayoutParams(size, size);
        params.setMargins(size / 4, size + size / 2, size / 4, size / 4);
        params.gravity = Gravity.TOP | Gravity.END;
        addView(imageView, params);

        TextView textView = new TextView(getContext());
        textView.setText(uiKitUser.userName);
        LayoutParams layoutParams = new LayoutParams(-2, -2);
        layoutParams.gravity = Gravity.BOTTOM | Gravity.END;
        layoutParams.setMargins(size / 4, size / 4, size / 4, size / 4);
        textView.setTextColor(Color.parseColor("#dddddd"));
        addView(textView, layoutParams);
    }

    public void showFullButton() {
        if (imageView != null) {
            imageView.setVisibility(VISIBLE);
        }
    }

    public void hideFullButton() {
        if (imageView != null) {
            imageView.setVisibility(GONE);
        }
    }

    public void setParentContainer(ZegoAudioVideoContainer liveVideoContainer) {
        this.audioVideoContainer = liveVideoContainer;
        imageView.setOnClickListener(v -> {
            if (audioVideoContainer != null) {
                boolean selected = imageView.isSelected();
                imageView.setSelected(!selected);
                audioVideoContainer.showScreenSharingViewInFullscreenMode(userID, !selected);
            }
        });
    }

    public void setToggleButtonRules(ZegoShowFullscreenModeToggleButtonRules toggleButtonRules) {
        this.toggleButtonRules = toggleButtonRules;

        if (toggleButtonRules == ZegoShowFullscreenModeToggleButtonRules.SHOW_WHEN_SCREEN_PRESSED) {
            Runnable runnable = this::hideFullButton;
            handler.postDelayed(runnable, 3000);
            screenSharingView.setOnClickListener(v -> {
                showFullButton();
                handler.removeCallbacks(runnable);
                handler.postDelayed(runnable, 3000);
            });
        } else if (toggleButtonRules == ZegoShowFullscreenModeToggleButtonRules.ALWAYS_HIDE) {
            hideFullButton();
        }
    }
}
