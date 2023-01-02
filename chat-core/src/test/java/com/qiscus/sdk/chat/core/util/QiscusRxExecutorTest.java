package com.qiscus.sdk.chat.core.util;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class QiscusRxExecutorTest {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void runTest() {
        new QiscusRxExecutor();
        QiscusRxExecutor.execute(Observable.just(0), new QiscusRxExecutor.Listener() {

            @Override
            public void onSuccess(Object result) {

            }

            @Override
            public void onError(Throwable throwable) {

            }
        });

        QiscusRxExecutor.execute(
                Observable.just(0), Schedulers.io(), AndroidSchedulers.mainThread(),
                new QiscusRxExecutor.Listener() {

                    @Override
                    public void onSuccess(Object result) {

                    }

                    @Override
                    public void onError(Throwable throwable) {

                    }
                });

    }
}