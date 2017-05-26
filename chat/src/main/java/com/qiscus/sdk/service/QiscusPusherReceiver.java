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
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.RemoteInput;
import android.util.Log;

import com.qiscus.sdk.Qiscus;
import com.qiscus.sdk.data.model.QiscusComment;

import static com.qiscus.sdk.service.QiscusPusherService.KEY_NOTIFICATION_REPLY;

/**
 * Created by zetra. on 9/8/16.
 */
public class QiscusPusherReceiver extends BroadcastReceiver{

    @Override
    public void onReceive(Context context, Intent intent) {
        QiscusComment comment = intent.getParcelableExtra("data");
        Bundle remoteInput = RemoteInput.getResultsFromIntent(intent);
        if (remoteInput != null) {
            CharSequence message = remoteInput.getCharSequence(KEY_NOTIFICATION_REPLY);
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(
                    Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(comment.getRoomId());
            QiscusComment qiscusComment = QiscusComment.generateMessage((String) message, comment.getRoomId(), comment.getTopicId());
            Qiscus.getChatConfig().sendReplyNotificationHandler().onSend(context, qiscusComment);
        } else {
            Qiscus.getChatConfig().getNotificationClickListener().onClick(context, comment);
        }
    }

}
