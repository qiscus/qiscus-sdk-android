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
import com.qiscus.sdk.chat.domain.model.Message
import com.qiscus.sdk.chat.domain.model.FileAttachmentMessage
import com.qiscus.sdk.chat.presentation.MentionClickHandler
import com.qiscus.sdk.chat.presentation.model.*

/**
 * Created on : August 19, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
@JvmOverloads
fun Message.toViewModel(@ColorInt mentionColor: Int, mentionClickHandler: MentionClickHandler? = null): MessageViewModel {
    return toViewModel(mentionColor, mentionColor, mentionColor, mentionClickHandler)
}

@JvmOverloads
fun Message.toViewModel(
        @ColorInt mentionAllColor: Int,
        @ColorInt mentionOtherColor: Int,
        @ColorInt mentionMeColor: Int,
        mentionClickHandler: MentionClickHandler? = null
): MessageViewModel {

    if (this is FileAttachmentMessage) {
        return determineFileViewModel(this, mentionAllColor, mentionOtherColor, mentionMeColor, mentionClickHandler)
    }

    when (type.rawType) {
        "text" -> return MessageTextViewModel(
                this,
                mentionAllColor = mentionAllColor,
                mentionOtherColor = mentionOtherColor,
                mentionMeColor = mentionMeColor,
                mentionClickListener = mentionClickHandler

        )
        "account_linking" -> return MessageAccountLinkingViewModel(
                this,
                mentionAllColor = mentionAllColor,
                mentionOtherColor = mentionOtherColor,
                mentionMeColor = mentionMeColor,
                mentionClickListener = mentionClickHandler

        )
        "buttons" -> return MessageButtonsViewModel(
                this,
                mentionAllColor = mentionAllColor,
                mentionOtherColor = mentionOtherColor,
                mentionMeColor = mentionMeColor,
                mentionClickListener = mentionClickHandler

        )
        "card" -> return MessageCardViewModel(
                this,
                mentionAllColor = mentionAllColor,
                mentionOtherColor = mentionOtherColor,
                mentionMeColor = mentionMeColor,
                mentionClickListener = mentionClickHandler

        )
        "contact_person" -> return MessageContactViewModel(
                this,
                mentionAllColor = mentionAllColor,
                mentionOtherColor = mentionOtherColor,
                mentionMeColor = mentionMeColor,
                mentionClickListener = mentionClickHandler

        )
        "location" -> return MessageLocationViewModel(
                this,
                mentionAllColor = mentionAllColor,
                mentionOtherColor = mentionOtherColor,
                mentionMeColor = mentionMeColor,
                mentionClickListener = mentionClickHandler

        )
        "system_event" -> return MessageSystemEventViewModel(
                this,
                mentionAllColor = mentionAllColor,
                mentionOtherColor = mentionOtherColor,
                mentionMeColor = mentionMeColor,
                mentionClickListener = mentionClickHandler

        )
        "reply" -> return MessageReplyViewModel(
                this,
                mentionAllColor = mentionAllColor,
                mentionOtherColor = mentionOtherColor,
                mentionMeColor = mentionMeColor,
                mentionClickListener = mentionClickHandler

        )
        else -> return MessageViewModel(
                this,
                mentionAllColor = mentionAllColor,
                mentionOtherColor = mentionOtherColor,
                mentionMeColor = mentionMeColor,
                mentionClickListener = mentionClickHandler

        )
    }
}

private fun determineFileViewModel(message: FileAttachmentMessage, mentionAllColor: Int, mentionOtherColor: Int,
                                   mentionMeColor: Int, mentionClickHandler: MentionClickHandler?,
                                   mimeTypeGuesser: MimeTypeGuesser = Qiscus.instance.component.dataComponent.mimeTypeGuesser)
        : MessageFileViewModel {

    val type = mimeTypeGuesser.getMimeTypeFromFileName(message.attachmentName)
    return when {
        type == null -> MessageFileViewModel(message, mentionAllColor = mentionAllColor, mentionOtherColor = mentionOtherColor,
                mentionMeColor = mentionMeColor, mentionClickListener = mentionClickHandler, mimeType = "")
        type.contains("image") -> MessageImageViewModel(message, mentionAllColor = mentionAllColor, mentionOtherColor = mentionOtherColor,
                mentionMeColor = mentionMeColor, mentionClickListener = mentionClickHandler, mimeType = type)
        type.contains("video") -> MessageVideoViewModel(message, mentionAllColor = mentionAllColor, mentionOtherColor = mentionOtherColor,
                mentionMeColor = mentionMeColor, mentionClickListener = mentionClickHandler, mimeType = type)
        type.contains("audio") -> MessageAudioViewModel(message, mentionAllColor = mentionAllColor, mentionOtherColor = mentionOtherColor,
                mentionMeColor = mentionMeColor, mentionClickListener = mentionClickHandler, mimeType = type)
        else -> MessageFileViewModel(message, mentionAllColor = mentionAllColor, mentionOtherColor = mentionOtherColor,
                mentionMeColor = mentionMeColor, mentionClickListener = mentionClickHandler, mimeType = type)
    }
}