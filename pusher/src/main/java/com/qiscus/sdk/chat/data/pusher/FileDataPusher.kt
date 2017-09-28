package com.qiscus.sdk.chat.data.pusher

import com.qiscus.sdk.chat.data.pubsub.file.FilePublisher
import com.qiscus.sdk.chat.data.pubsub.file.FileSubscriber
import com.qiscus.sdk.chat.domain.model.FileAttachmentProgress
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

/**
 * Created on : September 27, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
class FileDataPusher(private val publisher: PublishSubject<Any>) : FilePublisher, FileSubscriber {

    override fun onFileProgressUpdated(fileAttachmentProgress: FileAttachmentProgress) {
        publisher.onNext(fileAttachmentProgress)
    }

    override fun listenFileProgress(): Observable<FileAttachmentProgress> {
        return publisher.filter { it is FileAttachmentProgress }
                .map { it as FileAttachmentProgress }
    }
}