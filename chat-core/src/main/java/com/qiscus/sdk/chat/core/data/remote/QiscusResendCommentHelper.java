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
public class QiscusResendCommentHelper {

    private static final Map<String, Subscription> pendingTask = new ConcurrentHashMap<>();
    private static final Set<String> processingComment = new ConcurrentSkipListSet<>();

    private QiscusCore qiscusCore;

    public QiscusResendCommentHelper(QiscusCore qiscusCore) {
        this.qiscusCore = qiscusCore;
    }

    public void tryResendPendingComment() {
        qiscusCore.getDataStore()
                .getObservablePendingComments()
                .flatMap(Observable::from)
                .doOnNext(qMessage -> {
                    if (qMessage.isAttachment() && !pendingTask.containsKey(qMessage.getUniqueId())) {
                        resendFile(qMessage);
                    }
                })
                .filter(qMessage -> !qMessage.isAttachment())
                .take(1)
                .doOnNext(qMessage -> {
                    if (!pendingTask.containsKey(qMessage.getUniqueId())) {
                        resendComment(qMessage);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(commentSend -> {
                }, qiscusCore.getErrorLogger()::print);
    }

    public void cancelPendingComment(QMessage qMessage) {
        Subscription subscription = pendingTask.get(qMessage.getUniqueId());
        if (subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
        }
        pendingTask.remove(qMessage.getUniqueId());
        processingComment.remove(qMessage.getUniqueId());
    }

    private void resendComment(QMessage qMessage) {
        if (qMessage.isAttachment()) {
            resendFile(qMessage);
            return;
        }

        //Wait until this success
        if (!processingComment.isEmpty() && !processingComment.contains(qMessage.getUniqueId())) {
            return;
        }

        qMessage.setStatus(QMessage.STATE_SENDING);
        qiscusCore.getDataStore().addOrUpdate(qMessage);

        EventBus.getDefault().post(new QMessageResendEvent(qMessage));

        Subscription subscription = qiscusCore.getApi().sendMessage(qMessage)
                .doOnNext(this::commentSuccess)
                .doOnError(throwable -> commentFail(throwable, qMessage))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(commentSend -> {
                    tryResendPendingComment(); //Process next pending comments
                    EventBus.getDefault().post(new QMessageReceivedEvent(commentSend));
                }, qiscusCore.getErrorLogger()::print);

        pendingTask.put(qMessage.getUniqueId(), subscription);
        processingComment.add(qMessage.getUniqueId());
    }

    private void resendFile(QMessage qMessage) {
        qMessage.setStatus(QMessage.STATE_SENDING);
        qiscusCore.getDataStore().addOrUpdate(qMessage);

        if (qMessage.getAttachmentUri().toString().startsWith("http")) { //We forward file message
            forwardFile(qMessage);
            return;
        }

        File file = new File(qMessage.getAttachmentUri().toString());
        if (!file.exists()) { //File have been removed, so we can not upload it anymore
            qMessage.setStatus(QMessage.STATE_FAILED);
            qiscusCore.getDataStore().addOrUpdate(qMessage);
            EventBus.getDefault().post(new QMessageResendEvent(qMessage));
            return;
        }

        EventBus.getDefault().post(new QMessageResendEvent(qMessage));

        Subscription subscription = qiscusCore.getApi()
                .upload(file, total -> {
                })
                .flatMap(uri -> {
                    qMessage.updateAttachmentUrl(uri.toString());
                    return qiscusCore.getApi().sendMessage(qMessage);
                })
                .doOnNext(commentSend -> {
                    qiscusCore.getDataStore()
                            .addOrUpdateLocalPath(commentSend.getChatRoomId(), commentSend.getId(), file.getAbsolutePath());
                    commentSuccess(commentSend);
                })
                .doOnError(throwable -> commentFail(throwable, qMessage))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(commentSend ->
                                EventBus.getDefault().post(new QMessageReceivedEvent(commentSend)),
                        qiscusCore.getErrorLogger()::print);

        pendingTask.put(qMessage.getUniqueId(), subscription);
    }

    private void forwardFile(QMessage qMessage) {
        EventBus.getDefault().post(new QMessageResendEvent(qMessage));

        Subscription subscription = qiscusCore.getApi().sendMessage(qMessage)
                .doOnNext(this::commentSuccess)
                .doOnError(throwable -> commentFail(throwable, qMessage))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(commentSend ->
                                EventBus.getDefault().post(new QMessageReceivedEvent(commentSend)),
                        qiscusCore.getErrorLogger()::print);

        pendingTask.put(qMessage.getUniqueId(), subscription);
    }

    private void commentSuccess(QMessage qMessage) {
        pendingTask.remove(qMessage.getUniqueId());
        processingComment.remove(qMessage.getUniqueId());
        qMessage.setStatus(QMessage.STATE_SENT);
        QMessage savedQiscusComment = qiscusCore.getDataStore().getComment(qMessage.getUniqueId());
        if (savedQiscusComment != null && savedQiscusComment.getStatus() > qMessage.getStatus()) {
            qMessage.setStatus(savedQiscusComment.getStatus());
        }
        qiscusCore.getDataStore().addOrUpdate(qMessage);
    }

    private boolean mustFailed(Throwable throwable, QMessage qMessage) {
        //Error response from server
        //Means something wrong with server, e.g user is not participant of these room anymore
        return ((throwable instanceof HttpException && ((HttpException) throwable).code() >= 400) ||
                //if throwable from JSONException, e.g response from server not json as expected
                (throwable instanceof JSONException) ||
                // if attachment type
                qMessage.isAttachment());
    }

    private void commentFail(Throwable throwable, QMessage qMessage) {
        pendingTask.remove(qMessage.getUniqueId());
        if (!qiscusCore.getDataStore().isContains(qMessage)) { //Have been deleted
            return;
        }
        int state = QMessage.STATE_PENDING;
        if (mustFailed(throwable, qMessage)) {
            state = QMessage.STATE_FAILED;
            processingComment.remove(qMessage.getUniqueId());
        }

        //Kalo ternyata comment nya udah sukses dikirim sebelumnya, maka ga usah di update
        QMessage savedQiscusComment = qiscusCore.getDataStore().getComment(qMessage.getUniqueId());
        if (savedQiscusComment != null && savedQiscusComment.getStatus() > QMessage.STATE_SENDING) {
            return;
        }

        //Simpen statenya
        qMessage.setStatus(state);
        qiscusCore.getDataStore().addOrUpdate(qMessage);

        EventBus.getDefault().post(new QMessageResendEvent(qMessage));
    }

    public void cancelAll() {
        List<QMessage> pendingComments = qiscusCore.getDataStore().getPendingComments();
        for (QMessage qMessage : pendingComments) {
            Subscription subscription = pendingTask.get(qMessage.getUniqueId());
            if (subscription != null && !subscription.isUnsubscribed()) {
                subscription.unsubscribe();
            }
        }
        pendingTask.clear();
        processingComment.clear();
    }
}
