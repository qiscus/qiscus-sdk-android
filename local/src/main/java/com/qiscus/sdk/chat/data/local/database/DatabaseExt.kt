package com.qiscus.sdk.chat.data.local.database

import android.database.sqlite.SQLiteDatabase

/**
 * Created on : September 21, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */

internal inline fun SQLiteDatabase.transaction(action: SQLiteDatabase.() -> Unit) {
    beginTransaction()
    try {
        action(this)
        setTransactionSuccessful()
    } catch (e: Exception) {
        e.printStackTrace()
    } finally {
        endTransaction()
    }
}