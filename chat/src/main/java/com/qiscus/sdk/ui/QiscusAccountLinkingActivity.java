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

package com.qiscus.sdk.ui;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.qiscus.sdk.Qiscus;
import com.qiscus.sdk.R;
import com.trello.rxlifecycle.components.support.RxAppCompatActivity;

/**
 * Created on : March 04, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
public class QiscusAccountLinkingActivity extends RxAppCompatActivity {
    private static final String EXTRA_TITLE = "extra_title";
    private static final String EXTRA_URL = "extra_url";
    private static final String EXTRA_FINISH_URL = "extra_finish_url";
    private static final String EXTRA_SUCCESS_MESSAGE = "extra_success_message";

    private WebView webView;
    private ProgressBar progressBar;
    private boolean success;
    private String successMessage;

    public static Intent generateIntent(Context context, String title, String url, String finishUrl, String successMessage) {
        Intent intent = new Intent(context, QiscusAccountLinkingActivity.class);
        intent.putExtra(EXTRA_TITLE, title);
        intent.putExtra(EXTRA_URL, url);
        intent.putExtra(EXTRA_FINISH_URL, finishUrl);
        intent.putExtra(EXTRA_SUCCESS_MESSAGE, successMessage);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        onSetStatusBarColor();
        setContentView(R.layout.activity_qiscus_account_linking);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        TextView titleView = (TextView) findViewById(R.id.title);
        String title = getIntent().getStringExtra(EXTRA_TITLE);
        title = (title == null || title.isEmpty()) ? "Account Linking" : title;
        titleView.setText(title);

        successMessage = getIntent().getStringExtra(EXTRA_SUCCESS_MESSAGE);
        successMessage = (successMessage == null || successMessage.isEmpty()) ? "Account linking successfully." : successMessage;

        webView = (WebView) findViewById(R.id.web_view);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        setupWebView();

        toolbar.setBackgroundResource(Qiscus.getChatConfig().getAppBarColor());

        webView.loadUrl(getIntent().getStringExtra(EXTRA_URL));
    }

    protected void onSetStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, Qiscus.getChatConfig().getStatusBarColor()));
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setupWebView() {
        webView.setWebViewClient(new WebViewClient());
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setAppCacheEnabled(true);
        webView.setWebViewClient(new QiscusWebViewClient());
        webView.setWebChromeClient(new QiscusChromeClient());
    }

    private class QiscusChromeClient extends WebChromeClient {

        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            progressBar.setVisibility(newProgress == 0 || newProgress == 100 ? View.GONE : View.VISIBLE);
            if (success && newProgress >= 95) {
                showSuccessDialog();
            }
            super.onProgressChanged(view, newProgress);
        }
    }

    private class QiscusWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (url.equals(getIntent().getStringExtra(EXTRA_FINISH_URL))) {
                success = true;
            }
            return super.shouldOverrideUrlLoading(view, url);
        }

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            if (request.getUrl().toString().equals(getIntent().getStringExtra(EXTRA_FINISH_URL))) {
                success = true;
            }
            return super.shouldOverrideUrlLoading(view, request);
        }
    }

    private void showSuccessDialog() {
        success = false; // To prevent showing multiple dialog
        new AlertDialog.Builder(this)
                .setCancelable(false)
                .setMessage(successMessage)
                .setPositiveButton("OK", (dialog, which) -> {
                    dialog.dismiss();
                    finish();
                })
                .create()
                .show();
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
