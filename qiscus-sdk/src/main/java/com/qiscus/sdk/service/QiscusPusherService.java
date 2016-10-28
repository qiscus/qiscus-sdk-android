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

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.text.Spannable;
import android.text.SpannableStringBuilder;

import com.qiscus.sdk.Qiscus;
import com.qiscus.sdk.data.local.QiscusCacheManager;
import com.qiscus.sdk.data.model.QiscusComment;
import com.qiscus.sdk.data.remote.QiscusApi;
import com.qiscus.sdk.data.remote.QiscusPusherApi;
import com.qiscus.sdk.event.QiscusCommentReceivedEvent;
import com.qiscus.sdk.event.QiscusUserEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created on : June 29, 2016
 * Author     : zetbaitsu
 * Name       : Zetra
 * Email      : zetra@mail.ugm.ac.id
 * GitHub     : https://github.com/zetbaitsu
 * LinkedIn   : https://id.linkedin.com/in/zetbaitsu
 */
public class QiscusPusherService extends Service {
    private static SpannableStringBuilder fileMessage;

    static {
        fileMessage = new SpannableStringBuilder("Send a file attachment.");
        fileMessage.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD),
                0, fileMessage.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    private Subscription pusherEvent;
    private Timer timer;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

        if (Qiscus.hasSetupUser()) {
            if (pusherEvent != null && !pusherEvent.isUnsubscribed()) {
                pusherEvent.unsubscribe();
            }
            listenPusherEvent();
            scheduleSync(Qiscus.getHeartBeat());
        }
        return START_STICKY;
    }

    private void scheduleSync(long period) {
        if (timer != null) {
            stopSync();
        }
        timer = new Timer("qiscus_sync", true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                QiscusApi.getInstance().sync()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(qiscusComment -> {
                            EventBus.getDefault().post(new QiscusCommentReceivedEvent(qiscusComment));
                            qiscusComment.setUniqueId(String.valueOf(qiscusComment.getId()));
                            Qiscus.getDataStore().addOrUpdate(qiscusComment);
                        }, Throwable::printStackTrace);
            }
        }, 0, period);
    }

    private void stopSync() {
        timer.cancel();
    }

    private void listenPusherEvent() {
        pusherEvent = QiscusPusherApi.getInstance().listenNewComment()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(qiscusComment -> {
                    if (!qiscusComment.getSenderEmail().equalsIgnoreCase(Qiscus.getQiscusAccount().getEmail())) {
                        showPushNotification(qiscusComment);
                    }
                }, Throwable::printStackTrace);
    }

    private void showPushNotification(QiscusComment comment) {
        QiscusCacheManager.getInstance().addMessageNotifItem(comment.getMessage(), comment.getRoomId());

        Intent openIntent = new Intent("com.qiscus.OPEN_COMMENT_PN");
        openIntent.putExtra("data", comment);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, openIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this);
        notificationBuilder.setContentTitle(Qiscus.getChatConfig().getNotificationTitleHandler().getTitle(comment))
                .setContentIntent(pendingIntent)
                .setContentText(isAttachment(comment.getMessage()) ? fileMessage : comment.getMessage())
                .setTicker(isAttachment(comment.getMessage()) ? fileMessage : comment.getMessage())
                .setSmallIcon(Qiscus.getChatConfig().getNotificationSmallIcon())
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), Qiscus.getChatConfig().getNotificationBigIcon()))
                .setGroupSummary(true)
                .setGroup("CHAT_NOTIF_" + comment.getRoomId())
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL);

        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        List<String> notifItems = QiscusCacheManager.getInstance().getMessageNotifItems(comment.getRoomId());
        for (String message : notifItems) {
            if (isAttachment(message)) {
                inboxStyle.addLine(fileMessage);
            } else {
                inboxStyle.addLine(message);
            }
        }
        inboxStyle.setSummaryText(notifItems.size() + " new message");
        notificationBuilder.setStyle(inboxStyle);

        NotificationManagerCompat.from(this).notify(comment.getRoomId(), notificationBuilder.build());
    }

    public boolean isAttachment(String message) {
        return message.startsWith("[file]") && message.endsWith("[/file]");
    }

    @Subscribe
    public void onUserEvent(QiscusUserEvent userEvent) {
        switch (userEvent) {
            case LOGIN:
                if (pusherEvent == null || pusherEvent.isUnsubscribed()) {
                    listenPusherEvent();
                    scheduleSync(Qiscus.getHeartBeat());
                }
                break;
            case LOGOUT:
                if (pusherEvent != null && !pusherEvent.isUnsubscribed()) {
                    pusherEvent.unsubscribe();
                    stopSync();
                }
                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        sendBroadcast(new Intent("com.qiscus.START_SERVICE"));
    }
}
