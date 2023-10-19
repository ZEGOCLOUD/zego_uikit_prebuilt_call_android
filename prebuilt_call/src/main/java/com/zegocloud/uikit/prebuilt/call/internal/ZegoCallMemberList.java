package com.zegocloud.uikit.prebuilt.call.internal;


import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.zegocloud.uikit.ZegoUIKit;
import com.zegocloud.uikit.components.memberlist.ZegoMemberListComparator;
import com.zegocloud.uikit.components.memberlist.ZegoMemberListItemViewProvider;
import com.zegocloud.uikit.prebuilt.call.R;
import com.zegocloud.uikit.prebuilt.call.config.ZegoMemberListConfig;
import com.zegocloud.uikit.prebuilt.call.databinding.CallLayoutMemberlistBinding;
import com.zegocloud.uikit.service.defines.ZegoUIKitUser;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ZegoCallMemberList extends BottomSheetDialog {

    private CallLayoutMemberlistBinding binding;
    private ZegoMemberListItemViewProvider memberListItemProvider;
    private ZegoMemberListConfig memberListConfig;

    public ZegoCallMemberList(@NonNull Context context) {
        super(context, R.style.Call_TransparentDialog);
        Window window = getWindow();
        window.requestFeature(Window.FEATURE_NO_TITLE);
    }

    public ZegoCallMemberList(@NonNull Context context, int themeResId) {
        super(context, themeResId);
        Window window = getWindow();
        window.requestFeature(Window.FEATURE_NO_TITLE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = CallLayoutMemberlistBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Window window = getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = LayoutParams.MATCH_PARENT;
        lp.height = LayoutParams.WRAP_CONTENT;
        lp.dimAmount = 0.5f;
        lp.gravity = Gravity.BOTTOM;
        window.setAttributes(lp);
        setCanceledOnTouchOutside(true);
        window.setBackgroundDrawable(new ColorDrawable());

        binding.memberlistDown.setOnClickListener(v -> {
            dismiss();
        });
        if (memberListItemProvider != null) {
            binding.memberlist.setItemViewProvider(memberListItemProvider);
        }
        if (memberListConfig != null) {
            binding.memberlist.setShowCameraState(memberListConfig.showCameraState);
            binding.memberlist.setShowMicrophoneState(memberListConfig.showMicrophoneState);
        }
        binding.memberlist.setMemberListComparator(new ZegoMemberListComparator() {
            @Override
            public List<ZegoUIKitUser> sortUserList(List<ZegoUIKitUser> userList) {
                List<ZegoUIKitUser> sortUsers = new ArrayList<>();
                ZegoUIKitUser self = ZegoUIKit.getLocalUser();
                userList.remove(self);
                Collections.reverse(userList);
                sortUsers.add(self);
                sortUsers.addAll(userList);
                return sortUsers;
            }
        });

        // both need setPeekHeight & setLayoutParams
        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        int height = (int) (displayMetrics.heightPixels * 0.85f);
        getBehavior().setPeekHeight(height);
        CoordinatorLayout.LayoutParams params = new CoordinatorLayout.LayoutParams(-1, height);
        binding.memberlistLayout.setLayoutParams(params);
    }

    @Override
    public boolean onKeyUp(int keyCode, @NonNull KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_ESCAPE) {
            dismiss();
            return true;
        }
        return false;
    }

    public void setMemberListConfig(ZegoMemberListConfig memberListConfig) {
        this.memberListConfig = memberListConfig;
        if (memberListConfig != null && memberListConfig.memberListItemProvider != null) {
            this.memberListItemProvider = memberListConfig.memberListItemProvider;
        }
    }
}
