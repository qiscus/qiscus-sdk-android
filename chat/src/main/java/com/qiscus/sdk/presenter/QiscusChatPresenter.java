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

package com.qiscus.sdk.presenter;

import android.net.Uri;
import android.support.v4.util.Pair;
import android.webkit.MimeTypeMap;

import com.qiscus.sdk.Qiscus;
import com.qiscus.sdk.R;
import com.qiscus.sdk.data.local.QiscusCacheManager;
import com.qiscus.sdk.data.model.QiscusAccount;
import com.qiscus.sdk.data.model.QiscusChatRoom;
import com.qiscus.sdk.data.model.QiscusComment;
import com.qiscus.sdk.data.model.QiscusContact;
import com.qiscus.sdk.data.model.QiscusLocation;
import com.qiscus.sdk.data.model.QiscusRoomMember;
import com.qiscus.sdk.data.remote.QiscusApi;
import com.qiscus.sdk.data.remote.QiscusPusherApi;
import com.qiscus.sdk.event.QiscusChatRoomEvent;
import com.qiscus.sdk.event.QiscusCommentReceivedEvent;
import com.qiscus.sdk.event.QiscusCommentResendEvent;
import com.qiscus.sdk.event.QiscusMqttStatusEvent;
import com.qiscus.sdk.util.QiscusAndroidUtil;
import com.qiscus.sdk.util.QiscusErrorLogger;
import com.qiscus.sdk.util.QiscusFileUtil;
import com.qiscus.sdk.util.QiscusImageUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import retrofit2.HttpException;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func2;
import rx.schedulers.Schedulers;

public class QiscusChatPresenter extends QiscusPresenter<QiscusChatPresenter.View> {

    private QiscusChatRoom room;
    private int currentTopicId;
    private QiscusAccount qiscusAccount;
    private AtomicInteger lastDeliveredCommentId;
    private AtomicInteger lastReadCommentId;
    private Func2<QiscusComment, QiscusComment, Integer> commentComparator = (lhs, rhs) -> lhs.getId() != -1 && rhs.getId() != -1 ?
            QiscusAndroidUtil.compare(rhs.getId(), lhs.getId()) : rhs.getTime().compareTo(lhs.getTime());
    private Runnable listenRoomTask;

    public QiscusChatPresenter(View view, QiscusChatRoom room) {
        super(view);
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        this.room = room;
        this.currentTopicId = room.getLastTopicId();
        qiscusAccount = Qiscus.getQiscusAccount();
        lastDeliveredCommentId = new AtomicInteger(0);
        lastReadCommentId = new AtomicInteger(0);

        updateReadState();
        listenRoomTask = this::listenRoomEvent;
        QiscusAndroidUtil.runOnUIThread(listenRoomTask, 1000);
    }

    private void updateReadState() {
        QiscusAndroidUtil.runOnBackgroundThread(() -> {
            updateLastReadComment(Qiscus.getDataStore().getLatestReadComment(currentTopicId));
            updateLastDeliveredComment(Qiscus.getDataStore().getLatestDeliveredComment(currentTopicId));
        });
    }

    private boolean updateLastDeliveredCommentId(int id) {
        if (id > lastDeliveredCommentId.get()) {
            lastDeliveredCommentId.set(id);
            return true;
        }

        return false;
    }

    private boolean updateLastReadCommentId(int id) {
        if (id > lastReadCommentId.get()) {
            lastReadCommentId.set(id);
            updateLastDeliveredCommentId(id);
            return true;
        }
        return false;
    }

    private void commentSuccess(QiscusComment qiscusComment) {
        qiscusComment.setState(QiscusComment.STATE_ON_QISCUS);
        QiscusComment savedQiscusComment = Qiscus.getDataStore().getComment(qiscusComment.getId(), qiscusComment.getUniqueId());
        if (savedQiscusComment != null && savedQiscusComment.getState() > qiscusComment.getState()) {
            qiscusComment.setState(savedQiscusComment.getState());
        }
        Qiscus.getDataStore().addOrUpdate(qiscusComment);
    }

    private void commentFail(Throwable throwable, QiscusComment qiscusComment) {
        int state = QiscusComment.STATE_PENDING;
        if (throwable instanceof HttpException) { //Error response from server
            //Means something wrong with server, e.g user is not member of these room anymore
            HttpException httpException = (HttpException) throwable;
            if (httpException.code() >= 400 && httpException.code() < 500) {
                qiscusComment.setDownloading(false);
                state = QiscusComment.STATE_FAILED;
            }
        }

        qiscusComment.setState(state);
        QiscusComment savedQiscusComment = Qiscus.getDataStore().getComment(qiscusComment.getId(), qiscusComment.getUniqueId());
        if (savedQiscusComment != null) {
            if (savedQiscusComment.getState() < qiscusComment.getState()) {
                qiscusComment.setState(state);
                Qiscus.getDataStore().addOrUpdate(qiscusComment);
            } else {
                qiscusComment.setState(savedQiscusComment.getState());
            }
        } else {
            qiscusComment.setState(state);
            Qiscus.getDataStore().addOrUpdate(qiscusComment);
        }
    }

    private void sendComment(QiscusComment qiscusComment) {
        view.onSendingComment(qiscusComment);
        QiscusApi.getInstance().postComment(qiscusComment)
                .doOnSubscribe(() -> Qiscus.getDataStore().add(qiscusComment))
                .doOnNext(this::commentSuccess)
                .doOnError(throwable -> commentFail(throwable, qiscusComment))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(bindToLifecycle())
                .subscribe(commentSend -> {
                    if (commentSend.getTopicId() == currentTopicId) {
                        view.onSuccessSendComment(commentSend);
                    }
                }, throwable -> {
                    QiscusErrorLogger.print(throwable);
                    throwable.printStackTrace();
                    if (qiscusComment.getTopicId() == currentTopicId) {
                        view.onFailedSendComment(qiscusComment);
                    }
                });
    }

    public void sendComment(String content) {
        QiscusComment qiscusComment = QiscusComment.generateMessage(content, room.getId(), currentTopicId);
        sendComment(qiscusComment);
    }

    public void sendContact(QiscusContact contact) {
        QiscusComment qiscusComment = QiscusComment.generateContactMessage(contact, room.getId(), currentTopicId);
        sendComment(qiscusComment);
    }

    public void sendLocation(QiscusLocation location) {
        QiscusComment qiscusComment = QiscusComment.generateLocationMessage(location, room.getId(), currentTopicId);
        sendComment(qiscusComment);
    }

    public void sendCommentPostBack(String content, String payload) {
        QiscusComment qiscusComment = QiscusComment.generatePostBackMessage(content, payload, room.getId(), currentTopicId);
        sendComment(qiscusComment);
    }

    public void sendReplyComment(String content, QiscusComment originComment) {
        QiscusComment qiscusComment = QiscusComment.generateReplyMessage(content, room.getId(), currentTopicId, originComment);
        sendComment(qiscusComment);
    }

    public void resendComment(QiscusComment qiscusComment) {
        qiscusComment.setState(QiscusComment.STATE_SENDING);
        qiscusComment.setTime(new Date());
        if (qiscusComment.isAttachment()) {
            resendFile(qiscusComment);
        } else {
            sendComment(qiscusComment);
        }
    }

    public void sendFile(File file) {
        File compressedFile = file;
        if (QiscusImageUtil.isImage(file) && !file.getName().endsWith(".gif")) {
            try {
                compressedFile = QiscusImageUtil.compressImage(Uri.fromFile(file), currentTopicId);
            } catch (NullPointerException e) {
                view.showError(QiscusAndroidUtil.getString(R.string.qiscus_corrupted_file));
                return;
            }
        } else {
            compressedFile = QiscusFileUtil.saveFile(compressedFile, currentTopicId);
        }

        if (!file.exists()) { //File have been removed, so we can not upload it anymore
            view.showError(QiscusAndroidUtil.getString(R.string.qiscus_corrupted_file));
            return;
        }

        QiscusComment qiscusComment = QiscusComment.generateMessage(String.format("[file] %s [/file]", compressedFile.getPath()),
                room.getId(), currentTopicId);
        qiscusComment.setDownloading(true);
        view.onSendingComment(qiscusComment);

        File finalCompressedFile = compressedFile;
        QiscusApi.getInstance().uploadFile(compressedFile, percentage -> qiscusComment.setProgress((int) percentage))
                .doOnSubscribe(() -> Qiscus.getDataStore().add(qiscusComment))
                .flatMap(uri -> {
                    qiscusComment.setMessage(String.format("[file] %s [/file]", uri.toString()));
                    return QiscusApi.getInstance().postComment(qiscusComment);
                })
                .doOnNext(commentSend -> {
                    Qiscus.getDataStore()
                            .addOrUpdateLocalPath(commentSend.getTopicId(), commentSend.getId(), finalCompressedFile.getAbsolutePath());
                    qiscusComment.setDownloading(false);
                    commentSuccess(commentSend);
                })
                .doOnError(throwable -> commentFail(throwable, qiscusComment))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(bindToLifecycle())
                .subscribe(commentSend -> {
                    if (commentSend.getTopicId() == currentTopicId) {
                        view.onSuccessSendComment(commentSend);
                    }
                }, throwable -> {
                    QiscusErrorLogger.print(throwable);
                    throwable.printStackTrace();
                    if (qiscusComment.getTopicId() == currentTopicId) {
                        view.onFailedSendComment(qiscusComment);
                    }
                });
    }

    private void resendFile(QiscusComment qiscusComment) {
        if (QiscusAndroidUtil.isUrl(qiscusComment.getAttachmentUri().toString())) { //We forward file message
            forwardFile(qiscusComment);
            return;
        }

        File file = new File(qiscusComment.getAttachmentUri().toString());
        if (!file.exists()) { //File have been removed, so we can not upload it anymore
            qiscusComment.setDownloading(false);
            qiscusComment.setState(QiscusComment.STATE_FAILED);
            Qiscus.getDataStore().addOrUpdate(qiscusComment);
            view.onFailedSendComment(qiscusComment);
            return;
        }

        qiscusComment.setProgress(0);
        QiscusApi.getInstance().uploadFile(file, percentage -> qiscusComment.setProgress((int) percentage))
                .doOnSubscribe(() -> Qiscus.getDataStore().addOrUpdate(qiscusComment))
                .flatMap(uri -> {
                    qiscusComment.setMessage(String.format("[file] %s [/file]", uri.toString()));
                    return QiscusApi.getInstance().postComment(qiscusComment);
                })
                .doOnNext(commentSend -> {
                    Qiscus.getDataStore()
                            .addOrUpdateLocalPath(commentSend.getTopicId(), commentSend.getId(), file.getAbsolutePath());
                    qiscusComment.setDownloading(false);
                    commentSuccess(commentSend);
                })
                .doOnError(throwable -> commentFail(throwable, qiscusComment))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(bindToLifecycle())
                .subscribe(commentSend -> {
                    if (commentSend.getTopicId() == currentTopicId) {
                        view.onSuccessSendComment(commentSend);
                    }
                }, throwable -> {
                    QiscusErrorLogger.print(throwable);
                    throwable.printStackTrace();
                    if (qiscusComment.getTopicId() == currentTopicId) {
                        view.onFailedSendComment(qiscusComment);
                    }
                });
    }

    private void forwardFile(QiscusComment qiscusComment) {
        qiscusComment.setProgress(100);
        QiscusApi.getInstance().postComment(qiscusComment)
                .doOnSubscribe(() -> Qiscus.getDataStore().addOrUpdate(qiscusComment))
                .doOnNext(commentSend -> {
                    qiscusComment.setDownloading(false);
                    commentSuccess(commentSend);
                })
                .doOnError(throwable -> {
                    qiscusComment.setDownloading(false);
                    commentFail(throwable, qiscusComment);
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(bindToLifecycle())
                .subscribe(commentSend -> {
                    if (commentSend.getTopicId() == currentTopicId) {
                        view.onSuccessSendComment(commentSend);
                    }
                }, throwable -> {
                    QiscusErrorLogger.print(throwable);
                    throwable.printStackTrace();
                    if (qiscusComment.getTopicId() == currentTopicId) {
                        view.onFailedSendComment(qiscusComment);
                    }
                });
    }

    public void deleteComment(QiscusComment qiscusComment) {
        QiscusAndroidUtil.runOnBackgroundThread(() -> Qiscus.getDataStore().delete(qiscusComment));
        view.onCommentDeleted(qiscusComment);
    }

    private Observable<Pair<QiscusChatRoom, List<QiscusComment>>> getInitRoomData() {
        return QiscusApi.getInstance().getChatRoomComments(room.getId())
                .doOnSubscribe(() -> QiscusAndroidUtil.runOnUIThread(() -> {
                    if (view != null) {
                        view.showLoadMoreLoading();
                    }
                }))
                .doOnError(throwable -> {
                    QiscusErrorLogger.print(throwable);
                    throwable.printStackTrace();
                    QiscusAndroidUtil.runOnUIThread(() -> {
                        if (view != null) {
                            view.showError(QiscusAndroidUtil.getString(R.string.qiscus_failed_load_comments));
                            view.dismissLoading();
                        }
                    });
                })
                .doOnNext(roomData -> {
                    updateRoomState(roomData.first.getMember());
                    checkForLastRead(roomData.second);
                    updateCommentState(roomData.second, false);

                    Collections.sort(roomData.second, (lhs, rhs) -> lhs.getId() != -1 && rhs.getId() != -1 ?
                            QiscusAndroidUtil.compare(rhs.getId(), lhs.getId()) : rhs.getTime().compareTo(lhs.getTime()));

                    if (!roomData.first.isGroup()) {
                        roomData.first.setName(room.getName());
                    }
                    roomData.first.setSubtitle(room.getSubtitle());
                    Qiscus.getDataStore().addOrUpdate(roomData.first);
                })
                .doOnNext(roomData -> QiscusAndroidUtil.runOnUIThread(() -> {
                    if (view != null) {
                        view.dismissLoading();
                    }
                }))
                .subscribeOn(Schedulers.io())
                .onErrorReturn(throwable -> null);
    }

    private void updateRoomState(List<QiscusRoomMember> members) {
        for (QiscusRoomMember qiscusRoomMember : members) {
            if (!qiscusRoomMember.getEmail().equals(qiscusAccount.getEmail())) {
                updateLastReadCommentId(qiscusRoomMember.getLastReadCommentId());
                updateLastDeliveredCommentId(qiscusRoomMember.getLastDeliveredCommentId());
            }
        }
    }

    private void checkForLastRead(List<QiscusComment> qiscusComments) {
        for (QiscusComment qiscusComment : qiscusComments) {
            if (!qiscusComment.getSenderEmail().equals(qiscusAccount.getEmail())) {
                updateLastReadCommentId(qiscusComment.getId());
            }
        }
    }

    private Observable<List<QiscusComment>> getCommentsFromNetwork(int lastCommentId) {
        return QiscusApi.getInstance().getComments(room.getId(), currentTopicId, lastCommentId)
                .doOnNext(qiscusComment -> {
                    qiscusComment.setRoomId(room.getId());
                    updateCommentState(qiscusComment, false);
                })
                .toSortedList(commentComparator)
                .doOnNext(this::checkForLastRead)
                .subscribeOn(Schedulers.io());
    }

    private Observable<List<QiscusComment>> getLocalComments(int count, boolean forceFailedSendingComment) {
        return Qiscus.getDataStore().getObservableComments(currentTopicId, 2 * count)
                .flatMap(Observable::from)
                .toSortedList(commentComparator)
                .map(comments -> {
                    if (comments.size() > count) {
                        return comments.subList(0, count);
                    }
                    return comments;
                })
                .doOnNext(comments -> {
                    checkForLastRead(comments);
                    updateCommentState(comments, forceFailedSendingComment);
                })
                .subscribeOn(Schedulers.io());
    }

    private void updateCommentState(List<QiscusComment> comments, boolean fromLocal) {
        for (QiscusComment qiscusComment : comments) {
            updateCommentState(qiscusComment, fromLocal);
        }
    }

    private void updateCommentState(QiscusComment qiscusComment, boolean fromLocal) {
        if (fromLocal && qiscusComment.getState() == QiscusComment.STATE_SENDING) {
            qiscusComment.setState(QiscusComment.STATE_PENDING);
            Qiscus.getDataStore().addOrUpdate(qiscusComment);
        } else if (qiscusComment.getState() != QiscusComment.STATE_FAILED
                && qiscusComment.getState() != QiscusComment.STATE_PENDING
                && qiscusComment.getState() != QiscusComment.STATE_SENDING
                && qiscusComment.getState() != QiscusComment.STATE_READ) {
            if (qiscusComment.getId() > lastDeliveredCommentId.get()) {
                qiscusComment.setState(QiscusComment.STATE_ON_QISCUS);
            } else if (qiscusComment.getId() > lastReadCommentId.get()) {
                qiscusComment.setState(QiscusComment.STATE_DELIVERED);
            } else {
                qiscusComment.setState(QiscusComment.STATE_READ);
            }
            Qiscus.getDataStore().addOrUpdate(qiscusComment);
        }
    }

    public void loadComments(int count) {
        Observable.merge(getInitRoomData(), getLocalComments(count, true).map(comments -> Pair.create(room, comments)))
                .filter(qiscusChatRoomListPair -> qiscusChatRoomListPair != null)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(bindToLifecycle())
                .subscribe(roomData -> {
                    if (view != null) {
                        room = roomData.first;
                        view.initRoomData(roomData.first, roomData.second);
                    }
                }, throwable -> {
                    QiscusErrorLogger.print(throwable);
                    throwable.printStackTrace();
                    if (view != null) {
                        view.showError(QiscusAndroidUtil.getString(R.string.qiscus_failed_load_comments));
                        view.dismissLoading();
                    }
                });
    }

    private List<QiscusComment> cleanFailedComments(List<QiscusComment> qiscusComments) {
        List<QiscusComment> comments = new ArrayList<>();
        for (QiscusComment qiscusComment : qiscusComments) {
            if (qiscusComment.getId() != -1) {
                comments.add(qiscusComment);
            }
        }
        return comments;
    }

    private boolean isValidOlderComments(List<QiscusComment> qiscusComments, QiscusComment lastQiscusComment) {
        if (qiscusComments.isEmpty()) return false;

        qiscusComments = cleanFailedComments(qiscusComments);
        boolean containsLastValidComment = qiscusComments.size() <= 0 || lastQiscusComment.getId() == -1;
        int size = qiscusComments.size();

        if (size == 1) {
            return qiscusComments.get(0).getCommentBeforeId() == 0
                    && lastQiscusComment.getCommentBeforeId() == qiscusComments.get(0).getId();
        }

        for (int i = 0; i < size - 1; i++) {
            if (!containsLastValidComment && qiscusComments.get(i).getId() == lastQiscusComment.getCommentBeforeId()) {
                containsLastValidComment = true;
            }

            if (qiscusComments.get(i).getCommentBeforeId() != qiscusComments.get(i + 1).getId()) {
                return false;
            }
        }
        return containsLastValidComment;
    }

    private boolean isValidChainingComments(List<QiscusComment> qiscusComments) {
        qiscusComments = cleanFailedComments(qiscusComments);
        int size = qiscusComments.size();
        for (int i = 0; i < size - 1; i++) {
            if (qiscusComments.get(i).getCommentBeforeId() != qiscusComments.get(i + 1).getId()) {
                return false;
            }
        }
        return true;
    }

    public void loadOlderCommentThan(QiscusComment qiscusComment) {
        view.showLoadMoreLoading();
        Qiscus.getDataStore().getObservableOlderCommentsThan(qiscusComment, currentTopicId, 40)
                .flatMap(Observable::from)
                .filter(qiscusComment1 -> qiscusComment.getId() == -1 || qiscusComment1.getId() < qiscusComment.getId())
                .toSortedList(commentComparator)
                .map(comments -> {
                    if (comments.size() >= 20) {
                        return comments.subList(0, 20);
                    }
                    return comments;
                })
                .doOnNext(comments -> {
                    updateRepliedSender(comments);
                    checkForLastRead(comments);
                    updateCommentState(comments, true);
                })
                .flatMap(comments -> isValidOlderComments(comments, qiscusComment) ?
                        Observable.from(comments).toSortedList(commentComparator) :
                        getCommentsFromNetwork(qiscusComment.getId()).map(comments1 -> {
                            for (QiscusComment localComment : comments) {
                                if (localComment.getState() <= QiscusComment.STATE_SENDING) {
                                    comments1.add(localComment);
                                }
                            }
                            return comments1;
                        }))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(bindToLifecycle())
                .subscribe(comments -> {
                    if (view != null) {
                        view.onLoadMore(comments);
                        view.dismissLoading();
                    }
                }, throwable -> {
                    QiscusErrorLogger.print(throwable);
                    throwable.printStackTrace();
                    if (view != null) {
                        view.showError(QiscusAndroidUtil.getString(R.string.qiscus_failed_load_comments));
                        view.dismissLoading();
                    }
                });
    }

    private void updateRepliedSender(List<QiscusComment> comments) {
        for (QiscusComment comment : comments) {
            if (comment.getType() == QiscusComment.Type.REPLY) {
                QiscusComment repliedComment = comment.getReplyTo();
                if (repliedComment != null) {
                    for (QiscusRoomMember qiscusRoomMember : room.getMember()) {
                        if (repliedComment.getSenderEmail().equals(qiscusRoomMember.getEmail())) {
                            repliedComment.setSender(qiscusRoomMember.getUsername());
                            comment.setReplyTo(repliedComment);
                            break;
                        }
                    }
                }
            }
        }
    }

    @Subscribe
    public void onMqttEvent(QiscusMqttStatusEvent event) {
        view.onRealtimeStatusChanged(event == QiscusMqttStatusEvent.CONNECTED);
    }

    public void loadCommentsAfter(QiscusComment comment) {
        QiscusApi.getInstance().getCommentsAfter(room.getId(), currentTopicId, comment.getId())
                .doOnNext(qiscusComment -> {
                    qiscusComment.setRoomId(room.getId());
                    updateCommentState(qiscusComment, false);
                })
                .toSortedList(commentComparator)
                .doOnNext(this::checkForLastRead)
                .doOnNext(Collections::reverse)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(bindToLifecycle())
                .subscribe(comments -> {
                    if (view != null) {
                        view.onLoadMore(comments);
                    }
                }, Throwable::printStackTrace);
    }

    private void listenRoomEvent() {
        QiscusPusherApi.getInstance().listenRoom(room);
    }

    @Subscribe
    public void onRoomEvent(QiscusChatRoomEvent event) {
        QiscusAndroidUtil.runOnBackgroundThread(() -> handleEvent(event));
    }

    private void handleEvent(QiscusChatRoomEvent event) {
        if (event.getTopicId() == currentTopicId) {
            switch (event.getEvent()) {
                case TYPING:
                    QiscusAndroidUtil.runOnUIThread(() -> {
                        if (view != null) {
                            view.onUserTyping(event.getUser(), event.isTyping());
                        }
                    });
                    break;
                case DELIVERED:
                    QiscusComment deliveredComment = Qiscus.getDataStore()
                            .getComment(event.getCommentId(), event.getCommentUniqueId());
                    if (deliveredComment != null) {
                        deliveredComment.setId(event.getCommentId());
                        updateLastDeliveredComment(deliveredComment);
                        if (QiscusComment.STATE_DELIVERED > deliveredComment.getState()) {
                            deliveredComment.setState(QiscusComment.STATE_DELIVERED);
                            Qiscus.getDataStore().update(deliveredComment);
                        }
                    } else {
                        if (updateLastDeliveredCommentId(event.getCommentId())) {
                            QiscusAndroidUtil.runOnUIThread(() -> {
                                if (view != null) {
                                    view.updateLastDeliveredComment(lastDeliveredCommentId.get());
                                }
                            });
                        }
                    }
                    break;
                case READ:
                    QiscusComment readComment = Qiscus.getDataStore()
                            .getComment(event.getCommentId(), String.valueOf(event.getCommentId()));
                    if (readComment != null) {
                        readComment.setId(event.getCommentId());
                        updateLastReadComment(readComment);
                        if (QiscusComment.STATE_READ > readComment.getState()) {
                            readComment.setState(QiscusComment.STATE_READ);
                            Qiscus.getDataStore().update(readComment);
                        }
                    } else {
                        if (updateLastReadCommentId(event.getCommentId())) {
                            QiscusAndroidUtil.runOnUIThread(() -> {
                                if (view != null) {
                                    view.updateLastReadComment(lastReadCommentId.get());
                                }
                            });
                        }
                    }
                    break;
            }
        }
    }

    private void updateLastReadComment(QiscusComment qiscusComment) {
        if (qiscusComment != null && updateLastReadCommentId(qiscusComment.getId())) {
            QiscusAndroidUtil.runOnUIThread(() -> {
                if (view != null) {
                    view.updateLastReadComment(lastReadCommentId.get());
                }
            });
        }
    }

    private void updateLastDeliveredComment(QiscusComment qiscusComment) {
        if (qiscusComment != null && updateLastDeliveredCommentId(qiscusComment.getId())) {
            QiscusAndroidUtil.runOnUIThread(() -> {
                if (view != null) {
                    view.updateLastDeliveredComment(lastDeliveredCommentId.get());
                }
            });
        }
    }

    @Subscribe
    public void handleRetryCommentEvent(QiscusCommentResendEvent event) {
        if (event.getQiscusComment().getTopicId() == currentTopicId) {
            QiscusAndroidUtil.runOnUIThread(() -> {
                if (view != null) {
                    view.refreshComment(event.getQiscusComment());
                }
            });
        }
    }

    @Subscribe
    public void onCommentReceivedEvent(QiscusCommentReceivedEvent event) {
        if (event.getQiscusComment().getTopicId() == currentTopicId) {
            onGotNewComment(event.getQiscusComment());
        }
    }

    private void onGotNewComment(QiscusComment qiscusComment) {
        if (qiscusComment.getSenderEmail().equalsIgnoreCase(qiscusAccount.getEmail())) {
            QiscusAndroidUtil.runOnBackgroundThread(() -> commentSuccess(qiscusComment));
        } else {
            updateLastReadComment(qiscusComment);
            qiscusComment.setState(QiscusComment.STATE_READ);
            QiscusAndroidUtil.runOnBackgroundThread(() -> Qiscus.getDataStore().addOrUpdate(qiscusComment));
        }

        if (qiscusComment.isAttachment()) {
            QiscusAndroidUtil.runOnBackgroundThread(() -> {
                String path = QiscusFileUtil.generateFilePath(qiscusComment.getAttachmentName(), qiscusComment.getTopicId());
                boolean exist = QiscusFileUtil.isContains(path);
                if (!exist) {
                    String message = qiscusComment.getMessage();
                    int fileNameEndIndex = message.lastIndexOf(" [/file]");
                    int fileNameBeginIndex = message.lastIndexOf('/', fileNameEndIndex) + 1;
                    String fileName = message.substring(fileNameBeginIndex, fileNameEndIndex);
                    path = QiscusFileUtil.generateFilePath(fileName, qiscusComment.getTopicId());
                    exist = QiscusFileUtil.isContains(path);
                }
                if (exist) {
                    Qiscus.getDataStore().addOrUpdateLocalPath(qiscusComment.getTopicId(), qiscusComment.getId(), path);
                }
            });
        }

        if (qiscusComment.getTopicId() == currentTopicId) {
            QiscusAndroidUtil.runOnBackgroundThread(() -> {
                if (!qiscusComment.getSenderEmail().equalsIgnoreCase(qiscusAccount.getEmail())
                        && QiscusCacheManager.getInstance().getLastChatActivity().first) {
                    QiscusPusherApi.getInstance().setUserRead(room.getId(),
                            currentTopicId,
                            qiscusComment.getId(),
                            qiscusComment.getUniqueId());
                }
            });
            view.onNewComment(qiscusComment);
        }
    }

    public void downloadFile(final QiscusComment qiscusComment) {
        if (qiscusComment.isDownloading()) {
            return;
        }

        File file = Qiscus.getDataStore().getLocalPath(qiscusComment.getId());
        if (file == null) {
            qiscusComment.setDownloading(true);
            QiscusApi.getInstance()
                    .downloadFile(qiscusComment.getTopicId(), qiscusComment.getAttachmentUri().toString(),
                            qiscusComment.getAttachmentName(), percentage -> qiscusComment.setProgress((int) percentage))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .compose(bindToLifecycle())
                    .doOnNext(file1 -> {
                        if (QiscusImageUtil.isImage(file1)) {
                            QiscusImageUtil.addImageToGallery(file1);
                        }
                        qiscusComment.setDownloading(false);
                        Qiscus.getDataStore().addOrUpdateLocalPath(qiscusComment.getTopicId(), qiscusComment.getId(),
                                file1.getAbsolutePath());
                    })
                    .subscribe(file1 -> {
                        view.refreshComment(qiscusComment);
                        if (qiscusComment.getType() == QiscusComment.Type.AUDIO) {
                            qiscusComment.playAudio();
                        } else if (qiscusComment.getType() == QiscusComment.Type.FILE
                                || qiscusComment.getType() == QiscusComment.Type.VIDEO) {
                            view.onFileDownloaded(file1, MimeTypeMap.getSingleton().getMimeTypeFromExtension(qiscusComment.getExtension()));
                        }
                    }, throwable -> {
                        QiscusErrorLogger.print(throwable);
                        throwable.printStackTrace();
                        qiscusComment.setDownloading(false);
                        view.showError(QiscusAndroidUtil.getString(R.string.qiscus_failed_download_file));
                    });
        } else {
            if (qiscusComment.getType() == QiscusComment.Type.AUDIO) {
                qiscusComment.playAudio();
            } else if (qiscusComment.getType() == QiscusComment.Type.IMAGE) {
                view.startPhotoViewer(qiscusComment);
            } else {
                view.onFileDownloaded(file, MimeTypeMap.getSingleton().getMimeTypeFromExtension(qiscusComment.getExtension()));
            }
        }
    }

    public void clickChatButton(JSONObject jsonButton) {
        if ("postback".equals(jsonButton.opt("type"))) {
            sendCommentPostBack(jsonButton.optString("label", "Button"), jsonButton.optJSONObject("payload").toString());
        }
    }

    public void loadUntilComment(QiscusComment qiscusComment) {
        Qiscus.getDataStore().getObservableCommentsAfter(qiscusComment, currentTopicId)
                .map(comments -> comments.contains(qiscusComment) ? comments : new ArrayList<QiscusComment>())
                .flatMap(Observable::from)
                .toSortedList(commentComparator)
                .doOnNext(comments -> {
                    checkForLastRead(comments);
                    updateCommentState(comments, true);
                })
                .flatMap(comments -> isValidChainingComments(comments) ?
                        Observable.from(comments).toSortedList(commentComparator) :
                        Observable.just(new ArrayList<QiscusComment>()))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(bindToLifecycle())
                .subscribe(comments -> {
                    if (view != null) {
                        view.showCommentsAndScrollToTop(comments);
                    }
                }, Throwable::printStackTrace);
    }

    public void forward(List<QiscusComment> forwardComments) {
        for (int i = 0; i < forwardComments.size(); i++) {
            int finalI = i;
            QiscusAndroidUtil.runOnUIThread(() -> {
                QiscusComment forwardComment = forwardComments.get(finalI);
                QiscusComment qiscusComment;
                if (forwardComment.getType() == QiscusComment.Type.CONTACT) {
                    qiscusComment = QiscusComment.generateContactMessage(forwardComment.getContact(),
                            room.getId(), currentTopicId);
                } else if (forwardComment.getType() == QiscusComment.Type.LOCATION) {
                    qiscusComment = QiscusComment.generateLocationMessage(forwardComment.getLocation(),
                            room.getId(), currentTopicId);
                } else {
                    qiscusComment = QiscusComment.generateMessage(forwardComment.getMessage(),
                            room.getId(), currentTopicId);
                }
                resendComment(qiscusComment);
            }, i * 100);
        }
    }

    @Override
    public void detachView() {
        super.detachView();
        QiscusAndroidUtil.cancelRunOnUIThread(listenRoomTask);
        QiscusPusherApi.getInstance().unListenRoom(room);
        room = null;
        EventBus.getDefault().unregister(this);
    }

    public interface View extends QiscusPresenter.View {

        void showLoadMoreLoading();

        void initRoomData(QiscusChatRoom qiscusChatRoom, List<QiscusComment> comments);

        void showComments(List<QiscusComment> qiscusComments);

        void onLoadMore(List<QiscusComment> qiscusComments);

        void onSendingComment(QiscusComment qiscusComment);

        void onSuccessSendComment(QiscusComment qiscusComment);

        void onFailedSendComment(QiscusComment qiscusComment);

        void onNewComment(QiscusComment qiscusComment);

        void onCommentDeleted(QiscusComment qiscusComment);

        void refreshComment(QiscusComment qiscusComment);

        void updateLastDeliveredComment(int lastDeliveredCommentId);

        void updateLastReadComment(int lastReadCommentId);

        void onFileDownloaded(File file, String mimeType);

        void startPhotoViewer(QiscusComment qiscusComment);

        void onUserTyping(String user, boolean typing);

        void showCommentsAndScrollToTop(List<QiscusComment> qiscusComments);

        void onRealtimeStatusChanged(boolean connected);
    }
}
