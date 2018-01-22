package com.qiscus.sdk.chat.presentation.util

import android.support.v7.util.SortedList

/**
 * Created on : October 10, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
inline fun <T> SortedList<T>.indexOfFirst(predicate: (T) -> Boolean): Int {
    val size = size()
    (0 until size).forEach {
        if (predicate(get(it))) {
            return it
        }
    }

    return -1
}