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

import com.qiscus.sdk.Qiscus;
import com.qiscus.sdk.data.model.QiscusAccount;
import com.qiscus.sdk.data.model.QiscusChatRoom;
import com.qiscus.sdk.data.model.QiscusComment;
import com.qiscus.sdk.data.model.QiscusRoomMember;
import com.qiscus.sdk.data.remote.QiscusPusherApi;
import com.qiscus.sdk.event.QiscusChatRoomEvent;
import com.qiscus.sdk.util.QiscusAndroidUtil;
import com.qiscus.sdk.util.QiscusRawDataExtractor;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Kelas yang handle perubahan-perubahan state comment di suatu room, dan juga typing
 */
class QiscusRoomEventHandler {
    private QiscusAccount account;
    private QiscusChatRoom room;

    //comment terakhir yang sudah diterima semua anggota room
    private AtomicLong lastDeliveredCommentId;

    //comment terakhir yang sudah dibaca semua anggota room
    private AtomicLong lastReadCommentId;

    //status terakhir masing-masing anggota room
    Map<String, QiscusRoomMember> memberState;

    //task untuk listen mqtt room
    private Runnable listenRoomTask;

    //listener untuk setiap perubahan state
    private StateListener listener;

    QiscusRoomEventHandler(QiscusChatRoom qiscusChatRoom, StateListener listener) {
        this.listener = listener;

        //Tidak diset 0 karena kita akan mencari nilai terkecil dari semua anggota room
        lastDeliveredCommentId = new AtomicLong(0);
        lastReadCommentId = new AtomicLong(0);

        account = Qiscus.getQiscusAccount();
        setRoom(qiscusChatRoom);

        //Register event bus untuk dapetin event dari mqtt
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

        //Listen mqtt
        listenRoomTask = this::listenRoomEvent;
        QiscusAndroidUtil.runOnUIThread(listenRoomTask, 1000);
    }

    void setRoom(QiscusChatRoom room) {
        this.room = room;
        determineMemberState();
    }

    /**
     * mencari lastDeliveredCommentId dan lastReadCommentId yang terkecil dari semua anggota room
     * id terkecil berarti itu yang sudah komplit, karena pasti semua angggota sudah melakukan hal tsb.
     */
    private void determineMemberState() {
        if (memberState == null) {
            memberState = new HashMap<>();
        } else {
            memberState.clear();
        }

        if (room.getMember().isEmpty()) {
            return;
        }

        long minDelivered = Long.MAX_VALUE;
        long minRead = Long.MAX_VALUE;
        for (QiscusRoomMember member : room.getMember()) {
            if (!member.getEmail().equals(account.getEmail())) {
                if (member.getLastDeliveredCommentId() < minDelivered) {
                    minDelivered = member.getLastDeliveredCommentId();
                }

                if (member.getLastReadCommentId() < minRead) {
                    minRead = member.getLastReadCommentId();
                }

                memberState.put(member.getEmail(), member);
            }
        }

        if (minRead > minDelivered) {
            minDelivered = minRead;
        }

        if (minDelivered > lastDeliveredCommentId.get()) {
            lastDeliveredCommentId.set(minDelivered);
            QiscusAndroidUtil.runOnBackgroundThread(this::updateLocalLastDelivered);
            listener.onChangeLastDelivered(lastDeliveredCommentId.get());
        }

        if (minRead > lastReadCommentId.get()) {
            lastReadCommentId.set(minRead);
            QiscusAndroidUtil.runOnBackgroundThread(this::updateLocalLastRead);
            listener.onChangeLastRead(lastReadCommentId.get());
        }
    }

    private void listenRoomEvent() {
        QiscusPusherApi.getInstance().listenRoom(room);
    }

    @Subscribe
    public void onRoomEvent(QiscusChatRoomEvent event) {
        QiscusAndroidUtil.runOnBackgroundThread(() -> handleEvent(event));
    }

    private void handleEvent(QiscusChatRoomEvent event) {
        if (event.getRoomId() == room.getId()) {
            switch (event.getEvent()) {
                case TYPING:
                    listener.onUserTyping(event.getUser(), event.isTyping());
                    break;
                case DELIVERED:
                    if (updateLastDelivered(event.getUser(), event.getCommentId())) {
                        tryUpdateLastState();
                    }
                    break;
                case READ:
                    if (updateLastRead(event.getUser(), event.getCommentId())) {
                        tryUpdateLastState();
                    }
                    break;
            }
        }
    }

    /**
     * update lastDeliveredCommentId dengan yg terbaru dari mqtt
     *
     * @param email     anggota roomnya
     * @param commentId lastDeliveredCommentId nya
     * @return true jika commentId nya lebih besar dari yg sudah disimpan
     */
    private boolean updateLastDelivered(String email, long commentId) {
        QiscusRoomMember member = memberState.get(email);
        if (member != null && member.getLastDeliveredCommentId() < commentId) {
            member.setLastDeliveredCommentId(commentId);
            QiscusAndroidUtil.runOnBackgroundThread(() -> updateLocalMemberState(member));
            return true;
        }
        return false;
    }

    /**
     * update lastReadCommentId dengan yg terbaru dari mqtt
     *
     * @param email     anggota roomnya
     * @param commentId lastReadCommentId nya
     * @return true jika commentId nya lebih besar dari yg sudah disimpan
     */
    private boolean updateLastRead(String email, long commentId) {
        QiscusRoomMember member = memberState.get(email);
        if (member != null && member.getLastReadCommentId() < commentId) {
            member.setLastDeliveredCommentId(commentId);
            member.setLastReadCommentId(commentId);
            QiscusAndroidUtil.runOnBackgroundThread(() -> updateLocalMemberState(member));
            return true;
        }
        return false;
    }

    /**
     * Method yang akan mencoba meng-update lastDeliveredCommentId dan lastReadCommentId semua anggota room
     * jika berhasil di update akan men-trigger listener bahwa comment yg terakhir diterima atau dibaca telah berubah
     */
    private void tryUpdateLastState() {
        long minDelivered = Long.MAX_VALUE;
        long minRead = Long.MAX_VALUE;
        for (Map.Entry<String, QiscusRoomMember> member : memberState.entrySet()) {
            if (member.getValue().getLastDeliveredCommentId() < minDelivered) {
                minDelivered = member.getValue().getLastDeliveredCommentId();
            }

            if (member.getValue().getLastReadCommentId() < minRead) {
                minRead = member.getValue().getLastReadCommentId();
            }
        }

        if (minRead > minDelivered) {
            minDelivered = minRead;
        }

        if (minDelivered > lastDeliveredCommentId.get()) {
            lastDeliveredCommentId.set(minDelivered);
            QiscusAndroidUtil.runOnBackgroundThread(this::updateLocalLastDelivered);
            listener.onChangeLastDelivered(lastDeliveredCommentId.get());
        }

        if (minRead > lastReadCommentId.get()) {
            lastReadCommentId.set(minRead);
            QiscusAndroidUtil.runOnBackgroundThread(this::updateLocalLastRead);
            listener.onChangeLastRead(lastReadCommentId.get());
        }
    }

    /**
     * update local DB
     */
    private void updateLocalLastDelivered() {
        Qiscus.getDataStore().updateLastDeliveredComment(room.getId(), lastDeliveredCommentId.get());
    }

    /**
     * update local DB
     */
    private void updateLocalLastRead() {
        Qiscus.getDataStore().updateLastReadComment(room.getId(), lastReadCommentId.get());
    }

    /**
     * update local DB
     */
    private void updateLocalMemberState(QiscusRoomMember roomMember) {
        Qiscus.getDataStore().addOrUpdateRoomMember(room.getId(), roomMember, room.getDistinctId());
    }

    void transformCommentState(List<QiscusComment> comments, boolean fromLocal) {
        for (QiscusComment comment : comments) {
            transformCommentState(comment, fromLocal);
        }
    }

    void transformCommentState(QiscusComment qiscusComment, boolean fromLocal) {
        if (fromLocal && qiscusComment.getState() == QiscusComment.STATE_SENDING) {
            qiscusComment.setState(QiscusComment.STATE_PENDING);
            Qiscus.getDataStore().addOrUpdate(qiscusComment);
        } else if (qiscusComment.getState() != QiscusComment.STATE_FAILED
                && qiscusComment.getState() != QiscusComment.STATE_PENDING
                && qiscusComment.getState() != QiscusComment.STATE_SENDING
                && qiscusComment.getState() != QiscusComment.STATE_READ) {

            if (qiscusComment.getId() <= lastReadCommentId.get()) {
                qiscusComment.setState(QiscusComment.STATE_READ);
            } else if (qiscusComment.getId() <= lastDeliveredCommentId.get()) {
                qiscusComment.setState(QiscusComment.STATE_DELIVERED);
            } else {
                qiscusComment.setState(QiscusComment.STATE_ON_QISCUS);
            }

            Qiscus.getDataStore().addOrUpdate(qiscusComment);
        }
    }

    void onGotComment(QiscusComment qiscusComment) {
        if (!qiscusComment.getSender().equals(account.getEmail())) {
            //Handle room event such as invite user, kick user
            if (qiscusComment.getType() == QiscusComment.Type.SYSTEM_EVENT) {
                handleRoomChanged(qiscusComment);
            }

            if (updateLastRead(qiscusComment.getSenderEmail(), qiscusComment.getId())) {
                tryUpdateLastState();
            }
        }
    }

    private void handleRoomChanged(QiscusComment qiscusComment) {
        try {
            JSONObject payload = QiscusRawDataExtractor.getPayload(qiscusComment);
            QiscusRoomMember member = new QiscusRoomMember();
            switch (payload.optString("type")) {
                case "add_member":
                    member.setEmail(payload.optString("object_email"));
                    member.setUsername(payload.optString("object_username"));
                    handleMemberAdded(member);
                    break;
                case "join_room":
                    member.setEmail(payload.optString("subject_email"));
                    member.setUsername(payload.optString("subject_username"));
                    handleMemberAdded(member);
                    break;
                case "remove_member":
                    member.setEmail(payload.optString("object_email"));
                    member.setUsername(payload.optString("object_username"));
                    handleMemberRemoved(member);
                    break;
                case "left_room":
                    member.setEmail(payload.optString("subject_email"));
                    member.setUsername(payload.optString("subject_username"));
                    handleMemberRemoved(member);
                    break;
                case "change_room_name":
                    listener.onRoomNameChanged(payload.optString("room_name"));
                    break;
            }
        } catch (JSONException e) {
            //Do nothing
        }
    }

    private void handleMemberAdded(QiscusRoomMember member) {
        if (!memberState.containsKey(member.getEmail())) {
            member.setLastDeliveredCommentId(lastDeliveredCommentId.get());
            member.setLastReadCommentId(lastReadCommentId.get());
            memberState.put(member.getEmail(), member);

            listener.onRoomMemberAdded(member);
            QiscusAndroidUtil.runOnBackgroundThread(() ->
                    Qiscus.getDataStore().addOrUpdateRoomMember(room.getId(), member, room.getDistinctId()));
        }
    }

    private void handleMemberRemoved(QiscusRoomMember member) {
        if (memberState.remove(member.getEmail()) != null) {
            tryUpdateLastState();

            listener.onRoomMemberRemoved(member);
            QiscusAndroidUtil.runOnBackgroundThread(() ->
                    Qiscus.getDataStore().deleteRoomMember(room.getId(), member.getEmail()));
        }
    }

    void detach() {
        QiscusAndroidUtil.cancelRunOnUIThread(listenRoomTask);
        QiscusPusherApi.getInstance().unListenRoom(room);
        listener = null;
        EventBus.getDefault().unregister(this);
    }

    interface StateListener {
        void onRoomNameChanged(String roomName);

        void onRoomMemberAdded(QiscusRoomMember roomMember);

        void onRoomMemberRemoved(QiscusRoomMember roomMember);

        void onChangeLastDelivered(long lastDeliveredCommentId);

        void onChangeLastRead(long lastReadCommentId);

        void onUserTyping(String email, boolean typing);
    }
}
