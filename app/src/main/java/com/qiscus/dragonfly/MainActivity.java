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

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import com.qiscus.sdk.BuildConfig;
import com.qiscus.sdk.Qiscus;
import com.qiscus.sdk.chat.core.data.remote.QiscusPusherApi;
import com.qiscus.sdk.chat.core.event.QiscusChatRoomEvent;
import com.qiscus.sdk.chat.core.util.QiscusErrorLogger;
import com.qiscus.sdk.chat.core.util.QiscusLogger;
import com.qiscus.sdk.ui.QiscusChannelActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {
    private Button mLoginButton;
    private ProgressDialog mProgressDialog;
    private TextView mVersion;
    private boolean publishCustomEvent = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mLoginButton = findViewById(R.id.bt_login);
        mVersion = findViewById(R.id.tv_version);
        mLoginButton.setText(Qiscus.hasSetupUser() ? "Logout" : "Login");

        String versionSDK = getString(R.string.qiscus_version) + " " + BuildConfig.VERSION_NAME;
        mVersion.setText(versionSDK);
    }

    public void loginOrLogout(View view) {
        if (Qiscus.hasSetupUser()) {
            Qiscus.clearUser();
            mLoginButton.setText("Login");
        } else {
            showLoading();
            Qiscus.setUser("arief93", "arief93")
                    .withUsername("arief93")
                    .save()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(qiscusAccount -> {
                        Log.i("MainActivity", "Login with account: " + qiscusAccount);
                        mLoginButton.setText("Logout");
                        dismissLoading();
                    }, throwable -> {
                        QiscusErrorLogger.print(throwable);
                        showError(QiscusErrorLogger.getMessage(throwable));
                        dismissLoading();
                    });
        }
    }

    public void openChat(View view) {
        showLoading();
        Qiscus.buildChatWith("arief92")
                .build(this)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(intent -> {
                    revertCustomChatConfig();
                    startActivity(intent);
                    dismissLoading();
                }, throwable -> {
                    QiscusErrorLogger.print(throwable);
                    showError(QiscusErrorLogger.getMessage(throwable));
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
                    showError(QiscusErrorLogger.getMessage(throwable));
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
                    showError(QiscusErrorLogger.getMessage(throwable));
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
                    showError(QiscusErrorLogger.getMessage(throwable));
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
                    showError(QiscusErrorLogger.getMessage(throwable));
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
                    showError(QiscusErrorLogger.getMessage(throwable));
                    dismissLoading();
                });
    }

    public void showError(String errorMessage) {
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
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

}
