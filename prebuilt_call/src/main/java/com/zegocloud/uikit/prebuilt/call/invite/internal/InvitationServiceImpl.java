package com.zegocloud.uikit.prebuilt.call.invite.internal;

import android.app.Activity;
import android.app.Application;
import com.zegocloud.uikit.ZegoUIKit;
import com.zegocloud.uikit.prebuilt.call.invite.ZegoCallInvitationData;
import com.zegocloud.uikit.prebuilt.call.invite.ZegoUIKitPrebuiltCallConfigProvider;
import com.zegocloud.uikit.service.defines.ZegoInvitationListener;
import com.zegocloud.uikit.service.defines.ZegoScenario;
import com.zegocloud.uikit.service.defines.ZegoUIKitUser;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class InvitationServiceImpl {

    private Application application;

    private InvitationServiceImpl() {
    }

    private static final class Holder {

        private static final InvitationServiceImpl INSTANCE = new InvitationServiceImpl();
    }

    public static InvitationServiceImpl getInstance() {
        return InvitationServiceImpl.Holder.INSTANCE;
    }

    public String userID;
    public String userName;

    public AppActivityManager appActivityManager;
    private CallInvitationDialog invitationDialog;
    private ZegoUIKitPrebuiltCallConfigProvider provider;
    public static final int NONE_CALL_NO_REPLY = -5;
    public static final int NONE_RECEIVE_MISSED = -4;
    public static final int NONE_REJECTED = -3;
    public static final int NONE_CANCELED = -2;
    public static final int NONE_HANG_UP = -1;
    public static final int NONE = 0;
    public static final int OUTGOING = 1;
    public static final int CONNECTED = 2;
    public static final int INCOMING = 3;
    private int callState = NONE;

    private ZegoInvitationListener invitationListener = new ZegoInvitationListener() {
        @Override
        public void onInvitationReceived(ZegoUIKitUser inviter, int type, String data) {
            if (callState > 0) {
                ZegoUIKit.refuseInvitation(inviter.userID, data);
                return;
            }
            Activity topActivity = appActivityManager.getTopActivity();
            if (topActivity != null) {
                setCallState(INCOMING);
                RingtoneManager.playRingTone(topActivity);
                try {
                    JSONObject jsonObject = new JSONObject(data);
                    JSONArray invitees = jsonObject.getJSONArray("invitees");
                    List<ZegoUIKitUser> list = new ArrayList<>();
                    for (int i = 0; i < invitees.length(); i++) {
                        JSONObject invitee = (JSONObject) invitees.getJSONObject(i);
                        String user_id = invitee.getString("user_id");
                        String user_name = invitee.getString("user_name");
                        list.add(new ZegoUIKitUser(user_id, user_name));
                    }
                    ZegoCallInvitationData invitationData = new ZegoCallInvitationData();
                    invitationData.callID = jsonObject.getString("call_id");
                    invitationData.invitees = list;
                    invitationData.inviter = inviter;
                    invitationData.type = type;
                    invitationDialog = new CallInvitationDialog(topActivity, invitationData);
                    invitationDialog.show();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onInvitationTimeout(ZegoUIKitUser inviter, String data) {
            hideDialog();
            RingtoneManager.stopRingTone();
            setCallState(NONE_RECEIVE_MISSED);
        }

        @Override
        public void onInvitationResponseTimeout(List<ZegoUIKitUser> invitees, String data) {
            setCallState(NONE_CALL_NO_REPLY);
        }

        @Override
        public void onInvitationAccepted(ZegoUIKitUser invitee, String data) {
            setCallState(CONNECTED);
        }

        @Override
        public void onInvitationRefused(ZegoUIKitUser invitee, String data) {
            setCallState(NONE_REJECTED);
        }

        @Override
        public void onInvitationCanceled(ZegoUIKitUser inviter, String data) {
            setCallState(NONE_CANCELED);
            hideDialog();
            RingtoneManager.stopRingTone();
        }
    };

    private void hideDialog() {
        if (invitationDialog != null) {
            invitationDialog.hide();
        }
    }

    public void init(Application application, long appID, String appSign, String userID, String userName) {
        ZegoUIKit.init(application, appID, appSign, ZegoScenario.GENERAL);
        ZegoUIKit.login(userID, userName);

        this.userID = userID;
        this.userName = userName;
        this.application = application;

        appActivityManager = new AppActivityManager();
        this.application.registerActivityLifecycleCallbacks(appActivityManager);
        ZegoUIKit.addInvitationListener(invitationListener);
    }

    public void unInit() {
        this.userID = "";
        this.userName = "";
        this.application = null;
        ZegoUIKit.logout();
        application.unregisterActivityLifecycleCallbacks(appActivityManager);
        ZegoUIKit.removeInvitationListener(invitationListener);
    }

    public void setPrebuiltConfigProvider(ZegoUIKitPrebuiltCallConfigProvider provider) {
        this.provider = provider;
    }

    public ZegoUIKitPrebuiltCallConfigProvider getProvider() {
        return provider;
    }

    public int getCallState() {
        return callState;
    }

    public void setCallState(int callState) {
        this.callState = callState;
    }
}
