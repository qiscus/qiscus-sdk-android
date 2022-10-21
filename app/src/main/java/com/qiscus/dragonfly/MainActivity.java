/*
 * Copyright (c) 2016 Qiscus.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.qiscus.dragonfly;

import static android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.qiscus.sdk.Qiscus;
import com.qiscus.sdk.chat.core.QiscusCore;
import com.qiscus.sdk.chat.core.data.model.QiscusRefreshToken;
import com.qiscus.sdk.chat.core.data.remote.QiscusPusherApi;
import com.qiscus.sdk.chat.core.event.QiscusChatRoomEvent;
import com.qiscus.sdk.chat.core.event.QiscusRefreshTokenEvent;
import com.qiscus.sdk.chat.core.util.QiscusAndroidUtil;
import com.qiscus.sdk.chat.core.util.QiscusErrorLogger;
import com.qiscus.sdk.chat.core.util.QiscusLogger;
import com.qiscus.sdk.ui.QiscusChannelActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONException;
import org.json.JSONObject;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {
    private Button mLoginButton;
    private ProgressDialog mProgressDialog;
    private TextView mVersion;
    private boolean publishCustomEvent = false;
    private static final int UNAUTHORIZED = 403;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mLoginButton = findViewById(R.id.bt_login);
        mVersion = findViewById(R.id.tv_version);
        mLoginButton.setText(Qiscus.hasSetupUser() ? "Logout" : "Login");

        String versionSDK = getString(R.string.qiscus_version) + " " + "123"; //BuildConfig.VERSION_NAME;
        mVersion.setText(versionSDK);

        AlarmManager alarmMgr = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmMgr.canScheduleExactAlarms()) {

                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
                alertBuilder.setCancelable(true);
                alertBuilder.setTitle("Permission necessary");
                alertBuilder.setMessage("Schedule Exact Alarm permission is necessary for realtime");
                alertBuilder.setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        Intent intent = new Intent(
                                ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                                Uri.parse("package:" + this.getPackageName())
                        );

                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        this.getApplicationContext().startActivity(intent);
                    }
                });

                AlertDialog alert = alertBuilder.create();
                alert.show();

            }
        }
    }

    public void loginOrLogout(View view) {
        if (Qiscus.hasSetupUser()) {
            logoutUser();
            mLoginButton.setText("Login");
        } else {
            showLoading();
          /*  Qiscus.setUser("arief92", "arief92")
                    .withUsername("arief92")*/

            Qiscus.setUser("y@mail.com", "123456")
                    .withUsername("testing_y")
                    .save()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(qiscusAccount -> {
                        Log.i("MainActivity", "Login with account: " + qiscusAccount);
                        mLoginButton.setText("Logout");
                        dismissLoading();
                    }, throwable -> {
                        QiscusErrorLogger.print(throwable);
                        showError(throwable);
                        dismissLoading();
                    });
        }
    }

    private void logoutUser() {
        if (QiscusCore.hasSetupUser()) Qiscus.clearUser();
    }

    public void openChat(View view) {
        showLoading();
        Qiscus.buildChatWith("arief93")
                .build(this)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(intent -> {
                    revertCustomChatConfig();
                    startActivity(intent);
                    dismissLoading();
                }, throwable -> {
                    QiscusErrorLogger.print(throwable);
                    showError(throwable);
                    dismissLoading();
                });
    }

    public void openChatFragment(View view) {
        //Start a sample activity with qiscus chat fragment, so you can customize the toolbar.
        showLoading();
        Qiscus.buildChatRoomWith("rya.meyvriska24@gmail.com")
                .build()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(qiscusChatRoom -> ChatActivity.generateIntent(this, qiscusChatRoom))
                .subscribe(intent -> {
                    revertCustomChatConfig();
                    startActivity(intent);
                    dismissLoading();
                }, throwable -> {
                    QiscusErrorLogger.print(throwable);
                    showError(throwable);
                    dismissLoading();
                });
    }

    public void openSimpleCustomChat(View view) {
        showLoading();
        Qiscus.buildChatRoomWith("rya.meyvriska24@gmail.com")
                .build()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(qiscusChatRoom -> SimpleCustomChatActivity.generateIntent(this, qiscusChatRoom))
                .subscribe(intent -> {
                    revertCustomChatConfig();
                    startActivity(intent);
                    dismissLoading();
                }, throwable -> {
                    QiscusErrorLogger.print(throwable);
                    showError(throwable);
                    dismissLoading();
                });
    }

    public void openAdvanceCustomChat(View view) {
        showLoading();
        Qiscus.buildChatRoomWith("rya.meyvriska24@gmail.com")
                .build()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(qiscusChatRoom -> CustomChatActivity.generateIntent(this, qiscusChatRoom))
                .subscribe(intent -> {
                    setupCustomChatConfig();
                    startActivity(intent);
                    dismissLoading();
                }, throwable -> {
                    QiscusErrorLogger.print(throwable);
                    showError(throwable);
                    dismissLoading();
                });
    }

    public void openChannel(View view) {
        showLoading();
        Qiscus.buildGroupChatRoomWith("Cat Family")
                .build()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(qiscusChatRoom -> QiscusChannelActivity.generateIntent(this, qiscusChatRoom))
                .subscribe(intent -> {
                    revertCustomChatConfig();
                    startActivity(intent);
                    dismissLoading();
                }, throwable -> {
                    QiscusErrorLogger.print(throwable);
                    showError(throwable);
                    dismissLoading();
                });
    }

    public void buildRoom() {
        Qiscus.buildChatRoomWith("rya.meyvriska24@gmail.com")
                .build()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(qiscusChatRoom -> {
                    Log.d(MainActivity.class.getSimpleName(), "Room: " + qiscusChatRoom.toString());
                    Log.d(MainActivity.class.getSimpleName(),
                            "Last message: " + qiscusChatRoom.getLastComment().getMessage());
                }, throwable -> {
                    QiscusErrorLogger.print(throwable);
                    showError(throwable);
                    dismissLoading();
                });
    }

    public void showError(Throwable throwable) {
       /* if (isTokenExpired(throwable)) {
            callRefreshToken();
            return;
        } */

        String errorMessage = QiscusErrorLogger.getMessage(throwable);
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
    }

    // disable manual refresh token
   /* private boolean isTokenExpired(Throwable throwable) {
        if(throwable instanceof HttpException) {
            HttpException httpException = (HttpException) throwable;
            return httpException.code() == UNAUTHORIZED;
        }
        return false;
    }*/

    private void callRefreshToken() {
        QiscusCore.refreshToken(new QiscusCore.SetRefreshTokenListener() {
            @Override
            public void onSuccess(QiscusRefreshToken refreshToken) {
                boolean isTokenValid = refreshToken != null && !refreshToken.getRefreshToken().isEmpty();
                String message = isTokenValid ? "refresh token success" : "refresh token failed";
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(Throwable throwable) {
                QiscusErrorLogger.print(throwable);
                showError(throwable);
                dismissLoading();
            }
        });
    }

    public void showLoading() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage("Please wait...");
        }
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
    }

    public void dismissLoading() {
        mProgressDialog.dismiss();
    }

    private void revertCustomChatConfig() {
        Qiscus.getChatConfig()
                .setSendButtonIcon(R.drawable.ic_qiscus_send)
                .setShowAttachmentPanelIcon(R.drawable.ic_qiscus_attach);
    }

    private void setupCustomChatConfig() {
        Qiscus.getChatConfig()
                .setSendButtonIcon(R.drawable.ic_qiscus_send_on)
                .setShowAttachmentPanelIcon(R.drawable.ic_qiscus_send_off);
    }

    public void publishEvent(View view) {
        JSONObject data = new JSONObject();
        try {
            data.put("msg", "Listening Music...");
            data.put("active", !publishCustomEvent);

            QiscusPusherApi.getInstance().publishCustomEvent(1353686, data);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
        QiscusPusherApi.getInstance().subsribeCustomEvent(1353686);
    }

    @Override
    protected void onPause() {
        QiscusPusherApi.getInstance().unsubsribeCustomEvent(1353686);
        EventBus.getDefault().unregister(this);
        super.onPause();
    }

    @Subscribe
    public void onRoomChanged(QiscusChatRoomEvent event) {
        QiscusLogger.print(event.toString());
    }

    @Subscribe
    public void onRefreshToken(QiscusRefreshTokenEvent event) {
        if (event.isTokenExpired()) {
            callRefreshToken();
        } else if (event.isUnauthorized()) {
            // default is background thread
            QiscusAndroidUtil.runOnUIThread(this::logoutUser);
        } else {
            // do somethings
        }
    }

}
