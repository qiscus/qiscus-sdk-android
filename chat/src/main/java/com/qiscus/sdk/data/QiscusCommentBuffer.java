/*
 * Copyright (c) 2016 Qiscus.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.qiscus.sdk.data;

import android.support.annotation.RestrictTo;

import com.qiscus.sdk.Qiscus;
import com.qiscus.sdk.data.local.QiscusDataStore;
import com.qiscus.sdk.data.model.QiscusComment;
import com.qiscus.sdk.data.remote.QiscusApi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import rx.Emitter;
import rx.Observable;
import rx.schedulers.Schedulers;

/**
 * Created on : April 26, 2018
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public class QiscusCommentBuffer {
    private static final Queue<QiscusComment> data = new ConcurrentLinkedQueue<>();
    private static final AtomicLong lastPushTime = new AtomicLong(System.currentTimeMillis());
    private static final AtomicBoolean started = new AtomicBoolean(false);

    public static void push(QiscusComment comment) {
        if (!data.contains(comment)) {
            data.add(comment);
            lastPushTime.set(System.currentTimeMillis());
        }
    }

    public static void pull() {
        if (started.get()) {
            return;
        }

        started.set(true);

        Observable<List<QiscusComment>> task = Observable.create(subscriber -> {
            while (true) {

                if (!Qiscus.hasSetupUser()) { //Stop if logout
                    data.clear();
                    started.set(false);
                    subscriber.onCompleted();
                    return;
                }

                long duration = System.currentTimeMillis() - lastPushTime.get();
                if (duration >= 1000 && !data.isEmpty()) {
                    long commentIdNeedToSync = 0;
                    if (duration < 10000 && data.size() <= 20) {
                        commentIdNeedToSync = validateData();
                    }
                    if (commentIdNeedToSync == 0) {
                        List<QiscusComment> copy = new ArrayList<>(data);
                        Collections.sort(copy, (o1, o2) -> o1.getTime().compareTo(o2.getTime()));
                        data.clear();
                        subscriber.onNext(copy);
                    } else if (commentIdNeedToSync > 0) {
                        synchronizeData(commentIdNeedToSync);
                    }
                    continue;
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    subscriber.onError(e);
                    subscriber.onCompleted();
                    break;
                }
            }

            subscriber.onCompleted();

        }, Emitter.BackpressureMode.BUFFER);

        task.flatMap(Observable::from)
                .concatMap(comment -> Observable.just(comment).delay(500, TimeUnit.MILLISECONDS))
                .subscribeOn(Schedulers.newThread())
                .subscribe(QiscusNewCommentHandler::handle, throwable -> started.set(false));
    }

    private static void synchronizeData(long commentId) {
        List<QiscusComment> comments = QiscusApi.getInstance()
                .sync(commentId)
                .toList()
                .toBlocking()
                .first();

        for (QiscusComment comment : comments) {
            push(comment);
        }
    }

    /**
     * @return comment id will be used to sync, 0 if comments is valid, -1 if comments is empty
     */
    private static long validateData() {
        if (data.isEmpty()) {
            return -1;
        }

        QiscusComment minimumComment = data.peek();
        for (QiscusComment comment : data) {
            if (comment.getId() < minimumComment.getId()) {
                minimumComment = comment;
            }
        }

        QiscusDataStore dataStore = Qiscus.getDataStore();
        if (minimumComment.getCommentBeforeId() > 0 && !dataStore.isCommentsEmpty()) {
            QiscusComment beforeComment = dataStore.getComment(minimumComment.getCommentBeforeId());
            if (beforeComment == null) {
                return minimumComment.getCommentBeforeId();
            }
        }

        for (QiscusComment comment : data) {
            if (comment.getId() > minimumComment.getId()
                    && !containsCommentWithId(comment.getCommentBeforeId())) {
                return comment.getCommentBeforeId();
            }
        }

        return 0;
    }

    private static boolean containsCommentWithId(long id) {
        for (QiscusComment comment : data) {
            if (comment.getId() == id) {
                return true;
            }
        }
        return false;
    }
}
