package com.zegocloud.uikit.prebuilt.call.internal;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import com.zegocloud.uikit.ZegoUIKit;
import com.zegocloud.uikit.components.audiovideo.ZegoSwitchAudioOutputButton;
import com.zegocloud.uikit.components.audiovideo.ZegoSwitchCameraButton;
import com.zegocloud.uikit.components.audiovideo.ZegoToggleCameraButton;
import com.zegocloud.uikit.components.audiovideo.ZegoToggleMicrophoneButton;
import com.zegocloud.uikit.components.common.ZegoScreenSharingToggleButton;
import com.zegocloud.uikit.prebuilt.call.R;
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallConfig;
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallService;
import com.zegocloud.uikit.prebuilt.call.config.ZegoHangUpConfirmDialogInfo;
import com.zegocloud.uikit.prebuilt.call.config.ZegoInRoomChatConfig;
import com.zegocloud.uikit.prebuilt.call.config.ZegoMemberListConfig;
import com.zegocloud.uikit.prebuilt.call.config.ZegoMenuBarButtonConfig;
import com.zegocloud.uikit.prebuilt.call.config.ZegoMenuBarButtonName;
import com.zegocloud.uikit.prebuilt.call.config.ZegoMenuBarStyle;
import com.zegocloud.uikit.prebuilt.call.config.ZegoPrebuiltVideoConfig;
import com.zegocloud.uikit.prebuilt.call.config.ZegoTopMenuBarConfig;
import com.zegocloud.uikit.prebuilt.call.event.ZegoMenuBarButtonClickListener;
import com.zegocloud.uikit.prebuilt.call.core.CallInvitationServiceImpl;
import com.zegocloud.uikit.utils.Utils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TopMenuBar extends FrameLayout {

    private final List<View> showList = new ArrayList<>();
    private LinearLayout contentView;
    private TextView titleView;
    private String title;
    private Runnable runnable;
    private static final long HIDE_DELAY_TIME = 5000;
    private ZegoTopMenuBarConfig menuBarConfig;
    private ZegoPrebuiltVideoConfig screenSharingVideoConfig;
    private ZegoMemberListConfig memberListConfig;
    private ZegoHangUpConfirmDialogInfo hangUpConfirmDialogInfo;
    private Dialog beautyDialog;
    private ZegoInRoomChatConfig inRoomChatConfig;
    private Map<ZegoMenuBarButtonName, View> enumViewMap = new HashMap<>();

    public TopMenuBar(Context context) {
        super(context);
        initView();
    }

    public TopMenuBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public TopMenuBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        int paddingEnd = Utils.dp2px(8f, getResources().getDisplayMetrics());
        int paddingStart = Utils.dp2px(6f, getResources().getDisplayMetrics());
        setPadding(paddingStart, 0, paddingEnd, 0);

        contentView = new LinearLayout(getContext());
        contentView.setOrientation(LinearLayout.HORIZONTAL);
        contentView.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
        addView(contentView);

        titleView = new TextView(getContext());
        LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.CENTER_VERTICAL;
        titleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        titleView.setTextColor(Color.WHITE);
        if (title != null) {
            titleView.setText(title);
        }
        addView(titleView, layoutParams);
        runnable = () -> setVisibility(View.GONE);

        for (ZegoMenuBarButtonName name : ZegoMenuBarButtonName.values()) {
            View viewFromType = getViewFromType(name);
            if (viewFromType != null) {
                enumViewMap.put(name, viewFromType);
            }
        }
    }

    @NonNull
    private LayoutParams getChildLayoutParams() {
        int childWidth = Utils.dp2px(35f, getResources().getDisplayMetrics());
        int childHeight = Utils.dp2px(35f, getResources().getDisplayMetrics());
        int childMarginEnd = Utils.dp2px(6f, getResources().getDisplayMetrics());
        LayoutParams params = new LayoutParams(childWidth, childHeight);
        params.setMarginEnd(childMarginEnd);
        return params;
    }

    public void setConfig(ZegoTopMenuBarConfig config) {
        menuBarConfig = config;
        applyMenuBarStyle(config.style);
        applyMenuBarButtons(config.buttons);
        if (!menuBarConfig.isVisible) {
            setVisibility(View.GONE);
        }
        getHandler().postDelayed(runnable, HIDE_DELAY_TIME);
    }

    private void applyMenuBarButtons(List<ZegoMenuBarButtonName> buttons) {
        showList.clear();
        List<ZegoMenuBarButtonName> distinct = new ArrayList<>(buttons.size());
        for (ZegoMenuBarButtonName button : buttons) {
            if (!distinct.contains(button)) {
                distinct.add(button);
            }
        }
        if (distinct.size() > menuBarConfig.maxCount) {
            distinct = distinct.subList(0, menuBarConfig.maxCount);
        }
        List<View> menuBarViews = getMenuBarViews(distinct);
        showList.addAll(menuBarViews);
        notifyListChanged();
    }

    private List<View> getMenuBarViews(List<ZegoMenuBarButtonName> buttons) {
        List<View> viewList = new ArrayList<>();
        if (buttons != null && buttons.size() > 0) {
            for (ZegoMenuBarButtonName zegoMenuBarButton : buttons) {
                View viewFromType = enumViewMap.get(zegoMenuBarButton);
                if (viewFromType != null && !viewList.contains(viewFromType)) {
                    viewList.add(viewFromType);
                }
            }
        }
        return viewList;
    }

    private View getViewFromType(ZegoMenuBarButtonName menuBar) {
        View view = null;
        ZegoUIKitPrebuiltCallConfig callConfig = CallInvitationServiceImpl.getInstance().getCallConfig();
        switch (menuBar) {
            case TOGGLE_CAMERA_BUTTON: {
                view = new PermissionCameraButton(getContext());
                ((ZegoToggleCameraButton) view).setIcon(R.drawable.call_icon_top_camera_normal,
                    R.drawable.call_icon_top_camera_off);
                if (callConfig != null && callConfig.topMenuBarConfig != null) {
                    ZegoMenuBarButtonConfig buttonConfig = callConfig.topMenuBarConfig.buttonConfig;
                    if (buttonConfig != null) {
                        if (buttonConfig.toggleCameraOnImage != null) {
                            ((PermissionCameraButton) view).setOpenDrawable(buttonConfig.toggleCameraOnImage);
                        }
                        if (buttonConfig.toggleCameraOffImage != null) {
                            ((PermissionCameraButton) view).setCloseDrawable(buttonConfig.toggleCameraOffImage);
                        }
                    }
                }
            }
            break;
            case TOGGLE_MICROPHONE_BUTTON: {
                view = new PermissionMicrophoneButton(getContext());
                ((ZegoToggleMicrophoneButton) view).setIcon(R.drawable.call_icon_top_mic_normal,
                    R.drawable.call_icon_top_mic_off);
                if (callConfig != null && callConfig.topMenuBarConfig != null) {
                    ZegoMenuBarButtonConfig buttonConfig = callConfig.topMenuBarConfig.buttonConfig;
                    if (buttonConfig != null) {
                        if (buttonConfig.toggleMicrophoneOnImage != null) {
                            ((PermissionMicrophoneButton) view).setOpenDrawable(buttonConfig.toggleMicrophoneOnImage);
                        }
                        if (buttonConfig.toggleMicrophoneOffImage != null) {
                            ((PermissionMicrophoneButton) view).setCloseDrawable(buttonConfig.toggleMicrophoneOffImage);
                        }
                    }
                }
            }

            break;
            case SWITCH_CAMERA_BUTTON: {
                view = new ZegoSwitchCameraButton(getContext());
                ((ZegoSwitchCameraButton) view).setImageResource(R.drawable.call_icon_top_camera_switch,
                    R.drawable.call_icon_top_camera_switch);
                if (callConfig != null && callConfig.topMenuBarConfig != null) {
                    ZegoMenuBarButtonConfig buttonConfig = callConfig.topMenuBarConfig.buttonConfig;
                    if (buttonConfig != null) {
                        if (buttonConfig.switchCameraFrontImage != null) {
                            ((ZegoSwitchCameraButton) view).setOpenDrawable(buttonConfig.switchCameraFrontImage);
                        }
                        if (buttonConfig.switchCameraBackImage != null) {
                            ((ZegoSwitchCameraButton) view).setCloseDrawable(buttonConfig.switchCameraBackImage);
                        }
                    }
                }
            }

            break;
            case HANG_UP_BUTTON: {
                view = new ZegoLeaveCallButton(getContext());
                ((ZegoLeaveCallButton) view).setIcon(R.drawable.call_icon_top_leave);
                if (callConfig != null && callConfig.topMenuBarConfig != null) {
                    ZegoMenuBarButtonConfig buttonConfig = callConfig.topMenuBarConfig.buttonConfig;
                    if (buttonConfig != null) {
                        if (buttonConfig.hangUpButtonImage != null) {
                            ((ZegoLeaveCallButton) view).setImageDrawable(buttonConfig.hangUpButtonImage);
                        }
                    }
                }
                view.setOnClickListener(v -> {
                    ZegoMenuBarButtonClickListener clickListener = ZegoUIKitPrebuiltCallService.events.callEvents.getButtonClickListener();
                    if (clickListener != null) {
                        clickListener.onClick(ZegoMenuBarButtonName.HANG_UP_BUTTON, v);
                    }
                });
            }

            break;
            case SWITCH_AUDIO_OUTPUT_BUTTON: {
                view = new ZegoSwitchAudioOutputButton(getContext());
                ((ZegoSwitchAudioOutputButton) view).setIcon(R.drawable.call_icon_top_speaker_normal,
                    R.drawable.call_icon_top_speaker_close, R.drawable.call_icon_top_bluetooth);
                if (callConfig != null && callConfig.topMenuBarConfig != null) {
                    ZegoMenuBarButtonConfig buttonConfig = callConfig.topMenuBarConfig.buttonConfig;
                    if (buttonConfig != null) {
                        if (buttonConfig.audioOutputSpeakerImage != null) {
                            ((ZegoSwitchAudioOutputButton) view).setSpeakerImageIcon(
                                buttonConfig.audioOutputSpeakerImage);
                        }
                        if (buttonConfig.audioOutputBluetoothImage != null) {
                            ((ZegoSwitchAudioOutputButton) view).setBluetoothIcon(
                                buttonConfig.audioOutputBluetoothImage);
                        }

                        if (buttonConfig.audioOutputHeadphoneImage != null) {
                            ((ZegoSwitchAudioOutputButton) view).setEarpieceIcon(
                                buttonConfig.audioOutputHeadphoneImage);
                        }
                    }
                }
                view.setOnClickListener(v -> {
                    ZegoMenuBarButtonClickListener clickListener = ZegoUIKitPrebuiltCallService.events.callEvents.getButtonClickListener();
                    if (clickListener != null) {
                        clickListener.onClick(ZegoMenuBarButtonName.SWITCH_AUDIO_OUTPUT_BUTTON, v);
                    }
                });
            }
            break;
            case SHOW_MEMBER_LIST_BUTTON: {
                view = new ImageView(getContext());
                ((ImageView) view).setImageResource(R.drawable.call_icon_top_member_normal);
                if (callConfig != null && callConfig.topMenuBarConfig != null) {
                    ZegoMenuBarButtonConfig buttonConfig = callConfig.topMenuBarConfig.buttonConfig;
                    if (buttonConfig != null) {
                        if (buttonConfig.showMemberListButtonImage != null) {
                            ((ImageView) view).setImageDrawable(buttonConfig.showMemberListButtonImage);
                        }
                    }
                }
                view.setOnClickListener(v -> {
                    ZegoCallMemberList memberList = new ZegoCallMemberList(getContext());
                    if (memberListConfig != null) {
                        memberList.setMemberListConfig(memberListConfig);
                    }
                    memberList.show();
                    ZegoMenuBarButtonClickListener clickListener = ZegoUIKitPrebuiltCallService.events.callEvents.getButtonClickListener();
                    if (clickListener != null) {
                        clickListener.onClick(ZegoMenuBarButtonName.SHOW_MEMBER_LIST_BUTTON, v);
                    }
                });
            }
            break;
            case SCREEN_SHARING_TOGGLE_BUTTON: {
                view = new ZegoScreenSharingToggleButton(getContext());
                ((ZegoScreenSharingToggleButton) view).topBarStyle();
                if (callConfig != null && callConfig.topMenuBarConfig != null) {
                    ZegoMenuBarButtonConfig buttonConfig = callConfig.topMenuBarConfig.buttonConfig;
                    if (buttonConfig != null) {
                        if (buttonConfig.screenSharingToggleButtonOnImage != null) {
                            ((ZegoScreenSharingToggleButton) view).setOpenDrawable(
                                buttonConfig.screenSharingToggleButtonOnImage);
                        }
                        if (buttonConfig.screenSharingToggleButtonOffImage != null) {
                            ((ZegoScreenSharingToggleButton) view).setCloseDrawable(
                                buttonConfig.screenSharingToggleButtonOffImage);
                        }
                    }
                }
                if (screenSharingVideoConfig != null) {
                    ((ZegoScreenSharingToggleButton) view).setPresetResolution(screenSharingVideoConfig.resolution);
                }
                view.setOnClickListener(v -> {
                    ZegoMenuBarButtonClickListener clickListener = ZegoUIKitPrebuiltCallService.events.callEvents.getButtonClickListener();
                    if (clickListener != null) {
                        clickListener.onClick(ZegoMenuBarButtonName.SCREEN_SHARING_TOGGLE_BUTTON, v);
                    }
                });
            }
            break;
            case BEAUTY_BUTTON: {
                view = new BeautyButton(getContext());
                if (callConfig != null && callConfig.topMenuBarConfig != null) {
                    ZegoMenuBarButtonConfig buttonConfig = callConfig.topMenuBarConfig.buttonConfig;
                    if (buttonConfig != null) {
                        if (buttonConfig.beautyButtonImage != null) {
                            ((BeautyButton) view).setImageDrawable(buttonConfig.beautyButtonImage,
                                buttonConfig.beautyButtonImage);
                        }
                    }
                }
                view.setOnClickListener(v -> {
                    if (beautyDialog == null) {
                        beautyDialog = ZegoUIKit.getBeautyPlugin().getBeautyDialog(getContext());
                    }
                    if (beautyDialog != null) {
                        beautyDialog.show();
                    }
                    ZegoMenuBarButtonClickListener clickListener = ZegoUIKitPrebuiltCallService.events.callEvents.getButtonClickListener();
                    if (clickListener != null) {
                        clickListener.onClick(ZegoMenuBarButtonName.BEAUTY_BUTTON, v);
                    }
                });
                if (ZegoUIKit.getBeautyPlugin().isPluginExited()) {
                    view.setVisibility(VISIBLE);
                } else {
                    view.setVisibility(GONE);
                }
            }
            break;
            case CHAT_BUTTON: {
                view = new ImageView(getContext());
                ((ImageView) view).setImageResource(R.drawable.call_icon_chat_normal);
                if (callConfig != null && callConfig.topMenuBarConfig != null) {
                    ZegoMenuBarButtonConfig buttonConfig = callConfig.topMenuBarConfig.buttonConfig;
                    if (buttonConfig != null) {
                        if (buttonConfig.chatButtonImage != null) {
                            ((ImageView) view).setImageDrawable(buttonConfig.chatButtonImage);
                        }
                    }
                }
                view.setOnClickListener(v -> {
                    ZegoInRoomChatDialog inRoomChatDialog = new ZegoInRoomChatDialog(getContext());
                    inRoomChatDialog.setInRoomChatConfig(inRoomChatConfig);
                    inRoomChatDialog.show();
                    ZegoMenuBarButtonClickListener clickListener = ZegoUIKitPrebuiltCallService.events.callEvents.getButtonClickListener();
                    if (clickListener != null) {
                        clickListener.onClick(ZegoMenuBarButtonName.CHAT_BUTTON, v);
                    }
                });
            }
            break;
            case MINIMIZING_BUTTON: {
                view = new MiniVideoButton(getContext());
                if (callConfig != null && callConfig.topMenuBarConfig != null) {
                    ZegoMenuBarButtonConfig buttonConfig = callConfig.topMenuBarConfig.buttonConfig;
                    if (buttonConfig != null) {
                        if (buttonConfig.minimizingButtonImage != null) {
                            ((MiniVideoButton) view).setImageDrawable(buttonConfig.minimizingButtonImage,
                                buttonConfig.minimizingButtonImage);
                        }
                    }
                }
                view.setOnClickListener(v -> {
                    ZegoMenuBarButtonClickListener clickListener = ZegoUIKitPrebuiltCallService.events.callEvents.getButtonClickListener();
                    if (clickListener != null) {
                        clickListener.onClick(ZegoMenuBarButtonName.MINIMIZING_BUTTON, v);
                    }
                });
            }
            break;
        }
        if (view != null) {
            view.setTag(menuBar);
        }
        return view;
    }

    public void setInRoomChatConfig(ZegoInRoomChatConfig inRoomChatConfig) {
        this.inRoomChatConfig = inRoomChatConfig;
    }

    public void addButtons(List<View> viewList) {
        int length = menuBarConfig.maxCount - showList.size();
        if (length > 0) {
            if (viewList.size() > length) {
                viewList = viewList.subList(0, length);
            }
            showList.addAll(viewList);
        }
        notifyListChanged();
    }

    private void notifyListChanged() {
        contentView.removeAllViews();
        for (View view : showList) {
            contentView.addView(view, getChildLayoutParams());
        }
    }

    private void applyMenuBarStyle(ZegoMenuBarStyle style) {
        if (style == ZegoMenuBarStyle.LIGHT) {
            setBackground(null);
        } else {
            setBackgroundColor(Color.parseColor("#171821"));
        }
    }

    public void setTitleText(String title) {
        this.title = title;
        if (titleView != null && title != null) {
            titleView.setText(title);
        }
    }

    public void setOutSideClicked() {
        if (getVisibility() == View.VISIBLE) {
            if (menuBarConfig.hideByClick) {
                setVisibility(View.GONE);
            }
        } else {
            if (menuBarConfig.isVisible) {
                setVisibility(View.VISIBLE);
            }
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
    }

    public void setMemberListConfig(ZegoMemberListConfig memberListConfig) {
        this.memberListConfig = memberListConfig;
    }
}
