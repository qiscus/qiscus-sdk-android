package com.qiscus.sdk.chat.data.pusher.event

/**
 * Created on : September 22, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
data class UserTypingEvent(val roomId: String, val userId: String, val typing: Boolean)