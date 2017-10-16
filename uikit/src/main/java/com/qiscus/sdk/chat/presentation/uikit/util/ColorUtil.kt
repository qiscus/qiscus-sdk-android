package com.qiscus.sdk.chat.presentation.uikit.util

import android.graphics.Color
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