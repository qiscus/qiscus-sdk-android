package com.qiscus.sdk.chat.domain.interactor.message

import com.qiscus.sdk.chat.domain.executor.PostExecutionThread
import com.qiscus.sdk.chat.domain.executor.ThreadExecutor
import com.qiscus.sdk.chat.domain.interactor.ObservableUseCase
import com.qiscus.sdk.chat.domain.model.Message
import com.qiscus.sdk.chat.domain.pubsub.MessageObserver
import io.reactivex.Observable

/**
 * Created on : August 19, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
class ListenMessageDeleted(private val messageObserver: MessageObserver,
                           threadExecutor: ThreadExecutor, postExecutionThread: PostExecutionThread)
    : ObservableUseCase<Message, ListenMessageDeleted.Params?>(threadExecutor, postExecutionThread) {

    override fun buildUseCaseObservable(params: Params?): Observable<Message> {
        return messageObserver.listenMessageDeleted(params!!.roomId)
    }

    data class Params(val roomId: String)
}