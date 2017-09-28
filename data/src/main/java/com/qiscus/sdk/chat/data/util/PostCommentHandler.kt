package com.qiscus.sdk.chat.data.util

import com.qiscus.sdk.chat.data.model.CommentEntity

/**
 * Created on : September 24, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
interface PostCommentHandler {
    fun tryResendPendingComment()

    fun cancelPendingComment(comment: CommentEntity)
}