package com.qiscus.library.chat.sample.ui;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.qiscus.library.chat.sample.R;
import com.qiscus.library.chat.sample.data.local.LocalDataManager;
import com.qiscus.library.chat.sample.data.remote.SampleApi;
import com.qiscus.sdk.Qiscus;
import com.qiscus.sdk.data.remote.QiscusApi;
import com.qiscus.sdk.ui.QiscusActivity;
import com.qiscus.sdk.ui.QiscusChatActivity;
import com.qiscus.sdk.util.QiscusScheduler;

import butterknife.BindView;
import butterknife.ButterKnife;

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
        loginButton.setText(LocalDataManager.getInstance().isLogged() ? "Logout" : "Login");
    }

    public void loginOrLogout(View view) {
        if (LocalDataManager.getInstance().isLogged()) {
            LocalDataManager.getInstance().clearData();
            Qiscus.logout();
            loginButton.setText("Login");
        } else {
            showLoading();
            SampleApi.getInstance().login("mas@zetra.com", "12345678")
                    .compose(QiscusScheduler.get().applySchedulers(QiscusScheduler.Type.IO))
                    .compose(bindToLifecycle())
                    .subscribe(accountInfo -> {
                        LocalDataManager.getInstance().saveAccountInfo(accountInfo);
                        Qiscus.setQiscusAccount(accountInfo.getEmail(), accountInfo.getAuthenticationToken(), accountInfo.getFullname());
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
        QiscusApi.getInstance().getChatRoom(131)
                .compose(QiscusScheduler.get().applySchedulers(QiscusScheduler.Type.IO))
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
