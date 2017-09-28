package com.qiscus.sdk.chat.data.source.file

import io.reactivex.Single
import java.io.File

/**
 * Created on : September 26, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
interface FileRemote {
    fun upload(file: File, progressListener: ProgressListener): Single<String>

    fun download(attachmentUrl: String, progressListener: ProgressListener): Single<File>
}