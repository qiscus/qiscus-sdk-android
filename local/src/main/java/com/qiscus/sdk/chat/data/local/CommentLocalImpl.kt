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

package com.qiscus.sdk.chat.data.local

import android.database.DatabaseUtils
import com.qiscus.sdk.chat.data.local.database.Db
import com.qiscus.sdk.chat.data.local.database.DbOpenHelper
import com.qiscus.sdk.chat.data.local.database.transaction
import com.qiscus.sdk.chat.data.local.mapper.toCommentEntity
import com.qiscus.sdk.chat.data.local.mapper.toContentValues
import com.qiscus.sdk.chat.data.model.CommentEntity
import com.qiscus.sdk.chat.data.model.CommentIdEntity
import com.qiscus.sdk.chat.data.model.CommentStateEntity
import com.qiscus.sdk.chat.data.model.FileAttachmentCommentEntity
import com.qiscus.sdk.chat.data.pubsub.comment.CommentPublisher
import com.qiscus.sdk.chat.data.source.account.AccountLocal
import com.qiscus.sdk.chat.data.source.comment.CommentLocal
import com.qiscus.sdk.chat.data.source.file.FileLocal
import com.qiscus.sdk.chat.data.source.room.RoomLocal
import com.qiscus.sdk.chat.data.source.user.UserLocal


/**
 * Created on : August 19, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
class CommentLocalImpl(dbOpenHelper: DbOpenHelper,
                       private val accountLocal: AccountLocal,
                       private val roomLocal: RoomLocal,
                       private val userLocal: UserLocal,
                       private val fileLocal: FileLocal,
                       private val commentPublisher: CommentPublisher) : CommentLocal {

    private val database = dbOpenHelper.readableDatabase

    override fun addComment(commentEntity: CommentEntity) {
        if (!isExistComment(commentEntity.commentId)) {
            database.transaction {
                determineCommentState(commentEntity)
                database.insert(Db.CommentTable.TABLE_NAME, null, commentEntity.toContentValues())
            }
        }
    }

    private fun determineCommentState(commentEntity: CommentEntity) {
        if (commentEntity.state.intValue >= CommentStateEntity.ON_SERVER.intValue
                && commentEntity.state.intValue < CommentStateEntity.READ.intValue) {
            val lastRead = getLastReadCommentId(commentEntity.room.id)
            if (lastRead != null && commentEntity.commentId.id <= lastRead.id) {
                commentEntity.state = CommentStateEntity.READ
            } else {
                val lastDelivered = getLastDeliveredCommentId(commentEntity.room.id)
                if (lastDelivered != null && commentEntity.commentId.id <= lastDelivered.id) {
                    commentEntity.state = CommentStateEntity.DELIVERED
                }
            }
        }
    }

    override fun saveAndNotify(commentEntity: CommentEntity) {
        if (commentEntity is FileAttachmentCommentEntity && commentEntity.file == null) {
            commentEntity.file = fileLocal.getLocalPath(commentEntity.commentId)
        }

        val oldComment = getComment(commentEntity.commentId)
        if (oldComment != null) {
            if (oldComment != commentEntity && (commentEntity.state.intValue >= oldComment.state.intValue
                    || commentEntity.state.intValue <= CommentStateEntity.SENDING.intValue)) {
                val where = Db.CommentTable.COLUMN_UNIQUE_ID + " = " + DatabaseUtils.sqlEscapeString(commentEntity.commentId.uniqueId)
                database.transaction {
                    determineCommentState(commentEntity)
                    database.update(Db.CommentTable.TABLE_NAME, commentEntity.toContentValues(), where, null)
                    commentPublisher.onCommentUpdated(commentEntity)
                }
            }
        } else {
            database.transaction {
                determineCommentState(commentEntity)
                database.insert(Db.CommentTable.TABLE_NAME, null, commentEntity.toContentValues())
                commentPublisher.onCommentAdded(commentEntity)
            }
        }
    }

    override fun updateComment(commentEntity: CommentEntity) {
        if (commentEntity is FileAttachmentCommentEntity && commentEntity.file == null) {
            commentEntity.file = fileLocal.getLocalPath(commentEntity.commentId)
        }

        val oldComment = getComment(commentEntity.commentId)
        if (oldComment != null) {
            if (oldComment != commentEntity && (commentEntity.state.intValue >= oldComment.state.intValue
                    || commentEntity.state.intValue <= CommentStateEntity.SENDING.intValue)) {
                val where = Db.CommentTable.COLUMN_UNIQUE_ID + " = " + DatabaseUtils.sqlEscapeString(commentEntity.commentId.uniqueId)
                database.transaction {
                    determineCommentState(commentEntity)
                    database.update(Db.CommentTable.TABLE_NAME, commentEntity.toContentValues(), where, null)
                    commentPublisher.onCommentUpdated(commentEntity)
                }
            }
        }
    }

    override fun addOrUpdateComment(commentEntity: CommentEntity) {
        if (commentEntity is FileAttachmentCommentEntity && commentEntity.file == null) {
            commentEntity.file = fileLocal.getLocalPath(commentEntity.commentId)
        }

        val oldComment = getComment(commentEntity.commentId)
        if (oldComment != null) {
            if (oldComment != commentEntity && (commentEntity.state.intValue >= oldComment.state.intValue
                    || commentEntity.state.intValue <= CommentStateEntity.SENDING.intValue)) {
                val where = Db.CommentTable.COLUMN_UNIQUE_ID + " = " + DatabaseUtils.sqlEscapeString(commentEntity.commentId.uniqueId)
                database.transaction {
                    determineCommentState(commentEntity)
                    database.update(Db.CommentTable.TABLE_NAME, commentEntity.toContentValues(), where, null)
                    commentPublisher.onCommentUpdated(commentEntity)
                }
            }
        } else {
            database.transaction {
                determineCommentState(commentEntity)
                database.insert(Db.CommentTable.TABLE_NAME, null, commentEntity.toContentValues())
            }
        }
    }

    override fun deleteComment(commentEntity: CommentEntity) {
        if (!isExistComment(commentEntity.commentId)) {
            val where = Db.CommentTable.COLUMN_UNIQUE_ID + " = " + DatabaseUtils.sqlEscapeString(commentEntity.commentId.uniqueId)
            database.transaction {
                database.delete(Db.CommentTable.TABLE_NAME, where, null)
                commentPublisher.onCommentDeleted(commentEntity)
            }
        }
    }

    override fun getComment(commentIdEntity: CommentIdEntity): CommentEntity? {
        val query = "SELECT * FROM ${Db.CommentTable.TABLE_NAME} WHERE ${Db.CommentTable.COLUMN_UNIQUE_ID} " +
                "= ${DatabaseUtils.sqlEscapeString(commentIdEntity.uniqueId)}"
        val cursor = database.rawQuery(query, null)

        if (cursor.moveToNext()) {
            val comment = cursor.toCommentEntity()
            val sender = userLocal.getUser(comment.sender.id)
            if (sender != null) {
                comment.sender = sender
            }

            val room = roomLocal.getRoom(comment.room.id)
            if (room != null) {
                comment.room = room
            }

            if (comment is FileAttachmentCommentEntity) {
                comment.file = fileLocal.getLocalPath(comment.commentId)
            }

            cursor.close()
            return comment
        }

        cursor.close()
        return null
    }

    override fun getComments(roomId: String, limit: Int): List<CommentEntity> {
        var query = "SELECT * FROM " + Db.CommentTable.TABLE_NAME + " WHERE " + Db.CommentTable.COLUMN_ROOM_ID +
                " = " + DatabaseUtils.sqlEscapeString(roomId) + " ORDER BY " + Db.CommentTable.COLUMN_TIME + " DESC"

        if (limit != -1) {
            query += " LIMIT " + limit
        }

        val cursor = database.rawQuery(query, null)
        val comments = ArrayList<CommentEntity>()
        while (cursor.moveToNext()) {
            val comment = cursor.toCommentEntity()
            val sender = userLocal.getUser(comment.sender.id)
            if (sender != null) {
                comment.sender = sender
            }

            val room = roomLocal.getRoom(comment.room.id)
            if (room != null) {
                comment.room = room
            }

            if (comment is FileAttachmentCommentEntity) {
                comment.file = fileLocal.getLocalPath(comment.commentId)
            }

            comments.add(comment)
        }
        cursor.close()
        return comments
    }

    override fun getComments(roomId: String, lastCommentIdEntity: CommentIdEntity, limit: Int): List<CommentEntity> {
        val lastComment = getComment(lastCommentIdEntity) ?: return arrayListOf()

        var query = "SELECT * FROM " + Db.CommentTable.TABLE_NAME + " WHERE " +
                Db.CommentTable.COLUMN_ROOM_ID + " = " + DatabaseUtils.sqlEscapeString(roomId) + " AND " +
                Db.CommentTable.COLUMN_TIME + " <= " + lastComment.nanoTimeStamp +
                " ORDER BY " + Db.CommentTable.COLUMN_TIME + " DESC"

        if (limit != -1) {
            query += " LIMIT " + limit
        }

        val cursor = database.rawQuery(query, null)
        val comments = ArrayList<CommentEntity>()
        while (cursor.moveToNext()) {
            val comment = cursor.toCommentEntity()
            val sender = userLocal.getUser(comment.sender.id)
            if (sender != null) {
                comment.sender = sender
            }

            val room = roomLocal.getRoom(comment.room.id)
            if (room != null) {
                comment.room = room
            }

            if (comment is FileAttachmentCommentEntity) {
                comment.file = fileLocal.getLocalPath(comment.commentId)
            }

            comments.add(comment)
        }
        cursor.close()

        return comments
    }

    override fun getPendingComments(): List<CommentEntity> {
        val query = "SELECT * FROM " + Db.CommentTable.TABLE_NAME + " WHERE " +
                Db.CommentTable.COLUMN_STATE + " >= " + CommentStateEntity.PENDING.intValue + " AND " +
                Db.CommentTable.COLUMN_STATE + " <= " + CommentStateEntity.SENDING.intValue +
                " ORDER BY " + Db.CommentTable.COLUMN_TIME + " ASC"
        val cursor = database.rawQuery(query, null)
        val comments = ArrayList<CommentEntity>()
        while (cursor.moveToNext()) {
            val comment = cursor.toCommentEntity()
            val sender = userLocal.getUser(comment.sender.id)
            if (sender != null) {
                comment.sender = sender
            }

            val room = roomLocal.getRoom(comment.room.id)
            if (room != null) {
                comment.room = room
            }

            if (comment is FileAttachmentCommentEntity) {
                comment.file = fileLocal.getLocalPath(comment.commentId)
            }

            comments.add(comment)
        }
        cursor.close()
        return comments
    }

    override fun updateLastDeliveredComment(roomId: String, userId: String, commentId: CommentIdEntity) {
        getCommentsNeedUpdateDeliverState(roomId, commentId).forEach {
            it.state = CommentStateEntity.DELIVERED
            val where = Db.CommentTable.COLUMN_UNIQUE_ID + " = " + DatabaseUtils.sqlEscapeString(it.commentId.uniqueId)
            database.transaction {
                database.update(Db.CommentTable.TABLE_NAME, it.toContentValues(), where, null)
                commentPublisher.onCommentUpdated(it)
            }
        }

        roomLocal.updateRoomMemberDeliveredState(roomId, userId, commentId)
    }

    override fun updateLastReadComment(roomId: String, userId: String, commentId: CommentIdEntity) {
        getCommentsNeedUpdateReadState(roomId, commentId).forEach {
            it.state = CommentStateEntity.READ
            val where = Db.CommentTable.COLUMN_UNIQUE_ID + " = " + DatabaseUtils.sqlEscapeString(it.commentId.uniqueId)
            database.transaction {
                database.update(Db.CommentTable.TABLE_NAME, it.toContentValues(), where, null)
                commentPublisher.onCommentUpdated(it)
            }
        }

        roomLocal.updateRoomMemberReadState(roomId, userId, commentId)
    }

    private fun getCommentsNeedUpdateDeliverState(roomId: String, commentId: CommentIdEntity): List<CommentEntity> {
        val lastComment = getComment(commentId) ?: return arrayListOf()

        val query = "SELECT * FROM " + Db.CommentTable.TABLE_NAME + " WHERE " +
                Db.CommentTable.COLUMN_ROOM_ID + " = " + DatabaseUtils.sqlEscapeString(roomId) + " AND " +
                Db.CommentTable.COLUMN_STATE + " = " + CommentStateEntity.ON_SERVER.intValue + " AND " +
                Db.CommentTable.COLUMN_TIME + " <= " + lastComment.nanoTimeStamp +
                " ORDER BY " + Db.CommentTable.COLUMN_TIME + " DESC"

        val cursor = database.rawQuery(query, null)
        val comments = ArrayList<CommentEntity>()
        while (cursor.moveToNext()) {
            val comment = cursor.toCommentEntity()
            val sender = userLocal.getUser(comment.sender.id)
            if (sender != null) {
                comment.sender = sender
            }

            val room = roomLocal.getRoom(comment.room.id)
            if (room != null) {
                comment.room = room
            }

            if (comment is FileAttachmentCommentEntity) {
                comment.file = fileLocal.getLocalPath(comment.commentId)
            }

            comments.add(comment)
        }
        cursor.close()

        return comments
    }

    private fun getCommentsNeedUpdateReadState(roomId: String, commentId: CommentIdEntity): List<CommentEntity> {
        val lastComment = getComment(commentId) ?: return arrayListOf()

        val query = "SELECT * FROM " + Db.CommentTable.TABLE_NAME + " WHERE " +
                Db.CommentTable.COLUMN_ROOM_ID + " = " + DatabaseUtils.sqlEscapeString(roomId) + " AND " +
                Db.CommentTable.COLUMN_STATE + " >= " + CommentStateEntity.ON_SERVER.intValue + " AND " +
                Db.CommentTable.COLUMN_STATE + " < " + CommentStateEntity.READ.intValue + " AND " +
                Db.CommentTable.COLUMN_TIME + " <= " + lastComment.nanoTimeStamp +
                " ORDER BY " + Db.CommentTable.COLUMN_TIME + " DESC"

        val cursor = database.rawQuery(query, null)
        val comments = ArrayList<CommentEntity>()
        while (cursor.moveToNext()) {
            val comment = cursor.toCommentEntity()
            val sender = userLocal.getUser(comment.sender.id)
            if (sender != null) {
                comment.sender = sender
            }

            val room = roomLocal.getRoom(comment.room.id)
            if (room != null) {
                comment.room = room
            }

            if (comment is FileAttachmentCommentEntity) {
                comment.file = fileLocal.getLocalPath(comment.commentId)
            }

            comments.add(comment)
        }
        cursor.close()

        return comments
    }

    override fun getLastOnServerCommentId(): CommentIdEntity? {
        val query = "SELECT * FROM " + Db.CommentTable.TABLE_NAME + " WHERE " +
                Db.CommentTable.COLUMN_STATE + " >= " + CommentStateEntity.ON_SERVER.intValue +
                " ORDER BY " + Db.CommentTable.COLUMN_TIME + " DESC LIMIT 1"

        val cursor = database.rawQuery(query, null)

        if (cursor.moveToNext()) {
            val comment = cursor.toCommentEntity()
            cursor.close()
            return comment.commentId
        }

        cursor.close()
        return null
    }

    override fun getLastDeliveredCommentId(roomId: String): CommentIdEntity? {
        val query = "SELECT * FROM " + Db.CommentTable.TABLE_NAME + " WHERE " +
                Db.CommentTable.COLUMN_ROOM_ID + " = " + DatabaseUtils.sqlEscapeString(roomId) + " AND " +
                Db.CommentTable.COLUMN_STATE + " == " + CommentStateEntity.DELIVERED.intValue + " AND " +
                Db.CommentTable.COLUMN_SENDER_ID + " == " + DatabaseUtils.sqlEscapeString(accountLocal.getAccount().user.id) +
                " ORDER BY " + Db.CommentTable.COLUMN_TIME + " DESC LIMIT 1"

        val cursor = database.rawQuery(query, null)

        if (cursor.moveToNext()) {
            val comment = cursor.toCommentEntity()
            cursor.close()
            return comment.commentId
        }

        cursor.close()
        return null
    }

    override fun getLastReadCommentId(roomId: String): CommentIdEntity? {
        val query = "SELECT * FROM " + Db.CommentTable.TABLE_NAME + " WHERE " +
                Db.CommentTable.COLUMN_ROOM_ID + " = " + DatabaseUtils.sqlEscapeString(roomId) + " AND " +
                Db.CommentTable.COLUMN_STATE + " == " + CommentStateEntity.READ.intValue + " AND " +
                Db.CommentTable.COLUMN_SENDER_ID + " == " + DatabaseUtils.sqlEscapeString(accountLocal.getAccount().user.id) +
                " ORDER BY " + Db.CommentTable.COLUMN_TIME + " DESC LIMIT 1"

        val cursor = database.rawQuery(query, null)

        if (cursor.moveToNext()) {
            val comment = cursor.toCommentEntity()
            cursor.close()
            return comment.commentId
        }

        cursor.close()
        return null
    }

    override fun getOnServerComments(roomId: String, lastCommentIdEntity: CommentIdEntity, limit: Int): List<CommentEntity> {
        val lastComment = getComment(lastCommentIdEntity) ?: return arrayListOf()

        val query = "SELECT * FROM " + Db.CommentTable.TABLE_NAME + " WHERE " +
                Db.CommentTable.COLUMN_ROOM_ID + " = " + DatabaseUtils.sqlEscapeString(roomId) + " AND " +
                Db.CommentTable.COLUMN_TIME + " <= " + lastComment.nanoTimeStamp +
                Db.CommentTable.COLUMN_STATE + " >= " + CommentStateEntity.ON_SERVER.intValue +
                " ORDER BY " + Db.CommentTable.COLUMN_TIME + " DESC LIMIT $limit"

        val cursor = database.rawQuery(query, null)
        val comments = ArrayList<CommentEntity>()
        while (cursor.moveToNext()) {
            comments.add(cursor.toCommentEntity())
        }
        cursor.close()
        return comments
    }

    override fun clearData() {
        database.transaction {
            database.delete(Db.CommentTable.TABLE_NAME, null, null)
        }
    }

    private fun isExistComment(commentIdEntity: CommentIdEntity): Boolean {
        val query = "SELECT * FROM ${Db.CommentTable.TABLE_NAME} WHERE ${Db.CommentTable.COLUMN_UNIQUE_ID} " +
                "= ${DatabaseUtils.sqlEscapeString(commentIdEntity.uniqueId)}"
        val cursor = database.rawQuery(query, null)
        val contains = cursor.count > 0
        cursor.close()
        return contains
    }
}