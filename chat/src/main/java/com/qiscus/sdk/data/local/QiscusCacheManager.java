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
import android.support.v4.util.Pair;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.qiscus.sdk.Qiscus;
import com.qiscus.sdk.data.model.QiscusPushNotificationMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * Created on : May 25, 2016
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
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

    public boolean addMessageNotifItem(QiscusPushNotificationMessage message, int roomId) {
        List<QiscusPushNotificationMessage> notifItems = getMessageNotifItems(roomId);
        if (notifItems == null) {
            notifItems = new ArrayList<>();
        }

        if (!notifItems.contains(message)) {
            notifItems.add(message);
            sharedPreferences.edit()
                    .putString("push_notif_message_" + roomId, gson.toJson(notifItems))
                    .apply();
            return true;
        }
        return false;
    }

    public List<QiscusPushNotificationMessage> getMessageNotifItems(int roomId) {
        String json = sharedPreferences.getString("push_notif_message_" + roomId, "");
        return gson.fromJson(json, new TypeToken<List<QiscusPushNotificationMessage>>() {
        }.getType());
    }

    public boolean addRoomNotifItem(int roomId) {
        List<Integer> roomNotif = getRoomNotifItems();
        if (roomNotif == null) {
            roomNotif = new ArrayList<>();
        }

        if (!roomNotif.contains(roomId)) {
            roomNotif.add(roomId);
            sharedPreferences.edit()
                    .putString("push_notif_message_room_id", gson.toJson(roomNotif))
                    .apply();
            return true;
        }
        return false;
    }

    public List<Integer> getRoomNotifItems() {
        String json = sharedPreferences.getString("push_notif_message_room_id", "");
        return gson.fromJson(json, new TypeToken<List<Integer>>() {
        }.getType());
    }

    public void removeRoomNotif(int roomId) {
        List<Integer> json =  getRoomNotifItems();
        if (getRoomNotifItems() != null) {
            for (int i = 0; i < json.size(); i++) {
//                if (String.valueOf(json.get(0)).equals(String.valueOf(roomId))) {
                if (json.get(0) == roomId) {
                    json.remove(i);
                }
            }
        }
        sharedPreferences.edit()
                .putString("push_notif_message_room_id", gson.toJson(json))
                .apply();
    }

    public void clearMessageNotifItems(int roomId) {
        sharedPreferences.edit()
                .putString("push_notif_message_" + roomId, "")
                .apply();
    }

    public String getLastImagePath() {
        return sharedPreferences.getString("last_image_path", "");
    }

    public void setLastChatActivity(boolean active, int roomId) {
        sharedPreferences.edit()
                .putBoolean("last_chat_status", active)
                .putInt("last_active_chat", roomId)
                .apply();
    }

    public Pair<Boolean, Integer> getLastChatActivity() {
        return Pair.create(sharedPreferences.getBoolean("last_chat_status", false),
                sharedPreferences.getInt("last_active_chat", 0));
    }

    public void clearData() {
        sharedPreferences.edit().clear().apply();
    }
}
