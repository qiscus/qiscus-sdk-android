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

import com.qiscus.sdk.chat.data.model.MessageStateEntity
import com.qiscus.sdk.chat.data.model.MessageTypeEntity
import com.qiscus.sdk.chat.data.model.FileAttachmentMessageEntity
import com.qiscus.sdk.chat.domain.model.MessageState
import com.qiscus.sdk.chat.domain.model.MessageType
import com.qiscus.sdk.chat.domain.model.FileAttachmentMessage
import java.util.*

/**
 * Created on : August 19, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
fun FileAttachmentMessageEntity.toDomainModel(): FileAttachmentMessage {
    return FileAttachmentMessage(
            messageId.toDomainModel(),
            file,
            caption,
            text,
            sender.toDomainModel(),
            Date(nanoTimeStamp / 1000000),
            room.toDomainModel(),
            MessageState.valueOf(state.intValue),
            MessageType(type.rawType, type.payload)
    )
}

fun FileAttachmentMessage.toEntity(): FileAttachmentMessageEntity {
    return FileAttachmentMessageEntity(
            messageId.toEntity(),
            file,
            caption,
            text,
            sender.toEntity(),
            date.time * 1000000,
            room.toEntity(),
            MessageStateEntity.valueOf(state.intValue),
            MessageTypeEntity(type.rawType, type.payload)
    )
}