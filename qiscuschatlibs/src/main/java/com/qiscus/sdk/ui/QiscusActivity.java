package com.qiscus.sdk.ui;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.qiscus.sdk.R;
import com.trello.rxlifecycle.components.support.RxAppCompatActivity;

import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;

/**
 * Created on : August 18, 2016
 * Author     : zetbaitsu
 * Name       : Zetra
 * Email      : zetra@mail.ugm.ac.id
 * GitHub     : https://github.com/zetbaitsu
 * LinkedIn   : https://id.linkedin.com/in/zetbaitsu
 */
public abstract class QiscusActivity extends RxAppCompatActivity implements EasyPermissions.PermissionCallbacks {

    protected static final int RC_STORAGE_PERMISSION = 1;

    private static final String[] STORAGE_PERMISSION = {
            "android.permission.WRITE_EXTERNAL_STORAGE",
            "android.permission.READ_EXTERNAL_STORAGE"
    };

    protected abstract void onViewReady(Bundle savedInstanceState);

    protected void requestStoragePermission() {
        if (!EasyPermissions.hasPermissions(this, STORAGE_PERMISSION)) {
            EasyPermissions.requestPermissions(this, "To make this apps working properly we need to access external storage to save your chatting data. So please allow the apps to access the storage!",
                    RC_STORAGE_PERMISSION, STORAGE_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {

    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        EasyPermissions.checkDeniedPermissionsNeverAskAgain(this, "Please grant permissions to make apps working properly!", R.string.ok, R.string.cancel, perms);
    }
}
