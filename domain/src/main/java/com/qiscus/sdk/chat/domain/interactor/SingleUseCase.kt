package com.qiscus.sdk.chat.domain.interactor

import com.qiscus.sdk.chat.domain.executor.PostExecutionThread
import com.qiscus.sdk.chat.domain.executor.ThreadExecutor
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

/**
 * Abstract class for a ObservableUseCase that returns an instance of a [Single].
 */
abstract class SingleUseCase<T, in Params> protected constructor(
        threadExecutor: ThreadExecutor, postExecutionThread: PostExecutionThread)
    : AbstractUseCase(threadExecutor, postExecutionThread) {

    /**
     * Builds a [Single] which will be used when the current [SingleUseCase] is executed.
     */
    protected abstract fun buildUseCaseObservable(params: Params? = null): Single<T>

    /**
     * Executes the current use case.
     */
    fun execute(params: Params? = null, onSuccess: Action<T>) {
        val single = buildUseCaseObservable(params)
                .subscribeOn(Schedulers.from(threadExecutor))
                .observeOn(postExecutionThread.scheduler) as Single<T>
        addDisposable(single.subscribe({ onSuccess.call(it) }, {}))
    }

    fun execute(params: Params? = null, onSuccess: Action<T>, onError: Action<Throwable>) {
        val single = buildUseCaseObservable(params)
                .subscribeOn(Schedulers.from(threadExecutor))
                .observeOn(postExecutionThread.scheduler) as Single<T>
        addDisposable(single.subscribe({ onSuccess.call(it) }, { onError.call(it) }))
    }
}