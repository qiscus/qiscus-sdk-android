package com.qiscus.sdk.chat.data.local

import android.database.DatabaseUtils
import com.qiscus.sdk.chat.data.local.database.Db
import com.qiscus.sdk.chat.data.local.database.DbOpenHelper
import com.qiscus.sdk.chat.data.local.database.transaction
import com.qiscus.sdk.chat.data.local.mapper.getLocalPath
import com.qiscus.sdk.chat.data.local.mapper.toContentValues
import com.qiscus.sdk.chat.data.model.CommentIdEntity
import com.qiscus.sdk.chat.data.source.file.FileLocal
import java.io.File

/**
 * Created on : September 26, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
class FileLocalImpl(dbOpenHelper: DbOpenHelper) : FileLocal {

    private val database = dbOpenHelper.readableDatabase

    override fun saveLocalPath(commentIdEntity: CommentIdEntity, file: File) {
        val oldLocalPath = getLocalPath(commentIdEntity)
        if (oldLocalPath != null && oldLocalPath != file) {
            val where = Db.FileTable.COLUMN_COMMENT_UNIQUE_ID + " = " + DatabaseUtils.sqlEscapeString(commentIdEntity.uniqueId)
            database.transaction {
                database.update(Db.FileTable.TABLE_NAME,
                        toContentValues(commentIdEntity.uniqueId, file.absolutePath), where, null)
            }
        } else if (oldLocalPath == null) {
            database.transaction {
                database.insert(Db.FileTable.TABLE_NAME, null,
                        toContentValues(commentIdEntity.uniqueId, file.absolutePath))
            }
        }
    }

    override fun getLocalPath(commentIdEntity: CommentIdEntity): File? {
        val query = "SELECT * FROM ${Db.FileTable.TABLE_NAME} WHERE ${Db.FileTable.COLUMN_COMMENT_UNIQUE_ID} " +
                "= ${DatabaseUtils.sqlEscapeString(commentIdEntity.uniqueId)}"
        val cursor = database.rawQuery(query, null)

        if (cursor.moveToNext()) {
            val file = File(cursor.getLocalPath())
            cursor.close()
            if (file.exists()) {
                return file
            }
            return null
        }

        cursor.close()
        return null
    }
}