package com.qiscus.sdk.chat.domain.common

import com.qiscus.sdk.chat.domain.model.Comment
import com.qiscus.sdk.chat.domain.model.FileAttachmentComment
import java.io.File

/**
 * Created on : September 25, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
interface CommentFactory {
    fun createTextComment(roomId: String, message: String): Comment

    fun createFileAttachmentComment(roomId: String, file: File, caption: String): FileAttachmentComment
}