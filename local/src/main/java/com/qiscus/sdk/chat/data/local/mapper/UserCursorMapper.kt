package com.qiscus.sdk.chat.data.local.mapper

import android.content.ContentValues
import android.database.Cursor
import com.qiscus.sdk.chat.data.local.database.Db
import com.qiscus.sdk.chat.data.local.database.stringValue
import com.qiscus.sdk.chat.data.model.UserEntity

/**
 * Created on : September 21, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
fun UserEntity.toContentValues(): ContentValues {
    return ContentValues().apply {
        put(Db.UserTable.COLUMN_USER_ID, id)
        put(Db.UserTable.COLUMN_USER_NAME, name)
        put(Db.UserTable.COLUMN_USER_AVATAR, avatar)
    }
}

fun Cursor.toUserEntity(): UserEntity {
    return UserEntity(
            stringValue(Db.UserTable.COLUMN_USER_ID),
            stringValue(Db.UserTable.COLUMN_USER_NAME),
            stringValue(Db.UserTable.COLUMN_USER_AVATAR)
    )
}