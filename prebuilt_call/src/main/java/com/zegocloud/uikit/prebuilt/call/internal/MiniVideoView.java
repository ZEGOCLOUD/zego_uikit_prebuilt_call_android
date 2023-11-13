package com.zegocloud.uikit.prebuilt.call.internal;

import android.content.Context;
import android.content.res.ColorStateList;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import com.zegocloud.uikit.ZegoUIKit;
import com.zegocloud.uikit.plugin.invitation.ZegoInvitationType;
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallConfig;
import com.zegocloud.uikit.prebuilt.call.databinding.CallLayoutMiniViewBinding;
import com.zegocloud.uikit.prebuilt.call.invite.ZegoCallInvitationData;
import com.zegocloud.uikit.prebuilt.call.invite.internal.CallInvitationServiceImpl;
import com.zegocloud.uikit.service.defines.ZegoUIKitUser;

public class MiniVideoView extends ConstraintLayout {

    private CallLayoutMiniViewBinding binding;

    public MiniVideoView(@NonNull Context context) {
        super(context);
        initView(context);
    }

    public MiniVideoView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public MiniVideoView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    public MiniVideoView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView(context);
    }

    private void initView(Context context) {
        binding = CallLayoutMiniViewBinding.inflate(LayoutInflater.from(context), this, true);
        ZegoCallInvitationData invitationData = CallInvitationServiceImpl.getInstance().getCallInvitationData();

        ZegoUIKitUser localUser = ZegoUIKit.getLocalUser();
        ZegoUIKitPrebuiltCallConfig callConfig = CallInvitationServiceImpl.getInstance().getCallConfig();
        binding.miniViewVideoSmall.setAvatarViewProvider(callConfig.avatarViewProvider);
        binding.miniViewVideoSmall.setUserID(localUser.userID);
        binding.miniViewVideoLarge.setAvatarViewProvider(callConfig.avatarViewProvider);

        if (callConfig.miniVideoConfig != null && callConfig.miniVideoConfig.miniVideoTextColor != 0) {
            binding.miniViewText.setTextColor(callConfig.miniVideoConfig.miniVideoTextColor);
        }
        boolean isVoiceCall = invitationData.type == ZegoInvitationType.VOICE_CALL.getValue();
        if (isVoiceCall) {
            if (callConfig.miniVideoConfig != null && callConfig.miniVideoConfig.miniVideoAudioDrawable != null) {
                binding.miniViewIcon.setImageDrawable(callConfig.miniVideoConfig.miniVideoAudioDrawable);
            } else {
                binding.miniViewIcon.setImageResource(com.zegocloud.uikit.R.drawable.zego_uikit_icon_online_voice);
                if (callConfig.miniVideoConfig != null && callConfig.miniVideoConfig.miniVideoDrawableColor != 0) {
                    ColorStateList tintColor = ColorStateList.valueOf(
                        callConfig.miniVideoConfig.miniVideoDrawableColor);
                    binding.miniViewIcon.setImageTintList(tintColor);
                }
            }
        } else {
            if (callConfig.miniVideoConfig != null && callConfig.miniVideoConfig.miniVideoVideoDrawable != null) {
                binding.miniViewIcon.setImageDrawable(callConfig.miniVideoConfig.miniVideoVideoDrawable);
            } else {
                binding.miniViewIcon.setImageResource(com.zegocloud.uikit.R.drawable.zego_uikit_icon_online_video);
            }
        }

        if (localUser.equals(invitationData.inviter)) {
            if (isVoiceCall || invitationData.invitees.size() > 1) {
                binding.miniViewVideoParent.setVisibility(GONE);
            } else {
                binding.miniViewVideoParent.setVisibility(VISIBLE);
            }

            if (invitationData.invitees.size() == 1) {
                // 1v1
                ZegoUIKitUser uiKitUser = invitationData.invitees.get(0);
                binding.miniViewVideoLarge.setUserID(uiKitUser.userID);
            }
        } else {
            if (isVoiceCall || invitationData.invitees.size() > 1) {
                binding.miniViewVideoParent.setVisibility(GONE);
            } else {
                binding.miniViewVideoParent.setVisibility(VISIBLE);
            }
            if (invitationData.invitees.size() == 1) {
                binding.miniViewVideoLarge.setUserID(invitationData.inviter.userID);
            }
        }
    }

    public void setText(String text) {
        binding.miniViewText.setText(text);
    }

    public void updateVideo() {
        if (binding.miniViewVideoParent.getVisibility() == VISIBLE) {
            String smallUserID = binding.miniViewVideoSmall.getUserID();
            if (!TextUtils.isEmpty(smallUserID)) {
                binding.miniViewVideoSmall.setUserID(smallUserID);
            }
            String largeUserID = binding.miniViewVideoLarge.getUserID();
            if (!TextUtils.isEmpty(largeUserID)) {
                binding.miniViewVideoLarge.setUserID(largeUserID);
            }
        }
    }
}
