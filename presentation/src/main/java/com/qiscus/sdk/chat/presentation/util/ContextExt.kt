package com.qiscus.sdk.chat.presentation.util

import android.content.Context
import android.net.Uri
import android.support.customtabs.CustomTabsIntent
import com.qiscus.sdk.chat.presentation.R

/**
 * Created on : January 17, 2018
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
fun Context.startCustomTabActivity(url: String) {
    startCustomTabActivity(Uri.parse(url))
}

fun Context.startCustomTabActivity(url: Uri) {
    CustomTabsIntent.Builder()
            .setToolbarColor(getColor(resId = R.color.qiscus_primary))
            .setShowTitle(true)
            .addDefaultShareMenuItem()
            .enableUrlBarHiding()
            .build()
            .launchUrl(this, url)
}