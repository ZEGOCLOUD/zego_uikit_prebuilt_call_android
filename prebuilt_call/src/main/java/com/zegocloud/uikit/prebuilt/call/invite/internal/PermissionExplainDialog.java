package com.zegocloud.uikit.prebuilt.call.invite.internal;

import android.content.Context;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.button.MaterialButton;
import com.permissionx.guolindev.dialog.RationaleDialog;
import com.zegocloud.uikit.prebuilt.call.R;
import java.util.List;

public class PermissionExplainDialog extends RationaleDialog {

    List<String> permissions;

    public PermissionExplainDialog(@NonNull Context context, List<String> permissions) {
        super(context);
        this.permissions = permissions;
    }

    public PermissionExplainDialog(@NonNull Context context, int themeResId, List<String> permissions) {
        super(context, themeResId);
        this.permissions = permissions;
    }

    @NonNull
    @Override
    public View getPositiveButton() {
        MaterialButton materialButton = new MaterialButton(getContext());
        materialButton.setText(R.string.call_ok);
        return materialButton;
    }

    @Nullable
    @Override
    public View getNegativeButton() {
        MaterialButton materialButton = new MaterialButton(getContext());
        materialButton.setText(R.string.call_cancel);
        return materialButton;
    }

    @NonNull
    @Override
    public List<String> getPermissionsToRequest() {
        return permissions;
    }
}
