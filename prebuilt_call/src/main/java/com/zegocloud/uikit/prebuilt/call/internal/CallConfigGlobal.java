package com.zegocloud.uikit.prebuilt.call.internal;

import com.zegocloud.uikit.components.audiovideo.ZegoForegroundViewProvider;
import com.zegocloud.uikit.components.common.ZegoMemberListItemViewProvider;
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallConfig;
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallFragment.LeaveCallListener;

public class CallConfigGlobal {

    private static CallConfigGlobal sInstance;

    private CallConfigGlobal() {
    }

    public static CallConfigGlobal getInstance() {
        synchronized (CallConfigGlobal.class) {
            if (sInstance == null) {
                sInstance = new CallConfigGlobal();
            }
            return sInstance;
        }
    }

    private LeaveCallListener leaveCallListener;
    private ZegoMemberListItemViewProvider memberListItemProvider;
    private ZegoForegroundViewProvider videoViewForegroundViewProvider;
    private ZegoUIKitPrebuiltCallConfig config;

    public LeaveCallListener getLeaveCallListener() {
        return leaveCallListener;
    }

    public void setLeaveCallListener(LeaveCallListener leaveCallListener) {
        this.leaveCallListener = leaveCallListener;
    }

    public ZegoMemberListItemViewProvider getMemberListItemProvider() {
        return memberListItemProvider;
    }

    public void setMemberListItemProvider(ZegoMemberListItemViewProvider memberListItemProvider) {
        this.memberListItemProvider = memberListItemProvider;
    }

    public ZegoForegroundViewProvider getVideoViewForegroundViewProvider() {
        return videoViewForegroundViewProvider;
    }

    public void setVideoViewForegroundViewProvider(ZegoForegroundViewProvider provider) {
        this.videoViewForegroundViewProvider = provider;
    }

    public ZegoUIKitPrebuiltCallConfig getConfig() {
        return config;
    }

    public void setConfig(ZegoUIKitPrebuiltCallConfig config) {
        this.config = config;
    }
}