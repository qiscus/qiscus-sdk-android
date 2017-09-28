package com.qiscus.sdk.chat.data

import com.qiscus.sdk.chat.data.pubsub.file.FileSubscriber
import com.qiscus.sdk.chat.domain.model.FileAttachmentProgress
import com.qiscus.sdk.chat.domain.pubsub.FileObserver
import io.reactivex.Observable

/**
 * Created on : September 27, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
class FileDataObserver(private val fileSubscriber: FileSubscriber) : FileObserver {

    override fun listenFileAttachmentProgress(): Observable<FileAttachmentProgress> {
        return fileSubscriber.listenFileProgress()
    }
}