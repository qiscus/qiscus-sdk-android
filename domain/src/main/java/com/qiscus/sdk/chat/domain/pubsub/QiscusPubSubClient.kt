package com.qiscus.sdk.chat.domain.pubsub

/**
 * Created on : September 21, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
interface QiscusPubSubClient {
    fun connect()

    fun isConnected(): Boolean

    fun restartConnection()

    fun disconnect()

    fun listenNewComment()

    fun listenCommentState(roomId: String)

    fun unlistenCommentState(roomId: String)

    fun listenUserStatus(userId: String)

    fun unlistenUserStatus(userId: String)

    fun publishTypingStatus(roomId: String, typing: Boolean)

    fun listenUserTyping(roomId: String)

    fun unlistenUserTyping(roomId: String)
}