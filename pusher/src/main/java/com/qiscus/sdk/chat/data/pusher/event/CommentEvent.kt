package com.qiscus.sdk.chat.data.pusher.event

import com.qiscus.sdk.chat.data.model.CommentEntity

/**
 * Created on : September 20, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
internal data class CommentEvent(val commentEntity: CommentEntity, val type: Type) {
    internal enum class Type {
        ADDED, UPDATED, DELETED
    }
}