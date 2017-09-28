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