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
import com.qiscus.sdk.chat.data.local.database.longValue
import com.qiscus.sdk.chat.data.local.database.stringValue
import com.qiscus.sdk.chat.data.model.MemberStateEntity
import com.qiscus.sdk.chat.data.model.RoomMemberEntity
import com.qiscus.sdk.chat.data.model.UserEntity

/**
 * Created on : September 21, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
fun RoomMemberEntity.toContentValues(roomId: String): ContentValues {
    return ContentValues().apply {
        put(Db.RoomMemberTable.COLUMN_ROOM_ID, roomId)
        put(Db.RoomMemberTable.COLUMN_USER_ID, userEntity.id)
        put(Db.RoomMemberTable.COLUMN_LAST_DELIVERED, memberStateEntity.lastDeliveredMessageId)
        put(Db.RoomMemberTable.COLUMN_LAST_READ, memberStateEntity.lastReadMessageId)
    }
}

fun Cursor.toRoomMemberEntity(): RoomMemberEntity {
    return RoomMemberEntity(
            UserEntity(stringValue(Db.RoomMemberTable.COLUMN_USER_ID)),
            MemberStateEntity(stringValue(Db.RoomMemberTable.COLUMN_LAST_DELIVERED),
                    stringValue(Db.RoomMemberTable.COLUMN_LAST_READ))
    )
}

internal fun Cursor.getRoomId(): Long {
    return longValue(Db.RoomMemberTable.COLUMN_ROOM_ID)
}

internal fun Cursor.getUserId(): String {
    return stringValue(Db.RoomMemberTable.COLUMN_USER_ID)
}