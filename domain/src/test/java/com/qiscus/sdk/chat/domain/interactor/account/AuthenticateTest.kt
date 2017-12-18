package com.qiscus.sdk.chat.domain.interactor.account

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import com.qiscus.sdk.chat.domain.executor.PostExecutionThread
import com.qiscus.sdk.chat.domain.executor.ThreadExecutor
import com.qiscus.sdk.chat.domain.model.Account
import com.qiscus.sdk.chat.domain.model.User
import com.qiscus.sdk.chat.domain.pubsub.PubSubClient
import com.qiscus.sdk.chat.domain.repository.AccountRepository
import io.reactivex.Single
import org.junit.Before
import org.junit.Test

/**
 * Created on : September 01, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
class AuthenticateTest {
    private lateinit var authenticate: Authenticate

    private lateinit var mockAccountRepository: AccountRepository
    private lateinit var mockPubSubClient: PubSubClient
    private lateinit var mockThreadExecutor: ThreadExecutor
    private lateinit var mockPostExecutionThread: PostExecutionThread

    @Before
    fun setUp() {
        mockAccountRepository = mock()
        mockPubSubClient = mock()
        mockThreadExecutor = mock()
        mockPostExecutionThread = mock()
        authenticate = Authenticate(mockAccountRepository, mockPubSubClient, mockThreadExecutor, mockPostExecutionThread)
    }

    @Test
    fun buildUseCaseObservableCallsRepository() {
        val token = "asagfgafsg"
        stubAccountRepositoryLogin(token)
        authenticate.buildUseCaseObservable(Authenticate.Params(token)).test()
        verify(mockAccountRepository).authenticate(token)
        verify(mockPubSubClient).restartConnection()
    }

    @Test
    fun buildUseCaseObservableCompletes() {
        val token = "asagfgafsg"
        stubAccountRepositoryLogin(token)
        val testObserver = authenticate.buildUseCaseObservable(Authenticate.Params(token)).test()
        testObserver.assertComplete()
    }

    @Test
    fun buildUseCaseObservableReturnsData() {
        val token = "asagfgafsg"
        stubAccountRepositoryLogin(token)
        val testObserver = authenticate.buildUseCaseObservable(Authenticate.Params(token)).test()
        testObserver.assertValue(Account(User("", ""), ""))
    }

    private fun stubAccountRepositoryLogin(token: String) {
        whenever(mockAccountRepository.authenticate(token))
                .thenReturn(Single.just(Account(User("", ""), "")))
    }
}