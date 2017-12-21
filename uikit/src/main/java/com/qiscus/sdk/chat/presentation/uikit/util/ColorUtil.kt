package com.qiscus.sdk.chat.presentation.uikit.util

import android.content.Context
import android.graphics.Color
import android.support.annotation.ColorRes
import android.support.v4.content.ContextCompat
import com.qiscus.sdk.chat.core.Qiscus
import com.qiscus.sdk.chat.domain.common.randomInt

/**
 * Created on : October 16, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
@JvmOverloads
fun randomColor(alpha: Int = 100): Int {
    return Color.argb(alpha, randomInt(256), randomInt(256), randomInt(256))
}

@JvmOverloads
fun getColor(context: Context = Qiscus.instance.component.application, @ColorRes resId: Int): Int {
    return ContextCompat.getColor(context, resId)
}