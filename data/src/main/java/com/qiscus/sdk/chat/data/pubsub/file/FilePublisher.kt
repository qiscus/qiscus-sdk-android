package com.qiscus.sdk.chat.data.pubsub.file

import com.qiscus.sdk.chat.domain.model.FileAttachmentProgress

/**
 * Created on : September 27, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
interface FilePublisher {
    fun onFileProgressUpdated(fileAttachmentProgress: FileAttachmentProgress)
}