package com.zegocloud.uikit.prebuilt.call.invite.internal;

import android.Manifest.permission;
import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.permissionx.guolindev.PermissionX;
import com.zegocloud.uikit.ZegoUIKit;
import com.zegocloud.uikit.plugin.invitation.ZegoInvitationType;
import com.zegocloud.uikit.prebuilt.call.R;
import com.zegocloud.uikit.prebuilt.call.databinding.LayoutWaitingCallBinding;
import com.zegocloud.uikit.service.defines.ZegoUIKitUser;
import com.zegocloud.uikit.utils.GenericUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CallWaitingFragment extends Fragment {

    private LayoutWaitingCallBinding binding;
    private OnBackPressedCallback onBackPressedCallback;

    public CallWaitingFragment() {
    }

    public static CallWaitingFragment newInstance(Bundle bundle) {
        CallWaitingFragment fragment = new CallWaitingFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        onBackPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                //                ZegoUIKit.leaveRoom();
                //                setEnabled(false);
                //                requireActivity().onBackPressed();
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(onBackPressedCallback);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = LayoutWaitingCallBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        hideSystemNavigationBar();
        int type = getArguments().getInt("type");
        String page = getArguments().getString("page");
        ZegoUIKitUser inviter = getArguments().getParcelable("inviter");
        String userID = ZegoUIKit.getLocalUser().userID;
        ArrayList<ZegoUIKitUser> invitees = getArguments().getParcelableArrayList("invitees");

        ZegoUIKitUser showUser = null;
        if ("incoming".equals(page)) {
            showUser = inviter;
            binding.audioVideoView.setVisibility(View.GONE);
            binding.cameraSwitch.setVisibility(View.GONE);
            binding.callWaitingCancel.setVisibility(View.GONE);
            binding.callWaitingRefuse.setVisibility(View.VISIBLE);
            binding.callWaitingAcceptText.setVisibility(View.VISIBLE);
            binding.callWaitingRefuseText.setVisibility(View.VISIBLE);
            binding.callWaitingAccept.setVisibility(View.VISIBLE);
        } else if ("outgoing".equals(page)) {
            if (invitees.size() > 0) {
                showUser = invitees.get(0);
            }
            if (type == ZegoInvitationType.VOICE_CALL.getValue()) {
                binding.audioVideoView.setVisibility(View.GONE);
                binding.cameraSwitch.setVisibility(View.GONE);
            } else {
                binding.cameraSwitch.setVisibility(View.VISIBLE);
                binding.audioVideoView.setVisibility(View.VISIBLE);
                ZegoUIKit.turnCameraOn(userID, true);
                binding.audioVideoView.setUserID(userID);
            }
            binding.callWaitingCancel.setVisibility(View.VISIBLE);
            binding.callWaitingRefuse.setVisibility(View.GONE);
            binding.callWaitingAcceptText.setVisibility(View.GONE);
            binding.callWaitingRefuseText.setVisibility(View.GONE);
            binding.callWaitingAccept.setVisibility(View.GONE);
        } else {
            requireActivity().finish();
        }
        if (showUser != null) {
            binding.callUserIcon.setText(showUser.userName, false);
            binding.callUserName.setText(showUser.userName);
        }
        if (type == ZegoInvitationType.VOICE_CALL.getValue()) {
            binding.callWaitingAccept.setBackgroundResource(R.drawable.selector_dialog_voice_accept);
        } else {
            binding.callWaitingAccept.setBackgroundResource(R.drawable.selector_dialog_video_accept);
        }
        binding.getRoot().setBackgroundResource(R.drawable.img_bg);
        binding.callWaitingAccept.setInviterID(inviter.userID);
        binding.callWaitingRefuse.setInviterID(inviter.userID);
        binding.callWaitingCancel.setInvitees(GenericUtils.map(invitees, uiKitUser -> uiKitUser.userID));
        binding.callWaitingCancel.setOnClickListener(v -> {
            requireActivity().finish();
        });
        binding.callWaitingRefuse.setOnClickListener(v -> {
            requireActivity().finish();
        });
        binding.callWaitingAccept.setOnClickListener(v -> {
            CallInvitationServiceImpl.getInstance().setCallState(CallInvitationServiceImpl.CONNECTED);
        });

        binding.getRoot().post(() -> {
            requestPermissionIfNeeded(userID, type);
        });
    }

    private void requestPermissionIfNeeded(String userID, int type) {
        List<String> permissions = Arrays.asList(permission.CAMERA, permission.RECORD_AUDIO);
        boolean permissionGranted = true;
        for (String permission : permissions) {
            if (!PermissionX.isGranted(getContext(), permission)) {
                permissionGranted = false;
            }
            break;
        }
        if (!permissionGranted) {
            PermissionX.init(requireActivity()).permissions(permissions).onExplainRequestReason(
                    (scope, deniedList) -> scope.showRequestReasonDialog(deniedList, getString(R.string.permission_explain),
                        getString(R.string.ok))).onForwardToSettings(
                    (scope, deniedList) -> scope.showForwardToSettingsDialog(deniedList,
                        getString(R.string.permission_explain), getString(R.string.ok), getString(R.string.cancel)))
                .request((allGranted, grantedList, deniedList) -> {
                    if (allGranted) {
                        if (type == ZegoInvitationType.VIDEO_CALL.getValue()) {
                            ZegoUIKit.turnCameraOn(userID, false);
                            ZegoUIKit.turnCameraOn(userID, true);
                        }
                    }
                });
        }
    }

    private void hideSystemNavigationBar() {
        int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        binding.getRoot().setSystemUiVisibility(uiOptions);

        int statusBarHeight = getInternalDimensionSize(getContext(), "status_bar_height");
        binding.getRoot().setPadding(0, statusBarHeight, 0, 0);
    }


    static int getInternalDimensionSize(Context context, String key) {
        int result = 0;
        try {
            int resourceId = Resources.getSystem().getIdentifier(key, "dimen", "android");
            if (resourceId > 0) {
                int sizeOne = context.getResources().getDimensionPixelSize(resourceId);
                int sizeTwo = Resources.getSystem().getDimensionPixelSize(resourceId);

                if (sizeTwo >= sizeOne && !(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && !key.equals(
                    "status_bar_height"))) {
                    return sizeTwo;
                } else {
                    float densityOne = context.getResources().getDisplayMetrics().density;
                    float densityTwo = Resources.getSystem().getDisplayMetrics().density;
                    float f = sizeOne * densityTwo / densityOne;
                    return (int) ((f >= 0) ? (f + 0.5f) : (f - 0.5f));
                }
            }
        } catch (Resources.NotFoundException ignored) {
            return 0;
        }
        return result;
    }


}