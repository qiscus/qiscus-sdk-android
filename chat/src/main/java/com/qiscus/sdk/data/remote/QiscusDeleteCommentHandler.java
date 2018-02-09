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

package com.qiscus.sdk.data.remote;

import android.support.annotation.RestrictTo;
import android.util.Log;

import com.qiscus.sdk.Qiscus;
import com.qiscus.sdk.data.model.QiscusComment;
import com.qiscus.sdk.event.QiscusDeleteMessageEvent;
import com.qiscus.sdk.util.QiscusErrorLogger;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created on : February 08, 2018
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public final class QiscusDeleteCommentHandler {
    private QiscusDeleteCommentHandler() {

    }

    public static void handle(QiscusDeleteMessageEvent event) {
        Log.d("ZETRA", event.toString());
        if (event.isHardDelete()) {
            handleHardDelete(event);
        } else {
            handleSoftDelete(event);
        }
    }

    private static void handleSoftDelete(QiscusDeleteMessageEvent event) {
        Observable.from(event.getDeletedComments())
                .doOnNext(deletedComment -> {
                    QiscusComment qiscusComment = Qiscus.getDataStore()
                            .getComment(-1, deletedComment.getCommentUniqueId());
                    if (qiscusComment != null) {
                        qiscusComment.setMessage("This message has been deleted");
                        qiscusComment.setRawType("text");
                        Qiscus.getDataStore().addOrUpdate(qiscusComment);
                    }

                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(deletedComment -> {
                }, QiscusErrorLogger::print);
    }

    private static void handleHardDelete(QiscusDeleteMessageEvent event) {
        Observable.from(event.getDeletedComments())
                .doOnNext(deletedComment -> {
                    QiscusComment qiscusComment = Qiscus.getDataStore()
                            .getComment(-1, deletedComment.getCommentUniqueId());
                    if (qiscusComment != null) {
                        Qiscus.getDataStore().delete(qiscusComment);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(deletedComment -> {
                }, QiscusErrorLogger::print);
    }
}
