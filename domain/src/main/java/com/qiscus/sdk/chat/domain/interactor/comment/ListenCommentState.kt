package com.qiscus.sdk.chat.domain.interactor.comment

import com.qiscus.sdk.chat.domain.executor.PostExecutionThread
import com.qiscus.sdk.chat.domain.executor.ThreadExecutor
import com.qiscus.sdk.chat.domain.interactor.ObservableUseCase
import com.qiscus.sdk.chat.domain.model.Comment
import com.qiscus.sdk.chat.domain.pubsub.CommentObserver
import io.reactivex.Observable

/**
 * Created on : August 19, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
class ListenCommentState(private val commentObserver: CommentObserver,
                         threadExecutor: ThreadExecutor, postExecutionThread: PostExecutionThread)
    : ObservableUseCase<Comment, ListenCommentState.Params?>(threadExecutor, postExecutionThread) {

    override fun buildUseCaseObservable(params: ListenCommentState.Params?): Observable<Comment> {
        return commentObserver.listenCommentState(params!!.roomId)
    }

    data class Params(val roomId: String)
}