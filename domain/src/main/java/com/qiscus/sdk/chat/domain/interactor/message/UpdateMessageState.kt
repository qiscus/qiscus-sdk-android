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
import com.qiscus.sdk.chat.domain.interactor.CompletableUseCase
import com.qiscus.sdk.chat.domain.model.MessageId
import com.qiscus.sdk.chat.domain.model.MessageState
import com.qiscus.sdk.chat.domain.repository.MessageRepository
import io.reactivex.Completable

/**
 * Created on : September 27, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
class UpdateMessageState(private val messageRepository: MessageRepository,
                         threadExecutor: ThreadExecutor, postExecutionThread: PostExecutionThread) :
        CompletableUseCase<UpdateMessageState.Params>(threadExecutor, postExecutionThread) {

    override fun buildUseCaseObservable(params: Params?): Completable {
        return messageRepository.updateMessageState(params!!.roomId, params.messageId, params.messageState)
    }

    data class Params(val roomId: String, val messageId: MessageId, val messageState: MessageState)
}