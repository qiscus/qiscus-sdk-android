package com.qiscus.sdk.chat.domain.interactor.account

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import com.qiscus.sdk.chat.domain.executor.PostExecutionThread
import com.qiscus.sdk.chat.domain.executor.ThreadExecutor
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
class RequestNonceTest {
    private lateinit var requestNonce: RequestNonce

    private lateinit var mockAccountRepository: AccountRepository
    private lateinit var mockThreadExecutor: ThreadExecutor
    private lateinit var mockPostExecutionThread: PostExecutionThread

    @Before
    fun setUp() {
        mockAccountRepository = mock()
        mockThreadExecutor = mock()
        mockPostExecutionThread = mock()
        requestNonce = RequestNonce(mockAccountRepository, mockThreadExecutor, mockPostExecutionThread)
    }

    @Test
    fun buildUseCaseObservableCallsRepository() {
        requestNonce.buildUseCaseObservable()
        verify(mockAccountRepository).requestNonce()
    }

    @Test
    fun buildUseCaseObservableCompletes() {
        stubAccountRepositoryRequestNonce(Single.just("qwasqwqsa"))
        val testObserver = requestNonce.buildUseCaseObservable().test()
        testObserver.assertComplete()
    }

    @Test
    fun buildUseCaseObservableReturnsData() {
        val nonce = "asasasdsdasa"
        stubAccountRepositoryRequestNonce(Single.just(nonce))
        val testObserver = requestNonce.buildUseCaseObservable().test()
        testObserver.assertValue(nonce)
    }

    private fun stubAccountRepositoryRequestNonce(nonce: Single<String>) {
        whenever(mockAccountRepository.requestNonce()).thenReturn(nonce)
    }

}