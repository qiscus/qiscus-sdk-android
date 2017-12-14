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

package com.qiscus.sdk.chat.data.remote

import com.qiscus.sdk.chat.data.model.RoomEntity
import com.qiscus.sdk.chat.data.model.ParticipantEntity
import com.qiscus.sdk.chat.data.source.account.AccountLocal
import com.qiscus.sdk.chat.data.source.message.MessageLocal
import com.qiscus.sdk.chat.data.source.room.RoomLocal
import com.qiscus.sdk.chat.data.source.room.RoomRemote
import io.reactivex.Single

/**
 * Created on : September 01, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
class RoomRemoteImpl(private val accountLocal: AccountLocal,
                     private val qiscusRestApi: QiscusRestApi,
                     private val roomLocal: RoomLocal,
                     private val messageLocal: MessageLocal) : RoomRemote {

    override fun getRoom(roomId: String): Single<RoomEntity> {
        return qiscusRestApi.getChatRoom(accountLocal.getAccount().token, roomId)
                .doOnSuccess {
                    val participants = it.results.room.participants
                    roomLocal.updateParticipants(it.results.room.idStr, participants.map { it.toEntity() })

                    it.results.comments.map { it.toEntity(accountLocal.getAccount(), participants) }
                            .forEach { messageLocal.addOrUpdateMessage(it) }
                }
                .map { it.results.room.toEntity() }
    }

    override fun getRoomWithUserId(userId: String): Single<RoomEntity> {
        return qiscusRestApi.createOrGetChatRoom(accountLocal.getAccount().token, userId)
                .doOnSuccess {
                    val participants = it.results.room.participants
                    roomLocal.updateParticipants(it.results.room.idStr, participants.map { it.toEntity() })

                    it.results.comments.map { it.toEntity(accountLocal.getAccount(), participants) }
                            .forEach { messageLocal.addOrUpdateMessage(it) }
                }
                .map { it.results.room.toEntity() }
    }

    override fun createGroupRoom(userIds: List<String>, roomName: String, roomAvatarUrl: String): Single<RoomEntity> {
        return qiscusRestApi.createGroupChatRoom(accountLocal.getAccount().token, roomName, userIds, roomAvatarUrl, "")
                .doOnSuccess {
                    val participants = it.results.room.participants
                    roomLocal.updateParticipants(it.results.room.idStr, participants.map { it.toEntity() })

                    it.results.comments.map { it.toEntity(accountLocal.getAccount(), participants) }
                            .forEach { messageLocal.addOrUpdateMessage(it) }
                }
                .map { it.results.room.toEntity() }
    }

    override fun getRoomWithChannelId(channelId: String, roomAvatarUrl: String): Single<RoomEntity> {
        return qiscusRestApi.createOrGetGroupChatRoom(accountLocal.getAccount().token, channelId, "", roomAvatarUrl, "")
                .doOnSuccess {
                    val participants = it.results.room.participants
                    roomLocal.updateParticipants(it.results.room.idStr, participants.map { it.toEntity() })

                    it.results.comments.map { it.toEntity(accountLocal.getAccount(), participants) }
                            .forEach { messageLocal.addOrUpdateMessage(it) }
                }
                .map { it.results.room.toEntity() }
    }

    override fun getParticipants(roomId: String): Single<List<ParticipantEntity>> {
        return qiscusRestApi.getChatRoom(accountLocal.getAccount().token, roomId)
                .map { it.results.room.participants.map { it.toEntity() } }
    }

    override fun getRooms(page: Int, limit: Int): Single<List<RoomEntity>> {
        return qiscusRestApi.getChatRooms(accountLocal.getAccount().token, page, limit, true)
                .doOnSuccess {
                    it.results.roomsInfo.forEach {
                        val participants = it.participants
                        roomLocal.updateParticipants(it.idStr, participants.map { it.toEntity() })

                        val lastMessage = it.lastComment.toEntity(accountLocal.getAccount(), participants)
                        if (lastMessage.messageId.id != "0" && lastMessage.messageId.beforeId != "0") {
                            messageLocal.addOrUpdateMessage(lastMessage)
                        }
                    }
                }
                .map { it.results.roomsInfo.map { it.toEntity() } }
    }

    override fun getRoomsWithSpecificIds(roomIds: List<String>, channelIds: List<String>): Single<List<RoomEntity>> {
        return qiscusRestApi.getChatRooms(accountLocal.getAccount().token, roomIds, channelIds, true)
                .doOnSuccess {
                    it.results.roomsInfo.forEach {
                        val participants = it.participants
                        roomLocal.updateParticipants(it.idStr, participants.map { it.toEntity() })

                        val lastMessage = it.lastComment.toEntity(accountLocal.getAccount(), participants)
                        if (lastMessage.messageId.id != "0" && lastMessage.messageId.beforeId != "0") {
                            messageLocal.addOrUpdateMessage(lastMessage)
                        }
                    }
                }
                .map { it.results.roomsInfo.map { it.toEntity() } }
    }
}