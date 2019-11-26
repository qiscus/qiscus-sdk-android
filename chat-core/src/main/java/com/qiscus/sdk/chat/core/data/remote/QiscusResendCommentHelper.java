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
import com.qiscus.sdk.chat.core.data.model.QMessage;
import com.qiscus.sdk.chat.core.event.QMessageReceivedEvent;
import com.qiscus.sdk.chat.core.event.QMessageResendEvent;
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
                .doOnNext(qiscusMessage -> {
                    if (qiscusMessage.isAttachment() && !pendingTask.containsKey(qiscusMessage.getUniqueId())) {
                        resendFile(qiscusMessage);
                    }
                })
                .filter(qiscusMessage -> !qiscusMessage.isAttachment())
                .take(1)
                .doOnNext(qiscusMessage -> {
                    if (!pendingTask.containsKey(qiscusMessage.getUniqueId())) {
                        resendComment(qiscusMessage);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(commentSend -> {
                }, QiscusErrorLogger::print);
    }

    public static void cancelPendingComment(QMessage qiscusMessage) {
        Subscription subscription = pendingTask.get(qiscusMessage.getUniqueId());
        if (subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
        }
        pendingTask.remove(qiscusMessage.getUniqueId());
        processingComment.remove(qiscusMessage.getUniqueId());
    }

    private static void resendComment(QMessage qiscusMessage) {
        if (qiscusMessage.isAttachment()) {
            resendFile(qiscusMessage);
            return;
        }

        //Wait until this success
        if (!processingComment.isEmpty() && !processingComment.contains(qiscusMessage.getUniqueId())) {
            return;
        }

        qiscusMessage.setState(QMessage.STATE_SENDING);
        QiscusCore.getDataStore().addOrUpdate(qiscusMessage);

        EventBus.getDefault().post(new QMessageResendEvent(qiscusMessage));

        Subscription subscription = QiscusApi.getInstance().sendMessage(qiscusMessage)
                .doOnNext(QiscusResendCommentHelper::commentSuccess)
                .doOnError(throwable -> commentFail(throwable, qiscusMessage))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(commentSend -> {
                    tryResendPendingComment(); //Process next pending comments
                    EventBus.getDefault().post(new QMessageReceivedEvent(commentSend));
                }, QiscusErrorLogger::print);

        pendingTask.put(qiscusMessage.getUniqueId(), subscription);
        processingComment.add(qiscusMessage.getUniqueId());
    }

    private static void resendFile(QMessage qiscusMessage) {
        qiscusMessage.setState(QMessage.STATE_SENDING);
        QiscusCore.getDataStore().addOrUpdate(qiscusMessage);

        if (qiscusMessage.getAttachmentUri().toString().startsWith("http")) { //We forward file message
            forwardFile(qiscusMessage);
            return;
        }

        File file = new File(qiscusMessage.getAttachmentUri().toString());
        if (!file.exists()) { //File have been removed, so we can not upload it anymore
            qiscusMessage.setDownloading(false);
            qiscusMessage.setState(QMessage.STATE_FAILED);
            QiscusCore.getDataStore().addOrUpdate(qiscusMessage);
            EventBus.getDefault().post(new QMessageResendEvent(qiscusMessage));
            return;
        }

        qiscusMessage.setDownloading(true);
        qiscusMessage.setProgress(0);
        EventBus.getDefault().post(new QMessageResendEvent(qiscusMessage));

        Subscription subscription = QiscusApi.getInstance()
                .upload(file, percentage -> qiscusMessage.setProgress((int) percentage))
                .flatMap(uri -> {
                    qiscusMessage.updateAttachmentUrl(uri.toString());
                    return QiscusApi.getInstance().sendMessage(qiscusMessage);
                })
                .doOnNext(commentSend -> {
                    QiscusCore.getDataStore()
                            .addOrUpdateLocalPath(commentSend.getChatRoomId(), commentSend.getId(), file.getAbsolutePath());
                    qiscusMessage.setDownloading(false);
                    commentSuccess(commentSend);
                })
                .doOnError(throwable -> commentFail(throwable, qiscusMessage))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(commentSend ->
                                EventBus.getDefault().post(new QMessageReceivedEvent(commentSend)),
                        QiscusErrorLogger::print);

        pendingTask.put(qiscusMessage.getUniqueId(), subscription);
    }

    private static void forwardFile(QMessage qiscusMessage) {
        qiscusMessage.setDownloading(true);
        qiscusMessage.setProgress(100);
        EventBus.getDefault().post(new QMessageResendEvent(qiscusMessage));

        Subscription subscription = QiscusApi.getInstance().sendMessage(qiscusMessage)
                .doOnNext(commentSend -> {
                    qiscusMessage.setDownloading(false);
                    commentSuccess(commentSend);
                })
                .doOnError(throwable -> commentFail(throwable, qiscusMessage))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(commentSend ->
                                EventBus.getDefault().post(new QMessageReceivedEvent(commentSend)),
                        QiscusErrorLogger::print);

        pendingTask.put(qiscusMessage.getUniqueId(), subscription);
    }

    private static void commentSuccess(QMessage qiscusMessage) {
        pendingTask.remove(qiscusMessage.getUniqueId());
        processingComment.remove(qiscusMessage.getUniqueId());
        qiscusMessage.setState(QMessage.STATE_ON_QISCUS);
        QMessage savedQMessage = QiscusCore.getDataStore().getComment(qiscusMessage.getUniqueId());
        if (savedQMessage != null && savedQMessage.getState() > qiscusMessage.getState()) {
            qiscusMessage.setState(savedQMessage.getState());
        }
        QiscusCore.getDataStore().addOrUpdate(qiscusMessage);
    }

    private static boolean mustFailed(Throwable throwable, QMessage qiscusMessage) {
        //Error response from server
        //Means something wrong with server, e.g user is not participants of these room anymore
        return ((throwable instanceof HttpException && ((HttpException) throwable).code() >= 400) ||
                //if throwable from JSONException, e.g response from server not json as expected
                (throwable instanceof JSONException) ||
                // if attachment type
                qiscusMessage.isAttachment());
    }

    private static void commentFail(Throwable throwable, QMessage qiscusMessage) {
        pendingTask.remove(qiscusMessage.getUniqueId());
        if (!QiscusCore.getDataStore().isContains(qiscusMessage)) { //Have been deleted
            return;
        }
        int state = QMessage.STATE_PENDING;
        if (mustFailed(throwable, qiscusMessage)) {
            qiscusMessage.setDownloading(false);
            state = QMessage.STATE_FAILED;
            processingComment.remove(qiscusMessage.getUniqueId());
        }

        //Kalo ternyata comment nya udah sukses dikirim sebelumnya, maka ga usah di update
        QMessage savedQMessage = QiscusCore.getDataStore().getComment(qiscusMessage.getUniqueId());
        if (savedQMessage != null && savedQMessage.getState() > QMessage.STATE_SENDING) {
            return;
        }

        //Simpen statenya
        qiscusMessage.setState(state);
        QiscusCore.getDataStore().addOrUpdate(qiscusMessage);

        EventBus.getDefault().post(new QMessageResendEvent(qiscusMessage));
    }

    public static void cancelAll() {
        List<QMessage> pendingComments = QiscusCore.getDataStore().getPendingComments();
        for (QMessage qiscusMessage : pendingComments) {
            Subscription subscription = pendingTask.get(qiscusMessage.getUniqueId());
            if (subscription != null && !subscription.isUnsubscribed()) {
                subscription.unsubscribe();
            }
        }
        pendingTask.clear();
        processingComment.clear();
    }
}
