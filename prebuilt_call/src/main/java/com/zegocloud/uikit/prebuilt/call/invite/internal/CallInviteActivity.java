package com.zegocloud.uikit.prebuilt.call.invite.internal;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.zegocloud.uikit.plugin.invitation.ZegoInvitationType;
import com.zegocloud.uikit.prebuilt.call.R;
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallConfig;
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallFragment;
import com.zegocloud.uikit.prebuilt.call.invite.ZegoCallInvitationData;
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
        setContentView(R.layout.activity_prebuilt_call);

        callStateListener = new CallStateListener() {
            @Override
            public void onStateChanged(int before, int after) {
                if (after == CallInvitationServiceImpl.CONNECTED) {
                    showCallFragment();
                } else {
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
            showWaitingFragment();
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

    private void showWaitingFragment() {
        Bundle bundle = getIntent().getParcelableExtra("bundle");
        CallWaitingFragment fragment = CallWaitingFragment.newInstance(bundle);
        getSupportFragmentManager().beginTransaction().replace(R.id.call_fragment_container, fragment).commitNow();
    }
}