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
import com.qiscus.sdk.chat.data.pubsub.room.RoomSubscriber
import com.qiscus.sdk.chat.domain.model.Room
import com.qiscus.sdk.chat.domain.pubsub.RoomObserver
import io.reactivex.Observable

/**
 * Created on : September 22, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
class RoomDataObserver(private val roomSubscriber: RoomSubscriber) : RoomObserver {

    override fun listenRoomAdded(): Observable<Room> {
        return roomSubscriber.listenRoomAdded().map { it.toDomainModel() }
    }

    override fun listenRoomUpdated(): Observable<Room> {
        return roomSubscriber.listenRoomUpdated().map { it.toDomainModel() }
    }

    override fun listenRoomDeleted(): Observable<Room> {
        return roomSubscriber.listenRoomDeleted().map { it.toDomainModel() }
    }
}