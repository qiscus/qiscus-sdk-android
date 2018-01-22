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

package com.qiscus.sdk.chat.presentation.mapper

import com.qiscus.sdk.chat.core.Qiscus
import com.qiscus.sdk.chat.data.util.MimeTypeGuesser
import com.qiscus.sdk.chat.domain.model.FileAttachmentMessage
import com.qiscus.sdk.chat.domain.model.Message
import com.qiscus.sdk.chat.domain.util.containsUrl
import com.qiscus.sdk.chat.presentation.model.*

/**
 * Created on : August 19, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
fun Message.toViewModel(): MessageViewModel {

    if (this is FileAttachmentMessage) {
        return determineFileViewModel(this)
    }

    return when (type.rawType) {
        "text" -> if (text.containsUrl()) MessageLinkViewModel(this) else MessageTextViewModel(this)
        "account_linking" -> MessageAccountLinkingViewModel(this)
        "buttons" -> MessageButtonsViewModel(this)
        "card" -> MessageCardViewModel(this)
        "contact_person" -> MessageContactViewModel(this)
        "location" -> MessageLocationViewModel(this)
        "system_event" -> MessageSystemEventViewModel(this)
        "reply" -> MessageReplyViewModel(this)
        else -> MessageViewModel(this)
    }
}

private fun determineFileViewModel(message: FileAttachmentMessage, mimeTypeGuesser: MimeTypeGuesser = Qiscus.instance.component.dataComponent.mimeTypeGuesser)
        : MessageFileViewModel {

    val type = mimeTypeGuesser.getMimeTypeFromFileName(message.attachmentName)
    return when {
        type == null -> MessageFileViewModel(message, mimeType = "")
        type.contains("image") -> MessageImageViewModel(message, mimeType = type)
        type.contains("video") -> MessageVideoViewModel(message, mimeType = type)
        type.contains("audio") -> MessageAudioViewModel(message, mimeType = type)
        else -> MessageFileViewModel(message, mimeType = type)
    }
}