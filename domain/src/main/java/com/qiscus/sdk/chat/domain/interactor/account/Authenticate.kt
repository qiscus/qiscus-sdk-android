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

class Authenticate(private val accountRepository: AccountRepository, private val pubSubClient: QiscusPubSubClient,
                   threadExecutor: ThreadExecutor, postExecutionThread: PostExecutionThread) :
        SingleUseCase<Account, Authenticate.Params>(threadExecutor, postExecutionThread) {

    public override fun buildUseCaseObservable(params: Authenticate.Params?): Single<Account> {
        return accountRepository.authenticate(params!!.token)
                .doOnSuccess { pubSubClient.restartConnection() }
    }

    data class Params(val token: String)
}