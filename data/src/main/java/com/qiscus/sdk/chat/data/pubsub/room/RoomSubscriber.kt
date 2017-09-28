package com.qiscus.sdk.chat.data.pubsub.room

import com.qiscus.sdk.chat.data.model.RoomEntity
import com.qiscus.sdk.chat.data.model.UserEntity
import com.qiscus.sdk.chat.domain.model.UserTyping
import io.reactivex.Observable

/**
 * Created on : September 06, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
interface RoomSubscriber {
    fun listenRoomAdded(): Observable<RoomEntity>

    fun listenRoomUpdated(): Observable<RoomEntity>

    fun listenRoomDeleted(): Observable<RoomEntity>

    fun listenUserTyping(roomId: String): Observable<UserTyping>
}