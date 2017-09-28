package com.qiscus.sdk.chat.domain.model

/**
 * Created on : August 17, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
data class Room(
        val id: String,
        val uniqueId: String = "default",
        var name: String,
        var avatar: String = "",
        val group: Boolean = false,
        var options: String = ""
)