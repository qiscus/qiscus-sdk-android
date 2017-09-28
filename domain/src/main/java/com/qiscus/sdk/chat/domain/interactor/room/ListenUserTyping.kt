package com.qiscus.sdk.chat.domain.interactor.room

import com.qiscus.sdk.chat.domain.executor.PostExecutionThread
import com.qiscus.sdk.chat.domain.executor.ThreadExecutor
import com.qiscus.sdk.chat.domain.interactor.ObservableUseCase
import com.qiscus.sdk.chat.domain.model.UserTyping
import com.qiscus.sdk.chat.domain.pubsub.RoomObserver
import io.reactivex.Observable

/**
 * Created on : September 22, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
class ListenUserTyping(private val roomObserver: RoomObserver,
                       threadExecutor: ThreadExecutor, postExecutionThread: PostExecutionThread)
    : ObservableUseCase<UserTyping, ListenUserTyping.Params>(threadExecutor, postExecutionThread) {

    override fun buildUseCaseObservable(params: Params?): Observable<UserTyping> {
        return roomObserver.listenUserTyping(params!!.roomId)
    }

    data class Params(val roomId: String)
}