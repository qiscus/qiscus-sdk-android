package com.qiscus.dragonfly;

import android.app.ProgressDialog;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.qiscus.sdk.Qiscus;
import com.qiscus.sdk.util.QiscusDateUtil;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created on : September 28, 2016
 * Author     : zetbaitsu
 * Name       : Zetra
 * Email      : zetra@mail.ugm.ac.id
 * GitHub     : https://github.com/zetbaitsu
 * LinkedIn   : https://id.linkedin.com/in/zetbaitsu
 */
public class CustomChatActivity extends AppCompatActivity {
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.qiscus_primary_dark));
        }

        setContentView(R.layout.activity_custom_chat);

        customizeChatUI();

        showLoading();
        Qiscus.buildChatRoomWith("rya.meyvriska1@gmail.com")
                .build()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(CustomChatFragment::newInstance)
                .subscribe(qiscusChatFragment -> {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, qiscusChatFragment)
                            .commit();
                    dismissLoading();
                }, throwable -> {
                    throwable.printStackTrace();
                    showError(throwable.getMessage());
                    dismissLoading();
                });
    }

    private void customizeChatUI() {
        Qiscus.getChatConfig()
                .setLeftBubbleColor(R.color.qiscus_primary)
                .setRightBubbleColor(R.color.qiscus_primary_light)
                .setRightBubbleTextColor(R.color.qiscus_primary_text)
                .setRightBubbleTimeColor(R.color.qiscus_secondary_text)
                .setTimeFormat(QiscusDateUtil::toHour);
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
