package com.qiscus.sdk.chat.domain.util

import android.os.Parcel
import org.json.JSONObject
import java.io.File
import java.util.*

/**
 * Created on : January 22, 2018
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
// Parcel extensions
fun Parcel.readBoolean() = readInt() != 0

fun Parcel.writeBoolean(value: Boolean) = writeInt(if (value) 1 else 0)

inline fun <reified T : Enum<T>> Parcel.readEnum() =
        readInt().let { if (it >= 0) enumValues<T>()[it] else null }

fun <T : Enum<T>> Parcel.writeEnum(value: T?) = writeInt(value?.ordinal ?: -1)

inline fun <T> Parcel.readNullable(reader: () -> T) = if (readInt() != 0) reader() else null

inline fun <T> Parcel.writeNullable(value: T?, writer: (T) -> Unit) {
    if (value != null) {
        writeInt(1)
        writer(value)
    } else {
        writeInt(0)
    }
}

fun Parcel.readDate() = readNullable { Date(readLong()) }

fun Parcel.writeDate(value: Date?) = writeNullable(value) { writeLong(it.time) }

fun Parcel.readJSON() = readNullable { JSONObject(readString()) }

fun Parcel.writeJSON(value: JSONObject?) = writeNullable(value) { writeString(value.toString()) }

fun Parcel.readFile() = readNullable { File(readString()) }

fun Parcel.writeFile(value: File?) = writeNullable(value) { writeString(value?.absolutePath) }