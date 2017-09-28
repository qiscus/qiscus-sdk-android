package com.qiscus.sdk.chat.domain.interactor.room

import com.qiscus.sdk.chat.domain.executor.PostExecutionThread
import com.qiscus.sdk.chat.domain.executor.ThreadExecutor
import com.qiscus.sdk.chat.domain.interactor.CompletableUseCase
import com.qiscus.sdk.chat.domain.pubsub.RoomObserver
import io.reactivex.Completable

/**
 * Created on : September 27, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
class PublishTyping(private val roomObserver: RoomObserver,
                    threadExecutor: ThreadExecutor, postExecutionThread: PostExecutionThread)
    : CompletableUseCase<PublishTyping.Params>(threadExecutor, postExecutionThread) {

    override fun buildUseCaseObservable(params: Params?): Completable {
        return roomObserver.setTyping(params!!.roomId, params.typing)
    }

    data class Params(val roomId: String, val typing: Boolean)
}