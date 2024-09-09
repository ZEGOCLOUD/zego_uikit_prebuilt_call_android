package com.zegocloud.uikit.prebuilt.call.core.startup;

import android.app.Application;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.tencent.mmkv.MMKV;
import com.zegocloud.uikit.ZegoUIKit;
import com.zegocloud.uikit.prebuilt.call.core.CallInvitationServiceImpl;

/**
 * invoked when app start,before Application.onCreate
 */
public class PrebuiltCallInitializer extends ContentProvider {

    @Override
    public boolean onCreate() {
        MMKV.initialize(getContext());
        Application application = (Application) getContext();
        if (application != null) {
            ZegoUIKit.debugMode(application);
            CallInvitationServiceImpl.getInstance().setUpCallbacksOnAppStart(application);
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
}
