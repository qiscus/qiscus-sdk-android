package com.qiscus.sdk.presenter;

import android.net.Uri;
import android.webkit.MimeTypeMap;

import com.qiscus.sdk.data.local.QiscusDataBaseHelper;
import com.qiscus.sdk.data.model.QiscusChatRoom;
import com.qiscus.sdk.data.model.QiscusComment;
import com.qiscus.sdk.data.remote.QiscusPusherApi;
import com.qiscus.sdk.data.remote.QiscusApi;
import com.qiscus.sdk.event.QiscusCommentReceivedEvent;
import com.qiscus.sdk.util.QiscusScheduler;
import com.qiscus.sdk.util.QiscusFileUtil;
import com.qiscus.sdk.util.QiscusImageUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.File;
import java.util.List;

import rx.Observable;
import rx.Subscription;
import timber.log.Timber;

public class ChatPresenter extends BasePresenter<ChatPresenter.View> {

    private QiscusChatRoom room;
    private Subscription subscription;
    private int currentTopicId;

    public ChatPresenter(View view, QiscusChatRoom room) {
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
        QiscusComment savedQiscusComment = QiscusDataBaseHelper.getInstance().getComment(qiscusComment.getId(), qiscusComment.getUniqueId());
        if (savedQiscusComment != null) {
            if (savedQiscusComment.getState() != QiscusComment.STATE_ON_PUSHER) {
                QiscusDataBaseHelper.getInstance().addOrUpdate(qiscusComment);
                if (qiscusComment.getTopicId() == currentTopicId) {
                    view.onSuccessSendComment(qiscusComment);
                }
            }
        } else {
            QiscusDataBaseHelper.getInstance().addOrUpdate(qiscusComment);
            if (qiscusComment.getTopicId() == currentTopicId) {
                view.onSuccessSendComment(qiscusComment);
            }
        }
    }

    private void commentFail(QiscusComment qiscusComment) {
        QiscusComment savedQiscusComment = QiscusDataBaseHelper.getInstance().getComment(qiscusComment.getId(), qiscusComment.getUniqueId());
        if (savedQiscusComment != null) {
            if (savedQiscusComment.getState() != QiscusComment.STATE_ON_PUSHER) {
                qiscusComment.setState(QiscusComment.STATE_FAILED);
                if (qiscusComment.getTopicId() == currentTopicId) {
                    view.onFailedSendComment(qiscusComment);
                }
            }
        } else {
            qiscusComment.setState(QiscusComment.STATE_FAILED);
            if (qiscusComment.getTopicId() == currentTopicId) {
                view.onFailedSendComment(qiscusComment);
            }
        }
    }

    public void sendComment(String content) {
        final QiscusComment qiscusComment = QiscusComment.generateMessage(content, room.getId(), currentTopicId);
        view.onSendingComment(qiscusComment);
        QiscusApi.getInstance().postComment(qiscusComment)
                .compose(QiscusScheduler.get().applySchedulers(QiscusScheduler.Type.IO))
                .compose(bindToLifecycle())
                .subscribe(this::commentSuccess, throwable -> commentFail(qiscusComment));
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
                .compose(QiscusScheduler.get().applySchedulers(QiscusScheduler.Type.IO))
                .compose(bindToLifecycle())
                .subscribe(commentSend -> {
                    qiscusComment.setDownloading(false);
                    QiscusDataBaseHelper.getInstance()
                            .addOrUpdateLocalPath(commentSend.getTopicId(), commentSend.getId(), finalCompressedFile.getAbsolutePath());
                    commentSuccess(commentSend);
                    view.refreshComment(qiscusComment);
                }, throwable -> {
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
                    .compose(QiscusScheduler.get().applySchedulers(QiscusScheduler.Type.IO))
                    .compose(bindToLifecycle())
                    .subscribe(this::commentSuccess, throwable -> commentFail(qiscusComment));
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
                    .compose(QiscusScheduler.get().applySchedulers(QiscusScheduler.Type.IO))
                    .compose(bindToLifecycle())
                    .subscribe(commentSend -> {
                        qiscusComment.setDownloading(false);
                        QiscusDataBaseHelper.getInstance()
                                .addOrUpdateLocalPath(commentSend.getTopicId(), commentSend.getId(), file.getAbsolutePath());
                        commentSuccess(commentSend);
                    }, throwable -> {
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
                    .compose(QiscusScheduler.get().applySchedulers(QiscusScheduler.Type.IO))
                    .compose(bindToLifecycle())
                    .subscribe(commentSend -> {
                        qiscusComment.setDownloading(false);
                        QiscusDataBaseHelper.getInstance()
                                .addOrUpdateLocalPath(commentSend.getTopicId(), commentSend.getId(), file.getAbsolutePath());
                        commentSuccess(commentSend);
                    }, throwable -> {
                        qiscusComment.setDownloading(false);
                        commentFail(qiscusComment);
                    });
        }
    }

    private Observable<List<QiscusComment>> getCommentsFromNetwork(int lastCommentId) {
        return QiscusApi.getInstance().getComments(currentTopicId, lastCommentId)
                .compose(QiscusScheduler.get().applySchedulers(QiscusScheduler.Type.IO))
                .compose(bindToLifecycle())
                .doOnNext(comment -> {
                    comment.setRoomId(room.getId());
                    comment.setState(QiscusComment.STATE_ON_PUSHER);
                    QiscusDataBaseHelper.getInstance().addOrUpdate(comment);
                })
                .toList();
    }

    public void loadComments(int count) {
        view.showLoading();
        QiscusDataBaseHelper.getInstance().getObservableComments(currentTopicId, count)
                .compose(QiscusScheduler.get().applySchedulers(QiscusScheduler.Type.IO))
                .compose(bindToLifecycle())
                .flatMap(comments -> isValidComments(comments) ? Observable.from(comments).toList() : getCommentsFromNetwork(0))
                .subscribe(comments -> {
                    if (view != null) {
                        markAsRead();
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

    private void markAsRead() {
        QiscusApi.getInstance().markTopicAsRead(room.getLastTopicId())
                .compose(QiscusScheduler.get().applySchedulers(QiscusScheduler.Type.IO))
                .subscribe(aVoid -> Timber.i("mark topic as read"), Throwable::printStackTrace);
    }

    private boolean isValidComments(List<QiscusComment> qiscusComments) {
        boolean containsLastValidComment = false;
        int size = qiscusComments.size();

        if (size == 1) {
            return qiscusComments.get(0).getId() == room.getLastCommentId();
        }

        for (int i = 0; i < size - 1; i++) {
            if (!containsLastValidComment && qiscusComments.get(i).getId() == room.getLastCommentId()) {
                containsLastValidComment = true;
            }

            if (qiscusComments.get(i).getCommentBeforeId() != qiscusComments.get(i + 1).getId()) {
                return false;
            }
        }
        return containsLastValidComment;
    }

    private boolean isValidOlderComments(List<QiscusComment> qiscusComments, QiscusComment lastQiscusComment) {
        boolean containsLastValidComment = false;
        int size = qiscusComments.size();

        if (size == 1) {
            return qiscusComments.get(0).getCommentBeforeId() == 0;
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

    public void loadOlderCommentThan(QiscusComment qiscusComment) {
        view.showLoadMoreLoading();
        QiscusDataBaseHelper.getInstance().getObservableOlderCommentsThan(qiscusComment, currentTopicId, 20)
                .compose(QiscusScheduler.get().applySchedulers(QiscusScheduler.Type.IO))
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
        subscription = QiscusPusherApi.getInstance().getRoomEvents(room.getCodeEn())
                .compose(QiscusScheduler.get().applySchedulers(QiscusScheduler.Type.IO))
                .compose(bindToLifecycle())
                .subscribe(roomEventJsonObjectPair -> {
                    if (roomEventJsonObjectPair.first == QiscusPusherApi.RoomEvent.COMMENT_POSTED) {
                        QiscusComment qiscusComment = QiscusPusherApi.jsonToComment(roomEventJsonObjectPair.second);
                        onGotNewComment(qiscusComment);
                    }
                }, Throwable::printStackTrace);
    }

    private void onGotNewComment(QiscusComment qiscusComment) {
        qiscusComment.setState(QiscusComment.STATE_ON_PUSHER);
        QiscusDataBaseHelper.getInstance().addOrUpdate(qiscusComment);

        if (qiscusComment.isAttachment()) {
            if (QiscusFileUtil.isContains(qiscusComment.getTopicId(), qiscusComment.getAttachmentName())) {
                QiscusDataBaseHelper.getInstance()
                        .addOrUpdateLocalPath(qiscusComment.getTopicId(), qiscusComment.getId(),
                                              QiscusFileUtil.generateFilePath(qiscusComment.getAttachmentName(),
                                                                        qiscusComment.getTopicId()));
            }
        }

        if (qiscusComment.getTopicId() == currentTopicId) {
            view.onNewComment(qiscusComment);
        }
    }

    @Subscribe
    public void onCommentReceivedEvent(QiscusCommentReceivedEvent event) {
        onGotNewComment(event.getQiscusComment());
    }

    public void downloadFile(final QiscusComment qiscusComment) {
        if (qiscusComment.isDownloading()) {
            return;
        }

        File file = QiscusDataBaseHelper.getInstance().getLocalPath(qiscusComment.getId());
        if (file == null) {
            qiscusComment.setDownloading(true);
            QiscusApi.getInstance()
                    .downloadFile(qiscusComment.getTopicId(), qiscusComment.getAttachmentUri().toString(), qiscusComment.getAttachmentName(),
                                  percentage -> qiscusComment.setProgress((int) percentage))
                    .compose(QiscusScheduler.get().applySchedulers(QiscusScheduler.Type.IO))
                    .compose(bindToLifecycle())
                    .doOnNext(file1 -> {
                        if (QiscusImageUtil.isImage(file1)) {
                            QiscusImageUtil.addImageToGallery(file1);
                        }
                    })
                    .subscribe(file1 -> {
                        qiscusComment.setDownloading(false);
                        QiscusDataBaseHelper.getInstance().addOrUpdateLocalPath(qiscusComment.getTopicId(), qiscusComment.getId(),
                                                                          file1.getAbsolutePath());
                        view.refreshComment(qiscusComment);
                    }, throwable -> {
                        throwable.printStackTrace();
                        qiscusComment.setDownloading(false);
                        view.showError("Failed to download file!");
                    });
        } else {
            view.onFileDownloaded(file, MimeTypeMap.getSingleton().getMimeTypeFromExtension(qiscusComment.getExtension()));
        }
    }

    @Override
    public void detachView() {
        super.detachView();
        markAsRead();
        if (subscription != null) {
            subscription.unsubscribe();
        }
        subscription = null;
        room = null;
        EventBus.getDefault().unregister(this);
    }

    public interface View extends BasePresenter.View {

        void showLoadMoreLoading();

        void showComments(List<QiscusComment> qiscusComments);

        void onLoadMore(List<QiscusComment> qiscusComments);

        void onSendingComment(QiscusComment qiscusComment);

        void onSuccessSendComment(QiscusComment qiscusComment);

        void onFailedSendComment(QiscusComment qiscusComment);

        void onNewComment(QiscusComment qiscusComment);

        void refreshComment(QiscusComment qiscusComment);

        void onFileDownloaded(File file, String mimeType);
    }
}
