package com.qiscus.sdk.chat.domain.interactor

import com.qiscus.sdk.chat.domain.executor.PostExecutionThread
import com.qiscus.sdk.chat.domain.executor.ThreadExecutor
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

/**
 * Created on : September 26, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
abstract class AbstractUseCase protected constructor(
        protected val threadExecutor: ThreadExecutor,
        protected val postExecutionThread: PostExecutionThread) {
    private val disposables = CompositeDisposable()

    /**
     * Dispose from current [CompositeDisposable].
     */
    fun dispose() {
        if (!disposables.isDisposed) disposables.dispose()
    }

    /**
     * Dispose from current [CompositeDisposable].
     */
    protected fun addDisposable(disposable: Disposable) {
        disposables.add(disposable)
    }
}