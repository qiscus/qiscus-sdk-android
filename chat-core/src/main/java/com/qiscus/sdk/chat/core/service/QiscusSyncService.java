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

package com.qiscus.sdk.chat.core.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.qiscus.sdk.chat.core.QiscusCore;
import com.qiscus.sdk.chat.core.data.local.QiscusEventCache;
import com.qiscus.sdk.chat.core.data.remote.QiscusApi;
import com.qiscus.sdk.chat.core.data.remote.QiscusPusherApi;
import com.qiscus.sdk.chat.core.event.QiscusSyncEvent;
import com.qiscus.sdk.chat.core.event.QiscusUserEvent;
import com.qiscus.sdk.chat.core.util.QiscusAndroidUtil;
import com.qiscus.sdk.chat.core.util.QiscusErrorLogger;
import com.qiscus.sdk.chat.core.util.QiscusLogger;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

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

    @Override
    public void onCreate() {
        super.onCreate();
        QiscusLogger.print(TAG, "Creating...");
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

        if (QiscusCore.hasSetupUser()) {
            scheduleSync(QiscusCore.getHeartBeat());
            scheduleConnectionCheck();
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
        if (QiscusCore.hasSetupUser()) {
            QiscusCore.getTaskExecutor()
                    .execute(() -> {
                        try {
                            Thread.sleep(period);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        if (QiscusCore.isOnForeground() && QiscusCore.hasSetupUser()) {
                            if (!QiscusPusherApi.getInstance().isConnected()) {
                                QiscusPusherApi.getInstance().restartConnection();
                            }
                            syncComments();
                            syncEvents();
                            if (QiscusPusherApi.getInstance().isConnected()) {
                                scheduleSync(QiscusCore.getHeartBeat());
                            }
                        }
                    });
        }
    }

    private void scheduleConnectionCheck() {
        if (QiscusCore.hasSetupUser()) {
            QiscusCore.getTaskExecutor()
                    .execute(() -> {
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        if (QiscusCore.isOnForeground() && QiscusCore.hasSetupUser()) {
                            if (!QiscusPusherApi.getInstance().isConnected()) {
                                scheduleSync(QiscusCore.getHeartBeat());
                            }
                            scheduleConnectionCheck();
                        }
                    });
        }
    }

    private void syncEvents() {
        QiscusApi.getInstance().synchronizeEvent(QiscusEventCache.getInstance().getLastEventId())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(events -> {
                }, QiscusErrorLogger::print);
    }

    private void syncComments() {
        QiscusApi.getInstance().sync()
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
                .subscribe(QiscusPusherApi::handleReceivedComment, throwable -> {
                    QiscusErrorLogger.print(throwable);
                    EventBus.getDefault().post(QiscusSyncEvent.FAILED);
                    QiscusLogger.print("Sync failed...");
                });
    }

    @Subscribe
    public void onUserEvent(QiscusUserEvent userEvent) {
        switch (userEvent) {
            case LOGIN:
                QiscusAndroidUtil.runOnUIThread(() -> QiscusPusherApi.getInstance().restartConnection());
                scheduleSync(QiscusCore.getHeartBeat());
                scheduleConnectionCheck();
                break;
            case LOGOUT:
                break;
        }
    }

    @Override
    public void onDestroy() {
        QiscusLogger.print(TAG, "Destroying...");
        EventBus.getDefault().unregister(this);
        sendBroadcast(new Intent("com.qiscus.START_SERVICE"));
        super.onDestroy();
    }
}
