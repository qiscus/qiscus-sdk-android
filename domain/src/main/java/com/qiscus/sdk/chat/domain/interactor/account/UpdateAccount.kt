package com.qiscus.sdk.chat.domain.interactor.account

import com.qiscus.sdk.chat.domain.executor.PostExecutionThread
import com.qiscus.sdk.chat.domain.executor.ThreadExecutor
import com.qiscus.sdk.chat.domain.interactor.SingleUseCase
import com.qiscus.sdk.chat.domain.model.Account
import com.qiscus.sdk.chat.domain.repository.AccountRepository
import io.reactivex.Single

/**
 * Created on : September 24, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
class UpdateAccount(private val accountRepository: AccountRepository,
                    threadExecutor: ThreadExecutor,
                    postExecutionThread: PostExecutionThread) :
        SingleUseCase<Account, UpdateAccount.Params?>(threadExecutor, postExecutionThread) {

    override fun buildUseCaseObservable(params: Params?): Single<Account> {
        return accountRepository.updateAccount(params!!.name, params.avatarUrl)
    }

    data class Params(val name: String, val avatarUrl: String)
}