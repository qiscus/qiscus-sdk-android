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

package com.qiscus.sdk.chat.domain.model

import com.qiscus.sdk.chat.domain.common.generateUniqueId
import org.json.JSONObject
import java.util.*

/**
 * Created on : August 17, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
data class CommentId
@JvmOverloads constructor(val id: String = "", val commentBeforeId: String = "", val uniqueId: String = generateUniqueId()) {
    override fun equals(other: Any?): Boolean {
        if (other !is CommentId) {
            return false
        }

        return if (id.isBlank()) {
            uniqueId == other.uniqueId
        } else {
            id == other.id || uniqueId == other.uniqueId
        }
    }

    override fun hashCode(): Int {
        return uniqueId.hashCode()
    }
}

data class CommentType(val rawType: String, val payload: JSONObject) {
    constructor(rawType: String) : this(rawType, JSONObject())
}

enum class CommentState(val intValue: Int) {
    PENDING(0), SENDING(1), ON_SERVER(2), DELIVERED(3), READ(4), FAILED(-1);

    companion object {
        fun valueOf(intValue: Int): CommentState {
            return when (intValue) {
                0 -> PENDING
                1 -> SENDING
                2 -> ON_SERVER
                3 -> DELIVERED
                4 -> READ
                else -> FAILED
            }
        }
    }
}

open class Comment(
        val commentId: CommentId,
        val message: String,
        val sender: User,
        val date: Date,
        val room: Room,
        var state: CommentState,
        val type: CommentType
) {

    override fun toString(): String {
        return "Comment(commentId=$commentId, message='$message', sender=$sender, date=$date, " +
                "room=$room, state=$state, type=$type)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Comment) return false

        if (commentId != other.commentId) return false
        if (state != other.state) return false

        return true
    }

    override fun hashCode(): Int {
        return commentId.hashCode()
    }
}