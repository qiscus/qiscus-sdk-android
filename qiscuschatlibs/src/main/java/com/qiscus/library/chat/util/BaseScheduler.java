package com.qiscus.library.chat.util;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created on : December 09, 2015
 * Author     : zetbaitsu
 * Name       : Zetra
 * Email      : zetra@mail.ugm.ac.id
 * GitHub     : https://github.com/zetbaitsu
 * LinkedIn   : https://id.linkedin.com/in/zetbaitsu
 */
public enum BaseScheduler {
    HARVEST;
    private final Observable.Transformer newThread;
    private final Observable.Transformer io;
    private final Observable.Transformer computation;

    BaseScheduler() {
        newThread = new Observable.Transformer() {
            @Override
            public Observable call(Object o) {
                return ((Observable) o).subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread());
            }
        };

        io = new Observable.Transformer() {
            @Override
            public Observable call(Object o) {
                return ((Observable) o).subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread());
            }
        };

        computation = new Observable.Transformer() {
            @Override
            public Observable call(Object o) {
                return ((Observable) o).subscribeOn(Schedulers.computation())
                        .observeOn(AndroidSchedulers.mainThread());
            }
        };
    }

    public static BaseScheduler pluck() {
        return HARVEST;
    }

    @SuppressWarnings("unchecked")
    public <T> Observable.Transformer<T, T> applySchedulers(Type type) {
        switch (type) {
            case NEW_THREAD:
                return (Observable.Transformer<T, T>) newThread;
            case IO:
                return (Observable.Transformer<T, T>) io;
            case COMPUTATION:
                return (Observable.Transformer<T, T>) computation;
        }

        return (Observable.Transformer<T, T>) newThread;
    }

    public enum Type {
        NEW_THREAD, IO, COMPUTATION
    }
}
