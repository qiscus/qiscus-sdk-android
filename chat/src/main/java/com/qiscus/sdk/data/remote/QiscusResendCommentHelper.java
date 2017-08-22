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

import com.qiscus.sdk.Qiscus;
import com.qiscus.sdk.data.model.QiscusComment;

import java.util.Date;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created on : August 22, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
final class QiscusResendCommentHelper {
    static void tryResendFailedComment() {
        Qiscus.getDataStore()
                .getObservableFailedComments()
                .flatMap(Observable::from)
                .doOnNext(QiscusResendCommentHelper::resendComment)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(commentSend -> {

                }, throwable -> {
                    //Do nothing
                });
    }

    private static void resendComment(QiscusComment qiscusComment) {
        if (qiscusComment.isAttachment()) {
            return;
        }
        qiscusComment.setState(QiscusComment.STATE_SENDING);
        qiscusComment.setTime(new Date());
        QiscusApi.getInstance().postComment(qiscusComment)
                .doOnSubscribe(() -> Qiscus.getDataStore().add(qiscusComment))
                .doOnNext(QiscusResendCommentHelper::commentSuccess)
                .doOnError(throwable -> commentFail(qiscusComment))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(commentSend -> {

                }, throwable -> {
                    //Do nothing
                });
    }

    private static void commentSuccess(QiscusComment qiscusComment) {
        qiscusComment.setState(QiscusComment.STATE_ON_QISCUS);
        QiscusComment savedQiscusComment = Qiscus.getDataStore().getComment(qiscusComment.getId(), qiscusComment.getUniqueId());
        if (savedQiscusComment != null && savedQiscusComment.getState() > qiscusComment.getState()) {
            qiscusComment.setState(savedQiscusComment.getState());
        }
        Qiscus.getDataStore().addOrUpdate(qiscusComment);
    }

    private static void commentFail(QiscusComment qiscusComment) {
        qiscusComment.setState(QiscusComment.STATE_FAILED);
        QiscusComment savedQiscusComment = Qiscus.getDataStore().getComment(qiscusComment.getId(), qiscusComment.getUniqueId());
        if (savedQiscusComment != null) {
            if (savedQiscusComment.getState() < qiscusComment.getState()) {
                qiscusComment.setState(QiscusComment.STATE_FAILED);
                Qiscus.getDataStore().addOrUpdate(qiscusComment);
            } else {
                qiscusComment.setState(savedQiscusComment.getState());
            }
        } else {
            qiscusComment.setState(QiscusComment.STATE_FAILED);
            Qiscus.getDataStore().addOrUpdate(qiscusComment);
        }

    }
}
