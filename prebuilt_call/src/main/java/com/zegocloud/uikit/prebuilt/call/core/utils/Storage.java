package com.zegocloud.uikit.prebuilt.call.core.utils;

import android.content.Context;
import android.content.SharedPreferences;
import com.tencent.mmkv.MMKV;
import timber.log.Timber;

public class Storage {

    private static final String PREBUILT_CALL = "prebuilt_call";
    public static Context context;

    public static boolean set_appID(long appID) {
        return putLong("appID", appID);
    }

    public static long appID() {
        return getLong("appID", 0);
    }

    public static boolean set_appSign(String appSign) {
        return putString("appSign", appSign);
    }

    public static String appSign() {
        return getString("appSign", "");
    }


    public static boolean set_channelID(String channelID) {
        return putString("channelID", channelID);
    }

    public static String channelID() {
        return getString("channelID", "");
    }

    public static boolean set_ringtone(String ringtone) {
        return putString("ringtone", ringtone);
    }

    public static String ringtone() {
        return getString("ringtone", "");
    }

    public static boolean set_appToken(String appToken) {
        return putString("appToken", appToken);
    }

    public static String appToken() {
        return getString("appToken", "");
    }

    public static boolean set_userID(String userID) {
        return putString("userID", userID);
    }

    public static String userID() {
        return getString("userID", "");
    }

    public static boolean set_userName(String userName) {
        return putString("userName", userName);
    }

    public static String userName() {
        return getString("userName", "");
    }

    public static boolean putLong(String key, long value) {
        Timber.d("putLong() called with: key = [" + key + "], value = [" + value + "]");
        //        SharedPreferences sharedPreferences = context.getSharedPreferences(PREBUILT_CALL, Context.MODE_PRIVATE);
        //        return sharedPreferences.edit().putLong(key, value).commit();
        return MMKV.mmkvWithID(PREBUILT_CALL).encode(key, value);
    }

    public static long getLong(String key, long defValue) {
        //        SharedPreferences sharedPreferences = context.getSharedPreferences("prebuilt_call", Context.MODE_PRIVATE);
        //        return sharedPreferences.getLong(key, defValue);

        return MMKV.mmkvWithID(PREBUILT_CALL).decodeLong(key, defValue);
    }

    private static int getInt(String key, int defValue) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("prebuilt_call", Context.MODE_PRIVATE);
        return sharedPreferences.getInt(key, defValue);
    }

    private static boolean putInt(String key, int value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("prebuilt_call", Context.MODE_PRIVATE);
        return sharedPreferences.edit().putInt(key, value).commit();
    }

    private static boolean getBool(String key, boolean defValue) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("prebuilt_call", Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(key, defValue);
    }

    private static boolean putBoolean(String key, boolean value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("prebuilt_call", Context.MODE_PRIVATE);
        return sharedPreferences.edit().putBoolean(key, value).commit();
    }

    private static boolean putString(String key, String value) {
        Timber.d("putString() called with: key = [" + key + "], value = [" + value + "]");
        //        SharedPreferences sharedPreferences = context.getSharedPreferences("prebuilt_call", Context.MODE_PRIVATE);
        //        return sharedPreferences.edit().putString(key, value).commit();
        return MMKV.mmkvWithID(PREBUILT_CALL).encode(key, value);
    }

    private static String getString(String key, String defValue) {
        //        SharedPreferences sharedPreferences = context.getSharedPreferences("prebuilt_call", Context.MODE_PRIVATE);
        //        return sharedPreferences.getString(key, defValue);
        return MMKV.mmkvWithID(PREBUILT_CALL).decodeString(key, defValue);
    }

    public static void remove(String key) {
        //        SharedPreferences sharedPreferences = context.getSharedPreferences("prebuilt_call", Context.MODE_PRIVATE);
        //         sharedPreferences.edit().remove(key).commit();
        MMKV.mmkvWithID(PREBUILT_CALL).remove(key);
    }

    public static boolean contains(String key) {
        //        SharedPreferences sharedPreferences = context.getSharedPreferences("prebuilt_call", Context.MODE_PRIVATE);
        //        return sharedPreferences.contains(key);
        return MMKV.mmkvWithID(PREBUILT_CALL).contains(key);
    }
}
