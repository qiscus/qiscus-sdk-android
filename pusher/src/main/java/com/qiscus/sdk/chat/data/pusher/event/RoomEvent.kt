package com.qiscus.sdk.chat.data.pusher.event

import com.qiscus.sdk.chat.data.model.RoomEntity

/**
 * Created on : September 20, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
internal data class RoomEvent(val roomEntity: RoomEntity, val type: Type) {
    internal enum class Type {
        ADDED, UPDATED, DELETED
    }
}