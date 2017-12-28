package com.qiscus.sdk.chat.presentation.util

import android.os.Handler
import com.qiscus.sdk.chat.core.Qiscus

/**
 * Created on : December 28, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
val handler: Handler by lazy {
    Handler(Qiscus.instance.component.application.mainLooper)
}

@JvmOverloads
fun runOnUIThread(runnable: Runnable, delay: Long = 0) {
    handler.postDelayed(runnable, delay)
}

fun cancelRunOnUIThread(runnable: Runnable) {
    handler.removeCallbacks(runnable)
}