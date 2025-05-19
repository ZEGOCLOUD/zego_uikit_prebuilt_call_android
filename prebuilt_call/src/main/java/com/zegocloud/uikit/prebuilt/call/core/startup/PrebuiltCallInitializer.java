package com.zegocloud.uikit.prebuilt.call.core.startup;

import android.app.Application;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.tencent.mmkv.MMKV;
import com.zegocloud.uikit.ZegoUIKit;
import com.zegocloud.uikit.prebuilt.call.core.CallInvitationServiceImpl;
import com.zegocloud.uikit.prebuilt.call.core.notification.RingtoneManager;
import com.zegocloud.uikit.prebuilt.call.core.utils.Storage;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import timber.log.Timber;
import xcrash.ICrashCallback;
import xcrash.XCrash;
import xcrash.XCrash.InitParameters;

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

            File dir = getContext().getExternalFilesDir(null);
            if (dir == null || (!dir.exists() && !dir.mkdirs())) {
                dir = getContext().getFilesDir();
            }
            if (dir != null) {
                String baseDir = dir.getAbsolutePath();
                String crashFilesDir = baseDir + File.separator + "callkit_crash";
                initCrashDirs(getContext(), crashFilesDir);
            }
        }
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

    private static void initCrashDirs(Context context, String crashFilesDir) {
        ICrashCallback callback = new ICrashCallback() {
            @Override
            public void onCrash(String logPath, String emergency) throws Exception {
                File file = new File(logPath);
                String originalName = file.getName();

                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm");
                String formattedDate = dateFormat.format(new Date());
                File newFile = new File(crashFilesDir, formattedDate + "_" + originalName);
                boolean renameTo = file.renameTo(newFile);

                Timber.d(
                    "onCrash() called ,logPath:" + logPath + ",renameTo = [" + newFile.getName() + "], successed = ["
                        + renameTo + "]");
            }
        };
        XCrash.init(context, new InitParameters().setLogDir(crashFilesDir).setJavaRethrow(true).setJavaLogCountMax(10)
            .setJavaCallback(callback).setAnrCallback(callback).setNativeCallback(callback).setAnrLogCountMax(10));
    }
}
