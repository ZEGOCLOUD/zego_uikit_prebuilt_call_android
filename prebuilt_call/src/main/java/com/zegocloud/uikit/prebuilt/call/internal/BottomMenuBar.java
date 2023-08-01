package com.zegocloud.uikit.prebuilt.call.internal;

import android.content.Context;
import android.graphics.drawable.StateListDrawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.ImageView;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.ContextCompat;
import com.zegocloud.uikit.components.audiovideo.ZegoSwitchAudioOutputButton;
import com.zegocloud.uikit.components.audiovideo.ZegoSwitchCameraButton;
import com.zegocloud.uikit.components.audiovideo.ZegoToggleCameraButton;
import com.zegocloud.uikit.components.audiovideo.ZegoToggleMicrophoneButton;
import com.zegocloud.uikit.components.common.ZegoScreenSharingToggleButton;
import com.zegocloud.uikit.prebuilt.call.R;
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallFragment.LeaveCallListener;
import com.zegocloud.uikit.prebuilt.call.config.ZegoBottomMenuBarConfig;
import com.zegocloud.uikit.prebuilt.call.config.ZegoHangUpConfirmDialogInfo;
import com.zegocloud.uikit.prebuilt.call.config.ZegoMemberListConfig;
import com.zegocloud.uikit.prebuilt.call.config.ZegoMenuBarButtonName;
import com.zegocloud.uikit.prebuilt.call.config.ZegoMenuBarStyle;
import com.zegocloud.uikit.prebuilt.call.config.ZegoPrebuiltVideoConfig;
import com.zegocloud.uikit.utils.Utils;
import java.util.ArrayList;
import java.util.List;

public class BottomMenuBar extends LinearLayout {

    private float downY;
    private float currentY;
    private List<View> showList = new ArrayList<>();
    private List<View> hideList = new ArrayList<>();
    private MoreDialog moreDialog;
    private Runnable runnable;
    private static final long HIDE_DELAY_TIME = 5000;
    private ZegoBottomMenuBarConfig menuBarConfig;
    private ZegoPrebuiltVideoConfig screenSharingVideoConfig;
    private ZegoMemberListConfig memberListConfig;
    private ZegoHangUpConfirmDialogInfo hangUpConfirmDialogInfo;
    private LeaveCallListener leaveCallListener;

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
        runnable = () -> setVisibility(View.GONE);
    }

    private void applyMenuBarButtons(List<ZegoMenuBarButtonName> zegoMenuBarButtons) {
        showList.clear();
        hideList.clear();
        List<View> menuBarViews = getMenuBarViews(zegoMenuBarButtons);
        if (zegoMenuBarButtons.size() <= menuBarConfig.maxCount) {
            showList.addAll(menuBarViews);
        } else {
            int showChildCount = menuBarConfig.maxCount - 1;
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
        if (list != null && list.size() > 0) {
            for (ZegoMenuBarButtonName zegoMenuBarButton : list) {
                View viewFromType = getViewFromType(zegoMenuBarButton);
                viewList.add(viewFromType);
            }
        }
        return viewList;
    }

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
                view = new PermissionCameraButton(getContext());
                break;
            case TOGGLE_MICROPHONE_BUTTON:
                view = new PermissionMicrophoneButton(getContext());
                break;
            case SWITCH_CAMERA_BUTTON:
                view = new ZegoSwitchCameraButton(getContext());
                break;
            case HANG_UP_BUTTON:
                view = new ZegoLeaveCallButton(getContext());
                if (hangUpConfirmDialogInfo != null) {
                    ((ZegoLeaveCallButton) view).setHangUpConfirmInfo(hangUpConfirmDialogInfo);
                }
                if (leaveCallListener != null) {
                    ((ZegoLeaveCallButton) view).setLeaveListener(leaveCallListener);
                }
                break;
            case SWITCH_AUDIO_OUTPUT_BUTTON:
                view = new ZegoSwitchAudioOutputButton(getContext());
                break;
            case SHOW_MEMBER_LIST_BUTTON:
                view = new ImageView(getContext());
                ((ImageView) view).setImageResource(R.drawable.call_icon_top_member_normal);
                view.setOnClickListener(v -> {
                    ZegoCallMemberList memberList = new ZegoCallMemberList(getContext());
                    if (memberListConfig != null) {
                        memberList.setMemberListConfig(memberListConfig);
                    }
                    memberList.show();
                });
                break;
            case SCREEN_SHARING_TOGGLE_BUTTON:
                view = new ZegoScreenSharingToggleButton(getContext());
                ((ZegoScreenSharingToggleButton) view).bottomBarStyle();
                if (screenSharingVideoConfig != null) {
                    ((ZegoScreenSharingToggleButton) view).setPresetResolution(screenSharingVideoConfig.resolution);
                }

                break;
        }
        if (view != null) {
            view.setTag(menuBar);
        }
        return view;
    }

    public void addButtons(List<View> viewList) {
        for (View view : viewList) {
            if (showList.size() < menuBarConfig.maxCount) {
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
        removeAllViews();
        for (int i = 0; i < showList.size(); i++) {
            LayoutParams params = generateChildLayoutParams();
            if (i != 0) {
                int marginStart = Utils.dp2px(23f, getResources().getDisplayMetrics());
                if (showList.size() == 2) {
                    marginStart = Utils.dp2px(79f, getResources().getDisplayMetrics());
                } else if (showList.size() == 3) {
                    marginStart = Utils.dp2px(59.5f, getResources().getDisplayMetrics());
                } else if (showList.size() == 4) {
                    marginStart = Utils.dp2px(37f, getResources().getDisplayMetrics());
                }
                params.setMarginStart(marginStart);
            }
            View view = showList.get(i);
            addView(view, params);
        }
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

    public void setConfig(ZegoBottomMenuBarConfig bottomMenuBarConfig) {
        this.menuBarConfig = bottomMenuBarConfig;
        applyMenuBarStyle(bottomMenuBarConfig.style);
        applyMenuBarButtons(bottomMenuBarConfig.buttons);
        if (menuBarConfig.hideAutomatically) {
            getHandler().postDelayed(runnable, HIDE_DELAY_TIME);
        }
    }

    private void applyMenuBarStyle(ZegoMenuBarStyle style) {
        if (style == ZegoMenuBarStyle.LIGHT) {
            setBackground(null);
        } else {
            setBackgroundResource(R.drawable.call_background_bottom_menubar);
        }
    }

    public void setOutSideClicked() {
        if (getVisibility() == View.VISIBLE) {
            if (menuBarConfig.hideByClick) {
                setVisibility(View.GONE);
            }
        } else {
            setVisibility(View.VISIBLE);
            if (menuBarConfig.hideAutomatically) {
                getHandler().removeCallbacks(runnable);
                getHandler().postDelayed(runnable, HIDE_DELAY_TIME);
            }
        }
    }

    public void setScreenShareVideoConfig(ZegoPrebuiltVideoConfig screenSharingVideoConfig) {
        this.screenSharingVideoConfig = screenSharingVideoConfig;
        if (screenSharingVideoConfig == null) {
            return;
        }
        for (View view : showList) {
            if (view instanceof ZegoScreenSharingToggleButton) {
                ((ZegoScreenSharingToggleButton) view).setPresetResolution(screenSharingVideoConfig.resolution);
            }
        }
        for (View view : hideList) {
            if (view instanceof ZegoScreenSharingToggleButton) {
                ((ZegoScreenSharingToggleButton) view).setPresetResolution(screenSharingVideoConfig.resolution);
            }
        }
    }

    public void setMemberListConfig(ZegoMemberListConfig memberListConfig) {
        this.memberListConfig = memberListConfig;
    }

    public void setHangUpConfirmDialogInfo(ZegoHangUpConfirmDialogInfo hangUpConfirmDialogInfo) {
        this.hangUpConfirmDialogInfo = hangUpConfirmDialogInfo;
        for (View view : showList) {
            if (view instanceof ZegoLeaveCallButton) {
                ((ZegoLeaveCallButton) view).setHangUpConfirmInfo(hangUpConfirmDialogInfo);
            }
        }
        for (View view : hideList) {
            if (view instanceof ZegoLeaveCallButton) {
                ((ZegoLeaveCallButton) view).setHangUpConfirmInfo(hangUpConfirmDialogInfo);
            }
        }
    }

    public void setLeaveCallListener(LeaveCallListener leaveCallListener) {
        this.leaveCallListener = leaveCallListener;
        for (View view : showList) {
            if (view instanceof ZegoLeaveCallButton) {
                ((ZegoLeaveCallButton) view).setLeaveListener(leaveCallListener);
            }
        }
        for (View view : hideList) {
            if (view instanceof ZegoLeaveCallButton) {
                ((ZegoLeaveCallButton) view).setLeaveListener(leaveCallListener);
            }
        }
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
                ContextCompat.getDrawable(getContext(), R.drawable.call_icon_more_off));
            sld.addState(new int[]{}, ContextCompat.getDrawable(getContext(), R.drawable.call_icon_more));
            setImageDrawable(sld);
            setOnClickListener(v -> showMoreDialog());
        }
    }
}
