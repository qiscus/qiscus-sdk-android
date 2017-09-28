package com.qiscus.sdk.chat.domain.pubsub

import com.qiscus.sdk.chat.domain.model.FileAttachmentProgress
import io.reactivex.Observable

/**
 * Created on : September 27, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
interface FileObserver {
    fun listenFileAttachmentProgress(): Observable<FileAttachmentProgress>
}