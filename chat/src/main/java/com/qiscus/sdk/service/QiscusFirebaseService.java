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

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.qiscus.sdk.data.remote.QiscusPusherApi;

public class QiscusFirebaseService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        if (handleMessageReceived(remoteMessage)) {
            return;
        }
    }

    /**
     * Handle remote message from FCM to display push notification
     *
     * @param remoteMessage The message from firebase
     * @return true if the message is for Qiscus SDK, false if the message is not for Qiscus SDK
     */
    public static boolean handleMessageReceived(RemoteMessage remoteMessage) {
        if (remoteMessage.getData().containsKey("qiscus_sdk")) {
            if (!QiscusPusherApi.getInstance().isConnected()) {
                QiscusPusherApi.getInstance().restartConnection();
            }
            return true;
        }
        return false;
    }
}