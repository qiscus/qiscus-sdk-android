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
import com.qiscus.sdk.chat.domain.model.Comment
import com.qiscus.sdk.chat.domain.model.FileAttachmentComment
import com.qiscus.sdk.chat.presentation.MentionClickHandler
import com.qiscus.sdk.chat.presentation.model.*

/**
 * Created on : August 19, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
@JvmOverloads
fun Comment.toViewModel(@ColorInt mentionColor: Int, mentionClickHandler: MentionClickHandler? = null): CommentViewModel {
    return toViewModel(mentionColor, mentionColor, mentionColor, mentionClickHandler)
}

@JvmOverloads
fun Comment.toViewModel(
        @ColorInt mentionAllColor: Int,
        @ColorInt mentionOtherColor: Int,
        @ColorInt mentionMeColor: Int,
        mentionClickHandler: MentionClickHandler? = null
): CommentViewModel {

    if (this is FileAttachmentComment) {
        return determineFileViewModel(this, mentionAllColor, mentionOtherColor, mentionMeColor, mentionClickHandler)
    }

    when (type.rawType) {
        "text" -> return CommentTextViewModel(
                this,
                mentionAllColor = mentionAllColor,
                mentionOtherColor = mentionOtherColor,
                mentionMeColor = mentionMeColor,
                mentionClickListener = mentionClickHandler

        )
        "account_linking" -> return CommentAccountLinkingViewModel(
                this,
                mentionAllColor = mentionAllColor,
                mentionOtherColor = mentionOtherColor,
                mentionMeColor = mentionMeColor,
                mentionClickListener = mentionClickHandler

        )
        "buttons" -> return CommentButtonsViewModel(
                this,
                mentionAllColor = mentionAllColor,
                mentionOtherColor = mentionOtherColor,
                mentionMeColor = mentionMeColor,
                mentionClickListener = mentionClickHandler

        )
        "card" -> return CommentCardViewModel(
                this,
                mentionAllColor = mentionAllColor,
                mentionOtherColor = mentionOtherColor,
                mentionMeColor = mentionMeColor,
                mentionClickListener = mentionClickHandler

        )
        "contact_person" -> return CommentContactViewModel(
                this,
                mentionAllColor = mentionAllColor,
                mentionOtherColor = mentionOtherColor,
                mentionMeColor = mentionMeColor,
                mentionClickListener = mentionClickHandler

        )
        "location" -> return CommentLocationViewModel(
                this,
                mentionAllColor = mentionAllColor,
                mentionOtherColor = mentionOtherColor,
                mentionMeColor = mentionMeColor,
                mentionClickListener = mentionClickHandler

        )
        "system_event" -> return CommentSystemEventViewModel(
                this,
                mentionAllColor = mentionAllColor,
                mentionOtherColor = mentionOtherColor,
                mentionMeColor = mentionMeColor,
                mentionClickListener = mentionClickHandler

        )
        "reply" -> return CommentReplyViewModel(
                this,
                mentionAllColor = mentionAllColor,
                mentionOtherColor = mentionOtherColor,
                mentionMeColor = mentionMeColor,
                mentionClickListener = mentionClickHandler

        )
        else -> return CommentViewModel(
                this,
                mentionAllColor = mentionAllColor,
                mentionOtherColor = mentionOtherColor,
                mentionMeColor = mentionMeColor,
                mentionClickListener = mentionClickHandler

        )
    }
}

private fun determineFileViewModel(comment: FileAttachmentComment, mentionAllColor: Int, mentionOtherColor: Int,
                                   mentionMeColor: Int, mentionClickHandler: MentionClickHandler?,
                                   mimeTypeGuesser: MimeTypeGuesser = Qiscus.instance.component.dataComponent.mimeTypeGuesser)
        : CommentFileViewModel {

    val type = mimeTypeGuesser.getMimeTypeFromFileName(comment.attachmentName)
    return when {
        type == null -> CommentFileViewModel(comment, mentionAllColor = mentionAllColor, mentionOtherColor = mentionOtherColor,
                mentionMeColor = mentionMeColor, mentionClickListener = mentionClickHandler, mimeType = "")
        type.contains("image") -> CommentImageViewModel(comment, mentionAllColor = mentionAllColor, mentionOtherColor = mentionOtherColor,
                mentionMeColor = mentionMeColor, mentionClickListener = mentionClickHandler, mimeType = type)
        type.contains("video") -> CommentVideoViewModel(comment, mentionAllColor = mentionAllColor, mentionOtherColor = mentionOtherColor,
                mentionMeColor = mentionMeColor, mentionClickListener = mentionClickHandler, mimeType = type)
        type.contains("audio") -> CommentAudioViewModel(comment, mentionAllColor = mentionAllColor, mentionOtherColor = mentionOtherColor,
                mentionMeColor = mentionMeColor, mentionClickListener = mentionClickHandler, mimeType = type)
        else -> CommentFileViewModel(comment, mentionAllColor = mentionAllColor, mentionOtherColor = mentionOtherColor,
                mentionMeColor = mentionMeColor, mentionClickListener = mentionClickHandler, mimeType = type)
    }
}