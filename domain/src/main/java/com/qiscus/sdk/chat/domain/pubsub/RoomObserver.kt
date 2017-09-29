package com.qiscus.sdk.chat.domain.pubsub

import com.qiscus.sdk.chat.domain.model.Room
import io.reactivex.Observable

/**
 * Created on : September 22, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
interface RoomObserver {
    fun listenRoomAdded(): Observable<Room>

    fun listenRoomUpdated(): Observable<Room>

    fun listenRoomDeleted(): Observable<Room>
}