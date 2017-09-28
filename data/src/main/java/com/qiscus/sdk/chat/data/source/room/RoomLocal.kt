package com.qiscus.sdk.chat.data.source.room

import com.qiscus.sdk.chat.data.model.CommentIdEntity
import com.qiscus.sdk.chat.data.model.RoomEntity
import com.qiscus.sdk.chat.data.model.RoomMemberEntity
import io.reactivex.Single

/**
 * Created on : September 01, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
interface RoomLocal {
    fun addRoom(roomEntity: RoomEntity)

    fun updateRoom(roomEntity: RoomEntity)

    fun addOrUpdateRoom(roomEntity: RoomEntity)

    fun deleteRoom(roomEntity: RoomEntity)

    fun getRoom(roomId: String): RoomEntity?

    fun getRoomByUniqueId(roomUniqueId: String): RoomEntity?

    fun getRoomWithUserId(userId: String): RoomEntity?

    fun getRoomWithUserId(userId: String, uniqueId: String): RoomEntity?

    fun getRoomWithChannelId(channelId: String): RoomEntity?

    fun addRoomMember(roomId: String, roomUniqueId: String = "default", roomMemberEntity: RoomMemberEntity)

    fun updateRoomMemberDeliveredState(roomId: String, userId: String, commentIdEntity: CommentIdEntity)

    fun updateRoomMemberReadState(roomId: String, userId: String, commentIdEntity: CommentIdEntity)

    fun updateRoomMembers(roomId: String, roomUniqueId: String = "default", roomMemberEntities: List<RoomMemberEntity>)

    fun deleteRoomMember(roomId: String, userId: String)

    fun deleteRoomMembers(roomId: String)

    fun getRoomMembers(roomId: String): List<RoomMemberEntity>

    fun getRooms(page: Int, limit: Int): List<RoomEntity>

    fun getRoomsWithSpecificIds(roomIds: List<String>, channelIds: List<String>): List<RoomEntity>

    fun clearData()
}