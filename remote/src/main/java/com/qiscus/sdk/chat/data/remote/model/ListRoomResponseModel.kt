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
import com.qiscus.sdk.chat.data.mapper.transformToTypedCommentEntity
import com.qiscus.sdk.chat.data.model.*
import org.json.JSONObject

/**
 * Created on : September 28, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
data class ListRoomResponseModel(
        val results: Results,
        val status: Int
)

data class Results(
        val meta: Meta,
        val roomsInfo: List<RoomsInfo>
)

data class Meta(
        val currentPage: Int,
        val totalRoom: Int
)

data class RoomsInfo(
        val avatarUrl: String,
        val chatType: String,
        val id: Int,
        val idStr: String,
        val lastComment: LastComment,
        val options: String,
        val participants: List<Participant>,
        val rawRoomName: String,
        val roomName: String,
        val uniqueId: String,
        val unreadCount: Int
) {
    fun toEntity(): RoomEntity {
        return RoomEntity(
                idStr, if (chatType == "single") rawRoomName else uniqueId, roomName, avatarUrl,
                chatType != "single", options
        )
    }
}

data class LastComment(
        val commentBeforeId: Int,
        val commentBeforeIdStr: String,
        val disableLinkPreview: Boolean,
        val email: String,
        val id: Long,
        val idStr: String,
        val message: String,
        val payload: JsonElement,
        val roomId: Int,
        val roomIdStr: String,
        val timestamp: String,
        val topicId: Int,
        val topicIdStr: String,
        val type: String,
        val uniqueTempId: String,
        val unixNanoTimestamp: Long,
        val unixTimestamp: Int,
        val userAvatar: UserAvatar,
        val userAvatarUrl: String,
        val userId: Int,
        val userIdStr: String,
        val username: String
) {
    private var account: AccountEntity? = null
    private var participants: List<Participant>? = null

    private var lastDelivered = 0L
    private var lastRead = 0L

    fun toEntity(account: AccountEntity, participants: List<Participant>): CommentEntity {
        if (this.account == null) {
            this.account = account
            this.participants = participants
            findRoomMemberState()
        }
        return CommentEntity(
                CommentIdEntity(idStr, commentBeforeIdStr, uniqueTempId),
                message, UserEntity(email, username, userAvatarUrl),
                unixNanoTimestamp, RoomEntity(roomIdStr, name = ""),
                determineState(id), CommentTypeEntity(type, if (!payload.isJsonNull) JSONObject(payload.toString()) else JSONObject())
        ).transformToTypedCommentEntity()
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

    private fun determineState(commentId: Long): CommentStateEntity {
        return when {
            commentId <= lastRead -> CommentStateEntity.READ
            commentId <= lastDelivered -> CommentStateEntity.DELIVERED
            else -> CommentStateEntity.ON_SERVER
        }
    }
}

data class UserAvatar(
        val avatar: Avatar
)

data class Avatar(
        val url: String
)

data class Participant(
        val avatarUrl: String,
        val email: String,
        val id: Int,
        val idStr: String,
        val lastCommentReadId: Long,
        val lastCommentReadIdStr: String,
        val lastCommentReceivedId: Long,
        val lastCommentReceivedIdStr: String,
        val username: String
) {
    fun toEntity(): RoomMemberEntity {
        return RoomMemberEntity(UserEntity(email, username, avatarUrl),
                MemberStateEntity(lastCommentReceivedIdStr, lastCommentReadIdStr))
    }
}