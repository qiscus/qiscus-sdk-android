package com.qiscus.sdk.chat.data

import com.qiscus.sdk.chat.data.mapper.toDomainModel
import com.qiscus.sdk.chat.data.pubsub.comment.CommentSubscriber
import com.qiscus.sdk.chat.domain.model.Comment
import com.qiscus.sdk.chat.domain.pubsub.CommentObserver
import com.qiscus.sdk.chat.domain.pubsub.QiscusPubSubClient
import io.reactivex.Observable

/**
 * Created on : August 19, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
class CommentDataObserver(private val pubSubClient: QiscusPubSubClient,
                          private val commentSubscriber: CommentSubscriber) : CommentObserver {

    override fun listenNewComment(): Observable<Comment> {
        return commentSubscriber.listenCommentAdded().map { it.toDomainModel() }
    }

    override fun listenCommentState(roomId: String): Observable<Comment> {
        return commentSubscriber.listenCommentUpdated()
                .doOnSubscribe { pubSubClient.listenCommentState(roomId) }
                .doOnDispose { pubSubClient.unlistenCommentState(roomId) }
                .filter { it.room.id == roomId }
                .map { it.toDomainModel() }
    }
}