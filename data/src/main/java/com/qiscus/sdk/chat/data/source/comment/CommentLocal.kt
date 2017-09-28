package com.qiscus.sdk.chat.data.source.comment

import com.qiscus.sdk.chat.data.model.CommentEntity
import com.qiscus.sdk.chat.data.model.CommentIdEntity

interface CommentLocal {
    fun addComment(commentEntity: CommentEntity)

    fun saveAndNotify(commentEntity: CommentEntity)

    fun updateComment(commentEntity: CommentEntity)

    fun addOrUpdateComment(commentEntity: CommentEntity)

    fun deleteComment(commentEntity: CommentEntity)

    fun getComment(commentIdEntity: CommentIdEntity): CommentEntity?

    fun getComments(roomId: String, limit: Int = -1): List<CommentEntity>

    fun getComments(roomId: String, lastCommentIdEntity: CommentIdEntity, limit: Int = -1): List<CommentEntity>

    fun getPendingComments(): List<CommentEntity>

    fun updateLastDeliveredComment(roomId: String, userId: String, commentId: CommentIdEntity)

    fun updateLastReadComment(roomId: String, userId: String, commentId: CommentIdEntity)

    fun getLastDeliveredCommentId(roomId: String): CommentIdEntity?

    fun getLastReadCommentId(roomId: String): CommentIdEntity?

    fun clearData()
}