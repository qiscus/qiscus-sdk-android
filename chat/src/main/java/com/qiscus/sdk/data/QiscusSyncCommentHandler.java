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
import com.qiscus.sdk.data.model.QiscusComment;
import com.qiscus.sdk.data.remote.QiscusApi;
import com.qiscus.sdk.event.QiscusSyncEvent;
import com.qiscus.sdk.util.QiscusErrorLogger;
import com.qiscus.sdk.util.QiscusLogger;

import org.greenrobot.eventbus.EventBus;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created on : March 01, 2018
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public final class QiscusSyncCommentHandler {
    private QiscusSyncCommentHandler() {

    }

    public static void sync() {
        QiscusApi.getInstance().sync()
                .doOnSubscribe(() -> {
                    EventBus.getDefault().post((QiscusSyncEvent.STARTED));
                    QiscusLogger.print("Sync started...");
                })
                .doOnCompleted(() -> {
                    EventBus.getDefault().post((QiscusSyncEvent.COMPLETED));
                    QiscusLogger.print("Sync completed...");
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(QiscusCommentBuffer::push, throwable -> {
                    QiscusErrorLogger.print(throwable);
                    EventBus.getDefault().post(QiscusSyncEvent.FAILED);
                    QiscusLogger.print("Sync failed...");
                });
    }

    public static void synchronizeData() {
        synchronizeRoom();
        synchronizeComment();
    }

    public static void synchronizeRoom() {
        QiscusApi.getInstance().getChatRooms(0, 100, true)
                .flatMap(Observable::from)
                .doOnNext(qiscusChatRoom -> {
                    //If we already have valid comment in local, don't use comment from server
                    if (Qiscus.getDataStore().getLatestSentComment(qiscusChatRoom.getId()) != null) {
                        qiscusChatRoom.setLastComment(null);
                    }
                })
                .doOnNext(qiscusChatRoom -> Qiscus.getDataStore().addOrUpdate(qiscusChatRoom))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(qiscusChatRoom -> {
                }, throwable -> {
                });
    }

    private static void synchronizeComment() {
        Qiscus.getDataStore().getObservableChatRooms(1000)
                .flatMap(Observable::from)
                .flatMap(qiscusChatRoom -> {
                    QiscusComment lastComment = Qiscus.getDataStore().getLatestSentComment(qiscusChatRoom.getId());
                    if (lastComment != null) {
                        return QiscusApi.getInstance().getCommentsAfter(qiscusChatRoom.getId(), lastComment.getId());
                    }
                    return Observable.empty();
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(qiscusComment -> {
                }, throwable -> {
                });
    }
}
