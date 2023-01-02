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

package com.qiscus.sdk.chat.core.data.remote;

import com.qiscus.sdk.chat.core.QiscusCore;
import com.qiscus.sdk.chat.core.data.model.QiscusComment;
import com.qiscus.sdk.chat.core.event.QiscusCommentReceivedEvent;
import com.qiscus.sdk.chat.core.event.QiscusCommentResendEvent;
import com.qiscus.sdk.chat.core.util.QiscusErrorLogger;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

import retrofit2.HttpException;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created on : August 22, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
public final class QiscusResendCommentHelper {

    private static final Map<String, Subscription> pendingTask = new ConcurrentHashMap<>();
    private static final Set<String> processingComment = new ConcurrentSkipListSet<>();

    public static void tryResendPendingComment() {
        QiscusCore.getDataStore()
                .getObservablePendingComments()
                .flatMap(Observable::from)
                .doOnNext(qiscusComment -> {
                    if (qiscusComment.isAttachment() && !pendingTask.containsKey(qiscusComment.getUniqueId())) {
                        resendFile(qiscusComment);
                    }
                })
                .filter(qiscusComment -> !qiscusComment.isAttachment())
                .take(1)
                .doOnNext(qiscusComment -> {
                    if (!pendingTask.containsKey(qiscusComment.getUniqueId())) {
                        resendComment(qiscusComment);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(commentSend -> {
                }, QiscusErrorLogger::print);
    }

    public static void cancelPendingComment(QiscusComment qiscusComment) {
        Subscription subscription = pendingTask.get(qiscusComment.getUniqueId());
        if (subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
        }
        pendingTask.remove(qiscusComment.getUniqueId());
        processingComment.remove(qiscusComment.getUniqueId());
    }

    public static void resendComment(QiscusComment qiscusComment) {
        if (qiscusComment.isAttachment()) {
            resendFile(qiscusComment);
            return;
        }

        //Wait until this success
        if (!processingComment.isEmpty() && !processingComment.contains(qiscusComment.getUniqueId())) {
            return;
        }

        qiscusComment.setState(QiscusComment.STATE_SENDING);
        QiscusCore.getDataStore().addOrUpdate(qiscusComment);

        EventBus.getDefault().post(new QiscusCommentResendEvent(qiscusComment));

        Subscription subscription = QiscusApi.getInstance().sendMessage(qiscusComment)
                .doOnNext(QiscusResendCommentHelper::commentSuccess)
                .doOnError(throwable -> commentFail(throwable, qiscusComment))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(commentSend -> {
                    tryResendPendingComment(); //Process next pending comments
                    EventBus.getDefault().post(new QiscusCommentReceivedEvent(commentSend));
                }, QiscusErrorLogger::print);

        pendingTask.put(qiscusComment.getUniqueId(), subscription);
        processingComment.add(qiscusComment.getUniqueId());
    }

    private static void resendFile(QiscusComment qiscusComment) {
        qiscusComment.setState(QiscusComment.STATE_SENDING);
        QiscusCore.getDataStore().addOrUpdate(qiscusComment);

        if (qiscusComment.getAttachmentUri().toString().startsWith("http")) { //We forward file message
            forwardFile(qiscusComment);
            return;
        }

        File file = new File(qiscusComment.getAttachmentUri().toString());
        if (!file.exists()) { //File have been removed, so we can not upload it anymore
            qiscusComment.setDownloading(false);
            qiscusComment.setState(QiscusComment.STATE_FAILED);
            QiscusCore.getDataStore().addOrUpdate(qiscusComment);
            EventBus.getDefault().post(new QiscusCommentResendEvent(qiscusComment));
            return;
        }

        qiscusComment.setDownloading(true);
        qiscusComment.setProgress(0);
        EventBus.getDefault().post(new QiscusCommentResendEvent(qiscusComment));

        Subscription subscription = QiscusApi.getInstance()
                .upload(file, percentage -> qiscusComment.setProgress((int) percentage))
                .flatMap(uri -> {
                    qiscusComment.updateAttachmentUrl(uri.toString());
                    return QiscusApi.getInstance().sendMessage(qiscusComment);
                })
                .doOnNext(commentSend -> {
                    QiscusCore.getDataStore()
                            .addOrUpdateLocalPath(commentSend.getRoomId(), commentSend.getId(), file.getAbsolutePath());
                    qiscusComment.setDownloading(false);
                    commentSuccess(commentSend);
                })
                .doOnError(throwable -> commentFail(throwable, qiscusComment))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(commentSend ->
                                EventBus.getDefault().post(new QiscusCommentReceivedEvent(commentSend)),
                        QiscusErrorLogger::print);

        pendingTask.put(qiscusComment.getUniqueId(), subscription);
    }

    private static void forwardFile(QiscusComment qiscusComment) {
        qiscusComment.setDownloading(true);
        qiscusComment.setProgress(100);
        EventBus.getDefault().post(new QiscusCommentResendEvent(qiscusComment));

        Subscription subscription = QiscusApi.getInstance().sendMessage(qiscusComment)
                .doOnNext(commentSend -> {
                    qiscusComment.setDownloading(false);
                    commentSuccess(commentSend);
                })
                .doOnError(throwable -> commentFail(throwable, qiscusComment))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(commentSend ->
                                EventBus.getDefault().post(new QiscusCommentReceivedEvent(commentSend)),
                        QiscusErrorLogger::print);

        pendingTask.put(qiscusComment.getUniqueId(), subscription);
    }

    private static void commentSuccess(QiscusComment qiscusComment) {
        pendingTask.remove(qiscusComment.getUniqueId());
        processingComment.remove(qiscusComment.getUniqueId());
        qiscusComment.setState(QiscusComment.STATE_ON_QISCUS);
        QiscusComment savedQiscusComment = QiscusCore.getDataStore().getComment(qiscusComment.getUniqueId());
        if (savedQiscusComment != null && savedQiscusComment.getState() > qiscusComment.getState()) {
            qiscusComment.setState(savedQiscusComment.getState());
        }
        QiscusCore.getDataStore().addOrUpdate(qiscusComment);
    }

    public static boolean mustFailed(Throwable throwable, QiscusComment qiscusComment) {
        //Error response from server
        //Means something wrong with server, e.g user is not member of these room anymore
        return ((throwable instanceof HttpException && ((HttpException) throwable).code() >= 400) ||
                //if throwable from JSONException, e.g response from server not json as expected
                (throwable instanceof JSONException) ||
                // if attachment type
                qiscusComment.isAttachment());
    }

    public static void commentFail(Throwable throwable, QiscusComment qiscusComment) {
        pendingTask.remove(qiscusComment.getUniqueId());
        if (!QiscusCore.getDataStore().isContains(qiscusComment)) { //Have been deleted
            return;
        }
        int state = QiscusComment.STATE_PENDING;
        if (mustFailed(throwable, qiscusComment)) {
            qiscusComment.setDownloading(false);
            state = QiscusComment.STATE_FAILED;
            processingComment.remove(qiscusComment.getUniqueId());
        }

        //Kalo ternyata comment nya udah sukses dikirim sebelumnya, maka ga usah di update
        QiscusComment savedQiscusComment = QiscusCore.getDataStore().getComment(qiscusComment.getUniqueId());
        if (savedQiscusComment != null && savedQiscusComment.getState() > QiscusComment.STATE_SENDING) {
            return;
        }

        //Simpen statenya
        qiscusComment.setState(state);
        QiscusCore.getDataStore().addOrUpdate(qiscusComment);

        EventBus.getDefault().post(new QiscusCommentResendEvent(qiscusComment));
    }

    public static void cancelAll() {
        List<QiscusComment> pendingComments = QiscusCore.getDataStore().getPendingComments();
        for (QiscusComment qiscusComment : pendingComments) {
            Subscription subscription = pendingTask.get(qiscusComment.getUniqueId());
            if (subscription != null && !subscription.isUnsubscribed()) {
                subscription.unsubscribe();
            }
        }
        pendingTask.clear();
        processingComment.clear();
    }
}
