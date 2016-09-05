package com.qiscus.library.chat.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.parse.ParseAnalytics;
import com.parse.ParsePushBroadcastReceiver;
import com.qiscus.library.chat.Qiscus;
import com.qiscus.library.chat.R;
import com.qiscus.library.chat.data.local.CacheManager;
import com.qiscus.library.chat.data.model.QiscusComment;
import com.qiscus.library.chat.data.remote.PusherApi;
import com.qiscus.library.chat.data.remote.QiscusApi;
import com.qiscus.library.chat.event.CommentReceivedEvent;
import com.qiscus.library.chat.ui.ChatActivity;
import com.qiscus.library.chat.util.BaseScheduler;
import com.qiscus.library.chat.util.Qson;

import org.greenrobot.eventbus.EventBus;

import java.util.List;
import java.util.Random;

import timber.log.Timber;

public class ParseReceiver extends ParsePushBroadcastReceiver {

    private static SpannableStringBuilder fileMessage;

    static {
        fileMessage = new SpannableStringBuilder("Send a file attachment.");
        fileMessage.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD),
                            0, fileMessage.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    @Override
    protected void onPushReceive(Context context, Intent intent) {
        printPushData(intent);

        // Don't try to continue if not authorized
        if (!Qiscus.isLogged()) {
            return;
        }

        String data = intent.getStringExtra(KEY_PUSH_DATA);
        if (data != null) {
            JsonObject dataJson = Qson.pluck().getParser().fromJson(data, JsonObject.class);
            String action = dataJson.get("action").getAsString();
            try {
                switch (action) {
                    case "com.qiscus.COMMENT_RECEIVED":
                        JsonObject messageJson = dataJson.get("extra").getAsJsonObject();
                        QiscusComment qiscusComment = PusherApi.jsonToComment(messageJson);
                        if (!qiscusComment.getSenderEmail().equals(Qiscus.getQiscusAccount().getEmail())) {
                            showPushNotification(context, intent, qiscusComment.getRoomId());
                        }
                        break;

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void printPushData(Intent intent) {
        Bundle bundle = intent.getExtras();
        for (String key : bundle.keySet()) {
            Timber.d("Extra " + key + " -> " + bundle.get(key));
        }
    }

    private void openChatRoom(final Context context, int roomId) {
        QiscusApi.getInstance()
                .getChatRoom(roomId)
                .compose(BaseScheduler.pluck().applySchedulers(BaseScheduler.Type.IO))
                .subscribe(chatRoom -> {
                    Intent parentIntent = new Intent(context, ChatActivity.class);
                    parentIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
                    stackBuilder.addNextIntent(parentIntent);
                    stackBuilder.addNextIntent(ChatActivity.generateIntent(context, chatRoom));
                    stackBuilder.startActivities();
                }, throwable -> {
                    throwable.printStackTrace();
                    Toast.makeText(context, throwable.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    protected int getSmallIconId(Context context, Intent intent) {
        return R.drawable.circle_primary;
    }

    @Override
    protected Bitmap getLargeIcon(Context context, Intent intent) {
        return BitmapFactory.decodeResource(context.getResources(), R.drawable.circle_primary);
    }

    /**
     * Generate specific notification for every action
     *
     * @param context if we need context to start activity for example
     * @param intent  contains data
     * @return the notification
     */
    @Override
    protected Notification getNotification(Context context, Intent intent) {
        Bundle extras = intent.getExtras();

        Random random = new Random();
        int contentIntentRequestCode = random.nextInt();
        int deleteIntentRequestCode = random.nextInt();

        String packageName = context.getPackageName();

        Intent openIntent = new Intent(ParsePushBroadcastReceiver.ACTION_PUSH_OPEN);
        openIntent.putExtras(extras);
        openIntent.setPackage(packageName);

        Intent deleteIntent = new Intent(ParsePushBroadcastReceiver.ACTION_PUSH_DELETE);
        deleteIntent.putExtras(extras);
        deleteIntent.setPackage(packageName);

        PendingIntent pOpenIntent = PendingIntent.getBroadcast(context, contentIntentRequestCode,
                                                               openIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent pDeleteIntent = PendingIntent.getBroadcast(context, deleteIntentRequestCode,
                                                                 deleteIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        String data = intent.getStringExtra(KEY_PUSH_DATA);
        if (data != null) {
            JsonObject dataJson = Qson.pluck().getParser().fromJson(data, JsonObject.class);
            String action = dataJson.get("action").getAsString();
            switch (action) {
                case "com.qiscus.COMMENT_RECEIVED":
                    JsonObject messageJson = dataJson.get("extra").getAsJsonObject();
                    QiscusComment qiscusComment = PusherApi.jsonToComment(messageJson);
                    if (!qiscusComment.getSenderEmail().equals(Qiscus.getQiscusAccount().getEmail())) {
                        return generateCommentNotification(context, intent, pOpenIntent, pDeleteIntent, qiscusComment);
                    }
                    break;
            }
        }

        return super.getNotification(context, intent);
    }

    /**
     * What to do when push notification clicked
     *
     * @param context if we need context to start activity for example
     * @param intent  contains data
     */
    @Override
    protected void onPushOpen(final Context context, Intent intent) {
        // Send a Parse Analytics "push opened" event
        ParseAnalytics.trackAppOpenedInBackground(intent);

        String data = intent.getStringExtra(KEY_PUSH_DATA);
        if (data != null) {
            JsonObject dataJson = Qson.pluck().getParser().fromJson(data, JsonObject.class);
            String action = dataJson.get("action").getAsString();
            try {
                switch (action) {
                    case "com.qiscus.COMMENT_RECEIVED":
                        JsonObject messageJson = dataJson.get("extra").getAsJsonObject();
                        int roomId = messageJson.get("room_id").getAsInt();
                        CacheManager.getInstance().clearMessageNotifItems(roomId);
                        openChatRoom(context, roomId);
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public Notification generateCommentNotification(Context context, Intent intent, PendingIntent openIntent,
                                                    PendingIntent deleteIntent, QiscusComment qiscusComment) {

        EventBus.getDefault().post(new CommentReceivedEvent(qiscusComment));
        CacheManager.getInstance().addMessageNotifItem(qiscusComment.getMessage(), qiscusComment.getRoomId());

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context);
        notificationBuilder.setContentTitle(qiscusComment.getSender())
                .setContentText(isAttachment(qiscusComment.getMessage()) ? fileMessage : qiscusComment.getMessage())
                .setTicker(isAttachment(qiscusComment.getMessage()) ? fileMessage : qiscusComment.getMessage())
                .setSmallIcon(getSmallIconId(context, intent))
                .setLargeIcon(getLargeIcon(context, intent))
                .setContentIntent(openIntent)
                .setDeleteIntent(deleteIntent)
                .setGroupSummary(true)
                .setGroup("CHAT_NOTIF_" + qiscusComment.getRoomId())
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL);

        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        List<String> notifItems = CacheManager.getInstance().getMessageNotifItems(qiscusComment.getRoomId());
        for (String message : notifItems) {
            if (isAttachment(message)) {
                inboxStyle.addLine(fileMessage);
            } else {
                inboxStyle.addLine(message);
            }
        }
        inboxStyle.setSummaryText(notifItems.size() + " new message");
        notificationBuilder.setStyle(inboxStyle);

        return notificationBuilder.build();
    }

    private void showPushNotification(Context context, Intent intent, int notificationId) {
        NotificationManagerCompat.from(context).notify(notificationId, getNotification(context, intent));
    }

    public boolean isAttachment(String message) {
        return message.startsWith("[file]") && message.endsWith("[/file]");
    }
}
