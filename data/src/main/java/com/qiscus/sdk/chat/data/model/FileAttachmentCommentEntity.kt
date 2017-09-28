package com.qiscus.sdk.chat.data.model

import org.json.JSONObject
import java.io.File

/**
 * Created on : September 26, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
class FileAttachmentCommentEntity(
        commentId: CommentIdEntity,
        var file: File?,
        val caption: String,
        defaultMessage: String,
        sender: UserEntity,
        nanoTimeStamp: Long,
        room: RoomEntity,
        state: CommentStateEntity,
        type: CommentTypeEntity
) : CommentEntity(commentId, defaultMessage, sender, nanoTimeStamp, room, state, type) {
    private var attachmentUrl = ""

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

    fun updateAttachmentUrl(url: String) {
        attachmentUrl = url
        val payload = JSONObject().put("url", attachmentUrl).put("caption", caption)
        type.payload = payload
    }


    override fun toString(): String {
        return "FileAttachmentCommentEntity(commentId=$commentId, message='$message', sender=$sender, " +
                "nanoTimeStamp=$nanoTimeStamp, room=$room, state=$state, type=$type, file=$file, " +
                "caption='$caption', attachmentUrl='$attachmentUrl')"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FileAttachmentCommentEntity) return false
        if (!super.equals(other)) return false

        if (file != other.file) return false
        if (attachmentUrl != other.attachmentUrl) return false

        return true
    }

    override fun hashCode(): Int {
        return commentId.hashCode()
    }
}