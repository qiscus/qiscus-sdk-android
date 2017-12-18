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

import com.qiscus.sdk.chat.data.pubsub.user.UserSubscriber
import com.qiscus.sdk.chat.domain.model.UserStatus
import com.qiscus.sdk.chat.domain.model.UserTyping
import com.qiscus.sdk.chat.domain.pubsub.PubSubClient
import com.qiscus.sdk.chat.domain.pubsub.UserObserver
import io.reactivex.Completable
import io.reactivex.Observable

/**
 * Created on : September 28, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
class UserDataObserver(private val pubSubClient: PubSubClient,
                       private val userSubscriber: UserSubscriber) : UserObserver {

    override fun listenUserStatus(userId: String): Observable<UserStatus> {
        return userSubscriber.listenUserStatus(userId)
                .doOnSubscribe { pubSubClient.listenUserStatus(userId) }
                .doOnDispose { pubSubClient.unlistenUserStatus(userId) }
    }

    override fun listenUserTyping(roomId: String): Observable<UserTyping> {
        return userSubscriber.listenUserTyping(roomId)
                .doOnSubscribe { pubSubClient.listenUserTyping(roomId) }
                .doOnDispose { pubSubClient.unlistenUserTyping(roomId) }
    }

    override fun setTyping(roomId: String, typing: Boolean): Completable {
        return Completable.defer { Completable.complete() }
                .doOnSubscribe { pubSubClient.publishTypingStatus(roomId, typing) }
    }
}