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

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.qiscus.sdk.chat.core.QiscusCore;
import com.qiscus.sdk.chat.core.util.QiscusFirebaseMessagingUtil;

public class QiscusFirebaseService extends FirebaseMessagingService {

    /**
     * Handle remote message from FCM to display push notification
     *
     * @param remoteMessage The message from firebase
     * @return true if the message is for Qiscus SDK, false if the message is not for Qiscus SDK
     */
    public static boolean handleMessageReceived(RemoteMessage remoteMessage) {
        return QiscusFirebaseMessagingUtil.handleMessageReceived(remoteMessage);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        if (handleMessageReceived(remoteMessage)) {
            return;
        }
    }

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);

        QiscusCore.registerDeviceToken(s);
    }

    public static void getCurrentDeviceToken() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.e("Qiscus", "getCurrentDeviceToken Failed : " +
                                    task.getException());
                            return;
                        }

                        if (task.getResult() != null) {
                            String currentToken = task.getResult();

                            Log.e("currentToken", "getCurrentDeviceToken : " +
                                    currentToken);
                            QiscusCore.registerDeviceToken(currentToken);
                        }
                    }
                });
    }
}