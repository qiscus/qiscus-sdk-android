package com.qiscus.sdk.chat.domain.interactor.comment

import com.qiscus.sdk.chat.domain.executor.PostExecutionThread
import com.qiscus.sdk.chat.domain.executor.ThreadExecutor
import com.qiscus.sdk.chat.domain.interactor.SingleUseCase
import com.qiscus.sdk.chat.domain.model.Comment
import com.qiscus.sdk.chat.domain.model.CommentId
import com.qiscus.sdk.chat.domain.model.CommentState
import com.qiscus.sdk.chat.domain.repository.CommentRepository
import io.reactivex.Single

/**
 * Created on : September 24, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
class GetMoreComments(private val commentRepository: CommentRepository,
                      threadExecutor: ThreadExecutor, postExecutionThread: PostExecutionThread)
    : SingleUseCase<GetMoreComments.Result, GetMoreComments.Params>(threadExecutor, postExecutionThread) {

    public override fun buildUseCaseObservable(params: Params?): Single<GetMoreComments.Result> {
        return commentRepository.getComments(params!!.roomId, params.lastCommentId, params.limit)
                .map {
                    Result(it, it.filter { it.state.intValue > CommentState.SENDING.intValue }
                            .none { it.commentId.commentBeforeId == "0" })
                }
    }

    data class Params(val roomId: String, val lastCommentId: CommentId, val limit: Int = 20)

    data class Result(val comments: List<Comment>, val hasMoreMessages: Boolean)
}