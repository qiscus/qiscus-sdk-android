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

package com.qiscus.sdk.chat.data.pusher

import com.qiscus.sdk.chat.data.model.MessageEntity
import com.qiscus.sdk.chat.data.pubsub.message.MessagePublisher
import com.qiscus.sdk.chat.data.pubsub.message.MessageSubscriber
import com.qiscus.sdk.chat.data.pusher.event.MessageEvent
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

/**
 * Created on : August 19, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
class MessageDataPusher(private val publisher: PublishSubject<Any>) : MessagePublisher, MessageSubscriber {

    override fun onMessageAdded(messageEntity: MessageEntity) {
        publisher.onNext(MessageEvent(messageEntity, MessageEvent.Type.ADDED))
    }

    override fun onMessageUpdated(messageEntity: MessageEntity) {
        publisher.onNext(MessageEvent(messageEntity, MessageEvent.Type.UPDATED))
    }

    override fun onMessageDeleted(messageEntity: MessageEntity) {
        publisher.onNext(MessageEvent(messageEntity, MessageEvent.Type.DELETED))
    }

    override fun listenMessageAdded(): Observable<MessageEntity> {
        return publisher.filter { it is MessageEvent }
                .map { it as MessageEvent }
                .filter { it.type == MessageEvent.Type.ADDED }
                .map { it.messageEntity }
    }

    override fun listenMessageUpdated(): Observable<MessageEntity> {
        return publisher.filter { it is MessageEvent }
                .map { it as MessageEvent }
                .filter { it.type == MessageEvent.Type.UPDATED }
                .map { it.messageEntity }
    }

    override fun listenMessageDeleted(): Observable<MessageEntity> {
        return publisher.filter { it is MessageEvent }
                .map { it as MessageEvent }
                .filter { it.type == MessageEvent.Type.DELETED }
                .map { it.messageEntity }
    }
}