package com.qiscus.library.chat.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;

import com.qiscus.library.chat.R;
import com.trello.rxlifecycle.components.support.RxAppCompatActivity;

import java.util.List;

import butterknife.ButterKnife;
import pub.devrel.easypermissions.EasyPermissions;
import timber.log.Timber;

/**
 * Created on : August 18, 2016
 * Author     : zetbaitsu
 * Name       : Zetra
 * Email      : zetra@mail.ugm.ac.id
 * GitHub     : https://github.com/zetbaitsu
 * LinkedIn   : https://id.linkedin.com/in/zetbaitsu
 */
public abstract class BaseActivity extends RxAppCompatActivity implements
        EasyPermissions.PermissionCallbacks {

    protected static final int RC_CAMERA_PERMISSION = 1;
    protected static final int RC_AUDIO_PERMISSION = 2;
    protected static final int RC_STORAGE_PERMISSION = 3;

    private static final String[] CAMERA_PERMISSION = {
            "android.permission.CAMERA"
    };

    private static final String[] AUDIO_PERMISSION = {
            "android.permission.RECORD_AUDIO"
    };

    private static final String[] STORAGE_PERMISSION = {
            "android.permission.WRITE_EXTERNAL_STORAGE",
            "android.permission.READ_EXTERNAL_STORAGE"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getResourceLayout());
        ButterKnife.bind(this);
        Timber.tag(getClass().getSimpleName());
        onViewReady(savedInstanceState);
    }

    protected abstract int getResourceLayout();

    protected abstract void onViewReady(Bundle savedInstanceState);

    protected void requestCameraPermission() {
        if (!EasyPermissions.hasPermissions(this, CAMERA_PERMISSION)) {
            EasyPermissions.requestPermissions(this, "To make this apps working properly we need to access camera for video calling. So please allow the apps to use camera!",
                                               RC_CAMERA_PERMISSION, CAMERA_PERMISSION);
        }
    }

    protected void requestAudioPermission() {
        if (!EasyPermissions.hasPermissions(this, AUDIO_PERMISSION)) {
            EasyPermissions.requestPermissions(this, "To make this apps working properly we need to record audio for voice/video calling. So please allow the apps to record audio!",
                                               RC_AUDIO_PERMISSION, AUDIO_PERMISSION);
        }
    }

    protected void requestStoragePermission() {
        if (!EasyPermissions.hasPermissions(this, STORAGE_PERMISSION)) {
            EasyPermissions.requestPermissions(this, "To make this apps working properly we need to access external storage to save your chatting data. So please allow the apps to access the storage!",
                                               RC_STORAGE_PERMISSION, STORAGE_PERMISSION);
        }
    }

    protected void requestChangeNetworkStatePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            boolean canWrite = Settings.System.canWrite(this);
            Timber.d("System.canWrite() : " + canWrite);

            if (!canWrite) {
                AlertDialog dialog = new AlertDialog.Builder(this)
                        .setCancelable(false)
                        .setMessage("To make this apps working properly we need permission to change network state for voice/video call. So please allow the apps to modify your system settings!")
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                                intent.setData(Uri.parse("package:" + getPackageName()));
                                startActivity(intent);
                            }
                        })
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                onNetworkStatePermissionDenied();
                            }
                        }).create();
                dialog.show();
            }
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
        Timber.e("onPermissionsDenied:" + requestCode + ":" + perms.size());
        EasyPermissions.checkDeniedPermissionsNeverAskAgain(this, "Please grant permissions to make apps working properly!", R.string.ok, R.string.cancel, perms);
    }

    public void onNetworkStatePermissionDenied() {
        Timber.e("onNetworkStatePermissionDenied");
    }
}
