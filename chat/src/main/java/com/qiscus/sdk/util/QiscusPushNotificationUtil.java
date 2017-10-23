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
import android.text.TextUtils;

import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.qiscus.nirmana.Nirmana;
import com.qiscus.sdk.Qiscus;
import com.qiscus.sdk.R;
import com.qiscus.sdk.data.local.QiscusCacheManager;
import com.qiscus.sdk.data.model.QiscusChatRoom;
import com.qiscus.sdk.data.model.QiscusComment;
import com.qiscus.sdk.data.model.QiscusPushNotificationMessage;
import com.qiscus.sdk.data.model.QiscusRoomMember;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created on : June 15, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
public final class QiscusPushNotificationUtil {
    public static final String KEY_NOTIFICATION_REPLY = "KEY_NOTIFICATION_REPLY";

    public static void handlePushNotification(Context context, QiscusComment qiscusComment) {
        QiscusAndroidUtil.runOnBackgroundThread(() -> handlePN(context, qiscusComment));
    }

    private static void handlePN(Context context, QiscusComment qiscusComment) {
        if (Qiscus.getChatConfig().isEnablePushNotification()
                && !qiscusComment.getSenderEmail().equalsIgnoreCase(Qiscus.getQiscusAccount().getEmail())) {
            if (Qiscus.getChatConfig().isOnlyEnablePushNotificationOutsideChatRoom()) {
                Pair<Boolean, Integer> lastChatActivity = QiscusCacheManager.getInstance().getLastChatActivity();
                if (!lastChatActivity.first || !lastChatActivity.second.equals(qiscusComment.getRoomId()) ) {
                    showPushNotification(context, qiscusComment);
                }
            } else {
                showPushNotification(context, qiscusComment);
            }
        }
    }

    private static void showPushNotification(Context context, QiscusComment comment) {
        QiscusChatRoom room = Qiscus.getDataStore().getChatRoom(comment.getRoomId());
        Map<String, QiscusRoomMember> members = new HashMap<>();
        if (room != null) {
            for (QiscusRoomMember member : room.getMember()) {
                members.put(member.getEmail(), member);
            }
        }

        String messageText = comment.isGroupMessage() ? comment.getSender().split(" ")[0] + ": " : "";
        if (comment.getType() == QiscusComment.Type.SYSTEM_EVENT) {
            messageText = "";
        }
        switch (comment.getType()) {
            case IMAGE:
                messageText += "\uD83D\uDCF7 " + (TextUtils.isEmpty(comment.getCaption()) ?
                        QiscusTextUtil.getString(R.string.qiscus_send_a_photo) :
                        new QiscusSpannableBuilder(comment.getCaption(), members).build().toString());
                break;
            case VIDEO:
                messageText += "\uD83C\uDFA5 " + (TextUtils.isEmpty(comment.getCaption()) ?
                        QiscusTextUtil.getString(R.string.qiscus_send_a_video) :
                        new QiscusSpannableBuilder(comment.getCaption(), members).build().toString());
                break;
            case AUDIO:
                messageText += "\uD83D\uDD0A " + QiscusTextUtil.getString(R.string.qiscus_send_a_audio);
                break;
            case CONTACT:
                messageText += "\u260E " + QiscusTextUtil.getString(R.string.qiscus_contact) + ": " +
                        comment.getContact().getName();
                break;
            case LOCATION:
                messageText += "\uD83D\uDCCD " + comment.getMessage();
                break;
            default:
                messageText += comment.isAttachment() ? "\uD83D\uDCC4 " +
                        QiscusTextUtil.getString(R.string.qiscus_send_attachment) :
                        new QiscusSpannableBuilder(comment.getMessage(), members).build().toString();
                break;
        }

        if (!QiscusCacheManager.getInstance()
                .addMessageNotifItem(new QiscusPushNotificationMessage(comment.getId(), messageText), comment.getRoomId())) {
            return;
        }

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
                        QiscusAndroidUtil.runOnBackgroundThread(() -> {
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
        pendingIntent = PendingIntent.getBroadcast(context, Integer.parseInt(comment.getRoomId()), openIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context);
        notificationBuilder.setContentTitle(Qiscus.getChatConfig().getNotificationTitleHandler().getTitle(comment))
                .setContentIntent(pendingIntent)
                .setContentText(messageText)
                .setTicker(messageText)
                .setSmallIcon(Qiscus.getChatConfig().getNotificationSmallIcon())
                .setLargeIcon(largeIcon)
                .setColor(ContextCompat.getColor(context, Qiscus.getChatConfig().getInlineReplyColor()))
                .setGroupSummary(true)
                .setGroup("CHAT_NOTIF_" + comment.getRoomId())
                .setAutoCancel(true)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));

        if (Qiscus.getChatConfig().isEnableReplyNotification() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            String getRepliedTo = comment.getRoomName();
            RemoteInput remoteInput = new RemoteInput.Builder(KEY_NOTIFICATION_REPLY)
                    .setLabel(QiscusTextUtil.getString(R.string.qiscus_reply_to, getRepliedTo.toUpperCase()))
                    .build();

            NotificationCompat.Action replyAction = new NotificationCompat.Action.Builder(android.R.drawable.ic_menu_send,
                    QiscusTextUtil.getString(R.string.qiscus_reply_to, getRepliedTo.toUpperCase()), pendingIntent)
                    .addRemoteInput(remoteInput)
                    .build();
            notificationBuilder.addAction(replyAction);
        }

        boolean cancel = false;
        if (Qiscus.getChatConfig().getNotificationBuilderInterceptor() != null) {
            cancel = !Qiscus.getChatConfig().getNotificationBuilderInterceptor()
                    .intercept(notificationBuilder, comment);
        }

        if (cancel) {
            return;
        }

        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        List<QiscusPushNotificationMessage> notifItems = QiscusCacheManager.getInstance()
                .getMessageNotifItems(comment.getRoomId());
        if (notifItems == null) {
            notifItems = new ArrayList<>();
        }
        int notifSize = 5;
        if (notifItems.size() < notifSize) {
            notifSize = notifItems.size();
        }
        if (notifItems.size() > notifSize) {
            inboxStyle.addLine(".......");
        }
        int start = notifItems.size() - notifSize;
        for (int i = start; i < notifItems.size(); i++) {
            inboxStyle.addLine(notifItems.get(i).getMessage());
        }
        inboxStyle.setSummaryText(QiscusTextUtil.getString(R.string.qiscus_notif_count, notifItems.size()));
        notificationBuilder.setStyle(inboxStyle);

        if (notifSize <= 3) {
            notificationBuilder.setPriority(Notification.PRIORITY_HIGH);
        }

        QiscusAndroidUtil.runOnUIThread(() -> NotificationManagerCompat.from(context)
                .notify(Integer.parseInt(comment.getRoomId()), notificationBuilder.build()));
    }
}
