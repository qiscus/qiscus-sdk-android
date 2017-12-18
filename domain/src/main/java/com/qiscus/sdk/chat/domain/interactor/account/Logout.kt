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

package com.qiscus.sdk.chat.domain.interactor.account

import com.qiscus.sdk.chat.domain.executor.PostExecutionThread
import com.qiscus.sdk.chat.domain.executor.ThreadExecutor
import com.qiscus.sdk.chat.domain.interactor.CompletableUseCase
import com.qiscus.sdk.chat.domain.pubsub.PubSubClient
import com.qiscus.sdk.chat.domain.repository.AccountRepository
import com.qiscus.sdk.chat.domain.repository.MessageRepository
import com.qiscus.sdk.chat.domain.repository.RoomRepository
import com.qiscus.sdk.chat.domain.repository.UserRepository
import io.reactivex.Completable

/**
 * Created on : September 21, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
class Logout(
        private val accountRepository: AccountRepository,
        private val userRepository: UserRepository,
        private val roomRepository: RoomRepository,
        private val messageRepository: MessageRepository,
        private val pubSubClient: PubSubClient,
        threadExecutor: ThreadExecutor,
        postExecutionThread: PostExecutionThread) : CompletableUseCase<Void?>(threadExecutor, postExecutionThread) {

    override fun buildUseCaseObservable(params: Void?): Completable {
        return messageRepository.clearData()
                .andThen(roomRepository.clearData())
                .andThen(userRepository.clearData())
                .andThen(accountRepository.clearData())
                .doOnComplete { pubSubClient.disconnect() }
    }
}