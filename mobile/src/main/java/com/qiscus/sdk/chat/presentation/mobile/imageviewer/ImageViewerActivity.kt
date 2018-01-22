package com.qiscus.sdk.chat.presentation.mobile.imageviewer

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Window
import android.view.WindowManager
import com.qiscus.sdk.chat.presentation.mobile.R
import com.qiscus.sdk.chat.presentation.model.MessageImageViewModel

/**
 * Created on : January 18, 2018
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
fun Context.imageViewerIntent(messageImageViewModel: MessageImageViewModel): Intent {
    return Intent(this, ImageViewerActivity::class.java).apply {
        TODO()
    }
}

private const val EXTRA_MESSAGE = "extra_message"

class ImageViewerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.activity_qiscus_image_viewer)
        TODO()
    }
}