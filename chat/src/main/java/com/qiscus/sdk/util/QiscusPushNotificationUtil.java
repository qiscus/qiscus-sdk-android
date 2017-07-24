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

package com.qiscus.sdk.util;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.RingtoneManager;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.RemoteInput;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;

import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.qiscus.nirmana.Nirmana;
import com.qiscus.sdk.Qiscus;
import com.qiscus.sdk.R;
import com.qiscus.sdk.data.local.QiscusCacheManager;
import com.qiscus.sdk.data.model.QiscusChatRoom;
import com.qiscus.sdk.data.model.QiscusComment;
import com.qiscus.sdk.data.model.QiscusPushNotificationMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * Created on : June 15, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
public final class QiscusPushNotificationUtil {
    public static final String KEY_NOTIFICATION_REPLY = "KEY_NOTIFICATION_REPLY";
    private static SpannableStringBuilder fileMessage;
    private static final String GROUP_KEY_BUNDLED = "GROUP_KEY_BUNDLED";
    public static final int NOTIFICATION_BUNDLED_BASE_ID = 0;
    private static NotificationCompat.InboxStyle inboxStyle;
    private static boolean headsUp = false;

    static {
        fileMessage = new SpannableStringBuilder(QiscusAndroidUtil.getString(R.string.qiscus_send_attachment));
        fileMessage.setSpan(new StyleSpan(android.graphics.Typeface.BOLD),
                0, fileMessage.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    public static void handlePushNotification(Context context, QiscusComment qiscusComment) {
        QiscusAndroidUtil.runOnBackgroundThread(() -> handlePN(context, qiscusComment));
    }

    private static void handlePN(Context context, QiscusComment qiscusComment) {
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
        if (Qiscus.getChatConfig().isEnablePushNotification()
                && !qiscusComment.getSenderEmail().equalsIgnoreCase(Qiscus.getQiscusAccount().getEmail())) {
            if (Qiscus.getChatConfig().isOnlyEnablePushNotificationOutsideChatRoom()) {
                Pair<Boolean, Integer> lastChatActivity = QiscusCacheManager.getInstance().getLastChatActivity();
                if (!lastChatActivity.first || lastChatActivity.second != qiscusComment.getRoomId()) {
                    showPushNotification(context, qiscusComment);
                }
            } else {
                showPushNotification(context, qiscusComment);
            }
        }
    }

    private static void showPushNotification(Context context, QiscusComment comment) {
        String messageText = comment.isGroupMessage() ? comment.getSender().split(" ")[0] + ": " : "";
        messageText += isAttachment(comment.getMessage()) ? fileMessage : comment.getMessage();

        if (!QiscusCacheManager.getInstance()
                .addMessageNotifItem(new QiscusPushNotificationMessage(comment.getId(), messageText), comment.getRoomId())) {
            return;
        }

        headsUp = QiscusCacheManager.getInstance().addRoomNotifItem(comment.getRoomId());
        String finalMessageText = messageText;
        if (Qiscus.getChatConfig().isEnableAvatarAsNotificationIcon()) {
            QiscusAndroidUtil.runOnUIThread(() -> loadAvatar(context, comment, finalMessageText));
        } else {
            pushNotification(context, comment, finalMessageText,
                    BitmapFactory.decodeResource(context.getResources(), Qiscus.getChatConfig().getNotificationBigIcon()));
        }
    }

    private static void loadAvatar(Context context, QiscusComment comment, String finalMessageText) {
        Nirmana.getInstance().get()
                .load(comment.getRoomAvatar())
                .asBitmap()
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                        QiscusAndroidUtil.runOnBackgroundThread(() ->  {
                            try {
                                pushNotification(context, comment, finalMessageText, QiscusImageUtil.getCircularBitmap(resource));
                            } catch (Exception e) {
                                pushNotification(context, comment, finalMessageText,
                                        BitmapFactory.decodeResource(context.getResources(),
                                                Qiscus.getChatConfig().getNotificationBigIcon()));
                            }
                        });
                    }

                    @Override
                    public void onLoadFailed(Exception e, Drawable errorDrawable) {
                        super.onLoadFailed(e, errorDrawable);
                        QiscusAndroidUtil.runOnBackgroundThread(() -> pushNotification(context, comment, finalMessageText,
                                BitmapFactory.decodeResource(context.getResources(),
                                        Qiscus.getChatConfig().getNotificationBigIcon())));
                    }
                });
    }

    private static void pushNotification(Context context, QiscusComment comment, String messageText, Bitmap largeIcon) {

        PendingIntent pendingIntent;
        Intent openIntent = new Intent("com.qiscus.OPEN_COMMENT_PN");
        openIntent.putExtra("data", comment);
        pendingIntent = PendingIntent.getBroadcast(context, comment.getRoomId(), openIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            NotificationCompat.Builder summaryBuilder = new NotificationCompat.Builder(context)
                    .setSmallIcon(Qiscus.getChatConfig().getNotificationSmallIcon())
                    .setContentTitle(Qiscus.getChatConfig().getNotificationTitleHandler().getTitle(comment))
                    .setContentIntent(pendingIntent)
                    .setContentText(messageText)
                    .setTicker(messageText)
                    .setLargeIcon(largeIcon)
                    .setAutoCancel(true)
                    .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                    .setColor(ContextCompat.getColor(context, Qiscus.getChatConfig().getInlineReplyColor()))
                    .setStyle(new NotificationCompat.BigTextStyle()
                            .bigText(messageText))
                    .setGroup(GROUP_KEY_BUNDLED)
                    .setGroupSummary(true)
                    .setSubText(QiscusAndroidUtil.getString(R.string.qiscus_subtext_summary,
                            QiscusCacheManager.getInstance()
                                    .getRoomNotifItems().size()));

            QiscusAndroidUtil.runOnUIThread(() -> NotificationManagerCompat.from(context)
                .notify(NOTIFICATION_BUNDLED_BASE_ID, summaryBuilder.build()));
            summaryBuilder.setStyle(inboxStyle);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setContentTitle(Qiscus.getChatConfig().getNotificationTitleHandler().getTitle(comment))
                .setContentIntent(pendingIntent)
                .setContentText(messageText)
                .setTicker(messageText)
                .setLargeIcon(largeIcon)
                .setAutoCancel(true)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setSmallIcon(Qiscus.getChatConfig().getNotificationSmallIcon())
                .setColor(ContextCompat.getColor(context, Qiscus.getChatConfig().getInlineReplyColor()))
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(messageText))
                .setGroup(GROUP_KEY_BUNDLED);

        String getRepliedTo = (comment.isGroupMessage()) ? comment.getRoomName() : comment.getSender();
        if (Qiscus.getChatConfig().isEnableReplyNotification() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {

            RemoteInput remoteInput = new RemoteInput.Builder(KEY_NOTIFICATION_REPLY)
                    .setLabel(QiscusAndroidUtil.getString(R.string.qiscus_reply_to, getRepliedTo.toUpperCase()))
                    .build();

            NotificationCompat.Action replyAction = new NotificationCompat.Action.Builder(android.R.drawable.ic_menu_send,
                    QiscusAndroidUtil.getString(R.string.qiscus_reply_to, getRepliedTo.toUpperCase()), pendingIntent)
                    .addRemoteInput(remoteInput)
                    .build();
            builder.addAction(replyAction);

            if (headsUp) {
                builder.setPriority(Notification.PRIORITY_HIGH);
            }
        }

        boolean cancel = false;
        if (Qiscus.getChatConfig().getNotificationBuilderInterceptor() != null) {
            cancel = !Qiscus.getChatConfig().getNotificationBuilderInterceptor()
                    .intercept(builder, comment);
        }

        if (cancel) {
            return;
        }

        inboxStyle = new NotificationCompat.InboxStyle();
        List<QiscusPushNotificationMessage> notifItems = QiscusCacheManager.getInstance()
                .getMessageNotifItems(comment.getRoomId());
        if (notifItems == null) {
            notifItems = new ArrayList<>();
        }
        int notifSize = 5;
        if (notifItems.size() < notifSize) {
            notifSize = notifItems.size();
        }
        int start = notifItems.size() - notifSize;
        for (int i = start; i < notifItems.size(); i++) {
            QiscusPushNotificationMessage message = notifItems.get(i);
            if (isAttachment(message.getMessage())) {
                inboxStyle.addLine(fileMessage);
            } else {
                inboxStyle.addLine(message.getMessage());
            }
        }
        if (notifItems.size() > notifSize) {
            inboxStyle.addLine(".......");
        }
        inboxStyle.setBigContentTitle(getRepliedTo + ": (" +
                QiscusAndroidUtil.getString(R.string.qiscus_notif_count, notifItems.size())
                + ") ");
        builder.setStyle(inboxStyle);


        QiscusAndroidUtil.runOnUIThread(() -> NotificationManagerCompat.from(context)
                .notify(comment.getRoomId(), builder.build()));
    }

    private static boolean isAttachment(String message) {
        return message.startsWith("[file]") && message.endsWith("[/file]");
    }

}
