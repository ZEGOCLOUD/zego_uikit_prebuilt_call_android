package com.zegocloud.uikit.prebuilt.call.internal;

import android.Manifest.permission;
import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import com.permissionx.guolindev.PermissionX;
import com.permissionx.guolindev.callback.ExplainReasonCallback;
import com.permissionx.guolindev.callback.RequestCallback;
import com.permissionx.guolindev.request.ExplainScope;
import com.zegocloud.uikit.components.common.ZEGOImageButton;
import com.zegocloud.uikit.prebuilt.call.R;
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallConfig;
import com.zegocloud.uikit.prebuilt.call.invite.internal.CallInvitationServiceImpl;
import com.zegocloud.uikit.utils.Utils;
import java.util.List;

public class MiniVideoButton extends ZEGOImageButton {

    public MiniVideoButton(@NonNull Context context) {
        super(context);
        initView();
    }

    public MiniVideoButton(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public MiniVideoButton(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    @Override
    protected void initView() {
        super.initView();
        setImageResource(R.drawable.call_icon_minimal, R.drawable.call_icon_minimal);

        boolean isCallInvite = CallInvitationServiceImpl.getInstance().getCallInvitationConfig() != null;

        int padding = Utils.dp2px(6, getResources().getDisplayMetrics());
        setPadding(padding, padding, padding, padding);
        if (isCallInvite) {
            setVisibility(View.VISIBLE);
        } else {
            setVisibility(View.GONE);
        }

        setRoundPercent(1.0f);
    }

    @Override
    protected void afterClick() {
        super.afterClick();
        boolean activityContext = getContext() instanceof FragmentActivity;
        if (activityContext) {
            FragmentActivity activity = (FragmentActivity) getContext();
            if (checkAlertWindowPermission()) {
                activity.moveTaskToBack(true);
            } else {
                PermissionX.init(activity).permissions(permission.SYSTEM_ALERT_WINDOW)
                    .onExplainRequestReason(new ExplainReasonCallback() {
                        @Override
                        public void onExplainReason(@NonNull ExplainScope scope, @NonNull List<String> deniedList) {
                            String message = "";
                            String agree = "";
                            String disagree = "";
                            ZegoUIKitPrebuiltCallConfig callConfig = CallInvitationServiceImpl.getInstance()
                                .getCallConfig();
                            if (callConfig != null && callConfig.miniVideoConfig != null && !TextUtils.isEmpty(
                                callConfig.miniVideoConfig.permissionText)) {
                                message = callConfig.miniVideoConfig.permissionText;
                            }
                            if (callConfig != null && callConfig.zegoCallText != null) {
                                message = callConfig.zegoCallText.permissionFloatWindow;
                                agree = callConfig.zegoCallText.agree;
                                disagree = callConfig.zegoCallText.disagree;
                            }
                            scope.showRequestReasonDialog(deniedList, message, agree, disagree);
                        }
                    }).request(new RequestCallback() {
                        @Override
                        public void onResult(boolean allGranted, @NonNull List<String> grantedList,
                            @NonNull List<String> deniedList) {
                            if (allGranted) {
                                activity.moveTaskToBack(true);
                            }
                        }
                    });
            }
        }

    }

    private boolean checkAlertWindowPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Settings.canDrawOverlays(getContext());
        } else {
            return true;
        }
    }
}
