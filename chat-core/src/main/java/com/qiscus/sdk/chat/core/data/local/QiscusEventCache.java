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

package com.qiscus.sdk.chat.core.data.local;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.RestrictTo;

import com.qiscus.sdk.chat.core.QiscusCore;

/**
 * Created on : February 14, 2018
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public enum QiscusEventCache {
    INSTANCE;
    private final SharedPreferences sharedPreferences;

    QiscusEventCache() {
        sharedPreferences = QiscusCore.getApps().getSharedPreferences("events.cache", Context.MODE_PRIVATE);
    }

    public static QiscusEventCache getInstance() {
        return INSTANCE;
    }

    public long getLastEventId() {
        return sharedPreferences.getLong("last_event_id", 0);
    }

    public void saveLastEventId(long eventId) {
        if (eventId > getLastEventId()) {
            sharedPreferences.edit()
                    .putLong("last_event_id", eventId)
                    .apply();
        }
    }
}
