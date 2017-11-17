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

package com.qiscus.sdk.chat.domain.interactor.message

import com.qiscus.sdk.chat.domain.executor.PostExecutionThread
import com.qiscus.sdk.chat.domain.executor.ThreadExecutor
import com.qiscus.sdk.chat.domain.interactor.SingleUseCase
import com.qiscus.sdk.chat.domain.model.Message
import com.qiscus.sdk.chat.domain.model.MessageId
import com.qiscus.sdk.chat.domain.model.MessageState
import com.qiscus.sdk.chat.domain.repository.MessageRepository
import io.reactivex.Single

/**
 * Created on : September 24, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
class GetMessages(private val messageRepository: MessageRepository,
                  threadExecutor: ThreadExecutor, postExecutionThread: PostExecutionThread)
    : SingleUseCase<GetMessages.Result, GetMessages.Params>(threadExecutor, postExecutionThread) {

    public override fun buildUseCaseObservable(params: Params?): Single<GetMessages.Result> {
        if (params!!.lastMessageId != null) {
            return messageRepository.getMessages(params.roomId, params.lastMessageId!!, params.limit)
                    .map {
                        Result(it, it.isNotEmpty() && it.filter { it.state.intValue > MessageState.SENDING.intValue }
                                .none { it.messageId.beforeId == "0" })
                    }
        }

        return messageRepository.getMessages(params.roomId).map {
            Result(it, it.isNotEmpty() && it.filter { it.state.intValue > MessageState.SENDING.intValue }
                    .none { it.messageId.beforeId == "0" })
        }
    }

    data class Params @JvmOverloads constructor(val roomId: String, val lastMessageId: MessageId? = null, val limit: Int = 20)

    data class Result(val messages: List<Message>, private val moreMessages: Boolean) {
        fun hasMoreMessages(): Boolean {
            return moreMessages
        }
    }
}