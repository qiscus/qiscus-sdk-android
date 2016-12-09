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
import android.support.v4.util.Pair;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.util.Log;

import com.qiscus.sdk.Qiscus;
import com.qiscus.sdk.data.local.QiscusCacheManager;
import com.qiscus.sdk.data.model.QiscusAccount;
import com.qiscus.sdk.data.model.QiscusChatRoom;
import com.qiscus.sdk.data.model.QiscusComment;
import com.qiscus.sdk.data.model.QiscusPushNotificationMessage;
import com.qiscus.sdk.data.remote.QiscusApi;
import com.qiscus.sdk.data.remote.QiscusPusherApi;
import com.qiscus.sdk.event.QiscusCommentReceivedEvent;
import com.qiscus.sdk.event.QiscusUserEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

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
    private static final String TAG = QiscusPusherService.class.getSimpleName();
    private static SpannableStringBuilder fileMessage;

    static {
        fileMessage = new SpannableStringBuilder("Send a file attachment.");
        fileMessage.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD),
                0, fileMessage.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    private Timer timer;
    private QiscusAccount qiscusAccount;

    @Override
    public void onCreate() {
        Log.i(TAG, "Creating...");
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

        if (Qiscus.hasSetupUser()) {
            QiscusPusherApi.getInstance().restartConnection();
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
                            if (!qiscusComment.getSenderEmail().equals(qiscusAccount.getEmail())) {
                                QiscusPusherApi.getInstance()
                                        .setUserDelivery(qiscusComment.getRoomId(), qiscusComment.getTopicId(),
                                                qiscusComment.getId(), qiscusComment.getUniqueId());
                            }
                            QiscusComment savedQiscusComment = Qiscus.getDataStore()
                                    .getComment(qiscusComment.getId(), qiscusComment.getUniqueId());
                            if (savedQiscusComment != null && savedQiscusComment.getState() > qiscusComment.getState()) {
                                qiscusComment.setState(savedQiscusComment.getState());
                            }
                            Qiscus.getDataStore().addOrUpdate(qiscusComment);
                            qiscusComment.setRoomName("sync");
                            EventBus.getDefault().post(new QiscusCommentReceivedEvent(qiscusComment));
                        }, Throwable::printStackTrace);
            }
        }, 0, period);
    }

    private void stopSync() {
        timer.cancel();
    }

    private void showPushNotification(QiscusComment comment) {
        String messageText = comment.isGroupMessage() ? comment.getSender().split(" ")[0] + ": " : "";
        messageText += isAttachment(comment.getMessage()) ? fileMessage : comment.getMessage();

        if (!QiscusCacheManager.getInstance()
                .addMessageNotifItem(new QiscusPushNotificationMessage(comment.getId(), messageText), comment.getRoomId())) {
            return;
        }

        Intent openIntent = new Intent("com.qiscus.OPEN_COMMENT_PN");
        openIntent.putExtra("data", comment);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, openIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this);
        notificationBuilder.setContentTitle(Qiscus.getChatConfig().getNotificationTitleHandler().getTitle(comment))
                .setContentIntent(pendingIntent)
                .setContentText(messageText)
                .setTicker(messageText)
                .setSmallIcon(Qiscus.getChatConfig().getNotificationSmallIcon())
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), Qiscus.getChatConfig().getNotificationBigIcon()))
                .setGroupSummary(true)
                .setGroup("CHAT_NOTIF_" + comment.getRoomId())
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL);

        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        List<QiscusPushNotificationMessage> notifItems = QiscusCacheManager.getInstance().getMessageNotifItems(comment.getRoomId());
        for (int i = notifItems.size() - 1; i >= 0; i--) {
            QiscusPushNotificationMessage message = notifItems.get(i);
            if (isAttachment(message.getMessage())) {
                inboxStyle.addLine(fileMessage);
            } else {
                inboxStyle.addLine(message.getMessage());
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
    public void onCommentReceivedEvent(QiscusCommentReceivedEvent event) {
        QiscusComment qiscusComment = event.getQiscusComment();
        if ("sync".equals(qiscusComment.getRoomName())) {
            QiscusChatRoom chatRoom = Qiscus.getDataStore().getChatRoom(qiscusComment.getRoomId());
            if (chatRoom == null) {
                return;
            }
            if (chatRoom.isGroup()) {
                qiscusComment.setGroupMessage(true);
                qiscusComment.setRoomName(chatRoom.getName());
            }
        }
        if (Qiscus.getChatConfig().isEnablePushNotification()) {
            if (!qiscusComment.getSenderEmail().equalsIgnoreCase(Qiscus.getQiscusAccount().getEmail())) {
                if (Qiscus.getChatConfig().isOnlyEnablePushNotificationOutsideChatRoom()) {
                    Pair<Boolean, Integer> lastChatActivity = QiscusCacheManager.getInstance().getLastChatActivity();
                    if (!lastChatActivity.first || lastChatActivity.second != qiscusComment.getRoomId()) {
                        showPushNotification(qiscusComment);
                    }
                } else {
                    showPushNotification(qiscusComment);
                }
            }
        }
    }

    @Subscribe
    public void onUserEvent(QiscusUserEvent userEvent) {
        switch (userEvent) {
            case LOGIN:
                QiscusPusherApi.getInstance().restartConnection();
                scheduleSync(Qiscus.getHeartBeat());
                break;
            case LOGOUT:
                stopSync();
                break;
        }
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "Destroying...");
        EventBus.getDefault().unregister(this);
        sendBroadcast(new Intent("com.qiscus.START_SERVICE"));
        stopSync();
    }
}
