package com.qiscus.sdk.chat.domain.interactor.comment

import com.qiscus.sdk.chat.domain.executor.PostExecutionThread
import com.qiscus.sdk.chat.domain.executor.ThreadExecutor
import com.qiscus.sdk.chat.domain.interactor.SingleUseCase
import com.qiscus.sdk.chat.domain.model.Comment
import com.qiscus.sdk.chat.domain.repository.CommentRepository
import io.reactivex.Single

/**
 * Created on : September 24, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
class GetComments(private val commentRepository: CommentRepository,
                  threadExecutor: ThreadExecutor, postExecutionThread: PostExecutionThread)
    : SingleUseCase<List<Comment>, GetComments.Params>(threadExecutor, postExecutionThread) {

    public override fun buildUseCaseObservable(params: Params?): Single<List<Comment>> {
        return commentRepository.getComments(params!!.roomId)
    }

    data class Params(val roomId: String)
}