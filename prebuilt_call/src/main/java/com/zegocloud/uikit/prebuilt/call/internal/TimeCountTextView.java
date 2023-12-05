package com.zegocloud.uikit.prebuilt.call.internal;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import com.zegocloud.uikit.prebuilt.call.config.DurationUpdateListener;
import java.util.Locale;

public class TimeCountTextView extends AppCompatTextView {

    private long elapsedTime;
    private long startTimeLocal;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable checkTimeRunnable = new Runnable() {
        @Override
        public void run() {
            elapsedTime = System.currentTimeMillis() - startTimeLocal;
            updateText();
            if (updateListener != null) {
                updateListener.onDurationUpdate(elapsedTime / 1000);
            }
            handler.postDelayed(checkTimeRunnable, 1000);
        }
    };
    private DurationUpdateListener updateListener;

    public TimeCountTextView(@NonNull Context context) {
        super(context);
        initView();
    }

    public TimeCountTextView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public TimeCountTextView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        startTimeLocal = System.currentTimeMillis();
    }

    public void setElapsedTime(long elapsedTime) {
        this.elapsedTime = elapsedTime;
        startTimeLocal = System.currentTimeMillis() - this.elapsedTime;
    }

    public void startTimeCount() {
        handler.post(checkTimeRunnable);
    }

    public void stopTimeCount() {
        handler.removeCallbacks(checkTimeRunnable);
    }

    public void pauseTimeCount() {
        handler.removeCallbacks(checkTimeRunnable);
    }

    public void resumeTimeCount() {
        setElapsedTime(elapsedTime);
        startTimeCount();
    }

    public void resetTimeCount() {
        setElapsedTime(0);
    }

    private void updateText() {
        setText(getElapsedTimeString());
    }

    public void setUpdateListener(DurationUpdateListener updateListener) {
        this.updateListener = updateListener;
    }

    private String getElapsedTimeString() {
        String time;
        if (elapsedTime >= 60 * 60 * 1000) {
            int hour = (int) (elapsedTime / (60 * 60 * 1000));
            int minutes = (int) ((elapsedTime - hour * (60 * 60 * 1000)) / (60 * 1000));
            int seconds = (int) ((elapsedTime - hour * (60 * 60 * 1000) - minutes * (60 * 1000)) / 1000);
            time = String.format(Locale.getDefault(), "%d:%02d:%02d", hour, minutes, seconds);
        } else {
            int minutes = (int) (elapsedTime / (60 * 1000));
            int seconds = (int) ((elapsedTime - minutes * (60 * 1000)) / 1000);
            time = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        }
        return time;
    }
}
