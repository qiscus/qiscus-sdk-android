package com.qiscus.sdk.chat.presentation.uikit.util

import android.text.format.DateUtils
import android.util.Log
import com.qiscus.sdk.chat.presentation.uikit.R
import com.qiscus.sdk.chat.presentation.util.getString
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created on : December 20, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
val fullDateFormat: DateFormat by lazy {
    SimpleDateFormat(getString(resId = R.string.qiscus_date_format), Locale.getDefault())
}

val hourDateFormat: DateFormat by lazy {
    SimpleDateFormat(getString(resId = R.string.qiscus_hour_format), Locale.getDefault())
}

fun Date.toTodayOrDate(): String {
    val currentDateInString = fullDateFormat.format(Date())
    val dateInString = fullDateFormat.format(this)
    return if (currentDateInString == dateInString) getString(resId = R.string.qiscus_today) else dateInString
}

fun Date.toHour(): String {
    return hourDateFormat.format(this)
}

fun Date.isEqualIgnoreTime(other: Date): Boolean {
    return toTodayOrDate() == other.toTodayOrDate()
}

fun Date.getRelativeTimeDiff(): String {
    var timeDiff = DateUtils.getRelativeTimeSpanString(time, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS).toString()

    if (timeDiff.contains("0 ")) {
        timeDiff = getString(resId = R.string.qiscus_few_seconds_ago)
    }

    return timeDiff
}

fun Date.toFullDateFormat(): String {
    return getString(resId = R.string.qiscus_date_and_time, formatArgs = *arrayOf(toTodayOrDate(), toHour()))
}