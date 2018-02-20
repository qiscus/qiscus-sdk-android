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

package com.qiscus.sdk.data.remote;

import android.support.annotation.RestrictTo;

import com.qiscus.sdk.Qiscus;
import com.qiscus.sdk.data.model.QiscusRoomMember;
import com.qiscus.sdk.event.QiscusClearCommentsEvent;
import com.qiscus.sdk.util.QiscusErrorLogger;
import com.qiscus.sdk.util.QiscusPushNotificationUtil;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created on : February 14, 2018
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public final class QiscusClearCommentsHandler {
    private QiscusClearCommentsHandler() {

    }

    public static void handle(ClearCommentsData clearCommentsData) {
        if (clearCommentsData.getActor().getEmail().equals(Qiscus.getQiscusAccount().getEmail())) {
            Observable.from(clearCommentsData.getRoomIds())
                    .doOnNext(roomId -> {
                        if (Qiscus.getDataStore().deleteCommentsByRoomId(roomId, clearCommentsData.timestamp)) {
                            EventBus.getDefault().post(new QiscusClearCommentsEvent(roomId, clearCommentsData.timestamp));
                            QiscusPushNotificationUtil.clearPushNotification(Qiscus.getApps(), roomId);
                        }
                    })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(roomId -> {
                    }, QiscusErrorLogger::print);
        }
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public static class ClearCommentsData {
        private long timestamp;
        private QiscusRoomMember actor;
        private List<Long> roomIds;

        public long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }

        public QiscusRoomMember getActor() {
            return actor;
        }

        public void setActor(QiscusRoomMember actor) {
            this.actor = actor;
        }

        public List<Long> getRoomIds() {
            return roomIds;
        }

        public void setRoomIds(List<Long> roomIds) {
            this.roomIds = roomIds;
        }
    }
}
