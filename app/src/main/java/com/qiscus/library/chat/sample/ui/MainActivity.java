package com.qiscus.library.chat.sample.ui;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.qiscus.library.chat.sample.R;
import com.qiscus.sdk.Qiscus;
import com.qiscus.sdk.data.remote.QiscusApi;
import com.qiscus.sdk.ui.QiscusActivity;
import com.qiscus.sdk.ui.QiscusChatActivity;

import java.util.Collections;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MainActivity extends QiscusActivity {

    @BindView(R.id.bt_login) Button loginButton;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        onViewReady(savedInstanceState);
    }

    @Override
    protected void onViewReady(Bundle savedInstanceState) {
        loginButton.setText(Qiscus.isLogged() ? "Logout" : "Login");
    }

    public void loginOrLogout(View view) {
        if (Qiscus.isLogged()) {
            Qiscus.logout();
            loginButton.setText("Login");
        } else {
            showLoading();
            Qiscus.with("zetra1@gmail.com", "12345678")
                    .withUsername("Zetra")
                    .login()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .compose(bindToLifecycle())
                    .subscribe(qiscusAccount -> {
                        Log.i("MainActivity", "Login with account: " + qiscusAccount);
                        loginButton.setText("Logout");
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
        QiscusApi.getInstance().getChatRoom(Collections.singletonList("rya.meyvriska1@gmail.com"))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(bindToLifecycle())
                .subscribe(chatRoom -> {
                    startActivity(QiscusChatActivity.generateIntent(this, chatRoom));
                    dismissLoading();
                }, throwable -> {
                    throwable.printStackTrace();
                    showError(throwable.getMessage());
                    dismissLoading();
                });
    }

    public void showError(String errorMessage) {
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
    }

    public void showLoading() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Please wait...");
        }
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    public void dismissLoading() {
        progressDialog.dismiss();
    }
}
