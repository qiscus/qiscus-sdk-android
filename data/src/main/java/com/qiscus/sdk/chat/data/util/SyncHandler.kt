package com.qiscus.sdk.chat.data.util

import com.qiscus.sdk.chat.data.model.CommentIdEntity

/**
 * Created on : September 29, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
interface SyncHandler {
    fun sync()

    fun sync(lastCommentId: CommentIdEntity)

    fun sync(roomId: String, lastCommentId: CommentIdEntity)
}