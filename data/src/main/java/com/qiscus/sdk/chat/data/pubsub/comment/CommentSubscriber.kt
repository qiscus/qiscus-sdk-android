package com.qiscus.sdk.chat.data.pubsub.comment

import com.qiscus.sdk.chat.data.model.CommentEntity
import io.reactivex.Observable

/**
 * Created on : August 19, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
interface CommentSubscriber {
    fun listenCommentAdded(): Observable<CommentEntity>

    fun listenCommentUpdated(): Observable<CommentEntity>

    fun listenCommentDeleted(): Observable<CommentEntity>
}