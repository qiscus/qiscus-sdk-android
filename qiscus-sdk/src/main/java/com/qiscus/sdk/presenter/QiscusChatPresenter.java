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
import android.webkit.MimeTypeMap;

import com.qiscus.sdk.Qiscus;
import com.qiscus.sdk.data.model.QiscusChatRoom;
import com.qiscus.sdk.data.model.QiscusComment;
import com.qiscus.sdk.data.remote.QiscusApi;
import com.qiscus.sdk.data.remote.QiscusPusherApi;
import com.qiscus.sdk.event.QiscusCommentReceivedEvent;
import com.qiscus.sdk.util.QiscusFileUtil;
import com.qiscus.sdk.util.QiscusImageUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.File;
import java.util.List;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class QiscusChatPresenter extends QiscusPresenter<QiscusChatPresenter.View> {

    private QiscusChatRoom room;
    private Subscription subscription;
    private int currentTopicId;

    public QiscusChatPresenter(View view, QiscusChatRoom room) {
        super(view);
        this.room = room;
        this.currentTopicId = room.getLastTopicId();
        listenRoomEvent();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    private void commentSuccess(QiscusComment qiscusComment) {
        qiscusComment.setState(QiscusComment.STATE_ON_QISCUS);
        QiscusComment savedQiscusComment = Qiscus.getDataStore().getComment(qiscusComment.getId(), qiscusComment.getUniqueId());
        if (savedQiscusComment != null) {
            if (savedQiscusComment.getState() != QiscusComment.STATE_ON_PUSHER) {
                Qiscus.getDataStore().addOrUpdate(qiscusComment);
                if (qiscusComment.getTopicId() == currentTopicId) {
                    view.onSuccessSendComment(qiscusComment);
                }
            }
        } else {
            Qiscus.getDataStore().addOrUpdate(qiscusComment);
            if (qiscusComment.getTopicId() == currentTopicId) {
                view.onSuccessSendComment(qiscusComment);
            }
        }
    }

    private void commentFail(QiscusComment qiscusComment) {
        QiscusComment savedQiscusComment = Qiscus.getDataStore().getComment(qiscusComment.getId(), qiscusComment.getUniqueId());
        if (savedQiscusComment != null) {
            if (savedQiscusComment.getState() != QiscusComment.STATE_ON_PUSHER) {
                qiscusComment.setState(QiscusComment.STATE_FAILED);
                Qiscus.getDataStore().addOrUpdate(qiscusComment);
                if (qiscusComment.getTopicId() == currentTopicId) {
                    view.onFailedSendComment(qiscusComment);
                }
            }
        } else {
            qiscusComment.setState(QiscusComment.STATE_FAILED);
            Qiscus.getDataStore().addOrUpdate(qiscusComment);
            if (qiscusComment.getTopicId() == currentTopicId) {
                view.onFailedSendComment(qiscusComment);
            }
        }
    }

    public void sendComment(String content) {
        final QiscusComment qiscusComment = QiscusComment.generateMessage(content, room.getId(), currentTopicId);
        view.onSendingComment(qiscusComment);
        QiscusApi.getInstance().postComment(qiscusComment)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(bindToLifecycle())
                .subscribe(this::commentSuccess, throwable -> {
                    throwable.printStackTrace();
                    commentFail(qiscusComment);
                });
    }

    public void sendFile(File file) {
        File compressedFile = file;
        if (file.getName().endsWith(".gif")) {
            compressedFile = QiscusFileUtil.saveFile(compressedFile, currentTopicId);
        } else if (QiscusImageUtil.isImage(file)) {
            compressedFile = QiscusImageUtil.compressImage(Uri.fromFile(file), currentTopicId);
        } else {
            compressedFile = QiscusFileUtil.saveFile(compressedFile, currentTopicId);
        }

        final QiscusComment qiscusComment = QiscusComment.generateMessage(String.format("[file] %s [/file]", compressedFile.getPath()),
                room.getId(), currentTopicId);
        qiscusComment.setDownloading(true);
        view.onSendingComment(qiscusComment);

        final File finalCompressedFile = compressedFile;
        QiscusApi.getInstance().uploadFile(compressedFile, percentage -> qiscusComment.setProgress((int) percentage))
                .flatMap(uri -> {
                    qiscusComment.setMessage(String.format("[file] %s [/file]", uri.toString()));
                    return QiscusApi.getInstance().postComment(qiscusComment);
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(bindToLifecycle())
                .subscribe(commentSend -> {
                    Qiscus.getDataStore()
                            .addOrUpdateLocalPath(commentSend.getTopicId(), commentSend.getId(), finalCompressedFile.getAbsolutePath());
                    qiscusComment.setDownloading(false);
                    commentSuccess(commentSend);
                }, throwable -> {
                    throwable.printStackTrace();
                    qiscusComment.setDownloading(false);
                    commentFail(qiscusComment);
                });
    }

    public void resendComment(final QiscusComment qiscusComment) {
        if (qiscusComment.isAttachment()) {
            resendFile(qiscusComment);
        } else {
            qiscusComment.setState(QiscusComment.STATE_SENDING);
            view.onNewComment(qiscusComment);
            QiscusApi.getInstance().postComment(qiscusComment)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .compose(bindToLifecycle())
                    .subscribe(this::commentSuccess, throwable -> {
                        throwable.printStackTrace();
                        commentFail(qiscusComment);
                    });
        }
    }

    private void resendFile(QiscusComment qiscusComment) {
        File file = new File(qiscusComment.getAttachmentUri().toString());
        qiscusComment.setDownloading(true);
        qiscusComment.setState(QiscusComment.STATE_SENDING);
        view.onNewComment(qiscusComment);
        if (!file.exists()) {
            qiscusComment.setProgress(100);
            QiscusApi.getInstance().postComment(qiscusComment)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .compose(bindToLifecycle())
                    .subscribe(commentSend -> {
                        Qiscus.getDataStore()
                                .addOrUpdateLocalPath(commentSend.getTopicId(), commentSend.getId(), file.getAbsolutePath());
                        qiscusComment.setDownloading(false);
                        commentSuccess(commentSend);
                    }, throwable -> {
                        throwable.printStackTrace();
                        qiscusComment.setDownloading(false);
                        commentFail(qiscusComment);
                    });
        } else {
            qiscusComment.setProgress(0);
            QiscusApi.getInstance().uploadFile(file, percentage -> qiscusComment.setProgress((int) percentage))
                    .flatMap(uri -> {
                        qiscusComment.setMessage(String.format("[file] %s [/file]", uri.toString()));
                        return QiscusApi.getInstance().postComment(qiscusComment);
                    })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .compose(bindToLifecycle())
                    .subscribe(commentSend -> {
                        Qiscus.getDataStore()
                                .addOrUpdateLocalPath(commentSend.getTopicId(), commentSend.getId(), file.getAbsolutePath());
                        qiscusComment.setDownloading(false);
                        commentSuccess(commentSend);
                    }, throwable -> {
                        throwable.printStackTrace();
                        qiscusComment.setDownloading(false);
                        commentFail(qiscusComment);
                    });
        }
    }

    public void deleteComment(QiscusComment qiscusComment) {
        Qiscus.getDataStore().delete(qiscusComment);
        view.onCommentDeleted(qiscusComment);
    }

    private Observable<List<QiscusComment>> getCommentsFromNetwork(int lastCommentId) {
        return QiscusApi.getInstance().getComments(currentTopicId, lastCommentId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(bindToLifecycle())
                .doOnNext(comment -> {
                    comment.setRoomId(room.getId());
                    comment.setState(QiscusComment.STATE_ON_PUSHER);
                    Qiscus.getDataStore().addOrUpdate(comment);
                })
                .toList();
    }

    public void loadComments(int count) {
        view.showLoading();
        getCommentsFromNetwork(0)
                .startWith(Qiscus.getDataStore().getObservableComments(currentTopicId, count))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(bindToLifecycle())
                .subscribe(comments -> {
                    if (view != null) {
                        view.showComments(comments);
                        view.dismissLoading();
                    }
                }, throwable -> {
                    throwable.printStackTrace();
                    if (view != null) {
                        view.showError("Failed to load comments!");
                        view.dismissLoading();
                    }
                });
    }

    private boolean isValidOlderComments(List<QiscusComment> qiscusComments, QiscusComment lastQiscusComment) {
        boolean containsLastValidComment = false;
        int size = qiscusComments.size();

        if (size == 1) {
            return qiscusComments.get(0).getCommentBeforeId() == 0;
        }

        for (int i = 0; i < size - 1; i++) {
            if (qiscusComments.get(i).getId() == -1 || qiscusComments.get(i + 1).getId() == -1) {
                return true;
            }

            if (!containsLastValidComment && qiscusComments.get(i).getId() == lastQiscusComment.getCommentBeforeId()) {
                containsLastValidComment = true;
            }

            if (qiscusComments.get(i).getCommentBeforeId() != qiscusComments.get(i + 1).getId()) {
                return false;
            }
        }
        return containsLastValidComment;
    }

    public void loadOlderCommentThan(QiscusComment qiscusComment) {
        view.showLoadMoreLoading();
        Qiscus.getDataStore().getObservableOlderCommentsThan(qiscusComment, currentTopicId, 20)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(bindToLifecycle())
                .flatMap(comments -> isValidOlderComments(comments, qiscusComment) ?
                        Observable.from(comments).toList() : getCommentsFromNetwork(qiscusComment.getId()))
                .subscribe(comments -> {
                    if (view != null) {
                        view.onLoadMore(comments);
                        view.dismissLoading();
                    }
                }, throwable -> {
                    throwable.printStackTrace();
                    if (view != null) {
                        view.showError("Failed to load comments!");
                        view.dismissLoading();
                    }
                });
    }

    private void listenRoomEvent() {
        subscription = QiscusPusherApi.getInstance().listenNewComment()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(bindToLifecycle())
                .subscribe(this::onGotNewComment, Throwable::printStackTrace);
    }

    @Subscribe
    public void onCommentReceivedEvent(QiscusCommentReceivedEvent event) {
        if (event.getQiscusComment().getTopicId() == currentTopicId) {
            view.onNewComment(event.getQiscusComment());
        }
    }

    private void onGotNewComment(QiscusComment qiscusComment) {
        qiscusComment.setState(QiscusComment.STATE_ON_PUSHER);
        Qiscus.getDataStore().addOrUpdate(qiscusComment);

        if (qiscusComment.isAttachment()) {
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
        }

        if (qiscusComment.getTopicId() == currentTopicId) {
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
                    .downloadFile(qiscusComment.getTopicId(), qiscusComment.getAttachmentUri().toString(), qiscusComment.getAttachmentName(),
                            percentage -> qiscusComment.setProgress((int) percentage))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .compose(bindToLifecycle())
                    .doOnNext(file1 -> {
                        if (QiscusImageUtil.isImage(file1)) {
                            QiscusImageUtil.addImageToGallery(file1);
                        }
                    })
                    .subscribe(file1 -> {
                        qiscusComment.setDownloading(false);
                        Qiscus.getDataStore().addOrUpdateLocalPath(qiscusComment.getTopicId(), qiscusComment.getId(),
                                file1.getAbsolutePath());
                        view.refreshComment(qiscusComment);
                        if (qiscusComment.getType() == QiscusComment.Type.AUDIO) {
                            qiscusComment.playAudio();
                        } else if (qiscusComment.getType() == QiscusComment.Type.FILE) {
                            view.onFileDownloaded(file1, MimeTypeMap.getSingleton().getMimeTypeFromExtension(qiscusComment.getExtension()));
                        }
                    }, throwable -> {
                        throwable.printStackTrace();
                        qiscusComment.setDownloading(false);
                        view.showError("Failed to download file!");
                    });
        } else {
            if (qiscusComment.getType() == QiscusComment.Type.AUDIO) {
                qiscusComment.playAudio();
            } else {
                view.onFileDownloaded(file, MimeTypeMap.getSingleton().getMimeTypeFromExtension(qiscusComment.getExtension()));
            }
        }
    }

    @Override
    public void detachView() {
        super.detachView();
        if (subscription != null) {
            subscription.unsubscribe();
        }
        subscription = null;
        room = null;
        EventBus.getDefault().unregister(this);
    }

    public interface View extends QiscusPresenter.View {

        void showLoadMoreLoading();

        void showComments(List<QiscusComment> qiscusComments);

        void onLoadMore(List<QiscusComment> qiscusComments);

        void onSendingComment(QiscusComment qiscusComment);

        void onSuccessSendComment(QiscusComment qiscusComment);

        void onFailedSendComment(QiscusComment qiscusComment);

        void onNewComment(QiscusComment qiscusComment);

        void onCommentDeleted(QiscusComment qiscusComment);

        void refreshComment(QiscusComment qiscusComment);

        void onFileDownloaded(File file, String mimeType);
    }
}
