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
import android.text.TextUtils;

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
import com.qiscus.sdk.util.QiscusRawDataExtractor;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

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
            comment.setEncrypted(savedComment.isEncrypted());
        } else {
            QiscusEncryptionHandler.setPlaceHolder(comment);
        }
    }

    private static void handleOpponentComment(QiscusComment comment) {
        decryptComment(comment);
        updateUnreadCount(comment.getRoomId());
        notifyDelivered(comment);
        updateLastReadMember(comment);
        saveComment(comment);
    }

    private static void decryptComment(QiscusComment comment) {
        if (Qiscus.getChatConfig().isEnableEndToEndEncryption()) {
            if (comment.isGroupMessage()) {
                QiscusGroupEncryptionHandler.decrypt(comment);
            } else {
                QiscusEncryptionHandler.decrypt(comment);
            }
        }
        if (comment.getMessage().equals(QiscusEncryptionHandler.ENCRYPTED_PLACE_HOLDER)) {
            comment.setEncrypted(true);
        }
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
            chatRoom.setLastComment(null);
            Qiscus.getDataStore().addOrUpdate(chatRoom);
        }
    }

    private static void fetchAndSaveRoom(long roomId) {
        QiscusApi.getInstance()
                .getChatRoom(roomId)
                .doOnNext(qiscusChatRoom -> qiscusChatRoom.setLastComment(null))
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

        chatRoom.setLastComment(null);

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
            handleSenderKeyComment(comment);
            handleGroupEventComment(comment);
        } else {
            updateComment(comment);
        }
    }

    private static void addComment(QiscusComment comment) {
        Qiscus.getDataStore().add(comment);
        if (!comment.isEncrypted()) {
            postEvent(new QiscusCommentReceivedEvent(comment));
            pushNotification(comment);
        }
    }

    private static void handleSenderKeyComment(QiscusComment comment) {
        if (comment.getType() == QiscusComment.Type.CUSTOM) {
            try {
                JSONObject payload = QiscusRawDataExtractor.getPayload(comment);
                if (payload.optString("type").equals("qiscus_group_sender_key")) {
                    JSONObject content = payload.optJSONObject("content");
                    long roomId = content.optLong("group_room_id");
                    String senderKey = content.optString("sender_key");
                    boolean needReply = content.optBoolean("need_reply");
                    String sender = comment.getSenderEmail();
                    if (!TextUtils.isEmpty(senderKey)) {
                        QiscusGroupEncryptionHandler.updateRecipient(sender, roomId, senderKey, needReply);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private static void handleGroupEventComment(QiscusComment comment) {
        if (comment.getType() == QiscusComment.Type.SYSTEM_EVENT) {
            try {
                QiscusAccount account = Qiscus.getQiscusAccount();
                JSONObject payload = QiscusRawDataExtractor.getPayload(comment);
                String subjectEmail = payload.optString("subject_email");
                String objectEmail = payload.optString("object_email");

                boolean enableEncryption = Qiscus.getChatConfig().isEnableEndToEndEncryption();
                QiscusChatRoom room = Qiscus.getDataStore().getChatRoom(comment.getRoomId());
                QiscusRoomMember member = new QiscusRoomMember();

                switch (payload.optString("type")) {
                    case "add_member":
                        if (enableEncryption) {
                            if (objectEmail.equals(account.getEmail())) {
                                QiscusGroupEncryptionHandler.reInitSenderKey(comment.getRoomId(), true);
                            } else if (room != null) {
                                member.setEmail(objectEmail);
                                member.setUsername(payload.optString("object_username"));
                                Qiscus.getDataStore().addOrUpdateRoomMember(room.getId(), member, room.getDistinctId());
                            }
                        }
                        break;
                    case "join_room":
                        if (enableEncryption) {
                            if (subjectEmail.equals(account.getEmail())) {
                                QiscusGroupEncryptionHandler.reInitSenderKey(comment.getRoomId(), true);
                            } else if (room != null) {
                                member.setEmail(subjectEmail);
                                member.setUsername(payload.optString("subject_username"));
                                Qiscus.getDataStore().addOrUpdateRoomMember(room.getId(), member, room.getDistinctId());
                            }
                        }
                        break;
                    case "remove_member":
                        Qiscus.getDataStore().deleteRoomMember(comment.getRoomId(), objectEmail);
                        if (enableEncryption && !objectEmail.equals(account.getEmail())) {
                            QiscusGroupEncryptionHandler.reInitSenderKey(comment.getRoomId(), false);
                        }
                        break;
                    case "left_room":
                        Qiscus.getDataStore().deleteRoomMember(comment.getRoomId(), subjectEmail);
                        if (enableEncryption && !subjectEmail.equals(account.getEmail())) {
                            QiscusGroupEncryptionHandler.reInitSenderKey(comment.getRoomId(), false);
                        }
                        break;
                }
            } catch (Exception e) {
                //Do nothing
            }
        }
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
