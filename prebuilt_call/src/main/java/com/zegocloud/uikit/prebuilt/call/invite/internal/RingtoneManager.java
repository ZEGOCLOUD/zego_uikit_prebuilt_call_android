package com.zegocloud.uikit.prebuilt.call.invite.internal;

import android.annotation.TargetApi;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Vibrator;
import android.text.TextUtils;
import com.zegocloud.uikit.prebuilt.call.config.ZegoNotificationConfig;

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

    @TargetApi(Build.VERSION_CODES.O)
    public static void setIncomingOfflineRing() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            ZegoNotificationConfig androidNotificationConfig = CallInvitationServiceImpl.getInstance().getConfig().notificationConfig;
            if (androidNotificationConfig != null) {
                String channelID = androidNotificationConfig.channelID;
                String channelName = TextUtils.isEmpty(androidNotificationConfig.channelName) ? androidNotificationConfig.channelID : androidNotificationConfig.channelName;
                String soundName = getSoundName(androidNotificationConfig.sound);
                NotificationChannel channel = new NotificationChannel(channelID, channelName, NotificationManager.IMPORTANCE_HIGH);
                Uri sound = RingtoneManager.getUriFromRaw(RingtoneManager.context, soundName);
                channel.setSound(sound, null);
                NotificationManager notificationManager = RingtoneManager.context.getSystemService(NotificationManager.class);
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    public static String getSoundName(String sound) {
        if (TextUtils.isEmpty(sound)) {
            return "CallInvitation";
        }
        String[] splits = sound.split("\\.");
        String suffixStr = "";
        if (splits != null && splits.length > 1) {
            suffixStr = sound.substring(0, sound.length() - (splits[splits.length - 1].length() + 1));
        } else {
            suffixStr = sound;
        }
        return suffixStr;
    }

}
