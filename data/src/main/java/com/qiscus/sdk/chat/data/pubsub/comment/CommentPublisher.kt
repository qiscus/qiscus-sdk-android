package com.qiscus.sdk.chat.data.pubsub.comment

import com.qiscus.sdk.chat.data.model.CommentEntity

/**
 * Created on : August 19, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
interface CommentPublisher {
    fun onCommentAdded(commentEntity: CommentEntity)

    fun onCommentUpdated(commentEntity: CommentEntity)

    fun onCommentDeleted(commentEntity: CommentEntity)
}