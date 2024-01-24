package com.zegocloud.uikit.prebuilt.call.invite.widget;

import android.content.Context;
import android.util.AttributeSet;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.zegocloud.uikit.components.common.ZEGOImageButton;
import com.zegocloud.uikit.service.defines.ZegoUIKitUser;
import java.util.ArrayList;
import java.util.List;

public class ZegoCallButton extends ZEGOImageButton {

    protected List<ZegoUIKitUser> invitees = new ArrayList<>();
    protected int type;
    protected String data = "";
    protected int timeout = 60;
    private ZegoCallButtonListener callButtonListener;

    public ZegoCallButton(@NonNull Context context) {
        super(context);
    }

    public ZegoCallButton(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ZegoCallButton(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void initView() {
        super.initView();
    }

    @Override
    protected boolean beforeClick() {
        return super.beforeClick();
    }

    @Override
    protected void afterClick() {
        super.afterClick();
    }

    public void setCallButtonListener(ZegoCallButtonListener callButtonListener) {
        this.callButtonListener = callButtonListener;
    }
}
