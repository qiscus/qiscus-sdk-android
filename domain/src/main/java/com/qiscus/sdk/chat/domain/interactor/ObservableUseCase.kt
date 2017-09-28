package com.qiscus.sdk.chat.domain.interactor

import com.qiscus.sdk.chat.domain.executor.PostExecutionThread
import com.qiscus.sdk.chat.domain.executor.ThreadExecutor
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers

/**
 * Abstract class for a ObservableUseCase that returns an instance of a [Observable].
 */
abstract class ObservableUseCase<T, in Params> protected constructor(
        threadExecutor: ThreadExecutor, postExecutionThread: PostExecutionThread)
    : AbstractUseCase(threadExecutor, postExecutionThread) {

    /**
     * Builds a [Observable] which will be used when the current [ObservableUseCase] is executed.
     */
    protected abstract fun buildUseCaseObservable(params: Params? = null): Observable<T>

    /**
     * Executes the current use case.
     */
    fun execute(params: Params? = null, onNext: Action<T>) {
        val observable = buildUseCaseObservable(params)
                .subscribeOn(Schedulers.from(threadExecutor))
                .observeOn(postExecutionThread.scheduler) as Observable<T>
        addDisposable(observable.subscribe({ onNext.call(it) }, {}))
    }

    fun execute(params: Params? = null, onNext: Action<T>, onError: Action<Throwable>) {
        val observable = buildUseCaseObservable(params)
                .subscribeOn(Schedulers.from(threadExecutor))
                .observeOn(postExecutionThread.scheduler) as Observable<T>
        addDisposable(observable.subscribe({ onNext.call(it) }, { onError.call(it) }))
    }
}