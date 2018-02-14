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

package com.qiscus.sdk.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.qiscus.sdk.Qiscus;
import com.qiscus.sdk.data.local.QiscusEventCache;
import com.qiscus.sdk.data.model.QiscusAccount;
import com.qiscus.sdk.data.model.QiscusComment;
import com.qiscus.sdk.data.remote.QiscusApi;
import com.qiscus.sdk.data.remote.QiscusPusherApi;
import com.qiscus.sdk.event.QiscusCommentReceivedEvent;
import com.qiscus.sdk.event.QiscusSyncEvent;
import com.qiscus.sdk.event.QiscusUserEvent;
import com.qiscus.sdk.util.QiscusAndroidUtil;
import com.qiscus.sdk.util.QiscusErrorLogger;
import com.qiscus.sdk.util.QiscusLogger;
import com.qiscus.sdk.util.QiscusPushNotificationUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created on : June 29, 2016
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
public class QiscusSyncService extends Service {
    private static final String TAG = QiscusSyncService.class.getSimpleName();

    private QiscusAccount qiscusAccount;
    private ScheduledFuture<?> scheduledSync;

    @Override
    public void onCreate() {
        super.onCreate();
        QiscusLogger.print(TAG, "Creating...");
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

        if (Qiscus.hasSetupUser()) {
            QiscusAndroidUtil.runOnUIThread(() -> QiscusPusherApi.getInstance().restartConnection());
            scheduleSync(Qiscus.getHeartBeat());
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    private void scheduleSync(long period) {
        qiscusAccount = Qiscus.getQiscusAccount();
        stopSync();

        scheduledSync = Qiscus.getTaskExecutor()
                .scheduleWithFixedDelay(() -> {
                    if (Qiscus.isOnForeground()) {
                        syncComments();
                        syncEvents();
                    }
                }, 0, period, TimeUnit.MILLISECONDS);
    }

    private void syncEvents() {
        QiscusApi.getInstance().getEvents(QiscusEventCache.getInstance().getLastEventId())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(events -> {
                }, QiscusErrorLogger::print);
    }

    private void syncComments() {
        QiscusApi.getInstance().sync()
                .doOnNext(qiscusComment -> {
                    if (!qiscusComment.getSenderEmail().equals(qiscusAccount.getEmail())) {
                        QiscusPusherApi.getInstance()
                                .setUserDelivery(qiscusComment.getRoomId(), qiscusComment.getId());
                    }
                    QiscusComment savedQiscusComment = Qiscus.getDataStore()
                            .getComment(qiscusComment.getId(), qiscusComment.getUniqueId());
                    if (savedQiscusComment != null && savedQiscusComment.getState() > qiscusComment.getState()) {
                        qiscusComment.setState(savedQiscusComment.getState());
                    }
                })
                .doOnSubscribe(() -> {
                    EventBus.getDefault().post((QiscusSyncEvent.STARTED));
                    QiscusLogger.print("Sync started...");
                })
                .doOnCompleted(() -> {
                    EventBus.getDefault().post((QiscusSyncEvent.COMPLETED));
                    QiscusLogger.print("Sync completed...");
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(qiscusComment -> {
                    QiscusPushNotificationUtil.handlePushNotification(Qiscus.getApps(), qiscusComment);
                    EventBus.getDefault().post(new QiscusCommentReceivedEvent(qiscusComment));
                }, throwable -> {
                    QiscusErrorLogger.print(throwable);
                    EventBus.getDefault().post(QiscusSyncEvent.FAILED);
                    QiscusLogger.print("Sync failed...");
                });
    }

    private void stopSync() {
        if (scheduledSync != null) {
            scheduledSync.cancel(true);
        }
    }

    @Subscribe
    public void onUserEvent(QiscusUserEvent userEvent) {
        switch (userEvent) {
            case LOGIN:
                QiscusAndroidUtil.runOnUIThread(() -> QiscusPusherApi.getInstance().restartConnection());
                scheduleSync(Qiscus.getHeartBeat());
                break;
            case LOGOUT:
                stopSync();
                break;
        }
    }

    @Override
    public void onDestroy() {
        QiscusLogger.print(TAG, "Destroying...");
        EventBus.getDefault().unregister(this);
        sendBroadcast(new Intent("com.qiscus.START_SERVICE"));
        stopSync();
        super.onDestroy();
    }
}
