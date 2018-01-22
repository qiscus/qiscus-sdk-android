package com.qiscus.sdk.chat.presentation.util

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.support.annotation.ColorRes
import android.support.annotation.DrawableRes
import android.support.v4.content.ContextCompat
import com.qiscus.sdk.chat.core.Qiscus
import com.qiscus.sdk.chat.domain.util.randomInt

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

@JvmOverloads
fun getDrawable(context: Context = Qiscus.instance.component.application, @DrawableRes resId: Int): Drawable? {
    return ContextCompat.getDrawable(context, resId)
}