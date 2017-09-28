package com.qiscus.sdk.chat.data.local.mapper

import android.content.ContentValues
import android.database.Cursor
import com.qiscus.sdk.chat.data.local.database.Db
import com.qiscus.sdk.chat.data.local.database.intValue
import com.qiscus.sdk.chat.data.local.database.longValue
import com.qiscus.sdk.chat.data.local.database.stringValue
import com.qiscus.sdk.chat.data.mapper.transformToTypedCommentEntity
import com.qiscus.sdk.chat.data.model.*
import org.json.JSONObject
import java.util.*

/**
 * Created on : September 05, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
fun CommentEntity.toContentValues(): ContentValues {
    return ContentValues().apply {
        put(Db.CommentTable.COLUMN_ID, commentId.id)
        put(Db.CommentTable.COLUMN_ROOM_ID, room.id)
        put(Db.CommentTable.COLUMN_UNIQUE_ID, commentId.uniqueId)
        put(Db.CommentTable.COLUMN_COMMENT_BEFORE_ID, commentId.commentBeforeId)
        put(Db.CommentTable.COLUMN_MESSAGE, message)
        put(Db.CommentTable.COLUMN_SENDER_ID, sender.id)
        put(Db.CommentTable.COLUMN_SENDER_NAME, sender.name)
        put(Db.CommentTable.COLUMN_SENDER_AVATAR, sender.avatar)
        put(Db.CommentTable.COLUMN_TIME, nanoTimeStamp)
        put(Db.CommentTable.COLUMN_STATE, state.intValue)
        put(Db.CommentTable.COLUMN_TYPE, type.rawType)
        put(Db.CommentTable.COLUMN_PAYLOAD, type.payload.toString())
    }
}

fun Cursor.toCommentEntity(): CommentEntity {
    return CommentEntity(
            CommentIdEntity(stringValue(Db.CommentTable.COLUMN_ID),
                    stringValue(Db.CommentTable.COLUMN_COMMENT_BEFORE_ID),
                    stringValue(Db.CommentTable.COLUMN_UNIQUE_ID)),
            stringValue(Db.CommentTable.COLUMN_MESSAGE),
            UserEntity(stringValue(Db.CommentTable.COLUMN_SENDER_ID),
                    stringValue(Db.CommentTable.COLUMN_SENDER_NAME),
                    stringValue(Db.CommentTable.COLUMN_SENDER_AVATAR)),
            longValue(Db.CommentTable.COLUMN_TIME),
            RoomEntity(stringValue(Db.CommentTable.COLUMN_ROOM_ID), name = "Unknown"),
            CommentStateEntity.valueOf(intValue(Db.CommentTable.COLUMN_STATE)),
            CommentTypeEntity(stringValue(Db.CommentTable.COLUMN_TYPE),
                    JSONObject(stringValue(Db.CommentTable.COLUMN_PAYLOAD)))
    ).transformToTypedCommentEntity()
}