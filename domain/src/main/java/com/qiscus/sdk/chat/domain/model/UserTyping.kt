package com.qiscus.sdk.chat.domain.model

/**
 * Created on : September 22, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
data class UserTyping(val roomId: String, val user: User, val typing: Boolean)