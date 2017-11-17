package com.qiscus.sdk.chat.data.util

import com.qiscus.sdk.chat.data.model.MessageIdEntity

/**
 * Created on : September 29, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
interface SyncHandler {
    fun sync()

    fun sync(lastMessageId: MessageIdEntity)

    fun sync(roomId: String, lastMessageId: MessageIdEntity)
}