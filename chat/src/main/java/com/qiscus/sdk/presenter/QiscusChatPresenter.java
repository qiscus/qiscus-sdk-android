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

import androidx.core.util.Pair;
import android.webkit.MimeTypeMap;

import com.qiscus.sdk.Qiscus;
import com.qiscus.sdk.R;
import com.qiscus.sdk.chat.core.QiscusCore;
import com.qiscus.sdk.chat.core.data.local.QiscusCacheManager;
import com.qiscus.sdk.chat.core.data.model.QAccount;
import com.qiscus.sdk.chat.core.data.model.QChatRoom;
import com.qiscus.sdk.chat.core.data.model.QParticipant;
import com.qiscus.sdk.chat.core.data.model.QMessage;
import com.qiscus.sdk.chat.core.data.model.QUser;
import com.qiscus.sdk.chat.core.data.model.QiscusContact;
import com.qiscus.sdk.chat.core.data.model.QiscusLocation;
import com.qiscus.sdk.chat.core.data.remote.QiscusApi;
import com.qiscus.sdk.chat.core.data.remote.QiscusPusherApi;
import com.qiscus.sdk.chat.core.data.remote.QiscusResendCommentHelper;
import com.qiscus.sdk.chat.core.event.QiscusClearMessagesEvent;
import com.qiscus.sdk.chat.core.event.QMessageDeletedEvent;
import com.qiscus.sdk.chat.core.event.QMessageReceivedEvent;
import com.qiscus.sdk.chat.core.event.QMessageResendEvent;
import com.qiscus.sdk.chat.core.event.QiscusMqttStatusEvent;
import com.qiscus.sdk.chat.core.presenter.QiscusChatRoomEventHandler;
import com.qiscus.sdk.chat.core.util.QiscusAndroidUtil;
import com.qiscus.sdk.chat.core.util.QiscusErrorLogger;
import com.qiscus.sdk.chat.core.util.QiscusFileUtil;
import com.qiscus.sdk.chat.core.util.QiscusTextUtil;
import com.qiscus.sdk.util.QiscusImageUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.HttpException;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func2;
import rx.schedulers.Schedulers;

public class QiscusChatPresenter extends QiscusPresenter<QiscusChatPresenter.View> implements QiscusChatRoomEventHandler.StateListener {

    private QChatRoom room;
    private QAccount qAccount;
    private Func2<QMessage, QMessage, Integer> commentComparator = (lhs, rhs) -> rhs.getTimestamp().compareTo(lhs.getTimestamp());

    private Map<QMessage, Subscription> pendingTask;

    private QiscusChatRoomEventHandler chatRoomEventHandler;

    public QiscusChatPresenter(View view, QChatRoom room) {
        super(view);
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        this.room = room;
//        if (this.room.getParticipants().isEmpty()) {
//            this.room = Qiscus.getDataStore().getChatRoom(room.getId());
//        }
        qAccount = Qiscus.getQiscusAccount();
        pendingTask = new HashMap<>();

        chatRoomEventHandler = new QiscusChatRoomEventHandler(this.room, this);
    }

    private void commentSuccess(QMessage qiscusComment) {
        pendingTask.remove(qiscusComment);
        qiscusComment.setState(QMessage.STATE_ON_QISCUS);
        QMessage savedQMessage = Qiscus.getDataStore().getComment(qiscusComment.getUniqueId());
        if (savedQMessage != null && savedQMessage.getState() > qiscusComment.getState()) {
            qiscusComment.setState(savedQMessage.getState());
        }
        Qiscus.getDataStore().addOrUpdate(qiscusComment);
    }

    private boolean mustFailed(Throwable throwable, QMessage qiscusComment) {
        //Error response from server
        //Means something wrong with server, e.g user is not participants of these room anymore
        return ((throwable instanceof HttpException && ((HttpException) throwable).code() >= 400) ||
                //if throwable from JSONException, e.g response from server not json as expected
                (throwable instanceof JSONException) ||
                // if attachment type
                qiscusComment.isAttachment());
    }

    private void commentFail(Throwable throwable, QMessage qiscusComment) {
        pendingTask.remove(qiscusComment);
        if (!Qiscus.getDataStore().isContains(qiscusComment)) { //Have been deleted
            return;
        }

        int state = QMessage.STATE_PENDING;
        if (mustFailed(throwable, qiscusComment)) {
            qiscusComment.setDownloading(false);
            state = QMessage.STATE_FAILED;
        }

        //Kalo ternyata comment nya udah sukses dikirim sebelumnya, maka ga usah di update
        QMessage savedQMessage = Qiscus.getDataStore().getComment(qiscusComment.getUniqueId());
        if (savedQMessage != null && savedQMessage.getState() > QMessage.STATE_SENDING) {
            return;
        }

        //Simpen statenya
        qiscusComment.setState(state);
        Qiscus.getDataStore().addOrUpdate(qiscusComment);
    }

    public void cancelPendingComment(QMessage qiscusComment) {
        if (pendingTask.containsKey(qiscusComment)) {
            Subscription subscription = pendingTask.get(qiscusComment);
            if (!subscription.isUnsubscribed()) {
                subscription.unsubscribe();
            }
            pendingTask.remove(qiscusComment);
        }
    }

    private void sendComment(QMessage qiscusComment) {
        view.onSendingComment(qiscusComment);
        Subscription subscription = QiscusApi.getInstance().sendMessage(qiscusComment)
                .doOnSubscribe(() -> Qiscus.getDataStore().addOrUpdate(qiscusComment))
                .doOnNext(this::commentSuccess)
                .doOnError(throwable -> commentFail(throwable, qiscusComment))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(bindToLifecycle())
                .subscribe(commentSend -> {
                    if (commentSend.getChatRoomId() == room.getId()) {
                        view.onSuccessSendComment(commentSend);
                    }
                }, throwable -> {
                    QiscusErrorLogger.print(throwable);
                    throwable.printStackTrace();
                    if (qiscusComment.getChatRoomId() == room.getId()) {
                        view.onFailedSendComment(qiscusComment);
                    }
                });

        pendingTask.put(qiscusComment, subscription);
    }

    public void sendComment(String content) {
        QMessage qiscusComment = QMessage.generateMessage(room.getId(), content);
        sendComment(qiscusComment);
    }

    public void sendContact(QiscusContact contact) {
        QMessage qiscusComment = QMessage.generateContactMessage(room.getId(), contact);
        sendComment(qiscusComment);
    }

    public void sendLocation(QiscusLocation location) {
        QMessage qiscusComment = QMessage.generateLocationMessage(room.getId(), location);
        sendComment(qiscusComment);
    }

    public void sendCommentPostBack(String content, JSONObject payload) {
        QMessage qiscusComment = QMessage.generatePostBackMessage(room.getId(), content, payload);
        sendComment(qiscusComment);
    }

    public void sendReplyComment(String content, QMessage originComment) {
        QMessage qiscusComment = QMessage.generateReplyMessage(room.getId(), content, originComment);
        sendComment(qiscusComment);
    }

    public void resendComment(QMessage qiscusComment) {
        qiscusComment.setState(QMessage.STATE_SENDING);
        qiscusComment.setTimestamp(new Date());
        if (qiscusComment.isAttachment()) {
            resendFile(qiscusComment);
        } else {
            sendComment(qiscusComment);
        }
    }

    public void sendFile(File file) {
        sendFile(file, null);
    }

    public void sendFile(File file, String caption) {
        File compressedFile = file;
        if (QiscusImageUtil.isImage(file) && !file.getName().endsWith(".gif")) {
            try {
                compressedFile = QiscusImageUtil.compressImage(file);
            } catch (NullPointerException e) {
                view.showError(QiscusTextUtil.getString(R.string.qiscus_corrupted_file));
                return;
            }
        } else {
            compressedFile = QiscusFileUtil.saveFile(compressedFile);
        }

        if (!file.exists()) { //File have been removed, so we can not upload it anymore
            view.showError(QiscusTextUtil.getString(R.string.qiscus_corrupted_file));
            return;
        }

        QMessage qiscusComment = QMessage.generateFileAttachmentMessage(room.getId(),
                compressedFile.getPath(), caption, file.getName());
        qiscusComment.setDownloading(true);
        view.onSendingComment(qiscusComment);

        File finalCompressedFile = compressedFile;

        Subscription subscription = QiscusApi.getInstance().sendFileMessage(
                qiscusComment, finalCompressedFile, percentage -> {
                    qiscusComment.setProgress((int) percentage);
                }).doOnSubscribe(() -> Qiscus.getDataStore().addOrUpdate(qiscusComment))
                .doOnNext(this::commentSuccess)
                .doOnError(throwable -> commentFail(throwable, qiscusComment))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(bindToLifecycle())
                .subscribe(commentSend -> {
                    if (commentSend.getChatRoomId() == room.getId()) {
                        commentSend.setDownloading(false);
                        view.onSuccessSendComment(commentSend);
                        QiscusCore.getDataStore()
                                .addOrUpdateLocalPath(commentSend.getChatRoomId(),
                                        commentSend.getId(), finalCompressedFile.getAbsolutePath());
                    }
                }, throwable -> {
                    QiscusErrorLogger.print(throwable);
                    throwable.printStackTrace();
                    if (qiscusComment.getChatRoomId() == room.getId()) {
                        view.onFailedSendComment(qiscusComment);
                    }
                });


        pendingTask.put(qiscusComment, subscription);
    }

    private void resendFile(QMessage qiscusComment) {
        if (qiscusComment.getAttachmentUri().toString().startsWith("http")) { //We forward file message
            forwardFile(qiscusComment);
            return;
        }

        File file = new File(qiscusComment.getAttachmentUri().toString());
        if (!file.exists()) { //File have been removed, so we can not upload it anymore
            qiscusComment.setDownloading(false);
            qiscusComment.setState(QMessage.STATE_FAILED);
            Qiscus.getDataStore().addOrUpdate(qiscusComment);
            view.onFailedSendComment(qiscusComment);
            return;
        }

        qiscusComment.setDownloading(true);
        qiscusComment.setProgress(0);
        Subscription subscription = QiscusApi.getInstance()
                .upload(file, percentage -> qiscusComment.setProgress((int) percentage))
                .doOnSubscribe(() -> Qiscus.getDataStore().addOrUpdate(qiscusComment))
                .flatMap(uri -> {
                    qiscusComment.updateAttachmentUrl(uri.toString());
                    return QiscusApi.getInstance().sendMessage(qiscusComment);
                })
                .doOnNext(commentSend -> {
                    Qiscus.getDataStore()
                            .addOrUpdateLocalPath(commentSend.getChatRoomId(), commentSend.getId(), file.getAbsolutePath());
                    qiscusComment.setDownloading(false);
                    commentSuccess(commentSend);
                })
                .doOnError(throwable -> commentFail(throwable, qiscusComment))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(bindToLifecycle())
                .subscribe(commentSend -> {
                    if (commentSend.getChatRoomId() == room.getId()) {
                        view.onSuccessSendComment(commentSend);
                    }
                }, throwable -> {
                    QiscusErrorLogger.print(throwable);
                    throwable.printStackTrace();
                    if (qiscusComment.getChatRoomId() == room.getId()) {
                        view.onFailedSendComment(qiscusComment);
                    }
                });

        pendingTask.put(qiscusComment, subscription);
    }

    private void forwardFile(QMessage qiscusComment) {
        qiscusComment.setProgress(100);
        Subscription subscription = QiscusApi.getInstance().sendMessage(qiscusComment)
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
                    if (commentSend.getChatRoomId() == room.getId()) {
                        view.onSuccessSendComment(commentSend);
                    }
                }, throwable -> {
                    QiscusErrorLogger.print(throwable);
                    throwable.printStackTrace();
                    if (qiscusComment.getChatRoomId() == room.getId()) {
                        view.onFailedSendComment(qiscusComment);
                    }
                });

        pendingTask.put(qiscusComment, subscription);
    }

    public void deleteComment(QMessage qiscusComment) {
        cancelPendingComment(qiscusComment);
        QiscusResendCommentHelper.cancelPendingComment(qiscusComment);
        QiscusAndroidUtil.runOnBackgroundThread(() -> Qiscus.getDataStore().delete(qiscusComment));
        view.onCommentDeleted(qiscusComment);
    }

    private Observable<Pair<QChatRoom, List<QMessage>>> getInitRoomData() {
        return QiscusApi.getInstance().getChatRoomWithMessages(room.getId())
                .doOnError(throwable -> {
                    QiscusErrorLogger.print(throwable);
                    throwable.printStackTrace();
                    QiscusAndroidUtil.runOnUIThread(() -> {
                        if (view != null) {
                            view.onLoadCommentsError(throwable);
                        }
                    });
                })
                .doOnNext(roomData -> {
                    chatRoomEventHandler.setChatRoom(roomData.first);

                    Collections.sort(roomData.second, (lhs, rhs) -> rhs.getTimestamp().compareTo(lhs.getTimestamp()));

                    Qiscus.getDataStore().addOrUpdate(roomData.first);
                })
                .doOnNext(roomData -> {
                    for (QMessage qiscusComment : roomData.second) {
                        Qiscus.getDataStore().addOrUpdate(qiscusComment);
                    }
                })
                .subscribeOn(Schedulers.io())
                .onErrorReturn(throwable -> null);
    }

    private Observable<List<QMessage>> getCommentsFromNetwork(long lastCommentId) {
        return QiscusApi.getInstance().getPreviousMessagesById(room.getId(), 20, lastCommentId)
                .doOnNext(qiscusComment -> {
                    QiscusCore.getDataStore().addOrUpdate(qiscusComment);
                    qiscusComment.setChatRoomId(room.getId());
                })
                .toSortedList(commentComparator)
                .subscribeOn(Schedulers.io());
    }

    private Observable<List<QMessage>> getLocalComments(int count, boolean forceFailedSendingComment) {
        return Qiscus.getDataStore().getObservableComments(room.getId(), 2 * count)
                .flatMap(Observable::from)
                .toSortedList(commentComparator)
                .map(comments -> {
                    if (comments.size() > count) {
                        return comments.subList(0, count);
                    }
                    return comments;
                })
                .subscribeOn(Schedulers.io());
    }

    public List<QMessage> loadLocalComments(int count) {
        return Qiscus.getDataStore().getComments(room.getId(), count);
    }

    public void loadComments(int count) {
        Observable.merge(getInitRoomData(), getLocalComments(count, true)
                .map(comments -> Pair.create(room, comments)))
                .filter(qiscusChatRoomListPair -> qiscusChatRoomListPair != null)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(bindToLifecycle())
                .subscribe(roomData -> {
                    if (view != null) {
                        room = roomData.first;
                        view.initRoomData(roomData.first, roomData.second);
                        view.dismissLoading();
                    }
                }, throwable -> {
                    QiscusErrorLogger.print(throwable);
                    throwable.printStackTrace();
                    if (view != null) {
                        view.onLoadCommentsError(throwable);
                        view.dismissLoading();
                    }
                });
    }

    private List<QMessage> cleanFailedComments(List<QMessage> qiscusComments) {
        List<QMessage> comments = new ArrayList<>();
        for (QMessage qiscusComment : qiscusComments) {
            if (qiscusComment.getId() != -1) {
                comments.add(qiscusComment);
            }
        }
        return comments;
    }

    private boolean isValidOlderComments(List<QMessage> qiscusComments, QMessage lastQMessage) {
        if (qiscusComments.isEmpty()) return false;

        qiscusComments = cleanFailedComments(qiscusComments);
        boolean containsLastValidComment = qiscusComments.size() <= 0 || lastQMessage.getId() == -1;
        int size = qiscusComments.size();

        if (size == 1) {
            return qiscusComments.get(0).getPreviousMessageId() == 0
                    && lastQMessage.getPreviousMessageId() == qiscusComments.get(0).getId();
        }

        for (int i = 0; i < size - 1; i++) {
            if (!containsLastValidComment && qiscusComments.get(i).getId() == lastQMessage.getPreviousMessageId()) {
                containsLastValidComment = true;
            }

            if (qiscusComments.get(i).getPreviousMessageId() != qiscusComments.get(i + 1).getId()) {
                return false;
            }
        }
        return containsLastValidComment;
    }

    private boolean isValidChainingComments(List<QMessage> qiscusComments) {
        qiscusComments = cleanFailedComments(qiscusComments);
        int size = qiscusComments.size();
        for (int i = 0; i < size - 1; i++) {
            if (qiscusComments.get(i).getPreviousMessageId() != qiscusComments.get(i + 1).getId()) {
                return false;
            }
        }
        return true;
    }

    public void loadOlderCommentThan(QMessage qiscusComment) {
        view.showLoadMoreLoading();
        Qiscus.getDataStore().getObservableOlderCommentsThan(qiscusComment, room.getId(), 40)
                .flatMap(Observable::from)
                .filter(qiscusComment1 -> qiscusComment.getId() == -1 || qiscusComment1.getId() < qiscusComment.getId())
                .toSortedList(commentComparator)
                .map(comments -> {
                    if (comments.size() >= 20) {
                        return comments.subList(0, 20);
                    }
                    return comments;
                })
                .doOnNext(this::updateRepliedSender)
                .flatMap(comments -> isValidOlderComments(comments, qiscusComment) ?
                        Observable.from(comments).toSortedList(commentComparator) :
                        getCommentsFromNetwork(qiscusComment.getId()).map(comments1 -> {
                            for (QMessage localComment : comments) {
                                if (localComment.getState() <= QMessage.STATE_SENDING) {
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
                        view.onLoadCommentsError(throwable);
                        view.dismissLoading();
                    }
                });
    }

    private void updateRepliedSender(List<QMessage> comments) {
        for (QMessage comment : comments) {
            if (comment.getType() == QMessage.Type.REPLY) {
                QMessage repliedComment = comment.getReplyTo();
                if (repliedComment != null) {
                    for (QParticipant QParticipant : room.getParticipants()) {
                        if (repliedComment.getSenderEmail().equals(QParticipant.getId())) {
                            QUser qUser = new QUser();
                            qUser.setId(QParticipant.getId());
                            qUser.setName(QParticipant.getName());
                            qUser.setExtras(QParticipant.getExtras());
                            qUser.setAvatarUrl(QParticipant.getAvatarUrl());
                            repliedComment.setSender(qUser);
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

    public void loadCommentsAfter(QMessage comment) {
        QiscusApi.getInstance().getNextMessagesById(room.getId(), 20, comment.getId())
                .doOnNext(qiscusComment -> qiscusComment.setChatRoomId(room.getId()))
                .toSortedList(commentComparator)
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

    @Subscribe
    public void handleRetryCommentEvent(QMessageResendEvent event) {
        if (event.getQMessage().getChatRoomId() == room.getId()) {
            QiscusAndroidUtil.runOnUIThread(() -> {
                if (view != null) {
                    view.refreshComment(event.getQMessage());
                }
            });
        }
    }

    @Subscribe
    public void handleDeleteCommentEvent(QMessageDeletedEvent event) {
        if (event.getQMessage().getChatRoomId() == room.getId()) {
            QiscusAndroidUtil.runOnUIThread(() -> {
                if (view != null) {
                    if (event.isHardDelete()) {
                        view.onCommentDeleted(event.getQMessage());
                    } else {
                        view.refreshComment(event.getQMessage());
                    }
                }
            });
        }
    }

    @Subscribe
    public void onCommentReceivedEvent(QMessageReceivedEvent event) {
        if (event.getQMessage().getChatRoomId() == room.getId()) {
            onGotNewComment(event.getQMessage());
        }
    }

    @Subscribe
    public void handleClearCommentsEvent(QiscusClearMessagesEvent event) {
        if (event.getRoomId() == room.getId()) {
            QiscusAndroidUtil.runOnUIThread(() -> {
                if (view != null) {
                    view.clearCommentsBefore(event.getTimestamp());
                }
            });
        }
    }

    private void onGotNewComment(QMessage qiscusComment) {
        if (qiscusComment.getSenderEmail().equalsIgnoreCase(qAccount.getId())) {
            QiscusAndroidUtil.runOnBackgroundThread(() -> commentSuccess(qiscusComment));
        } else {
            chatRoomEventHandler.onGotComment(qiscusComment);
        }

        if (qiscusComment.getChatRoomId() == room.getId()) {
            QiscusAndroidUtil.runOnBackgroundThread(() -> {
                if (!qiscusComment.getSenderEmail().equalsIgnoreCase(qAccount.getId())
                        && QiscusCacheManager.getInstance().getLastChatActivity().first) {
                    QiscusPusherApi.getInstance().markAsRead(room.getId(), qiscusComment.getId());
                }
            });
            view.onNewComment(qiscusComment);
        }
    }

    public void downloadFile(final QMessage qiscusComment) {
        if (qiscusComment.isDownloading()) {
            return;
        }

        File file = Qiscus.getDataStore().getLocalPath(qiscusComment.getId());
        if (file == null) {
            qiscusComment.setDownloading(true);
            QiscusApi.getInstance()
                    .downloadFile(qiscusComment.getAttachmentUri().toString(), qiscusComment.getAttachmentName(),
                            percentage -> qiscusComment.setProgress((int) percentage))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .compose(bindToLifecycle())
                    .doOnNext(file1 -> {
                        QiscusFileUtil.notifySystem(file1);
                        qiscusComment.setDownloading(false);
                        Qiscus.getDataStore().addOrUpdateLocalPath(qiscusComment.getChatRoomId(), qiscusComment.getId(),
                                file1.getAbsolutePath());
                    })
                    .subscribe(file1 -> {
                        view.notifyDataChanged();
                        if (qiscusComment.getType() == QMessage.Type.AUDIO) {
                            qiscusComment.playAudio();
                        } else if (qiscusComment.getType() == QMessage.Type.FILE
                                || qiscusComment.getType() == QMessage.Type.VIDEO) {
                            view.onFileDownloaded(file1, MimeTypeMap.getSingleton().getMimeTypeFromExtension(qiscusComment.getExtension()));
                        }
                    }, throwable -> {
                        QiscusErrorLogger.print(throwable);
                        throwable.printStackTrace();
                        qiscusComment.setDownloading(false);
                        view.showError(QiscusTextUtil.getString(R.string.qiscus_failed_download_file));
                    });
        } else {
            if (qiscusComment.getType() == QMessage.Type.AUDIO) {
                qiscusComment.playAudio();
            } else if (qiscusComment.getType() == QMessage.Type.IMAGE) {
                view.startPhotoViewer(qiscusComment);
            } else {
                view.onFileDownloaded(file, MimeTypeMap.getSingleton().getMimeTypeFromExtension(qiscusComment.getExtension()));
            }
        }
    }

    public void clickChatButton(JSONObject jsonButton) {
        if ("postback".equals(jsonButton.opt("type"))) {
            String message = jsonButton.optString("postback_text", "");
            if (message.isEmpty()) {
                message = jsonButton.optString("label", "Button");
            }
            sendCommentPostBack(message, jsonButton.optJSONObject("payload"));
        }
    }

    public void clickCarouselItem(JSONObject payload) {
        if ("postback".equals(payload.optString("type"))) {
            sendCommentPostBack(payload.optString("postback_text", "postback"),
                    payload.optJSONObject("payload"));
        }
    }

    public void loadUntilComment(QMessage qiscusComment) {
        Qiscus.getDataStore().getObservableCommentsAfter(qiscusComment, room.getId())
                .map(comments -> comments.contains(qiscusComment) ? comments : new ArrayList<QMessage>())
                .doOnNext(qiscusComments -> {
                    if (qiscusComments.isEmpty()) {
                        QiscusAndroidUtil.runOnUIThread(() -> {
                            if (view != null) {
                                view.showError(QiscusTextUtil.getString(R.string.qiscus_message_too_far));
                            }
                        });
                    }
                })
                .flatMap(Observable::from)
                .toSortedList(commentComparator)
                .flatMap(comments -> isValidChainingComments(comments) ?
                        Observable.from(comments).toSortedList(commentComparator) :
                        Observable.just(new ArrayList<QMessage>()))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(bindToLifecycle())
                .subscribe(comments -> {
                    if (view != null) {
                        view.showCommentsAndScrollToTop(comments);
                    }
                }, Throwable::printStackTrace);
    }

    public void forward(List<QMessage> forwardComments) {
        for (int i = 0; i < forwardComments.size(); i++) {
            int finalI = i;
            QiscusAndroidUtil.runOnUIThread(() -> {
                QMessage forwardComment = forwardComments.get(finalI);
                QMessage qiscusComment;
                if (forwardComment.getType() == QMessage.Type.CONTACT) {
                    qiscusComment = QMessage.generateContactMessage(room.getId(), forwardComment.getContact());
                } else if (forwardComment.getType() == QMessage.Type.LOCATION) {
                    qiscusComment = QMessage.generateLocationMessage(room.getId(), forwardComment.getLocation());
                } else if (forwardComment.getType() == QMessage.Type.IMAGE) {
                    qiscusComment = QMessage.generateFileAttachmentMessage(room.getId(),
                            forwardComment.getAttachmentUri().toString(), forwardComment.getCaption(),
                            forwardComment.getAttachmentName());
                } else if (forwardComment.getType() == QMessage.Type.AUDIO) {
                    qiscusComment = QMessage.generateFileAttachmentMessage(room.getId(), forwardComment.getAttachmentUri().toString(),
                            forwardComment.getCaption(),
                            forwardComment.getAttachmentName());
                } else if (forwardComment.getType() == QMessage.Type.FILE) {
                    qiscusComment = QMessage.generateFileAttachmentMessage(room.getId(), forwardComment.getAttachmentUri().toString(),
                            forwardComment.getCaption(),
                            forwardComment.getAttachmentName());
                } else if (forwardComment.getType() == QMessage.Type.VIDEO) {
                    qiscusComment = QMessage.generateFileAttachmentMessage(room.getId(), forwardComment.getAttachmentUri().toString(),
                            forwardComment.getCaption(),
                            forwardComment.getAttachmentName());
                } else {
                    qiscusComment = QMessage.generateMessage(room.getId(), forwardComment.getMessage());
                }
                resendComment(qiscusComment);
            }, i * 100);
        }
    }

    private void clearUnreadCount() {
        room.setUnreadCount(0);
        room.setLastMessage(null);
        Qiscus.getDataStore().addOrUpdate(room);
    }

    @Override
    public void detachView() {
        super.detachView();
        chatRoomEventHandler.detach();
        clearUnreadCount();
        room = null;
        EventBus.getDefault().unregister(this);
    }

    public void deleteCommentsForEveryone(List<QMessage> comments) {
        deleteComments(comments);
    }

    /**
     * @param comments
     */
    private void deleteComments(List<QMessage> comments) {
        view.showDeleteLoading();
        Observable.from(comments)
                .map(QMessage::getUniqueId)
                .toList()
                .flatMap(uniqueIds -> QiscusApi.getInstance().deleteMessages(uniqueIds))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(bindToLifecycle())
                .subscribe(deletedComments -> {
                    if (view != null) {
                        view.dismissLoading();
                    }
                }, throwable -> {
                    if (view != null) {
                        view.dismissLoading();
                        view.showError(QiscusTextUtil.getString(R.string.failed_to_delete_messages));
                    }
                    QiscusErrorLogger.print(throwable);
                });
    }

    @Override
    public void onChatRoomNameChanged(String name) {
        room.setName(name);
        QiscusAndroidUtil.runOnUIThread(() -> {
            if (view != null) {
                view.onRoomChanged(room);
            }
        });
    }

    @Override
    public void onChatRoomMemberAdded(QParticipant member) {
        if (!room.getParticipants().contains(member)) {
            room.getParticipants().add(member);
            QiscusAndroidUtil.runOnUIThread(() -> {
                if (view != null) {
                    view.onRoomChanged(room);
                }
            });
        }
    }

    @Override
    public void onChatRoomMemberRemoved(QParticipant member) {
        int x = room.getParticipants().indexOf(member);
        if (x >= 0) {
            room.getParticipants().remove(x);
            QiscusAndroidUtil.runOnUIThread(() -> {
                if (view != null) {
                    view.onRoomChanged(room);
                }
            });
        }
    }

    @Override
    public void onUserTypng(String email, boolean typing) {
        QiscusAndroidUtil.runOnUIThread(() -> {
            if (view != null) {
                view.onUserTyping(email, typing);
            }
        });
    }

    @Override
    public void onChangeLastDelivered(long lastDeliveredCommentId) {
        QiscusAndroidUtil.runOnUIThread(() -> {
            if (view != null) {
                view.updateLastDeliveredComment(lastDeliveredCommentId);
            }
        });
    }

    @Override
    public void onChangeLastRead(long lastReadCommentId) {
        QiscusAndroidUtil.runOnUIThread(() -> {
            if (view != null) {
                view.updateLastReadComment(lastReadCommentId);
            }
        });
    }

    public interface View extends QiscusPresenter.View {

        void showLoadMoreLoading();

        void showDeleteLoading();

        void initRoomData(QChatRoom qChatRoom, List<QMessage> comments);

        void onRoomChanged(QChatRoom qChatRoom);

        void showComments(List<QMessage> qiscusComments);

        void onLoadMore(List<QMessage> qiscusComments);

        void onSendingComment(QMessage qiscusComment);

        void onSuccessSendComment(QMessage qiscusComment);

        void onFailedSendComment(QMessage qiscusComment);

        void onNewComment(QMessage qiscusComment);

        void onCommentDeleted(QMessage qiscusComment);

        void refreshComment(QMessage qiscusComment);

        void notifyDataChanged();

        void updateLastDeliveredComment(long lastDeliveredCommentId);

        void updateLastReadComment(long lastReadCommentId);

        void onFileDownloaded(File file, String mimeType);

        void startPhotoViewer(QMessage qiscusComment);

        void onUserTyping(String user, boolean typing);

        void showCommentsAndScrollToTop(List<QMessage> qiscusComments);

        void onRealtimeStatusChanged(boolean connected);

        void onLoadCommentsError(Throwable throwable);

        void clearCommentsBefore(long timestamp);
    }
}
