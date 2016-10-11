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

package com.qiscus.sdk.data.local;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.qiscus.sdk.Qiscus;

import java.util.ArrayList;
import java.util.List;

/**
 * Created on : May 25, 2016
 * Author     : zetbaitsu
 * Name       : Zetra
 * Email      : zetra@mail.ugm.ac.id
 * GitHub     : https://github.com/zetbaitsu
 * LinkedIn   : https://id.linkedin.com/in/zetbaitsu
 */

/**
 * Cache response from API here, so we can use it to speed up load data to the UI component
 */
public enum QiscusCacheManager {
    INSTANCE;
    private final SharedPreferences sharedPreferences;
    private final Gson gson;

    QiscusCacheManager() {
        sharedPreferences = Qiscus.getApps().getSharedPreferences("qiscus.cache", Context.MODE_PRIVATE);
        gson = new Gson();
    }

    public static QiscusCacheManager getInstance() {
        return INSTANCE;
    }

    public void cacheLastImagePath(String path) {
        sharedPreferences.edit()
                .putString("last_image_path", path)
                .apply();
    }

    public void addMessageNotifItem(String message, int roomId) {
        List<String> notifItems = getMessageNotifItems(roomId);
        if (notifItems == null) {
            notifItems = new ArrayList<>();
        }
        notifItems.add(message);
        sharedPreferences.edit()
                .putString("notif_message_" + roomId, gson.toJson(notifItems))
                .apply();
    }

    public List<String> getMessageNotifItems(int roomId) {
        String json = sharedPreferences.getString("notif_message_" + roomId, "");
        return gson.fromJson(json, new TypeToken<List<String>>() {}.getType());
    }

    public void clearMessageNotifItems(int roomId) {
        sharedPreferences.edit()
                .putString("notif_message_" + roomId, "")
                .apply();
    }

    public String getLastImagePath() {
        return sharedPreferences.getString("last_image_path", "");
    }

    public void clearData() {
        sharedPreferences.edit().clear().apply();
    }
}
