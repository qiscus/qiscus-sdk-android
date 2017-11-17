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

package com.qiscus.sdk.chat.data.remote

import com.qiscus.sdk.chat.data.model.MessageEntity
import com.qiscus.sdk.chat.data.model.MessageIdEntity
import com.qiscus.sdk.chat.data.source.account.AccountLocal
import com.qiscus.sdk.chat.data.source.message.MessageRemote
import io.reactivex.Completable
import io.reactivex.Single

/**
 * Created on : August 19, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
class MessageRemoteImpl(private val accountLocal: AccountLocal,
                        private val qiscusRestApi: QiscusRestApi) : MessageRemote {

    override fun postMessage(messageEntity: MessageEntity): Single<MessageEntity> {
        return qiscusRestApi.postMessage(accountLocal.getAccount().token, messageEntity.room.id,
                messageEntity.text, messageEntity.messageId.uniqueId, messageEntity.type.rawType,
                messageEntity.type.payload.toString())
                .map { it.results.comment.toEntity() }
    }

    override fun getMessages(roomId: String): Single<List<MessageEntity>> {
        return qiscusRestApi.getMessages(accountLocal.getAccount().token, roomId)
                .map { it.results.comments.map { it.toEntity() } }
    }

    override fun getMessages(roomId: String, lastMessageId: MessageIdEntity, limit: Int): Single<List<MessageEntity>> {
        return qiscusRestApi.getMessages(accountLocal.getAccount().token, roomId, lastMessageId.id, limit, false)
                .map { it.results.comments.map { it.toEntity() } }
    }

    override fun updateLastDeliveredMessage(roomId: String, messageId: MessageIdEntity): Completable {
        return qiscusRestApi.updateMessageStatus(accountLocal.getAccount().token, roomId, messageId.id, "")
    }

    override fun updateLastReadMessage(roomId: String, messageId: MessageIdEntity): Completable {
        return qiscusRestApi.updateMessageStatus(accountLocal.getAccount().token, roomId, "", messageId.id)
    }

    override fun sync(lastMessageId: MessageIdEntity): Single<List<MessageEntity>> {
        return qiscusRestApi.sync(accountLocal.getAccount().token, lastMessageId.id)
                .map { it.results.comments.map { it.toEntity() } }
    }

    override fun sync(roomId: String, lastMessageId: MessageIdEntity): Single<List<MessageEntity>> {
        return qiscusRestApi.getMessages(accountLocal.getAccount().token, roomId, lastMessageId.id, 20, true)
                .map { it.results.comments.map { it.toEntity() } }
    }
}