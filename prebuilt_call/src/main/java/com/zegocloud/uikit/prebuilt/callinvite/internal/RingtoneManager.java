package com.zegocloud.uikit.prebuilt.callinvite.internal;

import android.app.Service;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Vibrator;

public class RingtoneManager {

    private static MediaPlayer mediaPlayer;
    private static Vibrator vibrator;
    private static boolean isPlaying;

    public static void playRingTone(Context context) {
        if (isPlaying) {
            return;
        }
        isPlaying = true;
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (vibrator == null) {
            vibrator = (Vibrator) context.getSystemService(Service.VIBRATOR_SERVICE);
        }
        if (audioManager.getRingerMode() == AudioManager.RINGER_MODE_NORMAL) {
            Uri ringtoneUri = android.media.RingtoneManager
                .getActualDefaultRingtoneUri(context, android.media.RingtoneManager.TYPE_RINGTONE);
            if (ringtoneUri != null && mediaPlayer == null) {
                mediaPlayer = MediaPlayer.create(context, ringtoneUri);
                mediaPlayer.setLooping(true);
                mediaPlayer.start();
                vibrateDevice();
            }
        } else if (audioManager.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE) {
            vibrateDevice();
        } else {

        }
    }

    private static void vibrateDevice() {
        if (vibrator != null && vibrator.hasVibrator()) {
            vibrator.cancel();
            vibrator.vibrate(new long[]{600, 600, 600, 600}, 0);
        }
    }

    public static void stopRingTone() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        if (vibrator != null && vibrator.hasVibrator()) {
            vibrator.cancel();
        }
        isPlaying = false;
    }
}
