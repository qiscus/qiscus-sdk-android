package com.qiscus.sdk.chat.data.local.mapper

import android.content.ContentValues
import android.database.Cursor
import com.qiscus.sdk.chat.data.local.database.Db
import com.qiscus.sdk.chat.data.local.database.stringValue

/**
 * Created on : September 21, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
internal fun toContentValues(commentUniqueId: String, localPath: String): ContentValues {
    return ContentValues().apply {
        put(Db.FileTable.COLUMN_COMMENT_UNIQUE_ID, commentUniqueId)
        put(Db.FileTable.COLUMN_LOCAL_PATH, localPath)
    }
}

internal fun Cursor.getLocalPath(): String {
    return stringValue(Db.FileTable.COLUMN_LOCAL_PATH)
}