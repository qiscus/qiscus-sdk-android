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

data class SearchCommentResponseModel(var results: Results, var status: Int) {

    data class Results(var comments: List<Comment>)

    data class Comment(
            var chatType: String,
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
        fun toEntity(): CommentEntity {
            return CommentEntity(
                    CommentIdEntity(idStr, commentBeforeIdStr, uniqueTempId),
                    message, UserEntity(email, username, userAvatarUrl),
                    unixNanoTimestamp, RoomEntity(roomIdStr, name = roomName),
                    CommentStateEntity.ON_SERVER, CommentTypeEntity(type, if (!payload.isJsonNull) JSONObject(payload.toString()) else JSONObject())
            ).transformToTypedCommentEntity()
        }
    }
}