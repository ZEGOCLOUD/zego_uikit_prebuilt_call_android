package com.zegocloud.uikit.prebuilt.callinvite.internal;

import android.Manifest;
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
import com.zegocloud.uikit.components.invite.ZegoInvitationType;
import com.zegocloud.uikit.prebuilt.call.R;
import com.zegocloud.uikit.prebuilt.call.databinding.LayoutOutgoingCallBinding;
import com.zegocloud.uikit.service.defines.ZegoUIKitUser;

public class PrebuiltCallWaitingFragment extends Fragment {

    private LayoutOutgoingCallBinding binding;
    private OnBackPressedCallback onBackPressedCallback;

    public PrebuiltCallWaitingFragment() {
    }

    public static PrebuiltCallWaitingFragment newInstance(CallInvitation invitation) {
        PrebuiltCallWaitingFragment fragment = new PrebuiltCallWaitingFragment();
        Bundle args = new Bundle();
        args.putParcelable("invitation", invitation);
        fragment.setArguments(args);
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
        binding = LayoutOutgoingCallBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        hideSystemNavigationBar();
        CallInvitation invitationData = (CallInvitation) getArguments().getParcelable("invitation");
        String userID = invitationData.inviteUser.userID;
        if (invitationData.type == ZegoInvitationType.VOICE_CALL.getValue()) {
            ZegoUIKit.turnCameraOn(userID, false);
            binding.audioVideoView.setVisibility(View.GONE);
            binding.cameraSwitch.setVisibility(View.GONE);
        } else {
            ZegoUIKit.turnCameraOn(userID, true);
            binding.audioVideoView.setUserID(userID);
            binding.audioVideoView.setVisibility(View.VISIBLE);
        }

        if (invitationData.invitees.size() > 0) {
            ZegoUIKitUser zegoUIKitUser = invitationData.invitees.get(0);
            binding.callUserIcon.setText(zegoUIKitUser.userName, false);
            binding.callUserName.setText(zegoUIKitUser.userName);
        }
        binding.getRoot().setBackgroundResource(R.drawable.img_bg);
        binding.callWaitingCancel.setOnClickListener(v -> {
            InvitationServiceImpl.getInstance().setCallState(InvitationServiceImpl.NONE_CANCELED);
            requireActivity().finish();
        });
        binding.getRoot().post(()->{
            boolean permissionGranted =
                PermissionX.isGranted(getContext(), permission.CAMERA)
                    && PermissionX.isGranted(getContext(), permission.RECORD_AUDIO);
            if (!permissionGranted) {
                PermissionX.init(requireActivity())
                    .permissions(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
                    .onExplainRequestReason((scope, deniedList) -> {
                        scope.showRequestReasonDialog(deniedList, "We require camera&microphone access to connect a call",
                            "OK", "Cancel");
                    })
                    .request((allGranted, grantedList, deniedList) -> {
                        if (allGranted) {
                            if (invitationData.type == ZegoInvitationType.VIDEO_CALL.getValue()) {
                                ZegoUIKit.turnCameraOn(userID, false);
                                ZegoUIKit.turnCameraOn(userID, true);
                            }
                        }
                    });
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (InvitationServiceImpl.getInstance().getCallState() > 0) {
            InvitationServiceImpl.getInstance().setCallState(
                InvitationServiceImpl.NONE);
        }
    }

    private void hideSystemNavigationBar() {
        int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
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

                if (sizeTwo >= sizeOne && !(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
                    !key.equals("status_bar_height"))) {
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