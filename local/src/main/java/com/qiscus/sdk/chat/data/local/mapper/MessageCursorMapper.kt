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
import com.qiscus.sdk.chat.data.local.database.Db
import com.qiscus.sdk.chat.data.local.database.intValue
import com.qiscus.sdk.chat.data.local.database.longValue
import com.qiscus.sdk.chat.data.local.database.stringValue
import com.qiscus.sdk.chat.data.mapper.transformToTypedMessageEntity
import com.qiscus.sdk.chat.data.model.*
import org.json.JSONObject

/**
 * Created on : September 05, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
fun MessageEntity.toContentValues(): ContentValues {
    return ContentValues().apply {
        put(Db.MessageTable.COLUMN_ID, messageId.id)
        put(Db.MessageTable.COLUMN_ROOM_ID, room.id)
        put(Db.MessageTable.COLUMN_UNIQUE_ID, messageId.uniqueId)
        put(Db.MessageTable.COLUMN_BEFORE_ID, messageId.beforeId)
        put(Db.MessageTable.COLUMN_MESSAGE, text)
        put(Db.MessageTable.COLUMN_SENDER_ID, sender.id)
        put(Db.MessageTable.COLUMN_SENDER_NAME, sender.name)
        put(Db.MessageTable.COLUMN_SENDER_AVATAR, sender.avatar)
        put(Db.MessageTable.COLUMN_TIME, nanoTimeStamp)
        put(Db.MessageTable.COLUMN_STATE, state.intValue)
        put(Db.MessageTable.COLUMN_TYPE, type.rawType)
        put(Db.MessageTable.COLUMN_PAYLOAD, type.payload.toString())
    }
}

fun Cursor.toMessageEntity(): MessageEntity {
    return MessageEntity(
            MessageIdEntity(stringValue(Db.MessageTable.COLUMN_ID),
                    stringValue(Db.MessageTable.COLUMN_BEFORE_ID),
                    stringValue(Db.MessageTable.COLUMN_UNIQUE_ID)),
            stringValue(Db.MessageTable.COLUMN_MESSAGE),
            UserEntity(stringValue(Db.MessageTable.COLUMN_SENDER_ID),
                    stringValue(Db.MessageTable.COLUMN_SENDER_NAME),
                    stringValue(Db.MessageTable.COLUMN_SENDER_AVATAR)),
            longValue(Db.MessageTable.COLUMN_TIME),
            RoomEntity(stringValue(Db.MessageTable.COLUMN_ROOM_ID), name = "Unknown"),
            MessageStateEntity.valueOf(intValue(Db.MessageTable.COLUMN_STATE)),
            MessageTypeEntity(stringValue(Db.MessageTable.COLUMN_TYPE),
                    JSONObject(stringValue(Db.MessageTable.COLUMN_PAYLOAD)))
    ).transformToTypedMessageEntity()
}