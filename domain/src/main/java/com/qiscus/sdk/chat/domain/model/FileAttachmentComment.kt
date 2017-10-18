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

import com.qiscus.sdk.chat.domain.common.getAttachmentName
import java.io.File
import java.util.*

/**
 * Created on : September 26, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
class FileAttachmentComment(
        commentId: CommentId,
        var file: File?,
        val caption: String,
        defaultMessage: String,
        sender: User,
        date: Date,
        room: Room,
        state: CommentState,
        type: CommentType
) : Comment(commentId, defaultMessage, sender, date, room, state, type) {

    val attachmentUrl by lazy {
        var url = ""
        if (type.payload.has("url")) {
            url = type.payload.optString("url")
        }
        if (url.isBlank() && message.startsWith("[file]") && message.endsWith("[/file]")) {
            url = message.replace("[file]", "")
                    .replace("[/file]", "")
                    .trim()
        }
        url
    }

    val attachmentName by lazy {
        var name = attachmentUrl.getAttachmentName()
        if (name.isBlank() && file != null) {
            name = file!!.name
        }
        name
    }

    override fun toString(): String {
        return "FileAttachmentComment(commentId=$commentId, message='$message', sender=$sender, date=$date, " +
                "room=$room, state=$state, type=$type, file=$file, caption='$caption', " +
                "attachmentUrl='$attachmentUrl')"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FileAttachmentComment) return false
        if (!super.equals(other)) return false

        if (file != other.file) return false
        if (attachmentUrl != other.attachmentUrl) return false

        return true
    }

    override fun hashCode(): Int {
        return commentId.hashCode()
    }
}