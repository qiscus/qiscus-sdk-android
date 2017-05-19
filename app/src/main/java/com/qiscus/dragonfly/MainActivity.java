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
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.qiscus.sdk.Qiscus;

import java.io.IOException;

import retrofit2.HttpException;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {
    private Button mLoginButton;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mLoginButton = (Button) findViewById(R.id.bt_login);
        mLoginButton.setText(Qiscus.hasSetupUser() ? "Logout" : "Login");
    }

    public void loginOrLogout(View view) {
        if (Qiscus.hasSetupUser()) {
            Qiscus.clearUser();
            mLoginButton.setText("Login");
        } else {
            showLoading();
            Qiscus.setUser("zetra25@gmail.com", "12345678")
                    .withUsername("Zetra")
                    .save()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(qiscusAccount -> {
                        Log.i("MainActivity", "Login with account: " + qiscusAccount);
                        mLoginButton.setText("Logout");
                        dismissLoading();
                    }, throwable -> {
                        throwable.printStackTrace();
                        showError(throwable.getMessage());
                        dismissLoading();
                    });
        }
    }

    public void openChat(View view) {
        showLoading();
        Qiscus.buildChatWith("rya.meyvriska24@gmail.com")
                .withTitle("Rya Meyvriska")
                .build(this)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(intent -> {
                    revertCustomChatConfig();
                    startActivity(intent);
                    dismissLoading();
                }, throwable -> {
                    if (throwable instanceof HttpException) { //Error response from server
                        HttpException e = (HttpException) throwable;
                        try {
                            String errorMessage = e.response().errorBody().string();
                            Log.e("openChat", errorMessage);
                            showError(errorMessage);
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    } else if (throwable instanceof IOException) { //Error from network
                        showError("Can not connect to qiscus server!");
                    } else { //Unknown error
                        showError("Unexpected error!");
                    }
                    throwable.printStackTrace();
                    dismissLoading();
                });
    }

    public void openChatFragment(View view) {
        //Start a sample activity with qiscus chat fragment, so you can customize the toolbar.
        showLoading();
        Qiscus.buildChatRoomWith("rya.meyvriska24@gmail.com")
                .withTitle("Rya Meyvriska")
                .build()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(qiscusChatRoom -> ChatActivity.generateIntent(this, qiscusChatRoom))
                .subscribe(intent -> {
                    revertCustomChatConfig();
                    startActivity(intent);
                    dismissLoading();
                }, throwable -> {
                    throwable.printStackTrace();
                    showError(throwable.getMessage());
                    dismissLoading();
                });
    }

    public void openSimpleCustomChat(View view) {
        showLoading();
        Qiscus.buildChatRoomWith("rya.meyvriska24@gmail.com")
                .withTitle("Rya Meyvriska")
                .build()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(qiscusChatRoom -> SimpleCustomChatActivity.generateIntent(this, qiscusChatRoom))
                .subscribe(intent -> {
                    revertCustomChatConfig();
                    startActivity(intent);
                    dismissLoading();
                }, throwable -> {
                    throwable.printStackTrace();
                    showError(throwable.getMessage());
                    dismissLoading();
                });
    }

    public void openAdvanceCustomChat(View view) {
        showLoading();
        Qiscus.buildChatRoomWith("rya.meyvriska24@gmail.com")
                .withTitle("Rya Meyvriska")
                .build()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(qiscusChatRoom -> CustomChatActivity.generateIntent(this, qiscusChatRoom))
                .subscribe(intent -> {
                    setupCustomChatConfig();
                    startActivity(intent);
                    dismissLoading();
                }, throwable -> {
                    throwable.printStackTrace();
                    showError(throwable.getMessage());
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
                            "Last message: " + qiscusChatRoom.getLastCommentMessage());
                }, throwable -> {
                    throwable.printStackTrace();
                    showError(throwable.getMessage());
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
}
