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
        put(Db.RoomMemberTable.COLUMN_LAST_DELIVERED, memberStateEntity.lastDeliveredCommentId)
        put(Db.RoomMemberTable.COLUMN_LAST_READ, memberStateEntity.lastReadCommentId)
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