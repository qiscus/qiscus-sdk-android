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

package com.qiscus.sdk.chat.data.model

import com.qiscus.sdk.chat.domain.common.generateUniqueId
import org.json.JSONObject

/**
 * Created on : August 19, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
data class MessageIdEntity
@JvmOverloads constructor(val id: String = "", val beforeId: String = "", val uniqueId: String = generateUniqueId()) {
    override fun equals(other: Any?): Boolean {
        if (other !is MessageIdEntity) {
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

data class MessageTypeEntity(val rawType: String, var payload: JSONObject) {
    constructor(rawType: String) : this(rawType, JSONObject())
}

enum class MessageStateEntity(val intValue: Int) {
    PENDING(0), SENDING(1), ON_SERVER(2), DELIVERED(3), READ(4), FAILED(-1);

    companion object {
        fun valueOf(intValue: Int): MessageStateEntity {
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

open class MessageEntity(
        val messageId: MessageIdEntity,
        val text: String,
        var sender: UserEntity,
        val nanoTimeStamp: Long,
        var room: RoomEntity,
        var state: MessageStateEntity,
        val type: MessageTypeEntity
) {

    override fun toString(): String {
        return "MessageEntity(messageId=$messageId, text='$text', sender=$sender, nanoTimeStamp=$nanoTimeStamp, " +
                "room=$room, state=$state, type=$type)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MessageEntity) return false

        if (messageId != other.messageId) return false
        if (state != other.state) return false

        return true
    }

    override fun hashCode(): Int {
        return messageId.hashCode()
    }
}