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
 * Created on : August 19, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */

data class CommentResponseModel(var results: Results, var status: Int) {

    data class Results(var comment: Comment)

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
                    unixNanoTimestamp, RoomEntity(roomIdStr, name = ""),
                    CommentStateEntity.ON_SERVER,
                    CommentTypeEntity(type, if (!payload.isJsonNull) JSONObject(payload.toString()) else JSONObject())
            ).transformToTypedCommentEntity()
        }
    }
}