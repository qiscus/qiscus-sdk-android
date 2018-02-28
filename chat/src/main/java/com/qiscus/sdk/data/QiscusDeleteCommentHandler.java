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

package com.qiscus.sdk.data;

import android.support.annotation.RestrictTo;

import com.qiscus.sdk.Qiscus;
import com.qiscus.sdk.data.model.QiscusChatRoom;
import com.qiscus.sdk.data.model.QiscusComment;
import com.qiscus.sdk.data.model.QiscusRoomMember;
import com.qiscus.sdk.event.QiscusCommentDeletedEvent;
import com.qiscus.sdk.util.QiscusErrorLogger;
import com.qiscus.sdk.util.QiscusPushNotificationUtil;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created on : February 08, 2018
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public final class QiscusDeleteCommentHandler {
    private QiscusDeleteCommentHandler() {

    }

    public static void handle(DeletedCommentsData deletedCommentsData) {
        if (deletedCommentsData.isHardDelete()) {
            handleHardDelete(deletedCommentsData);
        } else {
            handleSoftDelete(deletedCommentsData);
        }
    }

    private static void handleSoftDelete(DeletedCommentsData deletedCommentsData) {
        Observable.from(deletedCommentsData.getDeletedComments())
                .map(deletedComment -> {
                    QiscusComment qiscusComment = Qiscus.getDataStore().getComment(deletedComment.getCommentUniqueId());
                    if (qiscusComment != null) {
                        qiscusComment.setMessage("This message has been deleted.");
                        qiscusComment.setRawType("text");
                        qiscusComment.setDeleted(true);

                        setRoomData(qiscusComment);
                        QiscusChatRoom chatRoom = Qiscus.getDataStore().getChatRoom(qiscusComment.getRoomId());
                        qiscusComment.setRoomName(chatRoom.getName());
                        qiscusComment.setRoomAvatar(chatRoom.getAvatarUrl());
                        qiscusComment.setGroupMessage(chatRoom.isGroup());
                    }
                    return qiscusComment;
                })
                .filter(qiscusComment -> qiscusComment != null)
                .doOnNext(qiscusComment -> {
                    Qiscus.getDataStore().addOrUpdate(qiscusComment);
                    Qiscus.getDataStore().deleteLocalPath(qiscusComment.getId());

                    EventBus.getDefault().post(new QiscusCommentDeletedEvent(qiscusComment));
                })
                .toList()
                .doOnNext(comments -> QiscusPushNotificationUtil
                        .handleDeletedCommentNotification(Qiscus.getApps(), comments, false))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(comments -> {
                }, QiscusErrorLogger::print);
    }

    private static void handleHardDelete(DeletedCommentsData deletedCommentsData) {
        Observable.from(deletedCommentsData.getDeletedComments())
                .map(deletedComment -> {
                    QiscusComment qiscusComment = Qiscus.getDataStore().getComment(deletedComment.getCommentUniqueId());
                    if (qiscusComment != null) {
                        setRoomData(qiscusComment);
                    }

                    return qiscusComment;
                })
                .filter(qiscusComment -> qiscusComment != null)
                .doOnNext(qiscusComment -> {
                    // Update chaining id and before id
                    QiscusComment commentAfter = Qiscus.getDataStore().getCommentByBeforeId(qiscusComment.getId());
                    if (commentAfter != null) {
                        commentAfter.setCommentBeforeId(qiscusComment.getCommentBeforeId());
                        Qiscus.getDataStore().addOrUpdate(commentAfter);
                    }

                    Qiscus.getDataStore().delete(qiscusComment);
                    EventBus.getDefault().post(new QiscusCommentDeletedEvent(qiscusComment, true));
                })
                .toList()
                .doOnNext(comments -> QiscusPushNotificationUtil
                        .handleDeletedCommentNotification(Qiscus.getApps(), comments, true))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(comments -> {
                }, QiscusErrorLogger::print);
    }

    private static void setRoomData(QiscusComment qiscusComment) {
        QiscusChatRoom chatRoom = Qiscus.getDataStore().getChatRoom(qiscusComment.getRoomId());
        if (chatRoom != null) {
            qiscusComment.setRoomName(chatRoom.getName());
            qiscusComment.setRoomAvatar(chatRoom.getAvatarUrl());
            qiscusComment.setGroupMessage(chatRoom.isGroup());
        }
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public static class DeletedCommentsData {
        private QiscusRoomMember actor;
        private boolean hardDelete;
        private List<DeletedComment> deletedComments;

        public QiscusRoomMember getActor() {
            return actor;
        }

        public void setActor(QiscusRoomMember actor) {
            this.actor = actor;
        }

        public boolean isHardDelete() {
            return hardDelete;
        }

        public void setHardDelete(boolean hardDelete) {
            this.hardDelete = hardDelete;
        }

        public List<DeletedComment> getDeletedComments() {
            return deletedComments;
        }

        public void setDeletedComments(List<DeletedComment> deletedComments) {
            this.deletedComments = deletedComments;
        }

        @Override
        public String toString() {
            return "DeletedCommentsData{" +
                    "actor=" + actor +
                    ", hardDelete=" + hardDelete +
                    ", deletedComments=" + deletedComments +
                    '}';
        }

        public static class DeletedComment {
            private long roomId;
            private String commentUniqueId;

            public DeletedComment(long roomId, String commentUniqueId) {
                this.roomId = roomId;
                this.commentUniqueId = commentUniqueId;
            }

            public long getRoomId() {
                return roomId;
            }

            public String getCommentUniqueId() {
                return commentUniqueId;
            }

            @Override
            public String toString() {
                return "DeletedComment{" +
                        "roomId=" + roomId +
                        ", commentUniqueId='" + commentUniqueId + '\'' +
                        '}';
            }
        }
    }
}
