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

package com.qiscus.sdk.chat.data.source.message

import com.qiscus.sdk.chat.data.model.MessageEntity
import com.qiscus.sdk.chat.data.model.MessageIdEntity

interface MessageLocal {
    fun addMessage(messageEntity: MessageEntity)

    fun saveAndNotify(messageEntity: MessageEntity)

    fun updateMessage(messageEntity: MessageEntity)

    fun addOrUpdateMessage(messageEntity: MessageEntity)

    fun deleteMessage(messageEntity: MessageEntity)

    fun getMessage(messageIdEntity: MessageIdEntity): MessageEntity?

    fun getMessages(roomId: String, limit: Int = -1): List<MessageEntity>

    fun getMessages(roomId: String, lastMessageIdEntity: MessageIdEntity, limit: Int = -1): List<MessageEntity>

    fun getPendingMessages(): List<MessageEntity>

    fun updateLastDeliveredMessage(roomId: String, userId: String, messageId: MessageIdEntity)

    fun updateLastReadMessage(roomId: String, userId: String, messageId: MessageIdEntity)

    fun getLastOnServerMessageId(): MessageIdEntity?

    fun getLastDeliveredMessageId(roomId: String): MessageIdEntity?

    fun getLastReadMessageId(roomId: String): MessageIdEntity?

    fun getOnServerMessages(roomId: String, lastMessageIdEntity: MessageIdEntity, limit: Int = 20): List<MessageEntity>

    fun clearData()
}