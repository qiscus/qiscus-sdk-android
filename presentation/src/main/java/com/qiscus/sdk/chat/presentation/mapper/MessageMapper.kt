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

import android.support.annotation.ColorInt
import com.qiscus.sdk.chat.core.Qiscus
import com.qiscus.sdk.chat.data.util.MimeTypeGuesser
import com.qiscus.sdk.chat.domain.common.containsUrl
import com.qiscus.sdk.chat.domain.model.Message
import com.qiscus.sdk.chat.domain.model.FileAttachmentMessage
import com.qiscus.sdk.chat.presentation.model.MentionClickListener
import com.qiscus.sdk.chat.presentation.model.*

/**
 * Created on : August 19, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
@JvmOverloads
fun Message.toViewModel(@ColorInt mentionColor: Int, mentionClickListener: MentionClickListener? = null): MessageViewModel {
    return toViewModel(mentionColor, mentionColor, mentionColor, mentionClickListener)
}

@JvmOverloads
fun Message.toViewModel(
        @ColorInt mentionAllColor: Int,
        @ColorInt mentionOtherColor: Int,
        @ColorInt mentionMeColor: Int,
        mentionClickListener: MentionClickListener? = null
): MessageViewModel {

    if (this is FileAttachmentMessage) {
        return determineFileViewModel(this, mentionAllColor, mentionOtherColor, mentionMeColor, mentionClickListener)
    }

    when (type.rawType) {
        "text" -> return if (text.containsUrl()) MessageLinkViewModel(
                this,
                mentionAllColor = mentionAllColor,
                mentionOtherColor = mentionOtherColor,
                mentionMeColor = mentionMeColor,
                mentionClickListener = mentionClickListener
        ) else MessageTextViewModel(
                this,
                mentionAllColor = mentionAllColor,
                mentionOtherColor = mentionOtherColor,
                mentionMeColor = mentionMeColor,
                mentionClickListener = mentionClickListener
        )
        "account_linking" -> return MessageAccountLinkingViewModel(
                this,
                mentionAllColor = mentionAllColor,
                mentionOtherColor = mentionOtherColor,
                mentionMeColor = mentionMeColor,
                mentionClickListener = mentionClickListener
        )
        "buttons" -> return MessageButtonsViewModel(
                this,
                mentionAllColor = mentionAllColor,
                mentionOtherColor = mentionOtherColor,
                mentionMeColor = mentionMeColor,
                mentionClickListener = mentionClickListener
        )
        "card" -> return MessageCardViewModel(
                this,
                mentionAllColor = mentionAllColor,
                mentionOtherColor = mentionOtherColor,
                mentionMeColor = mentionMeColor,
                mentionClickListener = mentionClickListener
        )
        "contact_person" -> return MessageContactViewModel(
                this,
                mentionAllColor = mentionAllColor,
                mentionOtherColor = mentionOtherColor,
                mentionMeColor = mentionMeColor,
                mentionClickListener = mentionClickListener
        )
        "location" -> return MessageLocationViewModel(
                this,
                mentionAllColor = mentionAllColor,
                mentionOtherColor = mentionOtherColor,
                mentionMeColor = mentionMeColor,
                mentionClickListener = mentionClickListener
        )
        "system_event" -> return MessageSystemEventViewModel(
                this,
                mentionAllColor = mentionAllColor,
                mentionOtherColor = mentionOtherColor,
                mentionMeColor = mentionMeColor,
                mentionClickListener = mentionClickListener
        )
        "reply" -> return MessageReplyViewModel(
                this,
                mentionAllColor = mentionAllColor,
                mentionOtherColor = mentionOtherColor,
                mentionMeColor = mentionMeColor,
                mentionClickListener = mentionClickListener
        )
        else -> return MessageViewModel(
                this,
                mentionAllColor = mentionAllColor,
                mentionOtherColor = mentionOtherColor,
                mentionMeColor = mentionMeColor,
                mentionClickListener = mentionClickListener
        )
    }
}

private fun determineFileViewModel(message: FileAttachmentMessage, mentionAllColor: Int, mentionOtherColor: Int,
                                   mentionMeColor: Int, mentionClickListener: MentionClickListener?,
                                   mimeTypeGuesser: MimeTypeGuesser = Qiscus.instance.component.dataComponent.mimeTypeGuesser)
        : MessageFileViewModel {

    val type = mimeTypeGuesser.getMimeTypeFromFileName(message.attachmentName)
    return when {
        type == null -> MessageFileViewModel(message, mentionAllColor = mentionAllColor, mentionOtherColor = mentionOtherColor,
                mentionMeColor = mentionMeColor, mentionClickListener = mentionClickListener, mimeType = "")
        type.contains("image") -> MessageImageViewModel(message, mentionAllColor = mentionAllColor, mentionOtherColor = mentionOtherColor,
                mentionMeColor = mentionMeColor, mentionClickListener = mentionClickListener, mimeType = type)
        type.contains("video") -> MessageVideoViewModel(message, mentionAllColor = mentionAllColor, mentionOtherColor = mentionOtherColor,
                mentionMeColor = mentionMeColor, mentionClickListener = mentionClickListener, mimeType = type)
        type.contains("audio") -> MessageAudioViewModel(message, mentionAllColor = mentionAllColor, mentionOtherColor = mentionOtherColor,
                mentionMeColor = mentionMeColor, mentionClickListener = mentionClickListener, mimeType = type)
        else -> MessageFileViewModel(message, mentionAllColor = mentionAllColor, mentionOtherColor = mentionOtherColor,
                mentionMeColor = mentionMeColor, mentionClickListener = mentionClickListener, mimeType = type)
    }
}