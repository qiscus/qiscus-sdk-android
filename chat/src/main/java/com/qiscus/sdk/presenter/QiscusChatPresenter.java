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
import com.qiscus.sdk.chat.core.data.model.QiscusAccount;
import com.qiscus.sdk.chat.core.data.model.QiscusChatRoom;
import com.qiscus.sdk.chat.core.data.model.QiscusComment;
import com.qiscus.sdk.chat.core.data.model.QiscusContact;
import com.qiscus.sdk.chat.core.data.model.QiscusLocation;
import com.qiscus.sdk.chat.core.data.model.QiscusRoomMember;
import com.qiscus.sdk.chat.core.data.remote.QiscusApi;
import com.qiscus.sdk.chat.core.data.remote.QiscusPusherApi;
import com.qiscus.sdk.chat.core.data.remote.QiscusResendCommentHelper;
import com.qiscus.sdk.chat.core.event.QiscusClearCommentsEvent;
import com.qiscus.sdk.chat.core.event.QiscusCommentDeletedEvent;
import com.qiscus.sdk.chat.core.event.QiscusCommentReceivedEvent;
import com.qiscus.sdk.chat.core.event.QiscusCommentResendEvent;
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

    private QiscusChatRoom room;
    private QiscusAccount qiscusAccount;
    private Func2<QiscusComment, QiscusComment, Integer> commentComparator = (lhs, rhs) -> rhs.getTime().compareTo(lhs.getTime());

    private Map<QiscusComment, Subscription> pendingTask;

    private QiscusChatRoomEventHandler chatRoomEventHandler;

    public QiscusChatPresenter(View view, QiscusChatRoom room) {
        super(view);
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        this.room = room;
        if (this.room.getMember().isEmpty()) {
            this.room = Qiscus.getDataStore().getChatRoom(room.getId());
        }
        qiscusAccount = Qiscus.getQiscusAccount();
        pendingTask = new HashMap<>();

        chatRoomEventHandler = new QiscusChatRoomEventHandler(this.room, this);
    }

    private void commentSuccess(QiscusComment qiscusComment) {
        pendingTask.remove(qiscusComment);
        qiscusComment.setState(QiscusComment.STATE_ON_QISCUS);
        QiscusComment savedQiscusComment = Qiscus.getDataStore().getComment(qiscusComment.getUniqueId());
        if (savedQiscusComment != null && savedQiscusComment.getState() > qiscusComment.getState()) {
            qiscusComment.setState(savedQiscusComment.getState());
        }
        Qiscus.getDataStore().addOrUpdate(qiscusComment);
    }

    private boolean mustFailed(Throwable throwable, QiscusComment qiscusComment) {
        //Error response from server
        //Means something wrong with server, e.g user is not member of these room anymore
        return ((throwable instanceof HttpException && ((HttpException) throwable).code() >= 400) ||
                //if throwable from JSONException, e.g response from server not json as expected
                (throwable instanceof JSONException) ||
                // if attachment type
                qiscusComment.isAttachment());
    }

    private void commentFail(Throwable throwable, QiscusComment qiscusComment) {
        pendingTask.remove(qiscusComment);
        if (!Qiscus.getDataStore().isContains(qiscusComment)) { //Have been deleted
            return;
        }

        int state = QiscusComment.STATE_PENDING;
        if (mustFailed(throwable, qiscusComment)) {
            qiscusComment.setDownloading(false);
            state = QiscusComment.STATE_FAILED;
        }

        //Kalo ternyata comment nya udah sukses dikirim sebelumnya, maka ga usah di update
        QiscusComment savedQiscusComment = Qiscus.getDataStore().getComment(qiscusComment.getUniqueId());
        if (savedQiscusComment != null && savedQiscusComment.getState() > QiscusComment.STATE_SENDING) {
            return;
        }

        //Simpen statenya
        qiscusComment.setState(state);
        Qiscus.getDataStore().addOrUpdate(qiscusComment);
    }

    public void cancelPendingComment(QiscusComment qiscusComment) {
        if (pendingTask.containsKey(qiscusComment)) {
            Subscription subscription = pendingTask.get(qiscusComment);
            if (!subscription.isUnsubscribed()) {
                subscription.unsubscribe();
            }
            pendingTask.remove(qiscusComment);
        }
    }

    private void sendComment(QiscusComment qiscusComment) {
        view.onSendingComment(qiscusComment);
        Subscription subscription = QiscusApi.getInstance().postComment(qiscusComment)
                .doOnSubscribe(() -> Qiscus.getDataStore().addOrUpdate(qiscusComment))
                .doOnNext(this::commentSuccess)
                .doOnError(throwable -> commentFail(throwable, qiscusComment))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(bindToLifecycle())
                .subscribe(commentSend -> {
                    if (commentSend.getRoomId() == room.getId()) {
                        view.onSuccessSendComment(commentSend);
                    }
                }, throwable -> {
                    QiscusErrorLogger.print(throwable);
                    throwable.printStackTrace();
                    if (qiscusComment.getRoomId() == room.getId()) {
                        view.onFailedSendComment(qiscusComment);
                    }
                });

        pendingTask.put(qiscusComment, subscription);
    }

    public void sendComment(String content) {
        QiscusComment qiscusComment = QiscusComment.generateMessage(room.getId(), content);
        sendComment(qiscusComment);
    }

    public void sendContact(QiscusContact contact) {
        QiscusComment qiscusComment = QiscusComment.generateContactMessage(room.getId(), contact);
        sendComment(qiscusComment);
    }

    public void sendLocation(QiscusLocation location) {
        QiscusComment qiscusComment = QiscusComment.generateLocationMessage(room.getId(), location);
        sendComment(qiscusComment);
    }

    public void sendCommentPostBack(String content, String payload) {
        QiscusComment qiscusComment = QiscusComment.generatePostBackMessage(room.getId(), content, payload);
        sendComment(qiscusComment);
    }

    public void sendReplyComment(String content, QiscusComment originComment) {
        QiscusComment qiscusComment = QiscusComment.generateReplyMessage(room.getId(), content, originComment);
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

        QiscusComment qiscusComment = QiscusComment.generateFileAttachmentMessage(room.getId(),
                compressedFile.getPath(), caption, file.getName());
        qiscusComment.setDownloading(true);
        view.onSendingComment(qiscusComment);

        File finalCompressedFile = compressedFile;

        Subscription subscription = QiscusApi.getInstance().sendFileMessage(room.getId(),
                qiscusComment,finalCompressedFile,percentage -> {
                    qiscusComment.setProgress((int) percentage);
                }).doOnSubscribe(() -> Qiscus.getDataStore().addOrUpdate(qiscusComment))
                .doOnNext(this::commentSuccess)
                .doOnError(throwable -> commentFail(throwable, qiscusComment))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(bindToLifecycle())
                .subscribe(commentSend -> {
                    if (commentSend.getRoomId() == room.getId()) {
                        commentSend.setDownloading(false);
                        view.onSuccessSendComment(commentSend);
                        QiscusCore.getDataStore()
                                .addOrUpdateLocalPath(commentSend.getRoomId(),
                                        commentSend.getId(), finalCompressedFile.getAbsolutePath());
                    }
                }, throwable -> {
                    QiscusErrorLogger.print(throwable);
                    throwable.printStackTrace();
                    if (qiscusComment.getRoomId() == room.getId()) {
                        view.onFailedSendComment(qiscusComment);
                    }
                });


        pendingTask.put(qiscusComment, subscription);
    }

    private void resendFile(QiscusComment qiscusComment) {
        if (qiscusComment.getAttachmentUri().toString().startsWith("http")) { //We forward file message
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

        qiscusComment.setDownloading(true);
        qiscusComment.setProgress(0);
        Subscription subscription = QiscusApi.getInstance()
                .uploadFile(file, percentage -> qiscusComment.setProgress((int) percentage))
                .doOnSubscribe(() -> Qiscus.getDataStore().addOrUpdate(qiscusComment))
                .flatMap(uri -> {
                    qiscusComment.updateAttachmentUrl(uri.toString());
                    return QiscusApi.getInstance().postComment(qiscusComment);
                })
                .doOnNext(commentSend -> {
                    Qiscus.getDataStore()
                            .addOrUpdateLocalPath(commentSend.getRoomId(), commentSend.getId(), file.getAbsolutePath());
                    qiscusComment.setDownloading(false);
                    commentSuccess(commentSend);
                })
                .doOnError(throwable -> commentFail(throwable, qiscusComment))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(bindToLifecycle())
                .subscribe(commentSend -> {
                    if (commentSend.getRoomId() == room.getId()) {
                        view.onSuccessSendComment(commentSend);
                    }
                }, throwable -> {
                    QiscusErrorLogger.print(throwable);
                    throwable.printStackTrace();
                    if (qiscusComment.getRoomId() == room.getId()) {
                        view.onFailedSendComment(qiscusComment);
                    }
                });

        pendingTask.put(qiscusComment, subscription);
    }

    private void forwardFile(QiscusComment qiscusComment) {
        qiscusComment.setProgress(100);
        Subscription subscription = QiscusApi.getInstance().postComment(qiscusComment)
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
                    if (commentSend.getRoomId() == room.getId()) {
                        view.onSuccessSendComment(commentSend);
                    }
                }, throwable -> {
                    QiscusErrorLogger.print(throwable);
                    throwable.printStackTrace();
                    if (qiscusComment.getRoomId() == room.getId()) {
                        view.onFailedSendComment(qiscusComment);
                    }
                });

        pendingTask.put(qiscusComment, subscription);
    }

    public void deleteComment(QiscusComment qiscusComment) {
        cancelPendingComment(qiscusComment);
        QiscusResendCommentHelper.cancelPendingComment(qiscusComment);
        QiscusAndroidUtil.runOnBackgroundThread(() -> Qiscus.getDataStore().delete(qiscusComment));
        view.onCommentDeleted(qiscusComment);
    }

    private Observable<Pair<QiscusChatRoom, List<QiscusComment>>> getInitRoomData() {
        return QiscusApi.getInstance().getChatRoomComments(room.getId())
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

                    Collections.sort(roomData.second, (lhs, rhs) -> rhs.getTime().compareTo(lhs.getTime()));

                    Qiscus.getDataStore().addOrUpdate(roomData.first);
                })
                .doOnNext(roomData -> {
                    for (QiscusComment qiscusComment : roomData.second) {
                        Qiscus.getDataStore().addOrUpdate(qiscusComment);
                    }
                })
                .subscribeOn(Schedulers.io())
                .onErrorReturn(throwable -> null);
    }

    private Observable<List<QiscusComment>> getCommentsFromNetwork(long lastCommentId) {
        return QiscusApi.getInstance().getComments(room.getId(), lastCommentId)
                .doOnNext(qiscusComment -> {
                    QiscusCore.getDataStore().addOrUpdate(qiscusComment);
                    qiscusComment.setRoomId(room.getId());
                })
                .toSortedList(commentComparator)
                .subscribeOn(Schedulers.io());
    }

    private Observable<List<QiscusComment>> getLocalComments(int count, boolean forceFailedSendingComment) {
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

    public List<QiscusComment> loadLocalComments(int count) {
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
                        view.onLoadCommentsError(throwable);
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
        QiscusApi.getInstance().getCommentsAfter(room.getId(), comment.getId())
                .doOnNext(qiscusComment -> qiscusComment.setRoomId(room.getId()))
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
    public void handleRetryCommentEvent(QiscusCommentResendEvent event) {
        if (event.getQiscusComment().getRoomId() == room.getId()) {
            QiscusAndroidUtil.runOnUIThread(() -> {
                if (view != null) {
                    view.refreshComment(event.getQiscusComment());
                }
            });
        }
    }

    @Subscribe
    public void handleDeleteCommentEvent(QiscusCommentDeletedEvent event) {
        if (event.getQiscusComment().getRoomId() == room.getId()) {
            QiscusAndroidUtil.runOnUIThread(() -> {
                if (view != null) {
                    if (event.isHardDelete()) {
                        view.onCommentDeleted(event.getQiscusComment());
                    } else {
                        view.refreshComment(event.getQiscusComment());
                    }
                }
            });
        }
    }

    @Subscribe
    public void onCommentReceivedEvent(QiscusCommentReceivedEvent event) {
        if (event.getQiscusComment().getRoomId() == room.getId()) {
            onGotNewComment(event.getQiscusComment());
        }
    }

    @Subscribe
    public void handleClearCommentsEvent(QiscusClearCommentsEvent event) {
        if (event.getRoomId() == room.getId()) {
            QiscusAndroidUtil.runOnUIThread(() -> {
                if (view != null) {
                    view.clearCommentsBefore(event.getTimestamp());
                }
            });
        }
    }

    private void onGotNewComment(QiscusComment qiscusComment) {
        if (qiscusComment.getSenderEmail().equalsIgnoreCase(qiscusAccount.getEmail())) {
            QiscusAndroidUtil.runOnBackgroundThread(() -> commentSuccess(qiscusComment));
        } else {
            chatRoomEventHandler.onGotComment(qiscusComment);
        }

        if (qiscusComment.getRoomId() == room.getId()) {
            QiscusAndroidUtil.runOnBackgroundThread(() -> {
                if (!qiscusComment.getSenderEmail().equalsIgnoreCase(qiscusAccount.getEmail())
                        && QiscusCacheManager.getInstance().getLastChatActivity().first) {
                    QiscusPusherApi.getInstance().setUserRead(room.getId(), qiscusComment.getId());
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
                    .downloadFile(qiscusComment.getAttachmentUri().toString(), qiscusComment.getAttachmentName(),
                            percentage -> qiscusComment.setProgress((int) percentage))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .compose(bindToLifecycle())
                    .doOnNext(file1 -> {
                        QiscusFileUtil.notifySystem(file1);
                        qiscusComment.setDownloading(false);
                        Qiscus.getDataStore().addOrUpdateLocalPath(qiscusComment.getRoomId(), qiscusComment.getId(),
                                file1.getAbsolutePath());
                    })
                    .subscribe(file1 -> {
                        view.notifyDataChanged();
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
                        view.showError(QiscusTextUtil.getString(R.string.qiscus_failed_download_file));
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
            String message = jsonButton.optString("postback_text", "");
            if (message.isEmpty()) {
                message = jsonButton.optString("label", "Button");
            }
            sendCommentPostBack(message, jsonButton.optJSONObject("payload").toString());
        }
    }

    public void clickCarouselItem(JSONObject payload) {
        if ("postback".equals(payload.optString("type"))) {
            sendCommentPostBack(payload.optString("postback_text", "postback"),
                    payload.optJSONObject("payload").toString());
        }
    }

    public void loadUntilComment(QiscusComment qiscusComment) {
        Qiscus.getDataStore().getObservableCommentsAfter(qiscusComment, room.getId())
                .map(comments -> comments.contains(qiscusComment) ? comments : new ArrayList<QiscusComment>())
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
                    qiscusComment = QiscusComment.generateContactMessage(room.getId(), forwardComment.getContact());
                } else if (forwardComment.getType() == QiscusComment.Type.LOCATION) {
                    qiscusComment = QiscusComment.generateLocationMessage(room.getId(), forwardComment.getLocation());
                } else if (forwardComment.getType() == QiscusComment.Type.IMAGE) {
                    qiscusComment = QiscusComment.generateFileAttachmentMessage(room.getId(),
                            forwardComment.getAttachmentUri().toString(), forwardComment.getCaption(),
                            forwardComment.getAttachmentName());
                } else if (forwardComment.getType() == QiscusComment.Type.AUDIO) {
                    qiscusComment = QiscusComment.generateFileAttachmentMessage(room.getId(), forwardComment.getAttachmentUri().toString(),
                            forwardComment.getCaption(),
                            forwardComment.getAttachmentName());
                } else if (forwardComment.getType() == QiscusComment.Type.FILE) {
                    qiscusComment = QiscusComment.generateFileAttachmentMessage(room.getId(), forwardComment.getAttachmentUri().toString(),
                            forwardComment.getCaption(),
                            forwardComment.getAttachmentName());
                } else if (forwardComment.getType() == QiscusComment.Type.VIDEO) {
                    qiscusComment = QiscusComment.generateFileAttachmentMessage(room.getId(), forwardComment.getAttachmentUri().toString(),
                            forwardComment.getCaption(),
                            forwardComment.getAttachmentName());
                } else {
                    qiscusComment = QiscusComment.generateMessage(room.getId(), forwardComment.getMessage());
                }
                resendComment(qiscusComment);
            }, i * 100);
        }
    }

    private void clearUnreadCount() {
        room.setUnreadCount(0);
        room.setLastComment(null);
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

    public void deleteCommentsForEveryone(List<QiscusComment> comments, boolean hardDelete) {
        deleteComments(comments, hardDelete);
    }

    /**
     * @param comments
     * @param hardDelete
     */
    private void deleteComments(List<QiscusComment> comments, boolean hardDelete) {
        view.showDeleteLoading();
        Observable.from(comments)
                .map(QiscusComment::getUniqueId)
                .toList()
                .flatMap(uniqueIds -> QiscusApi.getInstance().deleteComments(uniqueIds, hardDelete))
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
    public void onChatRoomMemberAdded(QiscusRoomMember member) {
        if (!room.getMember().contains(member)) {
            room.getMember().add(member);
            QiscusAndroidUtil.runOnUIThread(() -> {
                if (view != null) {
                    view.onRoomChanged(room);
                }
            });
        }
    }

    @Override
    public void onChatRoomMemberRemoved(QiscusRoomMember member) {
        int x = room.getMember().indexOf(member);
        if (x >= 0) {
            room.getMember().remove(x);
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

        void initRoomData(QiscusChatRoom qiscusChatRoom, List<QiscusComment> comments);

        void onRoomChanged(QiscusChatRoom qiscusChatRoom);

        void showComments(List<QiscusComment> qiscusComments);

        void onLoadMore(List<QiscusComment> qiscusComments);

        void onSendingComment(QiscusComment qiscusComment);

        void onSuccessSendComment(QiscusComment qiscusComment);

        void onFailedSendComment(QiscusComment qiscusComment);

        void onNewComment(QiscusComment qiscusComment);

        void onCommentDeleted(QiscusComment qiscusComment);

        void refreshComment(QiscusComment qiscusComment);

        void notifyDataChanged();

        void updateLastDeliveredComment(long lastDeliveredCommentId);

        void updateLastReadComment(long lastReadCommentId);

        void onFileDownloaded(File file, String mimeType);

        void startPhotoViewer(QiscusComment qiscusComment);

        void onUserTyping(String user, boolean typing);

        void showCommentsAndScrollToTop(List<QiscusComment> qiscusComments);

        void onRealtimeStatusChanged(boolean connected);

        void onLoadCommentsError(Throwable throwable);

        void clearCommentsBefore(long timestamp);
    }
}
