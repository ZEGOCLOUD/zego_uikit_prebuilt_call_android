package com.zegocloud.uikit.prebuilt.call.invite.internal;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import com.zegocloud.uikit.ZegoUIKit;
import com.zegocloud.uikit.prebuilt.call.R;
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallConfig;
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallFragment;
import com.zegocloud.uikit.prebuilt.call.invite.ZegoCallInvitationData;
import com.zegocloud.uikit.service.defines.ZegoInvitationListener;
import com.zegocloud.uikit.service.defines.ZegoUIKitUser;
import java.util.List;

public class CallInviteActivity extends AppCompatActivity {

    private CallInvitation zegoInvitation;

    public static void startActivity(Context context, CallInvitation invitation, boolean waitingPage) {
        Log.d(Constants.TAG, "startActivity() called with: context = [" + context + "], invitation = [" + invitation
            + "], waitingPage = [" + waitingPage + "]");
        Intent intent = new Intent(context, CallInviteActivity.class);
        intent.putExtra("invitation", invitation);
        intent.putExtra("waitingPage", waitingPage);
        context.startActivity(intent);
    }

    private ZegoInvitationListener invitationListener = new ZegoInvitationListener() {
        @Override
        public void onInvitationReceived(ZegoUIKitUser inviter, int type, String data) {

        }

        @Override
        public void onInvitationTimeout(ZegoUIKitUser inviter, String data) {
            finish();
        }

        @Override
        public void onInvitationResponseTimeout(List<ZegoUIKitUser> invitees, String data) {
            finish();
        }

        @Override
        public void onInvitationAccepted(ZegoUIKitUser invitee, String data) {
            showCallFragment();
        }

        @Override
        public void onInvitationRefused(ZegoUIKitUser invitee, String data) {
            finish();
        }

        @Override
        public void onInvitationCanceled(ZegoUIKitUser inviter, String data) {
            finish();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prebuilt_call);

        zegoInvitation = (CallInvitation) getIntent().getParcelableExtra("invitation");
        boolean waitingPage = getIntent().getBooleanExtra("waitingPage", true);
        if (waitingPage) {
            showWaitingFragment();
        } else {
            showCallFragment();
        }
    }

    private void showCallFragment() {
        ZegoCallInvitationData invitationData = CallInvitation.convertToZegoCallInvitationData(zegoInvitation);
        ZegoUIKitPrebuiltCallConfig config;
        InvitationServiceImpl service = InvitationServiceImpl.getInstance();
        if (service.getProvider() != null) {
            config = service.getProvider().requireConfig(invitationData);
        } else {
            config = new ZegoUIKitPrebuiltCallConfig();
        }
        ZegoUIKitPrebuiltCallFragment fragment = ZegoUIKitPrebuiltCallFragment.newInstance(invitationData,
            config);
        getSupportFragmentManager().beginTransaction()
            .replace(R.id.call_fragment_container, fragment)
            .commitNow();
    }

    private void showWaitingFragment() {
        CallWaitingFragment fragment = CallWaitingFragment.newInstance(zegoInvitation);
        getSupportFragmentManager().beginTransaction()
            .replace(R.id.call_fragment_container, fragment)
            .commitNow();
        ZegoUIKit.addInvitationListener(invitationListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ZegoUIKit.removeInvitationListener(invitationListener);
    }
}