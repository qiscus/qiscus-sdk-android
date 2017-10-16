/*
 * Copyright (c) 2016 Qiscus.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.qiscus.sdk.chat.data.local.mapper

import android.content.ContentValues
import android.database.Cursor
import com.qiscus.sdk.chat.data.local.database.*
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
        put(Db.RoomTable.COLUMN_UNREAD_COUNT, unreadCount)
    }
}

fun Cursor.toRoomEntity(): RoomEntity {
    return RoomEntity(
            stringValue(Db.RoomTable.COLUMN_ID),
            stringValue(Db.RoomTable.COLUMN_UNIQUE_ID),
            stringValue(Db.RoomTable.COLUMN_NAME),
            stringValue(Db.RoomTable.COLUMN_AVATAR_URL),
            booleanValue(Db.RoomTable.COLUMN_IS_GROUP),
            stringValue(Db.RoomTable.COLUMN_OPTIONS),
            intValue(Db.RoomTable.COLUMN_UNREAD_COUNT)
    )
}