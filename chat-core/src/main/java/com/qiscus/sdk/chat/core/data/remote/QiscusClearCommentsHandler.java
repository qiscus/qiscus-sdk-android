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
import androidx.core.app.NotificationManagerCompat;

import com.qiscus.sdk.chat.core.QiscusCore;
import com.qiscus.sdk.chat.core.data.model.QParticipant;
import com.qiscus.sdk.chat.core.event.QiscusClearMessageEvent;
import com.qiscus.sdk.chat.core.util.QiscusNumberUtil;

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
public class QiscusClearCommentsHandler {

    private QiscusCore qiscusCore;

    public QiscusClearCommentsHandler(QiscusCore qiscusCore) {
        this.qiscusCore = qiscusCore;
    }

    public void handle(ClearCommentsData clearCommentsData) {
        if (clearCommentsData.getActor().getId().equals(qiscusCore.getQiscusAccount().getId())) {
            Observable.from(clearCommentsData.getRoomIds())
                    .doOnNext(roomId -> {
                        if (qiscusCore.getDataStore().deleteCommentsByRoomId(roomId, clearCommentsData.timestamp)) {
                            EventBus.getDefault().post(new QiscusClearMessageEvent(roomId, clearCommentsData.timestamp));
//                            QiscusPushNotificationUtil.clearPushNotification(qiscusCore.getApps(), roomId);
                            NotificationManagerCompat.from(qiscusCore.getApps()).cancel(QiscusNumberUtil.convertToInt(roomId));
                            qiscusCore.getCacheManager().clearMessageNotifItems(roomId);
                        }
                    })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(roomId -> {
                    }, qiscusCore.getErrorLogger()::print);
        }
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public static class ClearCommentsData {
        private long timestamp;
        private QParticipant actor;
        private List<Long> roomIds;

        public long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }

        public QParticipant getActor() {
            return actor;
        }

        public void setActor(QParticipant actor) {
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
