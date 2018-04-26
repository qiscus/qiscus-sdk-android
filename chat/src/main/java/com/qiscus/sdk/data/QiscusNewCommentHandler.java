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
import android.support.v4.util.Pair;

import com.qiscus.sdk.Qiscus;
import com.qiscus.sdk.data.model.QiscusAccount;
import com.qiscus.sdk.data.model.QiscusChatRoom;
import com.qiscus.sdk.data.model.QiscusComment;
import com.qiscus.sdk.data.model.QiscusRoomMember;
import com.qiscus.sdk.data.remote.QiscusApi;
import com.qiscus.sdk.data.remote.QiscusPusherApi;
import com.qiscus.sdk.event.QiscusCommentReceivedEvent;
import com.qiscus.sdk.util.QiscusAndroidUtil;
import com.qiscus.sdk.util.QiscusPushNotificationUtil;

import org.greenrobot.eventbus.EventBus;

import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * Created on : February 26, 2018
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public final class QiscusNewCommentHandler {
    private static final ScheduledThreadPoolExecutor taskExecutor = new ScheduledThreadPoolExecutor(1);

    private QiscusNewCommentHandler() {

    }

    public static void handle(QiscusComment comment) {
        taskExecutor.schedule(() -> handleNewComment(comment), 0, MILLISECONDS);
    }

    private static void handleNewComment(QiscusComment comment) {
        // Kalau isinya sama dengan yang sudah ada atau komen sudah di hapus
        QiscusComment savedComment = Qiscus.getDataStore().getComment(comment.getUniqueId());
        if (savedComment != null && (savedComment.isDeleted() || savedComment.areContentsTheSame(comment))) {
            return;
        }

        if (comment.isMyComment()) {
            handleMyComment(comment);
        } else {
            handleOpponentComment(comment);
        }
    }

    private static void handleMyComment(QiscusComment comment) {
        keepOriginalMessage(comment);
        clearUnreadCount(comment.getRoomId());
        determineCommentState(comment);
        saveComment(comment);
    }

    /**
     * Mencegah mengubah pesan yang dia kirim sendiri menjadi pesan yang terenkripsi
     *
     * @param comment komennya
     */
    private static void keepOriginalMessage(QiscusComment comment) {
        QiscusComment savedComment = Qiscus.getDataStore().getComment(comment.getUniqueId());
        if (savedComment != null) {
            comment.setMessage(savedComment.getMessage());
            comment.setExtraPayload(savedComment.getExtraPayload());
        } else {
            QiscusEncryptionHandler.setPlaceHolder(comment);
        }
    }

    private static void handleOpponentComment(QiscusComment comment) {
        if (Qiscus.getChatConfig().isEnableEndToEndEncryption()) {
            QiscusEncryptionHandler.decrypt(comment);
        }
        updateUnreadCount(comment.getRoomId());
        notifyDelivered(comment);
        updateLastReadMember(comment);
        pushNotification(comment);
        saveComment(comment);
    }

    private static void clearUnreadCount(long roomId) {
        QiscusChatRoom chatRoom = Qiscus.getDataStore().getChatRoom(roomId);
        if (chatRoom != null && chatRoom.getUnreadCount() > 0) {
            chatRoom.setUnreadCount(0);
            Qiscus.getDataStore().addOrUpdate(chatRoom);
            return;
        }

        if (chatRoom == null) {
            fetchAndSaveRoom(roomId);
        }
    }

    private static void updateUnreadCount(long roomId) {
        QiscusChatRoom chatRoom = Qiscus.getDataStore().getChatRoom(roomId);

        if (chatRoom == null) {
            try {
                chatRoom = QiscusApi.getInstance().getChatRoom(roomId).toBlocking().first();
            } catch (Exception ignore) {
                // Ignore exception
            }
        }

        if (chatRoom != null) {
            chatRoom.setUnreadCount(chatRoom.getUnreadCount() + 1);
            Qiscus.getDataStore().addOrUpdate(chatRoom);
        }
    }

    private static void fetchAndSaveRoom(long roomId) {
        QiscusApi.getInstance()
                .getChatRoom(roomId)
                .doOnNext(qiscusChatRoom -> Qiscus.getDataStore().addOrUpdate(qiscusChatRoom))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(qiscusChatRoom -> {
                }, throwable -> {
                });
    }

    private static void determineCommentState(QiscusComment comment) {
        QiscusChatRoom chatRoom = Qiscus.getDataStore().getChatRoom(comment.getRoomId());

        if (chatRoom == null) {
            try {
                chatRoom = QiscusApi.getInstance().getChatRoom(comment.getRoomId()).toBlocking().first();
            } catch (Exception ignore) {
                // Ignore exception
            }
        }

        if (chatRoom != null) {
            List<QiscusRoomMember> members = chatRoom.getMember();
            determineCommentState(comment, members);
        }
    }

    private static void notifyDelivered(QiscusComment comment) {
        QiscusPusherApi.getInstance().setUserDelivery(comment.getRoomId(), comment.getId());
    }

    private static void updateLastReadMember(QiscusComment comment) {
        QiscusChatRoom chatRoom = Qiscus.getDataStore().getChatRoom(comment.getRoomId());
        if (chatRoom == null) {
            return;
        }

        List<QiscusRoomMember> members = chatRoom.getMember();
        for (QiscusRoomMember member : members) {
            if (member.getEmail().equals(comment.getSenderEmail()) && member.getLastReadCommentId() < comment.getId()) {
                member.setLastReadCommentId(comment.getId());
                member.setLastDeliveredCommentId(comment.getId());
                Qiscus.getDataStore().addOrUpdate(chatRoom);
                break;
            }
        }
    }

    private static void saveComment(QiscusComment comment) {
        QiscusComment savedComment = Qiscus.getDataStore().getComment(comment.getUniqueId());
        if (savedComment == null) {
            addComment(comment);
        } else {
            updateComment(comment);
        }
    }

    private static void addComment(QiscusComment comment) {
        Qiscus.getDataStore().add(comment);
        postEvent(new QiscusCommentReceivedEvent(comment));
    }

    private static void postEvent(QiscusCommentReceivedEvent event) {
        QiscusAndroidUtil.runOnUIThread(() -> EventBus.getDefault().post(event));
    }

    private static void updateComment(QiscusComment comment) {
        Qiscus.getDataStore().update(comment);
    }

    private static void pushNotification(QiscusComment comment) {
        QiscusPushNotificationUtil.handlePushNotification(Qiscus.getApps(), comment);
    }

    private static Pair<Long, Long> getPairedLastState(List<QiscusRoomMember> members) {
        long lastDelivered = Long.MAX_VALUE;
        long lastRead = Long.MAX_VALUE;
        QiscusAccount account = Qiscus.getQiscusAccount();
        for (QiscusRoomMember member : members) {
            if (!member.getEmail().equals(account.getEmail())) {
                if (member.getLastDeliveredCommentId() < lastDelivered) {
                    lastDelivered = member.getLastDeliveredCommentId();
                }

                if (member.getLastReadCommentId() < lastRead) {
                    lastRead = member.getLastReadCommentId();
                    if (lastRead > lastDelivered) {
                        lastDelivered = lastRead;
                    }
                }
            }
        }

        return Pair.create(lastDelivered, lastRead);
    }

    private static void determineCommentState(QiscusComment comment, List<QiscusRoomMember> members) {
        Pair<Long, Long> lastMemberState = getPairedLastState(members);
        if (comment.getId() > lastMemberState.first) {
            comment.setState(QiscusComment.STATE_ON_QISCUS);
        } else if (comment.getId() > lastMemberState.second) {
            comment.setState(QiscusComment.STATE_DELIVERED);
        } else {
            comment.setState(QiscusComment.STATE_READ);
        }
    }
}
