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

package com.qiscus.sdk.chat.data.mapper

import com.qiscus.sdk.chat.data.model.*
import com.qiscus.sdk.chat.domain.model.*
import java.util.*

/**
 * Created on : August 19, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
fun MessageIdEntity.toDomainModel(): MessageId {
    return MessageId(id, beforeId, uniqueId)
}

fun MessageId.toEntity(): MessageIdEntity {
    return MessageIdEntity(id, beforeId, uniqueId)
}

fun MessageEntity.toDomainModel(): Message {
    if (this is FileAttachmentMessageEntity) {
        return toDomainModel()
    }
    return Message(
            messageId.toDomainModel(),
            text,
            sender.toDomainModel(),
            Date(nanoTimeStamp/1000000),
            room.toDomainModel(),
            MessageState.valueOf(state.intValue),
            MessageType(type.rawType, type.payload)
    )
}

fun Message.toEntity(): MessageEntity {
    if (this is FileAttachmentMessage) {
        return toEntity()
    }

    return MessageEntity(
            messageId.toEntity(),
            text,
            sender.toEntity(),
            date.time * 1000000,
            room.toEntity(),
            MessageStateEntity.valueOf(state.intValue),
            MessageTypeEntity(type.rawType, type.payload)
    )
}

fun MessageEntity.transformToTypedMessageEntity(): MessageEntity {
    if (type.rawType == "file_attachment" || (text.startsWith("[file]") && text.endsWith("[/file]"))) {
        return FileAttachmentMessageEntity(
                messageId,
                null,
                type.payload.optString("caption", ""),
                text,
                sender,
                nanoTimeStamp,
                room,
                MessageStateEntity.valueOf(state.intValue),
                MessageTypeEntity(type.rawType, type.payload)
        )
    }
    return this
}