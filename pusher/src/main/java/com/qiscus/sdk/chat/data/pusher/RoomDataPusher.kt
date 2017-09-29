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