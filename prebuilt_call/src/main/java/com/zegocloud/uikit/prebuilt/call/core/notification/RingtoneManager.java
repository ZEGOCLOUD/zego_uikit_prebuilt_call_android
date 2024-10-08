package com.zegocloud.uikit.prebuilt.call.core.notification;

import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import java.io.IOException;

public class RingtoneManager {

    private static AudioManager audioManager;
    private static Vibrator vibrator;
    private static Uri outgoingUri;
    private static Uri incomingUri;
    private static Context context;
    private static MediaPlayer mediaPlayer;

    // not work in mate20 HK version
    public static Uri getUriFromRaw(Context context, String mp3Name) {
        return Uri.parse(
            ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + context.getPackageName() + "/raw/" + mp3Name);
    }

    public static Uri getUriFromID(Context context, int resource_id) {
        return Uri.parse(
            ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + context.getPackageName() + "/" + resource_id);
    }

    public static void init(Context context) {
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        vibrator = (Vibrator) context.getSystemService(Service.VIBRATOR_SERVICE);
        RingtoneManager.context = context.getApplicationContext();
    }

    public static void setOutgoingUri(Uri outgoingUri) {
        RingtoneManager.outgoingUri = outgoingUri;
    }

    public static void setIncomingUri(Uri incomingUri) {
        RingtoneManager.incomingUri = incomingUri;
    }

    public static Uri getOutgoingUri() {
        return outgoingUri;
    }

    public static Uri getIncomingUri() {
        return incomingUri;
    }

    public static void playRingTone(boolean incoming) {
        Uri uri = incoming ? incomingUri : outgoingUri;
        if (audioManager.getRingerMode() == AudioManager.RINGER_MODE_NORMAL) {
            if (incoming) {
                if (uri == null) {
                    uri = android.media.RingtoneManager.getActualDefaultRingtoneUri(context,
                        android.media.RingtoneManager.TYPE_RINGTONE);
                }
            }
            if (uri != null && mediaPlayer == null) {
                mediaPlayer = new MediaPlayer();
                AudioAttributes attribution = new AudioAttributes.Builder().setContentType(
                        AudioAttributes.CONTENT_TYPE_SONIFICATION).setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                    .setLegacyStreamType(AudioManager.STREAM_RING).build();
                mediaPlayer.setAudioAttributes(attribution);
                mediaPlayer.setLooping(true);
                try {
                    mediaPlayer.setDataSource(context, uri);
                    mediaPlayer.prepare();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                mediaPlayer.start();
            }
            if (incoming) {
                vibrateDevice();
            }
        } else if (audioManager.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE) {
            if (incoming) {
                vibrateDevice();
            }
        } else {

        }
    }

    private static void vibrateDevice() {
        if (vibrator != null && vibrator.hasVibrator()) {
            vibrator.cancel();
            long[] vibrationPattern = {0, 1000, 2000, 1000};
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createWaveform(vibrationPattern, 0));
            } else {
                vibrator.vibrate(vibrationPattern, 0);
            }
        }
    }

    public static void stopRingTone() {
        //        if (ringtone != null && ringtone.isPlaying()) {
        //            ringtone.stop();
        //            ringtone = null;
        //        }
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        if (vibrator != null && vibrator.hasVibrator()) {
            vibrator.cancel();
        }
    }
}
