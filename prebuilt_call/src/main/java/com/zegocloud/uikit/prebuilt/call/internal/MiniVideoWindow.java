package com.zegocloud.uikit.prebuilt.call.internal;

import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;
import com.zegocloud.uikit.utils.Utils;

public class MiniVideoWindow {

    private final int scaledTouchSlop;
    private final int longPressTimeout;
    private boolean isViewAddedToWindow;
    private WindowManager windowManager;
    private WindowManager.LayoutParams lp;
    private View contentView;
    private Context context;

    public MiniVideoWindow(Context context) {
        this.context = context;
        windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        lp = new WindowManager.LayoutParams();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            lp.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            lp.type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        lp.format = PixelFormat.RGBA_8888;
        lp.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;

        lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.gravity = Gravity.BOTTOM | Gravity.END;

        int heightPixels = context.getResources().getDisplayMetrics().heightPixels;
        lp.y = heightPixels / 2;
        lp.x = Utils.dp2px(8, context.getResources().getDisplayMetrics());

        scaledTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        longPressTimeout = ViewConfiguration.getLongPressTimeout();
    }

    public void showMinimalWindow(View contentView) {
        if (!isViewAddedToWindow) {
            isViewAddedToWindow = true;
            this.contentView = contentView;
            contentView.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    processTouchEvent(event);
                    return false;
                }
            });
            windowManager.addView(contentView, lp);
        }
    }

    public boolean isShown() {
        return isViewAddedToWindow;
    }

    int lastX;
    int lastY;
    boolean isClick;
    long downTime;
    int downX;
    int downY;

    private void processTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                downX = (int) event.getRawX();
                downY = (int) event.getRawY();
                lastX = downX;
                lastY = downY;
                downTime = System.currentTimeMillis();
                isClick = true;
                break;
            case MotionEvent.ACTION_MOVE:
                int currentX = (int) event.getRawX();
                int currentY = (int) event.getRawY();
                int distanceX = currentX - lastX;
                int distanceY = currentY - lastY;
                int widthPixels = context.getResources().getDisplayMetrics().widthPixels;
                lp.x = Math.min(Math.max((lp.x - distanceX), 0), widthPixels - contentView.getWidth());
                updatePosition(lp.x, lp.y - distanceY);
                lastX = currentX;
                lastY = currentY;
                if (Math.abs(currentX - downX) > scaledTouchSlop || Math.abs(currentY - downY) > scaledTouchSlop) {
                    isClick = false;
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                long time = System.currentTimeMillis() - downTime;
                if (time > longPressTimeout) {
                    isClick = false;
                }
                if (isClick) {
                    Intent intent2 = new Intent(context, context.getClass());
                    intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent2);
                }
                widthPixels = context.getResources().getDisplayMetrics().widthPixels;
                if (lp.x + contentView.getWidth() / 2 < widthPixels / 2) {
                    lp.x = 0;
                } else {
                    lp.x = widthPixels;
                }
                updatePosition(lp.x, lp.y);
                lastX = 0;
                lastY = 0;
                break;
        }
    }

    public void dismissMinimalWindow() {
        if (isViewAddedToWindow) {
            windowManager.removeViewImmediate(contentView);
            isViewAddedToWindow = false;
        }

        if (contentView != null) {
            ViewGroup viewParent = (ViewGroup) contentView.getParent();
            if (viewParent != null) {
                viewParent.removeView(contentView);
            }
        }
        contentView = null;
    }

    public void updatePosition(int x, int y) {
        lp.x = x;
        lp.y = y;
        windowManager.updateViewLayout(contentView, lp);
    }
}