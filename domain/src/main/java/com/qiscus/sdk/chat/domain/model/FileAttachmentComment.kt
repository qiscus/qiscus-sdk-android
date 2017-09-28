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
    private var attachmentUrl = ""
    private var attachmentName = ""

    init {
        determineAttachmentUrl()
    }

    private fun determineAttachmentUrl() {
        if (attachmentUrl.isBlank() && type.payload.has("url")) {
            attachmentUrl = type.payload.optString("url", "")
        }

        if (attachmentUrl.isBlank() && message.startsWith("[file]") && message.endsWith("[/file]")) {
            attachmentUrl = message.replace("[file]", "")
                    .replace("[/file]", "")
                    .trim()
        }
    }

    fun getAttachmentUrl(): String {
        if (attachmentUrl.isBlank()) {
            determineAttachmentUrl()
        }
        return attachmentUrl
    }

    fun getAttachmentName(): String {
        if (attachmentName.isBlank()) {
            attachmentName = getAttachmentUrl().getAttachmentName()
        }
        return attachmentName
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