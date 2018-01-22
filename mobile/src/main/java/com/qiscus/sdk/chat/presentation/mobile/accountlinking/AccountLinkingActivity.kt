package com.qiscus.sdk.chat.presentation.mobile.accountlinking

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.qiscus.sdk.chat.presentation.mobile.R
import kotlinx.android.synthetic.main.activity_qiscus_account_linking.*

/**
 * Created on : January 17, 2018
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */

fun Context.accountLinkingIntent(title: String, url: String, finishUrl: String, successMessage: String): Intent {
    return Intent(this, AccountLinkingActivity::class.java).apply {
        putExtra(EXTRA_TITLE, title)
        putExtra(EXTRA_URL, url)
        putExtra(EXTRA_FINISH_URL, finishUrl)
        putExtra(EXTRA_SUCCESS_MESSAGE, successMessage)
    }
}

private const val EXTRA_TITLE = "extra_title"
private const val EXTRA_URL = "extra_url"
private const val EXTRA_FINISH_URL = "extra_finish_url"
private const val EXTRA_SUCCESS_MESSAGE = "extra_success_message"

class AccountLinkingActivity : AppCompatActivity() {
    private var success = false
    private lateinit var successMessage: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qiscus_account_linking)
        var title = intent.getStringExtra(EXTRA_TITLE)
        title = if (title == null || title.isEmpty()) getString(R.string.qiscus_account_linking) else title
        titleView.text = title

        successMessage = intent.getStringExtra(EXTRA_SUCCESS_MESSAGE)
        successMessage = if (successMessage.isEmpty())
            getString(R.string.qiscus_success_linking_account)
        else
            successMessage

        setupWebView()

        webView.loadUrl(intent.getStringExtra(EXTRA_URL))
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        webView.webViewClient = WebViewClient()
        webView.settings.javaScriptEnabled = true
        webView.settings.setAppCacheEnabled(true)
        webView.webViewClient = QiscusWebViewClient()
        webView.webChromeClient = QiscusChromeClient()
    }

    private inner class QiscusChromeClient : WebChromeClient() {

        override fun onProgressChanged(view: WebView, newProgress: Int) {
            progressBar.visibility = if (newProgress == 0 || newProgress == 100) View.GONE else View.VISIBLE
            if (success && newProgress >= 95 && !isFinishing) {
                showSuccessDialog()
            }
            super.onProgressChanged(view, newProgress)
        }
    }

    private inner class QiscusWebViewClient : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            if (url == intent.getStringExtra(EXTRA_FINISH_URL)) {
                success = true
            }
            return super.shouldOverrideUrlLoading(view, url)
        }

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
            if (request.url.toString() == intent.getStringExtra(EXTRA_FINISH_URL)) {
                success = true
            }
            return super.shouldOverrideUrlLoading(view, request)
        }
    }

    private fun showSuccessDialog() {
        success = false // To prevent showing multiple dialog
        AlertDialog.Builder(this)
                .setCancelable(false)
                .setMessage(successMessage)
                .setPositiveButton(getString(R.string.qiscus_ok)) { dialog, _ ->
                    dialog.dismiss()
                    finish()
                }
                .create()
                .show()
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}