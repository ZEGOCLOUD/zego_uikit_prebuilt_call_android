package com.zegocloud.uikit.prebuilt.call.invite.internal;

import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Vibrator;
import android.util.Log;

public class RingtoneManager {

    private static AudioManager audioManager;
    private static Vibrator vibrator;
    private static Uri outgoingUri;
    private static Uri incomingUri;
    private static Context context;
    private static MediaPlayer mediaPlayer;

    public static Uri getUriFromRaw(Context context, String mp3Name) {
        return Uri.parse(
            ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + context.getPackageName() + "/raw/" + mp3Name);
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
            //            ringtone = android.media.RingtoneManager.getRingtone(context, uri);
            //            ringtone.setLooping(true);
            //            ringtone.play();
            if (uri != null && mediaPlayer == null) {
                mediaPlayer = MediaPlayer.create(context, uri);
                mediaPlayer.setLooping(true);
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
            vibrator.vibrate(new long[]{800, 800, 800, 800}, 0);
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
