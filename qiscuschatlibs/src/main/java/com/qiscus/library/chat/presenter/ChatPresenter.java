package com.qiscus.library.chat.presenter;

import android.net.Uri;
import android.webkit.MimeTypeMap;

import com.qiscus.library.chat.data.local.DataBaseHelper;
import com.qiscus.library.chat.data.model.ChatRoom;
import com.qiscus.library.chat.data.model.Comment;
import com.qiscus.library.chat.data.remote.PusherApi;
import com.qiscus.library.chat.data.remote.QiscusApi;
import com.qiscus.library.chat.event.CommentReceivedEvent;
import com.qiscus.library.chat.util.BaseScheduler;
import com.qiscus.library.chat.util.FileUtil;
import com.qiscus.library.chat.util.ImageUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.File;
import java.util.List;

import rx.Observable;
import rx.Subscription;
import timber.log.Timber;

public class ChatPresenter extends BasePresenter<ChatPresenter.View> {

    private ChatRoom room;
    private Subscription subscription;
    private int currentTopicId;

    public ChatPresenter(View view, ChatRoom room) {
        super(view);
        this.room = room;
        this.currentTopicId = room.getLastTopicId();
        listenRoomEvent();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    private void commentSuccess(Comment comment) {
        comment.setState(Comment.STATE_ON_QISCUS);
        Comment savedComment = DataBaseHelper.getInstance().getComment(comment.getId(), comment.getUniqueId());
        if (savedComment != null) {
            if (savedComment.getState() != Comment.STATE_ON_PUSHER) {
                DataBaseHelper.getInstance().addOrUpdate(comment);
                if (comment.getTopicId() == currentTopicId) {
                    view.onSuccessSendComment(comment);
                }
            }
        } else {
            DataBaseHelper.getInstance().addOrUpdate(comment);
            if (comment.getTopicId() == currentTopicId) {
                view.onSuccessSendComment(comment);
            }
        }
    }

    private void commentFail(Comment comment) {
        Comment savedComment = DataBaseHelper.getInstance().getComment(comment.getId(), comment.getUniqueId());
        if (savedComment != null) {
            if (savedComment.getState() != Comment.STATE_ON_PUSHER) {
                comment.setState(Comment.STATE_FAILED);
                if (comment.getTopicId() == currentTopicId) {
                    view.onFailedSendComment(comment);
                }
            }
        } else {
            comment.setState(Comment.STATE_FAILED);
            if (comment.getTopicId() == currentTopicId) {
                view.onFailedSendComment(comment);
            }
        }
    }

    public void sendComment(String content) {
        final Comment comment = Comment.generateMessage(content, room.getId(), currentTopicId);
        view.onSendingComment(comment);
        QiscusApi.getInstance().postComment(comment)
                .compose(BaseScheduler.pluck().applySchedulers(BaseScheduler.Type.IO))
                .compose(bindToLifecycle())
                .subscribe(this::commentSuccess, throwable -> commentFail(comment));
    }

    public void sendFile(File file) {
        File compressedFile = file;
        if (file.getName().endsWith(".gif")) {
            compressedFile = FileUtil.saveFile(compressedFile, currentTopicId);
        } else if (ImageUtil.isImage(file)) {
            compressedFile = ImageUtil.compressImage(Uri.fromFile(file), currentTopicId);
        } else {
            compressedFile = FileUtil.saveFile(compressedFile, currentTopicId);
        }

        final Comment comment = Comment.generateMessage(String.format("[file] %s [/file]", compressedFile.getPath()),
                                                        room.getId(), currentTopicId);
        comment.setDownloading(true);
        view.onSendingComment(comment);

        final File finalCompressedFile = compressedFile;
        QiscusApi.getInstance().uploadFile(compressedFile, percentage -> comment.setProgress((int) percentage))
                .flatMap(uri -> {
                    comment.setMessage(String.format("[file] %s [/file]", uri.toString()));
                    return QiscusApi.getInstance().postComment(comment);
                })
                .compose(BaseScheduler.pluck().applySchedulers(BaseScheduler.Type.IO))
                .compose(bindToLifecycle())
                .subscribe(commentSend -> {
                    comment.setDownloading(false);
                    DataBaseHelper.getInstance()
                            .addOrUpdateLocalPath(commentSend.getTopicId(), commentSend.getId(), finalCompressedFile.getAbsolutePath());
                    commentSuccess(commentSend);
                    view.refreshComment(comment);
                }, throwable -> {
                    comment.setDownloading(false);
                    commentFail(comment);
                });
    }

    public void resendComment(final Comment comment) {
        if (comment.isAttachment()) {
            resendFile(comment);
        } else {
            comment.setState(Comment.STATE_SENDING);
            view.onNewComment(comment);
            QiscusApi.getInstance().postComment(comment)
                    .compose(BaseScheduler.pluck().applySchedulers(BaseScheduler.Type.IO))
                    .compose(bindToLifecycle())
                    .subscribe(this::commentSuccess, throwable -> commentFail(comment));
        }
    }

    private void resendFile(Comment comment) {
        File file = new File(comment.getAttachmentUri().toString());
        comment.setDownloading(true);
        comment.setState(Comment.STATE_SENDING);
        view.onNewComment(comment);
        if (!file.exists()) {
            comment.setProgress(100);
            QiscusApi.getInstance().postComment(comment)
                    .compose(BaseScheduler.pluck().applySchedulers(BaseScheduler.Type.IO))
                    .compose(bindToLifecycle())
                    .subscribe(commentSend -> {
                        comment.setDownloading(false);
                        DataBaseHelper.getInstance()
                                .addOrUpdateLocalPath(commentSend.getTopicId(), commentSend.getId(), file.getAbsolutePath());
                        commentSuccess(commentSend);
                    }, throwable -> {
                        comment.setDownloading(false);
                        commentFail(comment);
                    });
        } else {
            comment.setProgress(0);
            QiscusApi.getInstance().uploadFile(file, percentage -> comment.setProgress((int) percentage))
                    .flatMap(uri -> {
                        comment.setMessage(String.format("[file] %s [/file]", uri.toString()));
                        return QiscusApi.getInstance().postComment(comment);
                    })
                    .compose(BaseScheduler.pluck().applySchedulers(BaseScheduler.Type.IO))
                    .compose(bindToLifecycle())
                    .subscribe(commentSend -> {
                        comment.setDownloading(false);
                        DataBaseHelper.getInstance()
                                .addOrUpdateLocalPath(commentSend.getTopicId(), commentSend.getId(), file.getAbsolutePath());
                        commentSuccess(commentSend);
                    }, throwable -> {
                        comment.setDownloading(false);
                        commentFail(comment);
                    });
        }
    }

    private Observable<List<Comment>> getCommentsFromNetwork(int lastCommentId) {
        return QiscusApi.getInstance().getComments(currentTopicId, lastCommentId)
                .compose(BaseScheduler.pluck().applySchedulers(BaseScheduler.Type.IO))
                .compose(bindToLifecycle())
                .doOnNext(comment -> {
                    comment.setRoomId(room.getId());
                    comment.setState(Comment.STATE_ON_PUSHER);
                    DataBaseHelper.getInstance().addOrUpdate(comment);
                })
                .toList();
    }

    public void loadComments(int count) {
        view.showLoading();
        DataBaseHelper.getInstance().getObservableComments(currentTopicId, count)
                .compose(BaseScheduler.pluck().applySchedulers(BaseScheduler.Type.IO))
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
                .compose(BaseScheduler.pluck().applySchedulers(BaseScheduler.Type.IO))
                .subscribe(aVoid -> Timber.i("mark topic as read"), Throwable::printStackTrace);
    }

    private boolean isValidComments(List<Comment> comments) {
        boolean containsLastValidComment = false;
        int size = comments.size();

        if (size == 1) {
            return comments.get(0).getId() == room.getLastCommentId();
        }

        for (int i = 0; i < size - 1; i++) {
            if (!containsLastValidComment && comments.get(i).getId() == room.getLastCommentId()) {
                containsLastValidComment = true;
            }

            if (comments.get(i).getCommentBeforeId() != comments.get(i + 1).getId()) {
                return false;
            }
        }
        return containsLastValidComment;
    }

    private boolean isValidOlderComments(List<Comment> comments, Comment lastComment) {
        boolean containsLastValidComment = false;
        int size = comments.size();

        if (size == 1) {
            return comments.get(0).getCommentBeforeId() == 0;
        }

        for (int i = 0; i < size - 1; i++) {
            if (!containsLastValidComment && comments.get(i).getId() == lastComment.getCommentBeforeId()) {
                containsLastValidComment = true;
            }

            if (comments.get(i).getCommentBeforeId() != comments.get(i + 1).getId()) {
                return false;
            }
        }
        return containsLastValidComment;
    }

    public void loadOlderCommentThan(Comment comment) {
        view.showLoadMoreLoading();
        DataBaseHelper.getInstance().getObservableOlderCommentsThan(comment, currentTopicId, 20)
                .compose(BaseScheduler.pluck().applySchedulers(BaseScheduler.Type.IO))
                .compose(bindToLifecycle())
                .flatMap(comments -> isValidOlderComments(comments, comment) ?
                        Observable.from(comments).toList() : getCommentsFromNetwork(comment.getId()))
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
        subscription = PusherApi.getInstance().getRoomEvents(room.getCodeEn())
                .compose(BaseScheduler.pluck().applySchedulers(BaseScheduler.Type.IO))
                .compose(bindToLifecycle())
                .subscribe(roomEventJsonObjectPair -> {
                    if (roomEventJsonObjectPair.first == PusherApi.RoomEvent.COMMENT_POSTED) {
                        Comment comment = PusherApi.jsonToComment(roomEventJsonObjectPair.second);
                        onGotNewComment(comment);
                    }
                }, Throwable::printStackTrace);
    }

    private void onGotNewComment(Comment comment) {
        comment.setState(Comment.STATE_ON_PUSHER);
        DataBaseHelper.getInstance().addOrUpdate(comment);

        if (comment.isAttachment()) {
            if (FileUtil.isContains(comment.getTopicId(), comment.getAttachmentName())) {
                DataBaseHelper.getInstance()
                        .addOrUpdateLocalPath(comment.getTopicId(), comment.getId(),
                                              FileUtil.generateFilePath(comment.getAttachmentName(),
                                                                        comment.getTopicId()));
            }
        }

        if (comment.getTopicId() == currentTopicId) {
            view.onNewComment(comment);
        }
    }

    @Subscribe
    public void onCommentReceivedEvent(CommentReceivedEvent event) {
        onGotNewComment(event.getComment());
    }

    public void downloadFile(final Comment comment) {
        if (comment.isDownloading()) {
            return;
        }

        File file = DataBaseHelper.getInstance().getLocalPath(comment.getId());
        if (file == null) {
            comment.setDownloading(true);
            QiscusApi.getInstance()
                    .downloadFile(comment.getTopicId(), comment.getAttachmentUri().toString(), comment.getAttachmentName(),
                                  percentage -> comment.setProgress((int) percentage))
                    .compose(BaseScheduler.pluck().applySchedulers(BaseScheduler.Type.IO))
                    .compose(bindToLifecycle())
                    .doOnNext(file1 -> {
                        if (ImageUtil.isImage(file1)) {
                            ImageUtil.addImageToGallery(file1);
                        }
                    })
                    .subscribe(file1 -> {
                        comment.setDownloading(false);
                        DataBaseHelper.getInstance().addOrUpdateLocalPath(comment.getTopicId(), comment.getId(),
                                                                          file1.getAbsolutePath());
                        view.refreshComment(comment);
                    }, throwable -> {
                        throwable.printStackTrace();
                        comment.setDownloading(false);
                        view.showError("Failed to download file!");
                    });
        } else {
            view.onFileDownloaded(file, MimeTypeMap.getSingleton().getMimeTypeFromExtension(comment.getExtension()));
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

        void showComments(List<Comment> comments);

        void onLoadMore(List<Comment> comments);

        void onSendingComment(Comment comment);

        void onSuccessSendComment(Comment comment);

        void onFailedSendComment(Comment comment);

        void onNewComment(Comment comment);

        void refreshComment(Comment comment);

        void onFileDownloaded(File file, String mimeType);
    }
}
