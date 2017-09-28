package com.qiscus.sdk.chat.domain.interactor.account

import com.qiscus.sdk.chat.domain.executor.PostExecutionThread
import com.qiscus.sdk.chat.domain.executor.ThreadExecutor
import com.qiscus.sdk.chat.domain.interactor.CompletableUseCase
import com.qiscus.sdk.chat.domain.pubsub.QiscusPubSubClient
import com.qiscus.sdk.chat.domain.repository.AccountRepository
import com.qiscus.sdk.chat.domain.repository.CommentRepository
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
        private val commentRepository: CommentRepository,
        private val pubSubClient: QiscusPubSubClient,
        threadExecutor: ThreadExecutor,
        postExecutionThread: PostExecutionThread) : CompletableUseCase<Void?>(threadExecutor, postExecutionThread) {

    override fun buildUseCaseObservable(params: Void?): Completable {
        return commentRepository.clearData()
                .andThen(roomRepository.clearData())
                .andThen(userRepository.clearData())
                .andThen(accountRepository.clearData())
                .doOnComplete { pubSubClient.disconnect() }
    }
}