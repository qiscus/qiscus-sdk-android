package com.qiscus.sdk.chat.presentation.uikit.util

import android.content.res.Resources

/**
 * Created on : October 05, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
fun dp2px(resources: Resources, dp: Float): Float {
    val scale = resources.displayMetrics.density
    return dp * scale + 0.5f
}

fun sp2px(resources: Resources, sp: Float): Float {
    val scale = resources.displayMetrics.scaledDensity
    return sp * scale
}