package com.qiscus.sdk.chat.domain.interactor.account

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import com.qiscus.sdk.chat.domain.executor.PostExecutionThread
import com.qiscus.sdk.chat.domain.executor.ThreadExecutor
import com.qiscus.sdk.chat.domain.model.Account
import com.qiscus.sdk.chat.domain.model.User
import com.qiscus.sdk.chat.domain.repository.AccountRepository
import io.reactivex.Single
import org.junit.Before
import org.junit.Test

/**
 * Created on : August 31, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
class AuthenticateWithKeyTest {
    private lateinit var authenticate: AuthenticateWithKey

    private lateinit var mockAccountRepository: AccountRepository
    private lateinit var mockThreadExecutor: ThreadExecutor
    private lateinit var mockPostExecutionThread: PostExecutionThread

    @Before
    fun setUp() {
        mockAccountRepository = mock()
        mockThreadExecutor = mock()
        mockPostExecutionThread = mock()
        authenticate = AuthenticateWithKey(mockAccountRepository, mockThreadExecutor, mockPostExecutionThread)
    }

    @Test
    fun buildUseCaseObservableCallsRepository() {
        val id = "1234"
        val key = "12345678"
        val name = "John"
        val avatar = "http://url.com/handsome.jpg"
        authenticate.buildUseCaseObservable(AuthenticateWithKey.Params(id, key, name, avatar))
        verify(mockAccountRepository).authenticateWithKey(id, key, name, avatar)
    }

    @Test
    fun buildUseCaseObservableCompletes() {
        val id = "1234"
        val key = "12345678"
        val name = "John"
        val avatar = "http://url.com/handsome.jpg"
        stubAccountRepositoryLogin(id, key, name, avatar)
        val testObserver = authenticate.buildUseCaseObservable(AuthenticateWithKey.Params(id, key, name, avatar)).test()
        testObserver.assertComplete()
    }

    @Test
    fun buildUseCaseObservableReturnsData() {
        val id = "1234"
        val key = "12345678"
        val name = "John"
        val avatar = "http://url.com/handsome.jpg"
        stubAccountRepositoryLogin(id, key, name, avatar)
        val testObserver = authenticate.buildUseCaseObservable(AuthenticateWithKey.Params(id, key, name, avatar)).test()
        testObserver.assertValue(Account(User(id, name, avatar), ""))
    }

    private fun stubAccountRepositoryLogin(id: String, key: String, name: String, avatar: String) {
        whenever(mockAccountRepository.authenticateWithKey(id, key, name, avatar))
                .thenReturn(Single.just(Account(User(id, name, avatar), "")))
    }
}