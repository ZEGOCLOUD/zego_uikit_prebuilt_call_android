package com.zegocloud.uikit.prebuilt.call.core;

import android.text.TextUtils;
import androidx.annotation.NonNull;
import org.json.JSONException;
import org.json.JSONObject;

public class JsonUtil {

    @NonNull
    public static JSONObject getJsonObjectFromString(String s) {
        if (TextUtils.isEmpty(s)) {
            return new JSONObject();
        }
        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(s);
        } catch (JSONException e) {
            jsonObject = new JSONObject();
        }
        return jsonObject;
    }

    public static String getStringValueFromJson(JSONObject jsonObject, String key, String defaultValue) {
        try {
            if (jsonObject.has(key)) {
                return jsonObject.getString(key);
            } else {
                return defaultValue;
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return defaultValue;
        }
    }

    public static int getIntValueFromJson(JSONObject jsonObject, String key, int defaultValue) {
        try {
            if (jsonObject.has(key)) {
                return jsonObject.getInt(key);
            } else {
                return defaultValue;
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return defaultValue;
        }
    }

    public static void putStringValueToJson(JSONObject jsonObject, String key, String value) {
        try {
            jsonObject.put(key, value);
        } catch (JSONException e) {
        }
    }

    public static <T> void putValueToJson(JSONObject jsonObject, String key, T value) {
        try {
            jsonObject.put(key, value);
        } catch (JSONException e) {
        }
    }
}
