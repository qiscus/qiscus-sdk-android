package com.qiscus.sdk.chat.data.source.room

import com.qiscus.sdk.chat.data.model.RoomEntity
import com.qiscus.sdk.chat.data.model.RoomMemberEntity
import io.reactivex.Single

/**
 * Created on : September 01, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
interface RoomRemote {
    fun getRoom(roomId: String): Single<RoomEntity>

    fun getRoomWithUserId(userId: String): Single<RoomEntity>

    fun createGroupRoom(userIds: List<String>, roomName: String, roomAvatarUrl: String): Single<RoomEntity>

    fun getRoomWithChannelId(channelId: String, roomAvatarUrl: String): Single<RoomEntity>

    fun getRoomMembers(roomId: String): Single<List<RoomMemberEntity>>

    fun getRooms(page: Int, limit: Int): Single<List<RoomEntity>>

    fun getRoomsWithSpecificIds(roomIds: List<String>, channelIds: List<String>): Single<List<RoomEntity>>
}