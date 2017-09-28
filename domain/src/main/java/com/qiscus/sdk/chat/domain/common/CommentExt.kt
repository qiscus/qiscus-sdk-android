package com.qiscus.sdk.chat.domain.common

import com.qiscus.sdk.chat.domain.model.Comment

/**
 * Created on : September 26, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
fun Comment.isAttachment(): Boolean {
    val trimmedMessage = message.trim()
    return trimmedMessage.startsWith("[file]") && trimmedMessage.endsWith("[/file]") ||
            type.rawType.isNotBlank() && type.rawType == "file_attachment"
}