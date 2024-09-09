package com.zegocloud.uikit.prebuilt.call.core.basic;

import android.os.Handler;
import android.os.Looper;
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallConfig;
import com.zegocloud.uikit.prebuilt.call.config.DurationUpdateListener;
import com.zegocloud.uikit.prebuilt.call.core.CallInvitationServiceImpl;

public class PrebuiltCallTimer {

    private DurationUpdateListener updateListener;
    private Handler handler = new Handler(Looper.getMainLooper());
    private long elapsedTime;
    private long startTimeLocal;
    private Runnable checkTimeRunnable = new Runnable() {
        @Override
        public void run() {
            elapsedTime = System.currentTimeMillis() - startTimeLocal;

            ZegoUIKitPrebuiltCallConfig callConfig = CallInvitationServiceImpl.getInstance().getCallConfig();
            if (callConfig != null && callConfig.durationConfig != null
                && callConfig.durationConfig.durationUpdateListener != null) {
                callConfig.durationConfig.durationUpdateListener.onDurationUpdate(elapsedTime / 1000);
            }
            if (updateListener != null) {
                updateListener.onDurationUpdate(elapsedTime / 1000);
            }
            handler.postDelayed(checkTimeRunnable, 1000);
        }
    };

    public void startRoomTimeCount() {
        startTimeLocal = System.currentTimeMillis();
        handler.post(checkTimeRunnable);
    }

    public void stopRoomTimeCount() {
        handler.removeCallbacks(checkTimeRunnable);
    }

    public void setDurationUpdateListener(DurationUpdateListener updateListener) {
        this.updateListener = updateListener;
    }

    public long getStartTimeLocal() {
        return startTimeLocal;
    }

    public void clear() {
        setDurationUpdateListener(null);
        elapsedTime = 0;
        startTimeLocal = 0;
    }
}
