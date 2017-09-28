package com.qiscus.sdk.chat.data.remote.model

import com.google.gson.JsonElement
import com.qiscus.sdk.chat.data.mapper.transformToTypedCommentEntity
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

        fun toEntity(account: AccountEntity, participants: List<Participant>): CommentEntity {
            if (this.account == null) {
                this.account = account
                this.participants = participants
                findRoomMemberState()
            }
            return CommentEntity(
                    CommentIdEntity(idStr, commentBeforeIdStr, uniqueTempId),
                    message, UserEntity(email, username, userAvatarUrl),
                    unixNanoTimestamp, RoomEntity(roomIdStr, name = roomName),
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
            var uniqueId: String
    ) {
        fun toEntity(): RoomEntity {
            return RoomEntity(
                    idStr, rawRoomName, roomName, avatarUrl, chatType != "single", if (options != null) options!! else ""
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
        fun toEntity(): RoomMemberEntity {
            return RoomMemberEntity(UserEntity(email, username, avatarUrl),
                    MemberStateEntity(lastCommentReceivedIdStr, lastCommentReadIdStr))
        }
    }
}