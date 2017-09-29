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