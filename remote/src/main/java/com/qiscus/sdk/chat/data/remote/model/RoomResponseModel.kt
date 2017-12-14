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

package com.qiscus.sdk.chat.data.remote.model

import com.google.gson.JsonElement
import com.qiscus.sdk.chat.data.mapper.transformToTypedMessageEntity
import com.qiscus.sdk.chat.data.model.*
import org.json.JSONObject

/**
 * Created on : August 31, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */

data class RoomResponseModel(var results: Results, var status: Int) {

    data class Results(var comments: List<Comment>, var room: Room)

    data class Comment(
            var commentBeforeId: Long,
            var commentBeforeIdStr: String,
            var disableLinkPreview: Boolean,
            var email: String,
            var id: Long,
            var idStr: String,
            var message: String,
            var payload: JsonElement,
            var roomId: Long,
            var roomIdStr: String,
            var roomName: String,
            var timestamp: String,
            var type: String,
            var uniqueTempId: String,
            var unixTimestamp: Long,
            var unixNanoTimestamp: Long,
            var userAvatarUrl: String,
            var userId: Long,
            var username: String
    ) {
        private var account: AccountEntity? = null
        private var participants: List<Participant>? = null

        private var lastDelivered = 0L
        private var lastRead = 0L

        fun toEntity(account: AccountEntity, participants: List<Participant>): MessageEntity {
            if (this.account == null) {
                this.account = account
                this.participants = participants
                findRoomMemberState()
            }
            return MessageEntity(
                    MessageIdEntity(idStr, commentBeforeIdStr, uniqueTempId),
                    message, UserEntity(email, username, userAvatarUrl),
                    unixNanoTimestamp, RoomEntity(roomIdStr, name = roomName),
                    determineState(id), MessageTypeEntity(type, if (!payload.isJsonNull) JSONObject(payload.toString()) else JSONObject())
            ).transformToTypedMessageEntity()
        }

        private fun findRoomMemberState() {
            participants?.filter { it.email != account?.user?.id }?.forEach {
                if (it.lastCommentReadId > lastRead) {
                    lastRead = it.lastCommentReadId
                    lastDelivered = lastRead
                }

                if (it.lastCommentReceivedId > lastDelivered) {
                    lastDelivered = it.lastCommentReceivedId
                }
            }
        }

        private fun determineState(commentId: Long): MessageStateEntity {
            return when {
                commentId <= lastRead -> MessageStateEntity.READ
                commentId <= lastDelivered -> MessageStateEntity.DELIVERED
                else -> MessageStateEntity.ON_SERVER
            }
        }
    }

    data class Room(
            var avatarUrl: String,
            var chatType: String,
            var id: Long,
            var idStr: String,
            var lastCommentId: Long,
            var lastCommentMessage: String,
            var options: String?,
            var participants: List<Participant>,
            var roomName: String,
            var rawRoomName: String,
            var uniqueId: String,
            var unreadCount: Int
    ) {
        fun toEntity(): RoomEntity {
            return RoomEntity(
                    idStr, if (chatType == "single") rawRoomName else uniqueId, roomName, avatarUrl,
                    chatType != "single", if (options != null) options!! else "", unreadCount
            )
        }
    }

    data class Participant(
            var avatarUrl: String,
            var email: String,
            var id: Long,
            var lastCommentReadId: Long,
            var lastCommentReadIdStr: String,
            var lastCommentReceivedId: Long,
            var lastCommentReceivedIdStr: String,
            var username: String
    ) {
        fun toEntity(): ParticipantEntity {
            return ParticipantEntity(UserEntity(email, username, avatarUrl),
                    ParticipantStateEntity(lastCommentReceivedIdStr, lastCommentReadIdStr))
        }
    }
}