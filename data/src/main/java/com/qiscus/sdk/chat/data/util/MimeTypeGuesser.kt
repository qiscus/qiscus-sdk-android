package com.qiscus.sdk.chat.data.util

import java.io.File

/**
 * Created on : September 26, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
interface MimeTypeGuesser {
    fun getMimeTypeFromFile(file: File): String?

    fun getMimeTypeFromFileUrl(fileUrl: String): String?

    fun getMimeTypeFromFileName(fileName: String): String?

    fun getMimeTypeFromExtension(extension: String): String?
}