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

import com.qiscus.sdk.chat.data.model.RoomEntity
import com.qiscus.sdk.chat.data.pubsub.room.RoomPublisher
import com.qiscus.sdk.chat.data.pubsub.room.RoomSubscriber
import com.qiscus.sdk.chat.data.pusher.event.RoomEvent
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

/**
 * Created on : September 20, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
class RoomDataPusher(private val publisher: PublishSubject<Any>) : RoomPublisher, RoomSubscriber {

    override fun onRoomAdded(roomEntity: RoomEntity) {
        publisher.onNext(RoomEvent(roomEntity, RoomEvent.Type.ADDED))
    }

    override fun onRoomUpdated(roomEntity: RoomEntity) {
        publisher.onNext(RoomEvent(roomEntity, RoomEvent.Type.UPDATED))
    }

    override fun onRoomDeleted(roomEntity: RoomEntity) {
        publisher.onNext(RoomEvent(roomEntity, RoomEvent.Type.DELETED))
    }

    override fun listenRoomAdded(): Observable<RoomEntity> {
        return publisher.filter { it is RoomEvent }
                .map { it as RoomEvent }
                .filter { it.type == RoomEvent.Type.ADDED }
                .map { it.roomEntity }
    }

    override fun listenRoomUpdated(): Observable<RoomEntity> {
        return publisher.filter { it is RoomEvent }
                .map { it as RoomEvent }
                .filter { it.type == RoomEvent.Type.UPDATED }
                .map { it.roomEntity }
    }

    override fun listenRoomDeleted(): Observable<RoomEntity> {
        return publisher.filter { it is RoomEvent }
                .map { it as RoomEvent }
                .filter { it.type == RoomEvent.Type.DELETED }
                .map { it.roomEntity }
    }
}