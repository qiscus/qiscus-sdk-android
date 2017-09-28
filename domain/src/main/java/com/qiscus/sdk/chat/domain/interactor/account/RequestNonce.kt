package com.qiscus.sdk.chat.domain.interactor.account

import com.qiscus.sdk.chat.domain.executor.PostExecutionThread
import com.qiscus.sdk.chat.domain.executor.ThreadExecutor
import com.qiscus.sdk.chat.domain.interactor.SingleUseCase
import com.qiscus.sdk.chat.domain.repository.AccountRepository
import io.reactivex.Single

/**
 * Created on : August 31, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
class RequestNonce(private val accountRepository: AccountRepository,
                   threadExecutor: ThreadExecutor, postExecutionThread: PostExecutionThread) :
        SingleUseCase<String, Void?>(threadExecutor, postExecutionThread) {

    public override fun buildUseCaseObservable(params: Void?): Single<String> {
        return accountRepository.requestNonce()
    }
}
