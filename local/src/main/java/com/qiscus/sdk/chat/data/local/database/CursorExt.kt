package com.qiscus.sdk.chat.data.local.database

import android.database.Cursor

/**
 * Created on : September 21, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
internal fun Cursor.intValue(columnName: String): Int {
    return getInt(getColumnIndexOrThrow(columnName))
}

internal fun Cursor.shortValue(columnName: String): Short {
    return getShort(getColumnIndexOrThrow(columnName))
}

internal fun Cursor.longValue(columnName: String): Long {
    return getLong(getColumnIndexOrThrow(columnName))
}

internal fun Cursor.doubleValue(columnName: String): Double {
    return getDouble(getColumnIndexOrThrow(columnName))
}

internal fun Cursor.floatValue(columnName: String): Float {
    return getFloat(getColumnIndexOrThrow(columnName))
}

internal fun Cursor.stringValue(columnName: String): String {
    return getString(getColumnIndexOrThrow(columnName))
}

internal fun Cursor.booleanValue(columnName: String): Boolean {
    return getInt(getColumnIndexOrThrow(columnName)) != 0
}

internal inline fun <T> Cursor.mapTo(transform: Cursor.() -> T): T {
    return transform()
}

internal inline fun <T> Cursor.map(transform: Cursor.() -> T): MutableCollection<T> {
    return mapInto(arrayListOf(), transform)
}

internal inline fun <T, R : MutableCollection<T>> Cursor.mapInto(result: R, transform: Cursor.() -> T): R {
    if (moveToFirst()) {
        do {
            result.add(transform())
        } while (moveToNext())
    }
    return result
}