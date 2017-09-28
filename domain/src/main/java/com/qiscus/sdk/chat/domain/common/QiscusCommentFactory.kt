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
}