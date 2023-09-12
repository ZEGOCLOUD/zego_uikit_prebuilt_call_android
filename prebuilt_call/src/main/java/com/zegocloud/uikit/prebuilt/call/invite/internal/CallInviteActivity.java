package com.zegocloud.uikit.prebuilt.call.invite.internal;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.zegocloud.uikit.ZegoUIKit;
import com.zegocloud.uikit.plugin.invitation.ZegoInvitationType;
import com.zegocloud.uikit.prebuilt.call.R;
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallConfig;
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallFragment;
import com.zegocloud.uikit.prebuilt.call.config.ZegoMenuBarButtonName;
import com.zegocloud.uikit.prebuilt.call.invite.ZegoCallInvitationData;
import com.zegocloud.uikit.prebuilt.call.invite.ZegoUIKitPrebuiltCallInvitationConfig;
import com.zegocloud.uikit.service.defines.ZegoOnlySelfInRoomListener;
import com.zegocloud.uikit.service.defines.ZegoUIKitUser;
import java.util.ArrayList;
import java.util.List;

/**
 * show waiting page and call page for call-invite
 */
public class CallInviteActivity extends AppCompatActivity {

    private CallStateListener callStateListener;

    public static void startOutgoingPage(Context context, ZegoUIKitUser inviter, List<ZegoUIKitUser> invitees,
        String callID, int type, int timeout) {
        Intent intent = new Intent(context, CallInviteActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("page", "outgoing");
        bundle.putParcelable("inviter", inviter);
        bundle.putParcelableArrayList("invitees", new ArrayList<>(invitees));
        bundle.putInt("type", type);
        bundle.putString("callID", callID);
        bundle.putInt("timeout", timeout);
        intent.putExtra("bundle", bundle);
        context.startActivity(intent);
    }

    public static void startIncomingPage(Context context, ZegoUIKitUser inviter, List<ZegoUIKitUser> invitees,
        String callID, int type) {
        Intent intent = new Intent(context, CallInviteActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("page", "incoming");
        bundle.putParcelable("inviter", inviter);
        bundle.putParcelableArrayList("invitees", new ArrayList<>(invitees));
        bundle.putInt("type", type);
        bundle.putString("callID", callID);
        intent.putExtra("bundle", bundle);
        context.startActivity(intent);
    }


    public static void startCallPage(Context context, ZegoUIKitUser inviter, List<ZegoUIKitUser> invitees,
        String callID, int type) {
        Intent intent = new Intent(context, CallInviteActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("page", "call");
        bundle.putParcelable("inviter", inviter);
        bundle.putParcelableArrayList("invitees", new ArrayList<>(invitees));
        bundle.putInt("type", type);
        bundle.putString("callID", callID);
        intent.putExtra("bundle", bundle);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.call_activity_prebuilt);

        callStateListener = new CallStateListener() {
            @Override
            public void onStateChanged(int before, int after) {
                if (after == CallInvitationServiceImpl.CONNECTED) {
                    showCallFragment();
                } else {
                    String userID = ZegoUIKit.getLocalUser().userID;
                    if (!TextUtils.isEmpty(userID)) {
                        if (ZegoUIKit.isCameraOn(userID)) {
                            ZegoUIKit.turnCameraOn(userID, false);
                        }
                    }
                    CallInvitationServiceImpl.getInstance().removeCallStateListener(callStateListener);
                    finish();
                }
            }
        };
        CallInvitationServiceImpl.getInstance().addCallStateListener(callStateListener);

        Bundle bundle = getIntent().getParcelableExtra("bundle");
        String page = bundle.getString("page");
        ArrayList<ZegoUIKitUser> invitees = bundle.getParcelableArrayList("invitees");

        boolean isOneOnOneCall = "outgoing".equals(page) && invitees.size() == 1;
        boolean isIncoming = "incoming".equals(page);
        if (isIncoming || isOneOnOneCall) {
            showWaitingFragment(isIncoming);
        } else {
            showCallFragment();
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        CallInvitationServiceImpl.getInstance().removeCallStateListener(callStateListener);
    }

    private void showCallFragment() {
        Fragment fragmentById = getSupportFragmentManager().findFragmentById(R.id.call_fragment_container);
        if (fragmentById instanceof ZegoUIKitPrebuiltCallFragment) {
            return;
        }
        Bundle bundle = getIntent().getParcelableExtra("bundle");
        ArrayList<ZegoUIKitUser> invitees = bundle.getParcelableArrayList("invitees");
        ZegoCallInvitationData invitationData = new ZegoCallInvitationData(bundle.getString("callID"),
            bundle.getInt("type"), invitees, bundle.getParcelable("inviter"), bundle.getString("custom_data"));
        ZegoUIKitPrebuiltCallConfig config = getPrebuiltCallConfig(invitationData);

        if (config.bottomMenuBarConfig.buttons.contains(ZegoMenuBarButtonName.BEAUTY_BUTTON)) {
            ZegoUIKit.getBeautyPlugin().setZegoBeautyPluginConfig(config.beautyConfig);
            CallInvitationServiceImpl.getInstance().initBeautyPlugin();
        }

        ZegoUIKitPrebuiltCallFragment fragment = ZegoUIKitPrebuiltCallFragment.newInstance(invitationData, config);
        if (invitees.size() > 1) {
            fragment.setOnOnlySelfInRoomListener(new ZegoOnlySelfInRoomListener() {
                @Override
                public void onOnlySelfInRoom() {

                }
            });
        }
        getSupportFragmentManager().beginTransaction().replace(R.id.call_fragment_container, fragment).commitNow();
    }

    @NonNull
    private ZegoUIKitPrebuiltCallConfig getPrebuiltCallConfig(ZegoCallInvitationData invitationData) {
        ZegoUIKitPrebuiltCallConfig config = null;
        CallInvitationServiceImpl service = CallInvitationServiceImpl.getInstance();
        if (service.getProvider() != null) {
            config = service.getProvider().requireConfig(invitationData);
        }
        if (config == null) {
            boolean isVideoCall = invitationData.type == ZegoInvitationType.VIDEO_CALL.getValue();
            boolean isGroupCall = invitationData.invitees.size() > 1;
            if (isVideoCall && isGroupCall) {
                config = ZegoUIKitPrebuiltCallConfig.groupVideoCall();
            } else if (!isVideoCall && isGroupCall) {
                config = ZegoUIKitPrebuiltCallConfig.groupVoiceCall();
            } else if (!isVideoCall) {
                config = ZegoUIKitPrebuiltCallConfig.oneOnOneVoiceCall();
            } else {
                config = ZegoUIKitPrebuiltCallConfig.oneOnOneVideoCall();
            }
        }
        return config;
    }

    private void showWaitingFragment(boolean isIncoming) {
        Bundle bundle = getIntent().getParcelableExtra("bundle");
        CallWaitingFragment fragment = CallWaitingFragment.newInstance(bundle);
        ArrayList<ZegoUIKitUser> invitees = bundle.getParcelableArrayList("invitees");
        ZegoCallInvitationData invitationData = new ZegoCallInvitationData(bundle.getString("callID"),
            bundle.getInt("type"), invitees, bundle.getParcelable("inviter"), bundle.getString("custom_data"));
        CallInvitationServiceImpl service = CallInvitationServiceImpl.getInstance();
        if (service.getProvider() != null) {
            ZegoUIKitPrebuiltCallConfig prebuiltCallConfig = service.getProvider().requireConfig(invitationData);
            if (prebuiltCallConfig.audioVideoViewConfig != null
                && prebuiltCallConfig.avatarViewProvider != null) {
                fragment.setAvatarViewProvider(prebuiltCallConfig.avatarViewProvider);
            }
        }
        ZegoUIKitPrebuiltCallInvitationConfig invitationConfig = CallInvitationServiceImpl.getInstance().getConfig();
        if (isIncoming) {
            if (invitationConfig.incomingCallBackground != null) {
                fragment.setBackground(invitationConfig.incomingCallBackground);
            }
        } else {
            if (invitationConfig.outgoingCallBackground != null) {
                fragment.setBackground(invitationConfig.outgoingCallBackground);
            }
        }
        getSupportFragmentManager().beginTransaction().replace(R.id.call_fragment_container, fragment).commitNow();
    }
}