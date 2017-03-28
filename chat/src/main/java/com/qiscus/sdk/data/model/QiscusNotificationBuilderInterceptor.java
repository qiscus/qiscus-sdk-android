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

package com.qiscus.sdk.data.model;

import android.support.v4.app.NotificationCompat;

/**
 * Created on : March 24, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
public interface QiscusNotificationBuilderInterceptor {
    /**
     * Intercept notification builder, to customizing notification
     *
     * @param notificationBuilder The builder
     * @param qiscusComment       comment to show
     * @return true to continue showing push notification, false to cancel push notification
     */
    boolean intercept(NotificationCompat.Builder notificationBuilder, QiscusComment qiscusComment);
}
