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

import androidx.annotation.RestrictTo;

import com.qiscus.sdk.chat.core.QiscusCore;
import com.qiscus.sdk.chat.core.data.model.QParticipant;
import com.qiscus.sdk.chat.core.data.model.QChatRoom;
import com.qiscus.sdk.chat.core.data.model.QMessage;
import com.qiscus.sdk.chat.core.event.QMessageDeletedEvent;
import com.qiscus.sdk.chat.core.util.QiscusErrorLogger;

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
                    QMessage qiscusMessage = QiscusCore.getDataStore().getComment(deletedComment.getCommentUniqueId());
                    if (qiscusMessage != null) {
                        qiscusMessage.setMessage("This message has been deleted.");
                        qiscusMessage.setRawType("text");
                        qiscusMessage.setDeleted(true);

                        setRoomData(qiscusMessage);
                    }
                    return qiscusMessage;
                })
                .filter(qiscusMessage -> qiscusMessage != null)
                .doOnNext(qiscusMessage -> {
                    QiscusCore.getDataStore().addOrUpdate(qiscusMessage);
                    QiscusCore.getDataStore().deleteLocalPath(qiscusMessage.getId());

                    EventBus.getDefault().post(new QMessageDeletedEvent(qiscusMessage));
                })
                .toList()
                .doOnNext(qiscusMessages -> {
                    if (QiscusCore.getChatConfig().getDeleteMessageListener() != null) {
                        QiscusCore.getChatConfig().getDeleteMessageListener()
                                .onHandleDeletedCommentNotification(QiscusCore.getApps(),
                                        qiscusMessages, false);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(comments -> {
                }, QiscusErrorLogger::print);
    }

    private static void handleHardDelete(DeletedCommentsData deletedCommentsData) {
        Observable.from(deletedCommentsData.getDeletedComments())
                .map(deletedComment -> {
                    QMessage qiscusMessage = QiscusCore.getDataStore().getComment(deletedComment.getCommentUniqueId());
                    if (qiscusMessage != null) {
                        qiscusMessage.setMessage("This message has been deleted.");
                        qiscusMessage.setRawType("text");
                        qiscusMessage.setDeleted(true);
                        qiscusMessage.setHardDeleted(true);
                        setRoomData(qiscusMessage);
                    }

                    return qiscusMessage;
                })
                .filter(qiscusMessage -> qiscusMessage != null)
                .doOnNext(qiscusMessage -> {
                    // Update chaining id and before id
                    QMessage commentAfter = QiscusCore.getDataStore().getCommentByBeforeId(qiscusMessage.getId());
                    if (commentAfter != null) {
                        commentAfter.setPreviousMessageId(qiscusMessage.getPreviousMessageId());
                        QiscusCore.getDataStore().addOrUpdate(commentAfter);
                    }

                    QiscusCore.getDataStore().addOrUpdate(qiscusMessage);
                    QiscusCore.getDataStore().deleteLocalPath(qiscusMessage.getId());
                    EventBus.getDefault().post(new QMessageDeletedEvent(qiscusMessage, true));
                })
                .toList()
                .doOnNext(qiscusMessages -> {
                    if (QiscusCore.getChatConfig().getDeleteMessageListener() != null) {
                        QiscusCore.getChatConfig().getDeleteMessageListener()
                                .onHandleDeletedCommentNotification(QiscusCore.getApps(),
                                        qiscusMessages, true);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(comments -> {
                }, QiscusErrorLogger::print);
    }

    private static void setRoomData(QMessage qiscusMessage) {
        QChatRoom chatRoom = QiscusCore.getDataStore().getChatRoom(qiscusMessage.getChatRoomId());
        if (chatRoom != null) {
            qiscusMessage.setRoomName(chatRoom.getName());
            qiscusMessage.setRoomAvatar(chatRoom.getAvatarUrl());
            qiscusMessage.setGroupMessage(chatRoom.getType().equals("group"));
        }
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public static class DeletedCommentsData {
        private QParticipant actor;
        private boolean hardDelete;
        private List<DeletedComment> deletedComments;

        public QParticipant getActor() {
            return actor;
        }

        public void setActor(QParticipant actor) {
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
