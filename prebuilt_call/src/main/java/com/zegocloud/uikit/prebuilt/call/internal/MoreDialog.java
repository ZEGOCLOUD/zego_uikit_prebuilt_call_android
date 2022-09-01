package com.zegocloud.uikit.prebuilt.call.internal;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import androidx.annotation.NonNull;
import com.zegocloud.uikit.utils.Utils;
import com.zegocloud.uikit.prebuilt.call.R;
import java.util.List;

public class MoreDialog extends Dialog {

    private FrameLayout root;
    private LinearLayout subRoot;

    /**
     * @param context
     */
    public MoreDialog(@NonNull Context context) {
        super(context);
        Window window = getWindow();
        window.requestFeature(Window.FEATURE_NO_TITLE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        subRoot = new LinearLayout(getContext());
        subRoot.setOrientation(LinearLayout.VERTICAL);
        subRoot.setLayoutParams(new LayoutParams(-1, -2));
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(Color.parseColor("#222222"));
        int corner = Utils.dp2px(16f, getContext().getResources().getDisplayMetrics());
        drawable.setCornerRadii(new float[]{corner, corner, corner, corner, 0, 0, 0, 0});
        subRoot.setBackground(drawable);
        int paddingTop = Utils.dp2px(12f, getContext().getResources().getDisplayMetrics());
        subRoot.setPadding(0, paddingTop, 0, 0);
        root = new FrameLayout(getContext());
        root.addView(subRoot);
        setContentView(root);

        Window window = getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(lp);
        window.setGravity(Gravity.BOTTOM);
        window.setBackgroundDrawable(new ColorDrawable());

        setCancelable(true);
        setCanceledOnTouchOutside(true);
    }

    public void setHideChildren(List<View> views) {
        for (int i = 0; i < subRoot.getChildCount(); i++) {
            LinearLayout child = (LinearLayout) subRoot.getChildAt(i);
            child.removeAllViews();
        }
        subRoot.removeAllViews();

        for (View view : views) {
            boolean added = false;
            int childCount = subRoot.getChildCount();
            if (childCount != 0) {
                ViewGroup child = (ViewGroup) subRoot.getChildAt(childCount - 1);
                if (child.getChildCount() < 5) {
                    child.addView(view, generateChildLayoutParams());
                    added = true;
                }
            }
            if (!added) {
                LinearLayout layout = new LinearLayout(getContext());
                layout.setOrientation(LinearLayout.HORIZONTAL);
                layout.setLayoutParams(new LayoutParams(-1, -2));
                layout.addView(view, generateChildLayoutParams());
                subRoot.addView(layout);
            }
        }
    }

    private LayoutParams generateChildLayoutParams() {
        int size = Utils.dp2px(48f, getContext().getResources().getDisplayMetrics());
        int marginTop = Utils.dp2px(15f, getContext().getResources().getDisplayMetrics());
        int marginBottom = Utils.dp2px(15f, getContext().getResources().getDisplayMetrics());
        int marginStart = Utils.dp2px(23f, getContext().getResources().getDisplayMetrics());
        LinearLayout.LayoutParams layoutParams = new LayoutParams(size, size);
        layoutParams.topMargin = marginTop;
        layoutParams.bottomMargin = marginBottom;
        layoutParams.setMarginStart(marginStart);
        return layoutParams;
    }

    @Override
    public void show() {
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        super.show();
        int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            | View.SYSTEM_UI_FLAG_FULLSCREEN;
        root.setSystemUiVisibility(uiOptions);
        this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
    }
}
