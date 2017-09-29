package com.qiscus.sdk.chat.data.pubsub.room

import com.qiscus.sdk.chat.data.model.RoomEntity

/**
 * Created on : September 06, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
interface RoomPublisher {
    fun onRoomAdded(roomEntity: RoomEntity)

    fun onRoomUpdated(roomEntity: RoomEntity)

    fun onRoomDeleted(roomEntity: RoomEntity)
}