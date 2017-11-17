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

package com.qiscus.sdk.chat.data.source.room

import com.qiscus.sdk.chat.data.model.MessageIdEntity
import com.qiscus.sdk.chat.data.model.RoomEntity
import com.qiscus.sdk.chat.data.model.RoomMemberEntity

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

    fun addRoomMember(roomId: String, roomMemberEntity: RoomMemberEntity)

    fun updateRoomMemberDeliveredState(roomId: String, userId: String, messageIdEntity: MessageIdEntity)

    fun updateRoomMemberReadState(roomId: String, userId: String, messageIdEntity: MessageIdEntity)

    fun updateRoomMembers(roomId: String, roomMemberEntities: List<RoomMemberEntity>)

    fun deleteRoomMember(roomId: String, userId: String)

    fun deleteRoomMembers(roomId: String)

    fun getRoomMembers(roomId: String): List<RoomMemberEntity>

    fun getRooms(page: Int, limit: Int): List<RoomEntity>

    fun getRoomsWithSpecificIds(roomIds: List<String>, channelIds: List<String>): List<RoomEntity>

    fun clearData()
}