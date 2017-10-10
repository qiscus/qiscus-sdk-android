/*
 * Copyright (c) 2016 Qiscus.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
class GetComments(private val commentRepository: CommentRepository,
                  threadExecutor: ThreadExecutor, postExecutionThread: PostExecutionThread)
    : SingleUseCase<GetComments.Result, GetComments.Params>(threadExecutor, postExecutionThread) {

    public override fun buildUseCaseObservable(params: Params?): Single<GetComments.Result> {
        if (params!!.lastCommentId != null) {
            return commentRepository.getComments(params.roomId, params.lastCommentId!!, params.limit)
                    .map {
                        Result(it, it.isNotEmpty() && it.filter { it.state.intValue > CommentState.SENDING.intValue }
                                .none { it.commentId.commentBeforeId == "0" })
                    }
        }

        return commentRepository.getComments(params.roomId).map {
            Result(it, it.isNotEmpty() && it.filter { it.state.intValue > CommentState.SENDING.intValue }
                    .none { it.commentId.commentBeforeId == "0" })
        }
    }

    data class Params @JvmOverloads constructor(val roomId: String, val lastCommentId: CommentId? = null, val limit: Int = 20)

    data class Result(val comments: List<Comment>, private val moreMessages: Boolean) {
        fun hasMoreMessages(): Boolean {
            return moreMessages
        }
    }
}