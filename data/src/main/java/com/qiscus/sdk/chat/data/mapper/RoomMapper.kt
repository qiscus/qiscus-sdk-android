package com.qiscus.sdk.chat.data.mapper

import com.qiscus.sdk.chat.data.model.RoomEntity
import com.qiscus.sdk.chat.domain.model.Room

/**
 * Created on : September 01, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
fun RoomEntity.toDomainModel(): Room {
    return Room(id, name, avatar, group, options)
}

fun Room.toEntity(): RoomEntity {
    return RoomEntity(id, "", name, avatar, group, options)
}