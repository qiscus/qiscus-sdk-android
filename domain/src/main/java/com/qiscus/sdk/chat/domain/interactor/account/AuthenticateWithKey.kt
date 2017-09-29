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
import com.qiscus.sdk.chat.domain.interactor.SingleUseCase
import com.qiscus.sdk.chat.domain.model.Account
import com.qiscus.sdk.chat.domain.pubsub.QiscusPubSubClient
import com.qiscus.sdk.chat.domain.repository.AccountRepository
import io.reactivex.Single

/**
 * Created on : August 30, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
class AuthenticateWithKey(private val accountRepository: AccountRepository, private val pubSubClient: QiscusPubSubClient,
                          threadExecutor: ThreadExecutor, postExecutionThread: PostExecutionThread) :
        SingleUseCase<Account, AuthenticateWithKey.Params>(threadExecutor, postExecutionThread) {

    public override fun buildUseCaseObservable(params: AuthenticateWithKey.Params?): Single<Account> {
        return accountRepository.authenticateWithKey(params!!.userId, params.userKey, params.name, params.avatarUrl)
                .doOnSuccess { pubSubClient.restartConnection() }
    }

    data class Params(val userId: String, val userKey: String, val name: String = userId, val avatarUrl: String = "") {
        constructor(userId: String, userKey: String, name: String) : this(userId, userKey, name, "")
        constructor(userId: String, userKey: String) : this(userId, userKey, userId)
    }
}