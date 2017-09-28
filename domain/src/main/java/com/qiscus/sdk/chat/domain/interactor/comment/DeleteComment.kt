package com.qiscus.sdk.chat.domain.interactor.comment

import com.qiscus.sdk.chat.domain.executor.PostExecutionThread
import com.qiscus.sdk.chat.domain.executor.ThreadExecutor
import com.qiscus.sdk.chat.domain.interactor.CompletableUseCase
import com.qiscus.sdk.chat.domain.model.Comment
import com.qiscus.sdk.chat.domain.repository.CommentRepository
import io.reactivex.Completable

/**
 * Created on : September 24, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
class DeleteComment(private val commentRepository: CommentRepository,
                    threadExecutor: ThreadExecutor, postExecutionThread: PostExecutionThread) :
        CompletableUseCase<DeleteComment.Params>(threadExecutor, postExecutionThread) {

    override fun buildUseCaseObservable(params: DeleteComment.Params?): Completable {
        return commentRepository.deleteComment(params!!.comment)
    }

    data class Params(val comment: Comment)
}