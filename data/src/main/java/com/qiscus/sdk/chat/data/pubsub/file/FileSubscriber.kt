package com.qiscus.sdk.chat.data.pubsub.file

import com.qiscus.sdk.chat.domain.model.FileAttachmentProgress
import io.reactivex.Observable

/**
 * Created on : September 27, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
interface FileSubscriber {
    fun listenFileProgress(): Observable<FileAttachmentProgress>
}