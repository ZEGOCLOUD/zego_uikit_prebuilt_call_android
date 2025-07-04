package com.zegocloud.uikit.prebuilt.call.core.startup;

import android.app.Application;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.tencent.mmkv.MMKV;
import com.zegocloud.uikit.ZegoUIKit;
import com.zegocloud.uikit.prebuilt.call.core.CallInvitationServiceImpl;
import com.zegocloud.uikit.prebuilt.call.core.notification.RingtoneManager;
import com.zegocloud.uikit.prebuilt.call.core.utils.Storage;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.Thread.UncaughtExceptionHandler;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import timber.log.Timber;

/**
 * invoked when app start,before Application.onCreate
 */
public class PrebuiltCallInitializer extends ContentProvider {

    @Override
    public boolean onCreate() {

        MMKV.initialize(getContext());
        Storage.init(getContext());
        Application application = (Application) getContext();
        if (application != null) {
            ZegoUIKit.debugMode(application);
            Timber.d("------------------PrebuiltCallInitializer onCreate() called,App start------------------");
            CallInvitationServiceImpl.getInstance().setUpCallbacksOnAppStart(application);
            RingtoneManager.init(application);
        }
        initCrashHandler(application);
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
        @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        return null;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return "";
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection,
        @Nullable String[] selectionArgs) {
        return 0;
    }

    private static void initCrashHandler(Application application) {
        UncaughtExceptionHandler defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(@NonNull Thread thread, @NonNull Throwable throwable) {
                // 获取完整的堆栈信息
                StringBuilder crashInfo = new StringBuilder();
                crashInfo.append("*** *** *** *** *** *** *** *** *** *** *** *** *** *** *** ***\n");
                crashInfo.append("Crash type: 'java'\n");
                crashInfo.append("Crash time: '").append(getFormattedTime(System.currentTimeMillis())).append("'\n");
                crashInfo.append("App ID: '").append(application.getPackageName()).append("'\n");
                crashInfo.append("App version: '").append(getAppVersion(application)).append("'\n");
                crashInfo.append("Rooted(Not Reliable): '").append(isRooted() ? "Yes" : "No").append("'\n");
                crashInfo.append("API level: '").append(Build.VERSION.SDK_INT).append("'\n");
                crashInfo.append("OS version: '").append(Build.VERSION.RELEASE).append("'\n");
                crashInfo.append("ABI list: '").append(getAbiList()).append("'\n");
                crashInfo.append("Manufacturer: '").append(Build.MANUFACTURER).append("'\n");
                crashInfo.append("Brand: '").append(Build.BRAND).append("'\n");
                crashInfo.append("Model: '").append(Build.MODEL).append("'\n");
                crashInfo.append("Build fingerprint: '").append(Build.FINGERPRINT).append("'\n");
                crashInfo.append(String.format(Locale.US, "pid: %d, tid: %d, name: %s  >>> %s <<<",
                    android.os.Process.myPid(), thread.getId(), thread.getName(), application.getPackageName())).append("\n");
                crashInfo.append("\njava stacktrace:\n");

                crashInfo.append(throwable.toString()).append("\n");

                // 遍历所有堆栈帧
                for (StackTraceElement element : throwable.getStackTrace()) {
                    crashInfo.append("\tat ").append(element.toString()).append("\n");
                }

                // 如果有 cause，递归处理
                Throwable cause = throwable.getCause();
                while (cause != null) {
                    crashInfo.append("Caused by: ").append(cause.toString()).append("\n");
                    for (StackTraceElement element : cause.getStackTrace()) {
                        crashInfo.append("\tat ").append(element.toString()).append("\n");
                    }
                    cause = cause.getCause();
                }

                // 使用 Timber 输出
                Timber.e(crashInfo.toString());

                // 调用默认处理器
                if (defaultHandler != null && defaultHandler != this) {
                    defaultHandler.uncaughtException(thread, throwable);
                }
            }
        });
    }

    private static String getFormattedTime(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    private static String getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            return "Unknown";
        }
    }

    private static boolean isRooted() {
        // 简单的 root 检测逻辑，实际项目中可能需要更复杂的实现
        String[] paths = {"/system/bin/su", "/system/xbin/su"};
        for (String path : paths) {
            if (new java.io.File(path).exists()) {
                return true;
            }
        }
        return false;
    }

    private static String getAbiList() {
        String[] abis = Build.SUPPORTED_ABIS;
        return String.join(",", abis);
    }
}
