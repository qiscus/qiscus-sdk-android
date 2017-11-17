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

package com.qiscus.sdk.chat.data

import com.qiscus.sdk.chat.data.mapper.toDomainModel
import com.qiscus.sdk.chat.data.pubsub.message.MessageSubscriber
import com.qiscus.sdk.chat.domain.model.Message
import com.qiscus.sdk.chat.domain.pubsub.MessageObserver
import com.qiscus.sdk.chat.domain.pubsub.QiscusPubSubClient
import io.reactivex.Observable

/**
 * Created on : August 19, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
class MessageDataObserver(private val pubSubClient: QiscusPubSubClient,
                          private val messageSubscriber: MessageSubscriber) : MessageObserver {

    override fun listenNewMessage(): Observable<Message> {
        return messageSubscriber.listenMessageAdded().map { it.toDomainModel() }
    }

    override fun listenMessageState(roomId: String): Observable<Message> {
        return messageSubscriber.listenMessageUpdated()
                .doOnSubscribe { pubSubClient.listenMessageState(roomId) }
                .doOnDispose { pubSubClient.unlistenMessageState(roomId) }
                .filter { it.room.id == roomId }
                .map { it.toDomainModel() }
    }

    override fun listenMessageDeleted(roomId: String): Observable<Message> {
        return messageSubscriber.listenMessageDeleted()
                .filter { it.room.id == roomId }
                .map { it.toDomainModel() }
    }
}