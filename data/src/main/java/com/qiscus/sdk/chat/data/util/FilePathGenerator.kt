package com.qiscus.sdk.chat.data.util

/**
 * Created on : September 26, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
interface FilePathGenerator {
    fun generateFilePath(attachmentUrl: String): String
}