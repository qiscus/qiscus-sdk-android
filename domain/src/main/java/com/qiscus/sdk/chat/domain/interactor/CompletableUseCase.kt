package com.qiscus.sdk.chat.domain.interactor

import com.qiscus.sdk.chat.domain.executor.PostExecutionThread
import com.qiscus.sdk.chat.domain.executor.ThreadExecutor
import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers

/**
 * Abstract class for a ObservableUseCase that returns an instance of a [Completable].
 */
abstract class CompletableUseCase<in Params> protected constructor(
        threadExecutor: ThreadExecutor, postExecutionThread: PostExecutionThread)
    : AbstractUseCase(threadExecutor, postExecutionThread) {

    /**
     * Builds a [Completable] which will be used when the current [CompletableUseCase] is executed.
     */
    protected abstract fun buildUseCaseObservable(params: Params? = null): Completable

    fun execute(params: Params? = null) {
        val completable = buildUseCaseObservable(params)
                .subscribeOn(Schedulers.from(threadExecutor))
                .observeOn(postExecutionThread.scheduler)
        addDisposable(completable.subscribe({}, {}))
    }

    fun execute(params: Params? = null, onComplete: Action<Void?>) {
        val completable = buildUseCaseObservable(params)
                .subscribeOn(Schedulers.from(threadExecutor))
                .observeOn(postExecutionThread.scheduler)
        addDisposable(completable.subscribe({ onComplete.call(null) }, {}))
    }

    fun execute(params: Params? = null, onComplete: Action<Void?>, onError: Action<Throwable>) {
        val completable = buildUseCaseObservable(params)
                .subscribeOn(Schedulers.from(threadExecutor))
                .observeOn(postExecutionThread.scheduler)
        addDisposable(completable.subscribe({ onComplete.call(null) }, { onError.call(it) }))
    }
}