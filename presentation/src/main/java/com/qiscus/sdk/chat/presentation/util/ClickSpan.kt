package com.qiscus.sdk.chat.presentation.util

import android.text.style.ClickableSpan
import android.view.View

/**
 * Created on : January 17, 2018
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
class ClickSpan(val onClick: () -> Unit): ClickableSpan() {
    override fun onClick(widget: View?) {
        onClick()
    }
}