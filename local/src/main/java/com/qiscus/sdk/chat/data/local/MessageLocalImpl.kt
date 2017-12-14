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
import com.qiscus.sdk.chat.data.local.mapper.toMessageEntity
import com.qiscus.sdk.chat.data.local.mapper.toContentValues
import com.qiscus.sdk.chat.data.model.MessageEntity
import com.qiscus.sdk.chat.data.model.MessageIdEntity
import com.qiscus.sdk.chat.data.model.MessageStateEntity
import com.qiscus.sdk.chat.data.model.FileAttachmentMessageEntity
import com.qiscus.sdk.chat.data.pubsub.message.MessagePublisher
import com.qiscus.sdk.chat.data.source.account.AccountLocal
import com.qiscus.sdk.chat.data.source.message.MessageLocal
import com.qiscus.sdk.chat.data.source.file.FileLocal
import com.qiscus.sdk.chat.data.source.room.RoomLocal
import com.qiscus.sdk.chat.data.source.user.UserLocal


/**
 * Created on : August 19, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
class MessageLocalImpl(dbOpenHelper: DbOpenHelper,
                       private val accountLocal: AccountLocal,
                       private val roomLocal: RoomLocal,
                       private val userLocal: UserLocal,
                       private val fileLocal: FileLocal,
                       private val messagePublisher: MessagePublisher) : MessageLocal {

    private val database = dbOpenHelper.readableDatabase

    override fun addMessage(messageEntity: MessageEntity) {
        if (!isExistMessage(messageEntity.messageId)) {
            database.transaction {
                determineMessageState(messageEntity)
                database.insert(Db.MessageTable.TABLE_NAME, null, messageEntity.toContentValues())
            }
        }
    }

    private fun determineMessageState(messageEntity: MessageEntity) {
        if (messageEntity.state.intValue >= MessageStateEntity.ON_SERVER.intValue
                && messageEntity.state.intValue < MessageStateEntity.READ.intValue) {
            val lastRead = getLastReadMessageId(messageEntity.room.id)
            if (lastRead != null && messageEntity.messageId.id <= lastRead.id) {
                messageEntity.state = MessageStateEntity.READ
            } else {
                val lastDelivered = getLastDeliveredMessageId(messageEntity.room.id)
                if (lastDelivered != null && messageEntity.messageId.id <= lastDelivered.id) {
                    messageEntity.state = MessageStateEntity.DELIVERED
                }
            }
        }
    }

    override fun saveAndNotify(messageEntity: MessageEntity) {
        if (messageEntity is FileAttachmentMessageEntity && messageEntity.file == null) {
            messageEntity.file = fileLocal.getLocalPath(messageEntity.messageId)
        }

        val oldMessage = getMessage(messageEntity.messageId)
        if (oldMessage != null) {
            if (oldMessage != messageEntity && (messageEntity.state.intValue >= oldMessage.state.intValue
                    || messageEntity.state.intValue <= MessageStateEntity.SENDING.intValue)) {
                val where = Db.MessageTable.COLUMN_UNIQUE_ID + " = " + DatabaseUtils.sqlEscapeString(messageEntity.messageId.uniqueId)
                database.transaction {
                    determineMessageState(messageEntity)
                    database.update(Db.MessageTable.TABLE_NAME, messageEntity.toContentValues(), where, null)
                    messagePublisher.onMessageUpdated(messageEntity)
                }
            }
        } else {
            database.transaction {
                determineMessageState(messageEntity)
                database.insert(Db.MessageTable.TABLE_NAME, null, messageEntity.toContentValues())
                messagePublisher.onMessageAdded(messageEntity)
            }
        }
    }

    override fun updateMessage(messageEntity: MessageEntity) {
        if (messageEntity is FileAttachmentMessageEntity && messageEntity.file == null) {
            messageEntity.file = fileLocal.getLocalPath(messageEntity.messageId)
        }

        val oldMessage = getMessage(messageEntity.messageId)
        if (oldMessage != null) {
            if (oldMessage != messageEntity && (messageEntity.state.intValue >= oldMessage.state.intValue
                    || messageEntity.state.intValue <= MessageStateEntity.SENDING.intValue)) {
                val where = Db.MessageTable.COLUMN_UNIQUE_ID + " = " + DatabaseUtils.sqlEscapeString(messageEntity.messageId.uniqueId)
                database.transaction {
                    determineMessageState(messageEntity)
                    database.update(Db.MessageTable.TABLE_NAME, messageEntity.toContentValues(), where, null)
                    messagePublisher.onMessageUpdated(messageEntity)
                }
            }
        }
    }

    override fun addOrUpdateMessage(messageEntity: MessageEntity) {
        if (messageEntity is FileAttachmentMessageEntity && messageEntity.file == null) {
            messageEntity.file = fileLocal.getLocalPath(messageEntity.messageId)
        }

        val oldMessage = getMessage(messageEntity.messageId)
        if (oldMessage != null) {
            if (oldMessage != messageEntity && (messageEntity.state.intValue >= oldMessage.state.intValue
                    || messageEntity.state.intValue <= MessageStateEntity.SENDING.intValue)) {
                val where = Db.MessageTable.COLUMN_UNIQUE_ID + " = " + DatabaseUtils.sqlEscapeString(messageEntity.messageId.uniqueId)
                database.transaction {
                    determineMessageState(messageEntity)
                    database.update(Db.MessageTable.TABLE_NAME, messageEntity.toContentValues(), where, null)
                    messagePublisher.onMessageUpdated(messageEntity)
                }
            }
        } else {
            database.transaction {
                determineMessageState(messageEntity)
                database.insert(Db.MessageTable.TABLE_NAME, null, messageEntity.toContentValues())
            }
        }
    }

    override fun deleteMessage(messageEntity: MessageEntity) {
        if (!isExistMessage(messageEntity.messageId)) {
            val where = Db.MessageTable.COLUMN_UNIQUE_ID + " = " + DatabaseUtils.sqlEscapeString(messageEntity.messageId.uniqueId)
            database.transaction {
                database.delete(Db.MessageTable.TABLE_NAME, where, null)
                messagePublisher.onMessageDeleted(messageEntity)
            }
        }
    }

    override fun getMessage(messageIdEntity: MessageIdEntity): MessageEntity? {
        val query = "SELECT * FROM ${Db.MessageTable.TABLE_NAME} WHERE ${Db.MessageTable.COLUMN_UNIQUE_ID} " +
                "= ${DatabaseUtils.sqlEscapeString(messageIdEntity.uniqueId)}"
        val cursor = database.rawQuery(query, null)

        if (cursor.moveToNext()) {
            val message = cursor.toMessageEntity()
            val sender = userLocal.getUser(message.sender.id)
            if (sender != null) {
                message.sender = sender
            }

            val room = roomLocal.getRoom(message.room.id)
            if (room != null) {
                message.room = room
            }

            if (message is FileAttachmentMessageEntity) {
                message.file = fileLocal.getLocalPath(message.messageId)
            }

            cursor.close()
            return message
        }

        cursor.close()
        return null
    }

    override fun getMessages(roomId: String, limit: Int): List<MessageEntity> {
        var query = "SELECT * FROM " + Db.MessageTable.TABLE_NAME + " WHERE " + Db.MessageTable.COLUMN_ROOM_ID +
                " = " + DatabaseUtils.sqlEscapeString(roomId) + " ORDER BY " + Db.MessageTable.COLUMN_TIME + " DESC"

        if (limit != -1) {
            query += " LIMIT " + limit
        }

        val cursor = database.rawQuery(query, null)
        val messages = ArrayList<MessageEntity>()
        while (cursor.moveToNext()) {
            val message = cursor.toMessageEntity()
            val sender = userLocal.getUser(message.sender.id)
            if (sender != null) {
                message.sender = sender
            }

            val room = roomLocal.getRoom(message.room.id)
            if (room != null) {
                message.room = room
            }

            if (message is FileAttachmentMessageEntity) {
                message.file = fileLocal.getLocalPath(message.messageId)
            }

            messages.add(message)
        }
        cursor.close()
        return messages
    }

    override fun getMessages(roomId: String, lastMessageIdEntity: MessageIdEntity, limit: Int): List<MessageEntity> {
        val lastMessage = getMessage(lastMessageIdEntity) ?: return arrayListOf()

        var query = "SELECT * FROM " + Db.MessageTable.TABLE_NAME + " WHERE " +
                Db.MessageTable.COLUMN_ROOM_ID + " = " + DatabaseUtils.sqlEscapeString(roomId) + " AND " +
                Db.MessageTable.COLUMN_TIME + " <= " + lastMessage.nanoTimeStamp +
                " ORDER BY " + Db.MessageTable.COLUMN_TIME + " DESC"

        if (limit != -1) {
            query += " LIMIT " + limit
        }

        val cursor = database.rawQuery(query, null)
        val messages = ArrayList<MessageEntity>()
        while (cursor.moveToNext()) {
            val message = cursor.toMessageEntity()
            val sender = userLocal.getUser(message.sender.id)
            if (sender != null) {
                message.sender = sender
            }

            val room = roomLocal.getRoom(message.room.id)
            if (room != null) {
                message.room = room
            }

            if (message is FileAttachmentMessageEntity) {
                message.file = fileLocal.getLocalPath(message.messageId)
            }

            messages.add(message)
        }
        cursor.close()

        return messages
    }

    override fun getPendingMessages(): List<MessageEntity> {
        val query = "SELECT * FROM " + Db.MessageTable.TABLE_NAME + " WHERE " +
                Db.MessageTable.COLUMN_STATE + " >= " + MessageStateEntity.PENDING.intValue + " AND " +
                Db.MessageTable.COLUMN_STATE + " <= " + MessageStateEntity.SENDING.intValue +
                " ORDER BY " + Db.MessageTable.COLUMN_TIME + " ASC"
        val cursor = database.rawQuery(query, null)
        val messages = ArrayList<MessageEntity>()
        while (cursor.moveToNext()) {
            val message = cursor.toMessageEntity()
            val sender = userLocal.getUser(message.sender.id)
            if (sender != null) {
                message.sender = sender
            }

            val room = roomLocal.getRoom(message.room.id)
            if (room != null) {
                message.room = room
            }

            if (message is FileAttachmentMessageEntity) {
                message.file = fileLocal.getLocalPath(message.messageId)
            }

            messages.add(message)
        }
        cursor.close()
        return messages
    }

    override fun updateLastDeliveredMessage(roomId: String, userId: String, messageId: MessageIdEntity) {
        getMessagesNeedUpdateDeliverState(roomId, messageId).forEach {
            it.state = MessageStateEntity.DELIVERED
            val where = Db.MessageTable.COLUMN_UNIQUE_ID + " = " + DatabaseUtils.sqlEscapeString(it.messageId.uniqueId)
            database.transaction {
                database.update(Db.MessageTable.TABLE_NAME, it.toContentValues(), where, null)
                messagePublisher.onMessageUpdated(it)
            }
        }

        roomLocal.updateParticipantDeliveredState(roomId, userId, messageId)
    }

    override fun updateLastReadMessage(roomId: String, userId: String, messageId: MessageIdEntity) {
        getMessagesNeedUpdateReadState(roomId, messageId).forEach {
            it.state = MessageStateEntity.READ
            val where = Db.MessageTable.COLUMN_UNIQUE_ID + " = " + DatabaseUtils.sqlEscapeString(it.messageId.uniqueId)
            database.transaction {
                database.update(Db.MessageTable.TABLE_NAME, it.toContentValues(), where, null)
                messagePublisher.onMessageUpdated(it)
            }
        }

        roomLocal.updateParticipantReadState(roomId, userId, messageId)
    }

    private fun getMessagesNeedUpdateDeliverState(roomId: String, messageId: MessageIdEntity): List<MessageEntity> {
        val lastMessage = getMessage(messageId) ?: return arrayListOf()

        val query = "SELECT * FROM " + Db.MessageTable.TABLE_NAME + " WHERE " +
                Db.MessageTable.COLUMN_ROOM_ID + " = " + DatabaseUtils.sqlEscapeString(roomId) + " AND " +
                Db.MessageTable.COLUMN_STATE + " = " + MessageStateEntity.ON_SERVER.intValue + " AND " +
                Db.MessageTable.COLUMN_TIME + " <= " + lastMessage.nanoTimeStamp +
                " ORDER BY " + Db.MessageTable.COLUMN_TIME + " DESC"

        val cursor = database.rawQuery(query, null)
        val messages = ArrayList<MessageEntity>()
        while (cursor.moveToNext()) {
            val message = cursor.toMessageEntity()
            val sender = userLocal.getUser(message.sender.id)
            if (sender != null) {
                message.sender = sender
            }

            val room = roomLocal.getRoom(message.room.id)
            if (room != null) {
                message.room = room
            }

            if (message is FileAttachmentMessageEntity) {
                message.file = fileLocal.getLocalPath(message.messageId)
            }

            messages.add(message)
        }
        cursor.close()

        return messages
    }

    private fun getMessagesNeedUpdateReadState(roomId: String, messageId: MessageIdEntity): List<MessageEntity> {
        val lastMessage = getMessage(messageId) ?: return arrayListOf()

        val query = "SELECT * FROM " + Db.MessageTable.TABLE_NAME + " WHERE " +
                Db.MessageTable.COLUMN_ROOM_ID + " = " + DatabaseUtils.sqlEscapeString(roomId) + " AND " +
                Db.MessageTable.COLUMN_STATE + " >= " + MessageStateEntity.ON_SERVER.intValue + " AND " +
                Db.MessageTable.COLUMN_STATE + " < " + MessageStateEntity.READ.intValue + " AND " +
                Db.MessageTable.COLUMN_TIME + " <= " + lastMessage.nanoTimeStamp +
                " ORDER BY " + Db.MessageTable.COLUMN_TIME + " DESC"

        val cursor = database.rawQuery(query, null)
        val messages = ArrayList<MessageEntity>()
        while (cursor.moveToNext()) {
            val message = cursor.toMessageEntity()
            val sender = userLocal.getUser(message.sender.id)
            if (sender != null) {
                message.sender = sender
            }

            val room = roomLocal.getRoom(message.room.id)
            if (room != null) {
                message.room = room
            }

            if (message is FileAttachmentMessageEntity) {
                message.file = fileLocal.getLocalPath(message.messageId)
            }

            messages.add(message)
        }
        cursor.close()

        return messages
    }

    override fun getLastOnServerMessageId(): MessageIdEntity? {
        val query = "SELECT * FROM " + Db.MessageTable.TABLE_NAME + " WHERE " +
                Db.MessageTable.COLUMN_STATE + " >= " + MessageStateEntity.ON_SERVER.intValue +
                " ORDER BY " + Db.MessageTable.COLUMN_TIME + " DESC LIMIT 1"

        val cursor = database.rawQuery(query, null)

        if (cursor.moveToNext()) {
            val message = cursor.toMessageEntity()
            cursor.close()
            return message.messageId
        }

        cursor.close()
        return null
    }

    override fun getLastDeliveredMessageId(roomId: String): MessageIdEntity? {
        val query = "SELECT * FROM " + Db.MessageTable.TABLE_NAME + " WHERE " +
                Db.MessageTable.COLUMN_ROOM_ID + " = " + DatabaseUtils.sqlEscapeString(roomId) + " AND " +
                Db.MessageTable.COLUMN_STATE + " = " + MessageStateEntity.DELIVERED.intValue + " AND " +
                Db.MessageTable.COLUMN_SENDER_ID + " = " + DatabaseUtils.sqlEscapeString(accountLocal.getAccount().user.id) +
                " ORDER BY " + Db.MessageTable.COLUMN_TIME + " DESC LIMIT 1"

        val cursor = database.rawQuery(query, null)

        if (cursor.moveToNext()) {
            val message = cursor.toMessageEntity()
            cursor.close()
            return message.messageId
        }

        cursor.close()
        return null
    }

    override fun getLastReadMessageId(roomId: String): MessageIdEntity? {
        val query = "SELECT * FROM " + Db.MessageTable.TABLE_NAME + " WHERE " +
                Db.MessageTable.COLUMN_ROOM_ID + " = " + DatabaseUtils.sqlEscapeString(roomId) + " AND " +
                Db.MessageTable.COLUMN_STATE + " = " + MessageStateEntity.READ.intValue + " AND " +
                Db.MessageTable.COLUMN_SENDER_ID + " = " + DatabaseUtils.sqlEscapeString(accountLocal.getAccount().user.id) +
                " ORDER BY " + Db.MessageTable.COLUMN_TIME + " DESC LIMIT 1"

        val cursor = database.rawQuery(query, null)

        if (cursor.moveToNext()) {
            val message = cursor.toMessageEntity()
            cursor.close()
            return message.messageId
        }

        cursor.close()
        return null
    }

    override fun getOnServerMessages(roomId: String, lastMessageIdEntity: MessageIdEntity, limit: Int): List<MessageEntity> {
        val lastMessage = getMessage(lastMessageIdEntity) ?: return arrayListOf()

        val query = "SELECT * FROM " + Db.MessageTable.TABLE_NAME + " WHERE " +
                Db.MessageTable.COLUMN_ROOM_ID + " = " + DatabaseUtils.sqlEscapeString(roomId) + " AND " +
                Db.MessageTable.COLUMN_TIME + " <= " + lastMessage.nanoTimeStamp + " AND " +
                Db.MessageTable.COLUMN_STATE + " >= " + MessageStateEntity.ON_SERVER.intValue +
                " ORDER BY " + Db.MessageTable.COLUMN_TIME + " DESC LIMIT $limit"

        val cursor = database.rawQuery(query, null)
        val messages = ArrayList<MessageEntity>()
        while (cursor.moveToNext()) {
            messages.add(cursor.toMessageEntity())
        }
        cursor.close()
        return messages
    }

    override fun clearData() {
        database.transaction {
            database.delete(Db.MessageTable.TABLE_NAME, null, null)
        }
    }

    private fun isExistMessage(messageIdEntity: MessageIdEntity): Boolean {
        val query = "SELECT * FROM ${Db.MessageTable.TABLE_NAME} WHERE ${Db.MessageTable.COLUMN_UNIQUE_ID} " +
                "= ${DatabaseUtils.sqlEscapeString(messageIdEntity.uniqueId)}"
        val cursor = database.rawQuery(query, null)
        val contains = cursor.count > 0
        cursor.close()
        return contains
    }
}