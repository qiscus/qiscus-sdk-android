package com.qiscus.sdk.chat.data.local.mapper

import android.content.ContentValues
import android.database.Cursor
import com.qiscus.sdk.chat.data.local.database.Db
import com.qiscus.sdk.chat.data.local.database.booleanValue
import com.qiscus.sdk.chat.data.local.database.longValue
import com.qiscus.sdk.chat.data.local.database.stringValue
import com.qiscus.sdk.chat.data.model.RoomEntity

/**
 * Created on : September 05, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
fun RoomEntity.toContentValues(): ContentValues {
    return ContentValues().apply {
        put(Db.RoomTable.COLUMN_ID, id)
        put(Db.RoomTable.COLUMN_UNIQUE_ID, uniqueId)
        put(Db.RoomTable.COLUMN_NAME, name)
        put(Db.RoomTable.COLUMN_IS_GROUP, group)
        put(Db.RoomTable.COLUMN_OPTIONS, options)
        put(Db.RoomTable.COLUMN_AVATAR_URL, avatar)
    }
}

fun Cursor.toRoomEntity(): RoomEntity {
    return RoomEntity(
            stringValue(Db.RoomTable.COLUMN_ID),
            stringValue(Db.RoomTable.COLUMN_UNIQUE_ID),
            stringValue(Db.RoomTable.COLUMN_NAME),
            stringValue(Db.RoomTable.COLUMN_AVATAR_URL),
            booleanValue(Db.RoomTable.COLUMN_IS_GROUP),
            stringValue(Db.RoomTable.COLUMN_OPTIONS)
    )
}