package com.qiscus.library.chat.presenter;

import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;

import com.trello.rxlifecycle.LifecycleTransformer;
import com.trello.rxlifecycle.RxLifecycle;

import rx.subjects.BehaviorSubject;

/**
 * Created on : August 18, 2016
 * Author     : zetbaitsu
 * Name       : Zetra
 * Email      : zetra@mail.ugm.ac.id
 * GitHub     : https://github.com/zetbaitsu
 * LinkedIn   : https://id.linkedin.com/in/zetbaitsu
 */
public abstract class BasePresenter<V extends BasePresenter.View> {
    private final BehaviorSubject<PresenterEvent> lifecycleSubject = BehaviorSubject.create();

    protected V view;

    public BasePresenter(V view) {
        this.view = view;
        lifecycleSubject.onNext(PresenterEvent.CREATE);
    }

    @NonNull
    @CheckResult
    public final <T> LifecycleTransformer<T> bindToLifecycle() {
        return RxLifecycle.bindUntilEvent(lifecycleSubject, PresenterEvent.DETACH);
    }

    @NonNull
    @CheckResult
    public final <T> LifecycleTransformer<T> bindUntilEvent(PresenterEvent presenterEvent) {
        return RxLifecycle.bindUntilEvent(lifecycleSubject, presenterEvent);
    }

    public void detachView() {
        view = null;
        lifecycleSubject.onNext(PresenterEvent.DETACH);
    }

    public interface View {
        void showError(String errorMessage);

        void showLoading();

        void dismissLoading();
    }
}
