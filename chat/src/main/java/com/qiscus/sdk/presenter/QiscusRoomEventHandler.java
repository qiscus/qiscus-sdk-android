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

import android.util.Pair;

import com.qiscus.sdk.Qiscus;
import com.qiscus.sdk.data.model.QiscusAccount;
import com.qiscus.sdk.data.model.QiscusChatRoom;
import com.qiscus.sdk.data.model.QiscusComment;
import com.qiscus.sdk.data.model.QiscusRoomMember;
import com.qiscus.sdk.data.remote.QiscusPusherApi;
import com.qiscus.sdk.event.QiscusChatRoomEvent;
import com.qiscus.sdk.util.QiscusAndroidUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Kelas yang handle perubahan-perubahan state comment di suatu room, dan juga typing
 */
class QiscusRoomEventHandler {
    private QiscusAccount account;
    private QiscusChatRoom room;

    //comment terakhir yang sudah diterima semua anggota room
    private AtomicInteger lastDeliveredCommentId;

    //comment terakhir yang sudah dibaca semua anggota room
    private AtomicInteger lastReadCommentId;

    //status terakhir masing-masing anggota room
    Map<String, Pair<Integer, Integer>> memberState;

    //task untuk listen mqtt room
    private Runnable listenRoomTask;

    //listener untuk setiap perubahan state
    private StateListener listener;

    QiscusRoomEventHandler(QiscusChatRoom qiscusChatRoom, StateListener listener) {
        this.listener = listener;

        //Tidak diset 0 karena kita akan mencari nilai terkecil dari semua anggota room
        lastDeliveredCommentId = new AtomicInteger(Integer.MAX_VALUE);
        lastReadCommentId = new AtomicInteger(Integer.MAX_VALUE);

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

        for (QiscusRoomMember member : room.getMember()) {
            if (!member.getEmail().equals(account.getEmail())) {
                if (member.getLastDeliveredCommentId() < lastDeliveredCommentId.get()) {
                    lastDeliveredCommentId.set(member.getLastDeliveredCommentId());
                }

                if (member.getLastReadCommentId() < lastReadCommentId.get()) {
                    lastReadCommentId.set(member.getLastReadCommentId());
                    if (lastReadCommentId.get() > lastDeliveredCommentId.get()) {
                        lastDeliveredCommentId.set(lastReadCommentId.get());
                    }
                }

                memberState.put(member.getEmail(),
                        Pair.create(member.getLastDeliveredCommentId(), member.getLastReadCommentId()));
            }
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
        if (event.getTopicId() == room.getLastTopicId()) {
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
    private boolean updateLastDelivered(String email, int commentId) {
        Pair<Integer, Integer> state = memberState.get(email);
        if (state != null) {
            if (state.first < commentId) {
                memberState.put(email, Pair.create(commentId, state.second));
                return true;
            }
        } else {
            memberState.put(email, Pair.create(commentId, commentId));
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
    private boolean updateLastRead(String email, int commentId) {
        Pair<Integer, Integer> state = memberState.get(email);
        if (state != null) {
            if (state.second < commentId) {
                memberState.put(email, Pair.create(commentId, commentId));
                return true;
            }
        } else {
            memberState.put(email, Pair.create(commentId, commentId));
            return true;
        }
        return false;
    }

    /**
     * Method yang akan mencoba meng-update lastDeliveredCommentId dan lastReadCommentId semua anggota room
     * jika berhasil di update akan men-trigger listener bahwa comment yg terakhir diterima atau dibaca telah berubah
     */
    private void tryUpdateLastState() {
        int minDelivered = Integer.MAX_VALUE;
        int minRead = Integer.MAX_VALUE;
        for (Map.Entry<String, Pair<Integer, Integer>> state : memberState.entrySet()) {
            if (state.getValue().first < minDelivered) {
                minDelivered = state.getValue().first;
            }

            if (state.getValue().second < minRead) {
                minRead = state.getValue().second;
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
    private void updateLocalMemberState() {
        for (QiscusRoomMember member : room.getMember()) {
            Pair<Integer, Integer> state = memberState.get(member.getEmail());
            if (state != null) {
                member.setLastDeliveredCommentId(state.first);
                member.setLastReadCommentId(state.second);
                Qiscus.getDataStore().addRoomMember(room.getId(), member, room.getDistinctId());
            }
        }
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
            if (qiscusComment.getId() > lastDeliveredCommentId.get()) {
                qiscusComment.setState(QiscusComment.STATE_ON_QISCUS);
            } else if (qiscusComment.getId() > lastReadCommentId.get()) {
                qiscusComment.setState(QiscusComment.STATE_DELIVERED);
            } else {
                qiscusComment.setState(QiscusComment.STATE_READ);
            }
            Qiscus.getDataStore().addOrUpdate(qiscusComment);
        }
    }

    void onGotComment(QiscusComment qiscusComment) {
        if (!qiscusComment.getSender().equals(account.getEmail())) {
            if (updateLastRead(qiscusComment.getSenderEmail(), qiscusComment.getId())) {
                tryUpdateLastState();
            }
        }
    }

    void detach() {
        QiscusAndroidUtil.runOnBackgroundThread(this::updateLocalMemberState);
        QiscusAndroidUtil.cancelRunOnUIThread(listenRoomTask);
        QiscusPusherApi.getInstance().unListenRoom(room);
        listener = null;
        EventBus.getDefault().unregister(this);
    }

    interface StateListener {
        void onChangeLastDelivered(int lastDeliveredCommentId);

        void onChangeLastRead(int lastReadCommentId);

        void onUserTyping(String email, boolean typing);
    }
}
