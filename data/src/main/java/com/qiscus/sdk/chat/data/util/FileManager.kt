package com.qiscus.sdk.chat.data.util

import java.io.File

/**
 * Created on : September 26, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
interface FileManager {
    fun saveFile(file: File): File
}