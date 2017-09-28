package com.qiscus.sdk.chat.domain.repository

import com.qiscus.sdk.chat.domain.model.Comment
import com.qiscus.sdk.chat.domain.model.CommentId
import com.qiscus.sdk.chat.domain.model.CommentState
import com.qiscus.sdk.chat.domain.model.FileAttachmentComment
import io.reactivex.Completable
import io.reactivex.Single

/**
 * Created on : August 19, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
interface CommentRepository {
    fun postComment(comment: Comment): Completable

    fun downloadAttachmentComment(comment: FileAttachmentComment): Single<FileAttachmentComment>

    fun getComments(roomId: String): Single<List<Comment>>

    fun getComments(roomId: String, lastCommentId: CommentId): Single<List<Comment>>

    fun updateCommentState(roomId: String, commentId: CommentId, commentState: CommentState): Completable

    fun deleteComment(comment: Comment): Completable

    fun clearData(): Completable
}