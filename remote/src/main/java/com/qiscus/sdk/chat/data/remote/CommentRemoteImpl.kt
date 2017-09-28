package com.qiscus.sdk.chat.data.remote

import com.qiscus.sdk.chat.data.model.CommentEntity
import com.qiscus.sdk.chat.data.model.CommentIdEntity
import com.qiscus.sdk.chat.data.source.account.AccountLocal
import com.qiscus.sdk.chat.data.source.comment.CommentRemote
import io.reactivex.Completable
import io.reactivex.Single

/**
 * Created on : August 19, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
class CommentRemoteImpl(private val accountLocal: AccountLocal,
                        private val qiscusRestApi: QiscusRestApi) : CommentRemote {

    override fun postComment(commentEntity: CommentEntity): Single<CommentEntity> {
        return qiscusRestApi.postComment(accountLocal.getAccount().token, commentEntity.room.id,
                commentEntity.message, commentEntity.commentId.uniqueId, commentEntity.type.rawType,
                commentEntity.type.payload.toString())
                .map { it.results.comment.toEntity() }
    }

    override fun getComments(roomId: String): Single<List<CommentEntity>> {
        return qiscusRestApi.getComments(accountLocal.getAccount().token, roomId, false)
                .map { it.results.comments.map { it.toEntity() } }
    }

    override fun getComments(roomId: String, lastCommentId: CommentIdEntity, limit: Int): Single<List<CommentEntity>> {
        return qiscusRestApi.getComments(accountLocal.getAccount().token, roomId, lastCommentId.id, limit, false)
                .map { it.results.comments.map { it.toEntity() } }
    }

    override fun updateLastDeliveredComment(roomId: String, commentId: CommentIdEntity): Completable {
        return qiscusRestApi.updateCommentStatus(accountLocal.getAccount().token, roomId, commentId.id, "")
    }

    override fun updateLastReadComment(roomId: String, commentId: CommentIdEntity): Completable {
        return qiscusRestApi.updateCommentStatus(accountLocal.getAccount().token, roomId, "", commentId.id)
    }
}