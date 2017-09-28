package com.qiscus.sdk.chat.data.source.comment

import com.qiscus.sdk.chat.data.model.CommentEntity
import com.qiscus.sdk.chat.data.model.CommentIdEntity
import io.reactivex.Completable
import io.reactivex.Single

interface CommentRemote {
    fun postComment(commentEntity: CommentEntity): Single<CommentEntity>

    fun getComments(roomId: String): Single<List<CommentEntity>>

    fun getComments(roomId: String, lastCommentId: CommentIdEntity): Single<List<CommentEntity>>

    fun updateLastDeliveredComment(roomId: String, commentId: CommentIdEntity): Completable

    fun updateLastReadComment(roomId: String, commentId: CommentIdEntity): Completable
}