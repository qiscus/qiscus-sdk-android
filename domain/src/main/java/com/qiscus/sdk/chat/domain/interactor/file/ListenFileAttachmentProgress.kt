package com.qiscus.sdk.chat.domain.interactor.file

import com.qiscus.sdk.chat.domain.executor.PostExecutionThread
import com.qiscus.sdk.chat.domain.executor.ThreadExecutor
import com.qiscus.sdk.chat.domain.interactor.ObservableUseCase
import com.qiscus.sdk.chat.domain.model.FileAttachmentProgress
import com.qiscus.sdk.chat.domain.pubsub.FileObserver
import io.reactivex.Observable

/**
 * Created on : September 27, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
class ListenFileAttachmentProgress(private val fileObserver: FileObserver,
                                   threadExecutor: ThreadExecutor, postExecutionThread: PostExecutionThread)
    : ObservableUseCase<FileAttachmentProgress, Void>(threadExecutor, postExecutionThread) {

    override fun buildUseCaseObservable(params: Void?): Observable<FileAttachmentProgress> {
        return fileObserver.listenFileAttachmentProgress()
    }
}