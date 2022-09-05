package com.zegocloud.uikit.prebuilt.call.internal;

import android.content.Context;
import android.graphics.drawable.StateListDrawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.ContextCompat;
import com.zegocloud.uikit.components.audiovideo.ZegoSwitchAudioOutputButton;
import com.zegocloud.uikit.components.audiovideo.ZegoSwitchCameraButton;
import com.zegocloud.uikit.components.audiovideo.ZegoToggleCameraButton;
import com.zegocloud.uikit.components.audiovideo.ZegoToggleMicrophoneButton;
import com.zegocloud.uikit.prebuilt.call.R;
import com.zegocloud.uikit.prebuilt.call.ZegoHangUpConfirmDialogInfo;
import com.zegocloud.uikit.prebuilt.call.ZegoMenuBarButtonName;
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallFragment.HangUpListener;
import com.zegocloud.uikit.utils.Utils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BottomMenuBar extends LinearLayout {

    private Map<ZegoMenuBarButtonName, OnClickListener> clickListenerMap = new HashMap();
    private float downY;
    private float currentY;
    private int limitedCount = 5;
    private List<ZegoMenuBarButtonName> zegoMenuBarButtons = new ArrayList<>();
    private List<View> showList = new ArrayList<>();
    private List<View> hideList = new ArrayList<>();
    private MoreDialog moreDialog;
    private ZegoHangUpConfirmDialogInfo hangUpConfirmDialogInfo;
    private HangUpListener hangUpListener;

    public BottomMenuBar(@NonNull Context context) {
        super(context);
        initView();
    }

    public BottomMenuBar(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public BottomMenuBar(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        setOrientation(LinearLayout.HORIZONTAL);
        setLayoutParams(new LayoutParams(-1, -2));
        setGravity(Gravity.CENTER_HORIZONTAL);
    }

    public void setButtons(List<ZegoMenuBarButtonName> zegoMenuBarButtons) {
        this.zegoMenuBarButtons.clear();
        this.zegoMenuBarButtons.addAll(zegoMenuBarButtons);

        showList.clear();
        hideList.clear();
        List<View> menuBarViews = getMenuBarViews(zegoMenuBarButtons);
        if (zegoMenuBarButtons.size() <= limitedCount) {
            showList.addAll(menuBarViews);
        } else {
            int showChildCount = limitedCount - 1;
            if (showChildCount > 0) {
                showList.addAll(menuBarViews.subList(0, showChildCount));
                hideList = menuBarViews.subList(showChildCount, menuBarViews.size());
            }
            showList.add(new MoreButton(getContext()));
        }
        notifyListChanged();
    }

    private List<View> getMenuBarViews(List<ZegoMenuBarButtonName> list) {
        List<View> viewList = new ArrayList<>();
        for (ZegoMenuBarButtonName zegoMenuBarButton : list) {
            View viewFromType = getViewFromType(zegoMenuBarButton);
            //            OnClickListener onClickListener = clickListenerMap.get(zegoMenuBarButton);
            //            if (onClickListener != null) {
            //                viewFromType.setOnClickListener(new OnClickListener() {
            //                    @Override
            //                    public void onClick(View v) {
            //                        onClickListener.onClick(v);
            //                    }
            //                });
            //            }
            viewList.add(viewFromType);
        }
        return viewList;
    }

    private void resetVisibleChildren(List<View> viewList) {
        removeAllViews();
        for (int i = 0; i < viewList.size(); i++) {
            LayoutParams params = generateChildLayoutParams();
            if (i != 0) {
                int marginStart = Utils.dp2px(23f, getResources().getDisplayMetrics());
                if (viewList.size() == 2) {
                    marginStart = Utils.dp2px(79f, getResources().getDisplayMetrics());
                } else if (viewList.size() == 3) {
                    marginStart = Utils.dp2px(59.5f, getResources().getDisplayMetrics());
                } else if (viewList.size() == 4) {
                    marginStart = Utils.dp2px(37f, getResources().getDisplayMetrics());
                }
                params.setMarginStart(marginStart);
            }
            View view = viewList.get(i);
            addView(view, params);
        }
    }

    public void setHangUpListener(HangUpListener listener) {
        this.hangUpListener = listener;
        boolean find = false;
        for (View view : showList) {
            if (view instanceof ZegoHangUpButton) {
                ((ZegoHangUpButton) view).setHangUpListener(hangUpListener);
                find = true;
                break;
            }
        }
        if (!find) {
            for (View view : hideList) {
                if (view instanceof ZegoHangUpButton) {
                    ((ZegoHangUpButton) view).setHangUpListener(hangUpListener);
                    find = true;
                    break;
                }
            }
        }
    }

    public void setHangUpConfirmDialogInfo(ZegoHangUpConfirmDialogInfo dialogInfo) {
        this.hangUpConfirmDialogInfo = dialogInfo;
        boolean find = false;
        for (View view : showList) {
            if (view instanceof ZegoHangUpButton) {
                ((ZegoHangUpButton) view).setHangUpConfirmInfo(hangUpConfirmDialogInfo);
                find = true;
                break;
            }
        }
        if (!find) {
            for (View view : hideList) {
                if (view instanceof ZegoHangUpButton) {
                    ((ZegoHangUpButton) view).setHangUpConfirmInfo(hangUpConfirmDialogInfo);
                    find = true;
                    break;
                }
            }
        }
    }

    //    public void setOnClickListener(ZegoMenuBarButtonName buttonName, View.OnClickListener onClickListener) {
    //        clickListenerMap.put(buttonName, onClickListener);
    //        for (View view : showList) {
    //            if (Objects.equals(view.getTag(), buttonName)) {
    //                view.setOnClickListener(new OnClickListener() {
    //                    @Override
    //                    public void onClick(View v) {
    //                        onClickListener.onClick(v);
    //                    }
    //                });
    //                break;
    //            }
    //        }
    //        for (View view : hideList) {
    //            if (Objects.equals(view.getTag(), buttonName)) {
    //                view.setOnClickListener(new OnClickListener() {
    //                    @Override
    //                    public void onClick(View v) {
    //                        onClickListener.onClick(v);
    //                    }
    //                });
    //                break;
    //            }
    //        }
    //    }

    private LayoutParams generateChildLayoutParams() {
        int size = Utils.dp2px(48f, getResources().getDisplayMetrics());
        int marginTop = Utils.dp2px(31f, getResources().getDisplayMetrics());
        int marginBottom = Utils.dp2px(25f, getResources().getDisplayMetrics());
        LinearLayout.LayoutParams layoutParams = new LayoutParams(size, size);
        layoutParams.topMargin = marginTop;
        layoutParams.bottomMargin = marginBottom;
        return layoutParams;
    }

    private View getViewFromType(ZegoMenuBarButtonName menuBar) {
        View view = null;
        switch (menuBar) {
            case TOGGLE_CAMERA_BUTTON:
                view = new ZegoToggleCameraButton(getContext());
                break;
            case TOGGLE_MICROPHONE_BUTTON:
                view = new ZegoToggleMicrophoneButton(getContext());
                break;
            case SWITCH_CAMERA_BUTTON:
                view = new ZegoSwitchCameraButton(getContext());
                break;
            case HANG_UP_BUTTON:
                view = new ZegoHangUpButton(getContext());
                if (hangUpConfirmDialogInfo != null) {
                    ((ZegoHangUpButton) view).setHangUpConfirmInfo(hangUpConfirmDialogInfo);
                }
                if (hangUpListener != null) {
                    ((ZegoHangUpButton) view).setHangUpListener(hangUpListener);
                }
                break;
            case SWITCH_AUDIO_OUTPUT_BUTTON:
                view = new ZegoSwitchAudioOutputButton(getContext());
                break;
        }
        if (view != null) {
            view.setTag(menuBar);
        }
        return view;
    }

    public void addButtons(List<View> viewList) {
        if (viewList.size() == 0) {
            return;
        }
        for (View view : viewList) {
            if (showList.size() < limitedCount) {
                showList.add(view);
            } else {
                View lastView = showList.get(showList.size() - 1);
                if (!(lastView instanceof MoreButton)) {
                    showList.remove(lastView);
                    showList.add(new MoreButton(getContext()));
                    hideList.add(lastView);
                }
                hideList.add(view);
            }
        }
        notifyListChanged();
    }

    private void showMoreDialog() {
        if (moreDialog == null) {
            moreDialog = new MoreDialog(getContext());
        }
        if (!moreDialog.isShowing()) {
            moreDialog.show();
        }
        moreDialog.setHideChildren(hideList);
    }

    private void notifyListChanged() {
        resetVisibleChildren(showList);
        if (moreDialog != null) {
            moreDialog.setHideChildren(hideList);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            currentY = event.getY();
            downY = currentY;
        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            float currentY = event.getY();
            int scaledTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
            if (currentY - downY < -scaledTouchSlop) {

            }
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            currentY = 0;
            downY = 0;
        }
        return super.onTouchEvent(event);
    }

    public void setLimitedCount(int limitedCount) {
        this.limitedCount = limitedCount;
    }

    public class MoreButton extends AppCompatImageView {

        public MoreButton(@NonNull Context context) {
            super(context);
            initView();
        }

        public MoreButton(@NonNull Context context, @Nullable AttributeSet attrs) {
            super(context, attrs);
            initView();
        }

        private void initView() {
            StateListDrawable sld = new StateListDrawable();
            sld.addState(new int[]{android.R.attr.state_pressed},
                ContextCompat.getDrawable(getContext(), R.drawable.icon_more_off));
            sld.addState(new int[]{}, ContextCompat.getDrawable(getContext(), R.drawable.icon_more));
            setImageDrawable(sld);
            setOnClickListener(v -> showMoreDialog());
        }
    }
}
