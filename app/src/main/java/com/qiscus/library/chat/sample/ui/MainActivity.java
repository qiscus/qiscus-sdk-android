package com.qiscus.library.chat.sample.ui;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.qiscus.library.chat.data.remote.QiscusApi;
import com.qiscus.library.chat.sample.R;
import com.qiscus.library.chat.sample.data.local.LocalDataManager;
import com.qiscus.library.chat.sample.data.remote.SampleApi;
import com.qiscus.library.chat.ui.BaseActivity;
import com.qiscus.library.chat.ui.ChatActivity;
import com.qiscus.library.chat.util.BaseScheduler;

import butterknife.BindView;

public class MainActivity extends BaseActivity {

    @BindView(R.id.bt_login) Button loginButton;

    private ProgressDialog progressDialog;

    @Override
    protected int getResourceLayout() {
        return R.layout.activity_main;
    }

    @Override
    protected void onViewReady(Bundle savedInstanceState) {
        loginButton.setText(LocalDataManager.getInstance().isLogged() ? "Logout" : "Login");
    }

    public void loginOrLogout(View view) {
        if (LocalDataManager.getInstance().isLogged()) {
            LocalDataManager.getInstance().clearData();
            com.qiscus.library.chat.data.local.LocalDataManager.getInstance().clearData();
            loginButton.setText("Login");
        } else {
            showLoading();
            SampleApi.getInstance().login("mas@zetra.com", "12345678")
                    .compose(BaseScheduler.pluck().applySchedulers(BaseScheduler.Type.IO))
                    .compose(bindToLifecycle())
                    .subscribe(accountInfo -> {
                        LocalDataManager.getInstance().saveAccountInfo(accountInfo);
                        com.qiscus.library.chat.data.local.LocalDataManager.getInstance().saveAccountInfo(accountInfo);
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
                .compose(BaseScheduler.pluck().applySchedulers(BaseScheduler.Type.IO))
                .compose(bindToLifecycle())
                .subscribe(chatRoom -> {
                    startActivity(ChatActivity.generateIntent(this, chatRoom));
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
