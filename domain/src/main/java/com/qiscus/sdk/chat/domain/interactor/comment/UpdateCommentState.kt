package com.qiscus.sdk.chat.domain.interactor.comment

import com.qiscus.sdk.chat.domain.executor.PostExecutionThread
import com.qiscus.sdk.chat.domain.executor.ThreadExecutor
import com.qiscus.sdk.chat.domain.interactor.CompletableUseCase
import com.qiscus.sdk.chat.domain.model.CommentId
import com.qiscus.sdk.chat.domain.model.CommentState
import com.qiscus.sdk.chat.domain.repository.CommentRepository
import io.reactivex.Completable

/**
 * Created on : September 27, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
class UpdateCommentState(private val commentRepository: CommentRepository,
                         threadExecutor: ThreadExecutor, postExecutionThread: PostExecutionThread) :
        CompletableUseCase<UpdateCommentState.Params>(threadExecutor, postExecutionThread) {

    override fun buildUseCaseObservable(params: Params?): Completable {
        return commentRepository.updateCommentState(params!!.roomId, params.commentId, params.commentState)
    }

    data class Params(val roomId: String, val commentId: CommentId, val commentState: CommentState)
}