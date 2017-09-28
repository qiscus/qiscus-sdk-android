package com.qiscus.sdk.chat.data.source.file

import com.qiscus.sdk.chat.data.model.CommentIdEntity
import java.io.File

/**
 * Created on : September 26, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
interface FileLocal {
    fun saveLocalPath(commentIdEntity: CommentIdEntity, file: File)

    fun getLocalPath(commentIdEntity: CommentIdEntity): File?
}