package com.qiscus.sdk.chat.presentation.model

import com.qiscus.sdk.chat.domain.model.FileAttachmentProgress

/**
 * Created on : December 27, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
interface ProgressListener {
    fun onProgress(fileAttachmentProgress: FileAttachmentProgress)
}