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
import android.util.Log;

import com.qiscus.library.chat.R;
import com.qiscus.sdk.Qiscus;
import com.qiscus.sdk.data.local.QiscusCacheManager;
import com.qiscus.sdk.data.model.QiscusComment;
import com.qiscus.sdk.data.remote.QiscusPusherApi;
import com.qiscus.sdk.event.QiscusUserEvent;
import com.qiscus.sdk.util.QiscusScheduler;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.List;

import rx.Subscription;

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

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("QiscusPusherService", "Creating...");

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

        if (Qiscus.isLogged()) {
            listenPusherEvent();
        }
    }

    private void listenPusherEvent() {
        pusherEvent = QiscusPusherApi.getInstance().getRoomEvents(Qiscus.getToken())
                .compose(QiscusScheduler.get().applySchedulers(QiscusScheduler.Type.IO))
                .subscribe(roomEventJsonObjectPair -> {
                    if (roomEventJsonObjectPair.first == QiscusPusherApi.RoomEvent.INCOMING_COMMENT) {
                        QiscusComment qiscusComment = QiscusPusherApi.jsonToComment(roomEventJsonObjectPair.second);
                        if (!qiscusComment.getSenderEmail().equalsIgnoreCase(Qiscus.getQiscusAccount().getEmail())) {
                            showPushNotification(qiscusComment);
                        }
                    }
                }, Throwable::printStackTrace);
    }

    private void showPushNotification(QiscusComment comment) {
        QiscusCacheManager.getInstance().addMessageNotifItem(comment.getMessage(), comment.getRoomId());

        Intent openIntent = new Intent("com.qiscus.OPEN_COMMENT_PN");
        openIntent.putExtra("data", comment);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, openIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this);
        notificationBuilder.setContentTitle(comment.getSender())
                .setContentIntent(pendingIntent)
                .setContentText(isAttachment(comment.getMessage()) ? fileMessage : comment.getMessage())
                .setTicker(isAttachment(comment.getMessage()) ? fileMessage : comment.getMessage())
                .setSmallIcon(R.drawable.ic_chat)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_chat))
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
                }
                break;
            case LOGOUT:
                if (pusherEvent != null && !pusherEvent.isUnsubscribed()) {
                    pusherEvent.unsubscribe();
                }
                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
