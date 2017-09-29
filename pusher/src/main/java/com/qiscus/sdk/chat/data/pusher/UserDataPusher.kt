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

import com.qiscus.sdk.chat.data.mapper.toDomainModel
import com.qiscus.sdk.chat.data.pubsub.user.UserPublisher
import com.qiscus.sdk.chat.data.pubsub.user.UserSubscriber
import com.qiscus.sdk.chat.data.pusher.event.UserStatusEvent
import com.qiscus.sdk.chat.data.pusher.event.UserTypingEvent
import com.qiscus.sdk.chat.data.source.user.UserLocal
import com.qiscus.sdk.chat.domain.model.UserStatus
import com.qiscus.sdk.chat.domain.model.UserTyping
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import java.util.*

/**
 * Created on : September 28, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
class UserDataPusher(private val publisher: PublishSubject<Any>,
                     private val userLocal: UserLocal) : UserPublisher, UserSubscriber {

    override fun onUserStatusChanged(userId: String, online: Boolean, lastActive: Date) {
        publisher.onNext(UserStatusEvent(userId, online, lastActive))
    }

    override fun onUserTyping(roomId: String, userId: String, typing: Boolean) {
        publisher.onNext(UserTypingEvent(roomId, userId, typing))
    }

    override fun listenUserStatus(userId: String): Observable<UserStatus> {
        return publisher.filter { it is UserStatusEvent }
                .map { it as UserStatusEvent }
                .filter { it.userId == userId }
                .flatMap {
                    val user = userLocal.getUser(it.userId) ?:
                            return@flatMap Observable.empty<UserStatus>()

                    return@flatMap Observable.just(UserStatus(user.toDomainModel(), it.online, it.lastActive))
                }
    }

    override fun listenUserTyping(roomId: String): Observable<UserTyping> {
        return publisher.filter { it is UserTypingEvent }
                .map { it as UserTypingEvent }
                .filter { it.roomId == roomId }
                .flatMap {
                    val user = userLocal.getUser(it.userId) ?:
                            return@flatMap Observable.empty<UserTyping>()

                    return@flatMap Observable.just(UserTyping(it.roomId, user.toDomainModel(), it.typing))
                }
    }
}