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

package com.qiscus.sdk.chat.domain.common

import com.qiscus.sdk.chat.domain.model.*
import com.qiscus.sdk.chat.domain.repository.AccountRepository
import org.json.JSONObject
import java.io.File
import java.util.*

/**
 * Created on : September 25, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
class QiscusCommentFactory(private val accountRepository: AccountRepository) : CommentFactory {

    override fun createTextComment(roomId: String, message: String): Comment {
        return Comment(CommentId(), message, accountRepository.getAccount().blockingGet().user,
                Date(), Room(roomId, name = ""), CommentState.SENDING, CommentType("text"))
    }

    override fun createFileAttachmentComment(roomId: String, file: File, caption: String): FileAttachmentComment {
        val payload = JSONObject().put("url", "").put("caption", caption)
        return FileAttachmentComment(CommentId(), file, caption, String.format("[file] %s [/file]", ""),
                accountRepository.getAccount().blockingGet().user, Date(), Room(roomId, name = ""),
                CommentState.SENDING, CommentType("file_attachment", payload))
    }

    override fun createCustomComment(roomId: String, defaultTextMessage: String, type: String, content: JSONObject): Comment {
        return Comment(CommentId(), defaultTextMessage, accountRepository.getAccount().blockingGet().user,
                Date(), Room(roomId, name = ""), CommentState.SENDING,
                CommentType("custom", JSONObject().put("type", type).put("content", content)))
    }
}