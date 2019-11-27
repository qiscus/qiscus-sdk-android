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

package com.qiscus.sdk.chat.core.data.remote;

import android.provider.Settings;

import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.qiscus.sdk.chat.core.QiscusCore;
import com.qiscus.sdk.chat.core.data.local.QiscusEventCache;
import com.qiscus.sdk.chat.core.data.model.QAccount;
import com.qiscus.sdk.chat.core.data.model.QChatRoom;
import com.qiscus.sdk.chat.core.data.model.QParticipant;
import com.qiscus.sdk.chat.core.data.model.QMessage;
import com.qiscus.sdk.chat.core.data.model.QUser;
import com.qiscus.sdk.chat.core.event.QiscusChatRoomEvent;
import com.qiscus.sdk.chat.core.event.QMessageReceivedEvent;
import com.qiscus.sdk.chat.core.event.QiscusMqttStatusEvent;
import com.qiscus.sdk.chat.core.event.QiscusUserEvent;
import com.qiscus.sdk.chat.core.event.QiscusUserStatusEvent;
import com.qiscus.sdk.chat.core.util.QiscusAndroidUtil;
import com.qiscus.sdk.chat.core.util.QiscusErrorLogger;
import com.qiscus.sdk.chat.core.util.QiscusLogger;
import com.qiscus.sdk.chat.core.util.QiscusTextUtil;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public enum QiscusPusherApi implements MqttCallbackExtended, IMqttActionListener {

    INSTANCE;
    private static final String TAG = QiscusPusherApi.class.getSimpleName();
    private static final long RETRY_PERIOD = 4000;
    public static final long CONNECTED_SYNC_INTERVAL = 30000;
    public static final long DISCONNECTED_SYNC_INTERVAL = 5000;

    private static Gson gson;
    private static long reconnectCounter;

    static {
        gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").create();
    }

    private String clientId;
    private MqttAndroidClient mqttAndroidClient;
    private QAccount qAccount;
    private Runnable fallBackListenRoom;
    private Runnable fallBackListenUserStatus;
    private Runnable fallbackListenEvent;
    private ScheduledFuture<?> scheduledConnect;
    private ScheduledFuture<?> scheduledListenComment;
    private ScheduledFuture<?> scheduledListenNotification;
    private ScheduledFuture<?> scheduledListenRoom;
    private ScheduledFuture<?> scheduledListenUserStatus;
    private ScheduledFuture<?> scheduledListenEvent;
    private boolean connecting;
    private Runnable fallbackConnect = this::connect;
    private Runnable fallBackListenComment = this::listenComment;
    private Runnable fallBackListenNotification = this::listenNotification;
    private ScheduledFuture<?> scheduledUserStatus;

    private int setOfflineCounter;

    QiscusPusherApi() {
        QiscusLogger.print("QiscusPusherApi", "Creating...");
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

        clientId = QiscusCore.getApps().getPackageName() + "-";
        clientId += Settings.Secure.getString(QiscusCore.getApps().getContentResolver(), Settings.Secure.ANDROID_ID);

        buildClient();

        connecting = false;
    }

    public static QiscusPusherApi getInstance() {
        return INSTANCE;
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public static void handleReceivedComment(QMessage qiscusMessage) {
        QiscusAndroidUtil.runOnBackgroundThread(() -> handleComment(qiscusMessage));
    }

    private static void handleComment(QMessage qiscusMessage) {
        QMessage savedComment = QiscusCore.getDataStore().getComment(qiscusMessage.getUniqueId());

        if (savedComment != null && (savedComment.isDeleted() || savedComment.areContentsTheSame(qiscusMessage))) {
            return;
        }

        if (!qiscusMessage.isMyComment()) {
            QiscusPusherApi.getInstance().markAsDelivered(qiscusMessage.getChatRoomId(), qiscusMessage.getId());
        }

        if (QiscusCore.getChatConfig().getNotificationListener() != null) {
            QiscusCore.getChatConfig().getNotificationListener()
                    .onHandlePushNotification(QiscusCore.getApps(), qiscusMessage);
        }

        QiscusAndroidUtil.runOnUIThread(() -> EventBus.getDefault().post(new QMessageReceivedEvent(qiscusMessage)));
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public static void handleNotification(JSONObject jsonObject) {
        long eventId = jsonObject.optLong("id");

        QiscusEventCache.getInstance().setLastEventId(eventId);

        if (jsonObject.optString("action_topic").equals("delete_message")) {
            JSONObject payload = jsonObject.optJSONObject("payload");

            JSONObject actorJson = payload.optJSONObject("actor");
            QParticipant actor = new QParticipant();
            actor.setId(actorJson.optString("email"));
            actor.setName(actorJson.optString("name"));

            List<QiscusDeleteCommentHandler.DeletedCommentsData.DeletedComment> deletedComments = new ArrayList<>();
            JSONObject dataJson = payload.optJSONObject("data");
            JSONArray deletedCommentsJson = dataJson.optJSONArray("deleted_messages");
            int deletedCommentsJsonSize = deletedCommentsJson.length();
            for (int i = 0; i < deletedCommentsJsonSize; i++) {
                JSONObject deletedCommentJson = deletedCommentsJson.optJSONObject(i);
                long roomId = Long.valueOf(deletedCommentJson.optString("room_id", "0"));

                JSONArray commentUniqueIds = deletedCommentJson.optJSONArray("message_unique_ids");
                int commentUniqueIdsSize = commentUniqueIds.length();
                for (int j = 0; j < commentUniqueIdsSize; j++) {
                    deletedComments.add(new QiscusDeleteCommentHandler.DeletedCommentsData
                            .DeletedComment(roomId, commentUniqueIds.optString(j)));
                }
            }

            QiscusDeleteCommentHandler.DeletedCommentsData deletedCommentsData
                    = new QiscusDeleteCommentHandler.DeletedCommentsData();
            deletedCommentsData.setActor(actor);
            deletedCommentsData.setHardDelete(dataJson.optBoolean("is_hard_delete"));
            deletedCommentsData.setDeletedComments(deletedComments);

            QiscusDeleteCommentHandler.handle(deletedCommentsData);
        } else if (jsonObject.optString("action_topic").equals("clear_room")) {
            JSONObject payload = jsonObject.optJSONObject("payload");

            JSONObject actorJson = payload.optJSONObject("actor");
            QParticipant actor = new QParticipant();
            actor.setId(actorJson.optString("email"));
            actor.setName(actorJson.optString("name"));

            List<Long> roomIds = new ArrayList<>();
            JSONObject dataJson = payload.optJSONObject("data");
            JSONArray clearedRoomsJson = dataJson.optJSONArray("deleted_rooms");
            int clearedRoomsJsonSize = clearedRoomsJson.length();
            for (int i = 0; i < clearedRoomsJsonSize; i++) {
                JSONObject clearedRoomJson = clearedRoomsJson.optJSONObject(i);
                roomIds.add(clearedRoomJson.optLong("id"));
            }

            QiscusClearMessagesHandler.ClearMessagesData clearMessagesData
                    = new QiscusClearMessagesHandler.ClearMessagesData();
            //timestamp is in nano seconds format, convert it to milliseconds by divide it
            clearMessagesData.setTimestamp(jsonObject.optLong("timestamp") / 1000000L);
            clearMessagesData.setActor(actor);
            clearMessagesData.setRoomIds(roomIds);

            QiscusClearMessagesHandler.handle(clearMessagesData);
        } else if (jsonObject.optString("action_topic").equals("delivered")) {
            JSONObject payload = jsonObject.optJSONObject("payload");
            JSONObject dataJson = payload.optJSONObject("data");

            Long commentId = dataJson.optLong("comment_id");
            String commentUniqueID = dataJson.optString("comment_unique_id");
            Long roomId = dataJson.optLong("room_id");
            String sender = dataJson.optString("email");

            QMessage savedComment = QiscusCore.getDataStore().getComment(commentUniqueID);
            QAccount qAccount = QiscusCore.getQiscusAccount();

            if (savedComment != null && savedComment.getState() != QMessage.STATE_READ &&
                    !sender.equals(qAccount.getId())) {

                QiscusChatRoomEvent event = new QiscusChatRoomEvent()
                        .setRoomId(roomId)
                        .setUser(sender)
                        .setEvent(QiscusChatRoomEvent.Event.DELIVERED)
                        .setCommentId(commentId)
                        .setCommentUniqueId(commentUniqueID);
                EventBus.getDefault().post(event);
            }
        } else if (jsonObject.optString("action_topic").equals("read")) {
            JSONObject payload = jsonObject.optJSONObject("payload");
            JSONObject dataJson = payload.optJSONObject("data");

            Long commentId = dataJson.optLong("comment_id");
            String commentUniqueID = dataJson.optString("comment_unique_id");
            Long roomId = dataJson.optLong("room_id");
            String sender = dataJson.optString("email");

            QAccount qAccount = QiscusCore.getQiscusAccount();
            if (!sender.equals(qAccount.getId())) {
                QiscusChatRoomEvent event = new QiscusChatRoomEvent()
                        .setRoomId(roomId)
                        .setUser(sender)
                        .setEvent(QiscusChatRoomEvent.Event.READ)
                        .setCommentId(commentId)
                        .setCommentUniqueId(commentUniqueID);
                EventBus.getDefault().post(event);
            }
        }
    }

    @Nullable
    public static QMessage jsonToComment(JsonObject jsonObject) {
        try {
            QMessage qiscusMessage = new QMessage();
            qiscusMessage.setId(jsonObject.get("id").getAsLong());
            qiscusMessage.setChatRoomId(jsonObject.get("room_id").getAsLong());
            qiscusMessage.setUniqueId(jsonObject.get("unique_temp_id").getAsString());
            qiscusMessage.setPreviousMessageId(jsonObject.get("comment_before_id").getAsLong());
            qiscusMessage.setMessage(jsonObject.get("message").getAsString());

            QUser qUser = new QUser();
            qUser.setAvatarUrl(jsonObject.get("user_avatar").getAsString());
            qUser.setName(jsonObject.get("username").isJsonNull() ? null : jsonObject.get("username").getAsString());
            qUser.setId(jsonObject.get("email").getAsString());
            if (jsonObject.has("user_extras") && !jsonObject.get("user_extras").isJsonNull()) {
                try {
                    qUser.setExtras(new JSONObject(jsonObject.get("user_extras").getAsJsonObject().toString()));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            qiscusMessage.setSender(qUser);

            qiscusMessage.setSenderEmail(jsonObject.get("email").getAsString());
            qiscusMessage.setSenderAvatar(jsonObject.get("user_avatar").getAsString());

            //timestamp is in nano seconds format, convert it to milliseconds by divide it
            long timestamp = jsonObject.get("unix_nano_timestamp").getAsLong() / 1000000L;
            qiscusMessage.setTimestamp(new Date(timestamp));
            qiscusMessage.setState(QMessage.STATE_ON_QISCUS);

            if (jsonObject.has("is_deleted")) {
                qiscusMessage.setDeleted(jsonObject.get("is_deleted").getAsBoolean());
            }

            qiscusMessage.setRoomName(jsonObject.get("room_name").isJsonNull() ?
                    qiscusMessage.getSender().getName() : jsonObject.get("room_name").getAsString());
            if (jsonObject.has("room_avatar")) {
                qiscusMessage.setRoomAvatar(jsonObject.get("room_avatar").getAsString());
            }

            qiscusMessage.setGroupMessage(!"single".equals(jsonObject.get("chat_type").getAsString()));
            if (!qiscusMessage.isGroupMessage()) {
                qiscusMessage.setRoomName(qiscusMessage.getSender().getName());
            }
            if (jsonObject.has("type")) {
                qiscusMessage.setRawType(jsonObject.get("type").getAsString());
                if (jsonObject.has("payload") && !jsonObject.get("payload").isJsonNull()) {
                    qiscusMessage.setPayload(new JSONObject(jsonObject.get("payload").getAsJsonObject().toString()));
                }

                if (qiscusMessage.getType() == QMessage.Type.BUTTONS
                        || qiscusMessage.getType() == QMessage.Type.REPLY
                        || qiscusMessage.getType() == QMessage.Type.CARD) {
                    JsonObject payload = jsonObject.get("payload").getAsJsonObject();
                    if (payload.has("text")) {
                        String text = payload.get("text").getAsString();
                        if (QiscusTextUtil.isNotBlank(text)) {
                            qiscusMessage.setMessage(text.trim());
                        }
                    }
                }
            }

            if (jsonObject.has("extras") && !jsonObject.get("extras").isJsonNull()) {
                qiscusMessage.setExtras(new JSONObject(jsonObject.get("extras").getAsJsonObject().toString()));
            }

            return qiscusMessage;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Nullable
    public static QMessage jsonToComment(String json) {
        return jsonToComment(gson.fromJson(json, JsonObject.class));
    }

    private void buildClient() {
        Long epochTimeLong = System.currentTimeMillis() / 1000;
        String epochTime = epochTimeLong.toString();

        mqttAndroidClient = null;
        mqttAndroidClient = new MqttAndroidClient(QiscusCore.getApps().getApplicationContext(),
                QiscusCore.getMqttBrokerUrl(), clientId + epochTime, new MemoryPersistence());
        mqttAndroidClient.setCallback(this);
        mqttAndroidClient.setTraceEnabled(false);
    }

    /**
     * If isEnableMqttLB = true, MQTT broker url is from own MQTT_LB and save to shared pref
     */
    private void getMqttBrokerUrlFromLB() {
        QiscusLogger.print("isEnableMqttLB : " + QiscusCore.isEnableMqttLB());
        QiscusLogger.print("urlLB : " + QiscusCore.getBaseURLLB());

        boolean isValid = QiscusCore.isEnableMqttLB() &&
                QiscusCore.willGetNewNodeMqttBrokerUrl() &&
                QiscusAndroidUtil.isNetworkAvailable();

        if (isValid) {
            QiscusApi.getInstance()
                    .getMqttBaseUrl()
                    .map(s -> String.format("ssl://%s:1885", s))
                    .doOnNext(node -> QiscusCore.setCacheMqttBrokerUrl(node, false))
                    .map(node -> QiscusCore.getMqttBrokerUrl())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(mqttBaseUrl -> {
                                QiscusLogger.print("New MQTT Broker URL = " + mqttBaseUrl);
                                buildClient();
                            },
                            QiscusErrorLogger::print);
        }
    }

    private void eventReport(String moduleName, String event, String message) {
        if (QiscusCore.hasSetupUser()) {
            QiscusApi.getInstance()
                    .eventReport(moduleName, event, message)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(aVoid -> {

                    }, QiscusErrorLogger::print);
        }
    }

    public void connect() {
        if (QiscusCore.hasSetupUser() && !connecting && QiscusAndroidUtil.isNetworkAvailable()) {
            connecting = true;
            qAccount = QiscusCore.getQiscusAccount();
            MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
            mqttConnectOptions.setAutomaticReconnect(false);
            mqttConnectOptions.setCleanSession(false);
            mqttConnectOptions.setWill("u/" + qAccount.getId()
                    + "/s", ("0:" + Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTimeInMillis())
                    .getBytes(), 2, true);
            EventBus.getDefault().post(QiscusMqttStatusEvent.RECONNETING);
            try {
                mqttAndroidClient.connect(mqttConnectOptions, null, this);
                QiscusLogger.print(TAG, "Connecting...");
                eventReport("MQTT", "CONNECTING", "Connecting...");
            } catch (MqttException | IllegalStateException e) {
                //Do nothing
                if (e != null) {
                    try {
                        eventReport("MQTT", "CONNECTING", e.toString());
                        QiscusLogger.print(TAG, "Connecting... error" + e.toString());
                    } catch (NullPointerException d) {
                        //ignored
                    } catch (Exception d) {
                        //ignored
                    }
                } else {
                    QiscusLogger.print(TAG, "Connecting... " + "Failure to connecting");
                    eventReport("MQTT", "CONNECTING", "Failure to connecting");
                }
            } catch (NullPointerException | IllegalArgumentException e) {
                if (e != null) {
                    try {
                        QiscusLogger.print(TAG, "Connecting... error" + e.toString());
                        eventReport("MQTT", "CONNECTING", e.toString());
                    } catch (NullPointerException d) {
                        //ignored
                    } catch (Exception d) {
                        //ignored
                    }
                } else {
                    QiscusLogger.print(TAG, "Connecting... " + "Failure to connecting");
                    eventReport("MQTT", "CONNECTING", "Failure to connecting");
                }
                restartConnection();
            }
        }
    }

    public boolean isConnected() {
        try {
            return mqttAndroidClient != null && mqttAndroidClient.isConnected();
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public void restartConnection() {
        if (isConnected()) {
            QiscusLogger.print(TAG, "Connected... " + "connectCompleteFromRestartConnection");
            eventReport("MQTT", "CONNECTED", "connectCompleteFromRestartConnection ");
            return;
        }

        getMqttBrokerUrlFromLB();
        QiscusLogger.print("QiscusPusherApi", "Restart connection...");
        try {
            connecting = false;
            mqttAndroidClient.disconnect();
            mqttAndroidClient.close();
            QiscusAndroidUtil.runOnBackgroundThread(() -> eventReport("MQTT",
                    "RESTART_CONNECTION", "Restart connection..."), 1000);

        } catch (MqttException | NullPointerException | IllegalArgumentException e) {
            //Do nothing
        }

        clearTasks();

        buildClient();
        connect();
    }

    private void clearTasks() {
        if (scheduledConnect != null) {
            scheduledConnect.cancel(true);
            scheduledConnect = null;
        }
        if (scheduledListenComment != null) {
            scheduledListenComment.cancel(true);
            scheduledListenComment = null;
        }
        if (scheduledListenNotification != null) {
            scheduledListenNotification.cancel(true);
            scheduledListenNotification = null;
        }
        if (scheduledListenRoom != null) {
            scheduledListenRoom.cancel(true);
            scheduledListenRoom = null;
        }
        if (scheduledListenUserStatus != null) {
            scheduledListenUserStatus.cancel(true);
        }
        if (scheduledListenEvent != null) {
            scheduledListenEvent.cancel(true);
            scheduledListenEvent = null;
        }
    }

    public void disconnect() {
        QiscusLogger.print(TAG, "Disconnecting...");
        QiscusCore.setSyncInterval(DISCONNECTED_SYNC_INTERVAL);
        publishOnlinePresence(false);
        try {
            connecting = false;
            mqttAndroidClient.disconnect();
            mqttAndroidClient.close();
            eventReport("MQTT", "DISCONNECT", "Disconnect");
        } catch (MqttException | NullPointerException d) {
            //Do nothing
        } catch (IllegalArgumentException e) {
            //Do nothing
        }
        clearTasks();
        stopUserStatus();
    }

    private void listenComment() {
        QiscusLogger.print(TAG, "Listening comment...");
        try {
            mqttAndroidClient.subscribe(qAccount.getToken() + "/c", 2);
            eventReport("MQTT", "LISTEN_COMMENT", qAccount.getToken() + "/c");
        } catch (MqttException e) {
            //Do nothing
        } catch (NullPointerException | IllegalArgumentException e) {
            if (e != null) {
                eventReport("MQTT", "LISTEN_COMMENT",
                        "Failure listen comment, try again in "
                                + RETRY_PERIOD + " ms" + ", withError = " + e.getMessage());
            }
            QiscusErrorLogger.print(TAG, "Failure listen comment, try again in " + RETRY_PERIOD + " ms");
            connect();
            scheduledListenComment = QiscusAndroidUtil.runOnBackgroundThread(fallBackListenComment, RETRY_PERIOD);
        }
    }

    private void listenNotification() {
        QiscusLogger.print(TAG, "Listening notification...");
        try {
            mqttAndroidClient.subscribe(qAccount.getToken() + "/n", 2);
            QiscusAndroidUtil.runOnBackgroundThread(() -> eventReport("MQTT",
                    "LISTEN_NOTIFICATION", qAccount.getToken() + "/n"), 1000);
        } catch (MqttException e) {
            //Do nothing
        } catch (NullPointerException | IllegalArgumentException e) {
            if (e != null) {
                QiscusAndroidUtil.runOnBackgroundThread(() -> {
                    try {
                        eventReport("MQTT", "LISTEN_NOTIFICATION",
                                "Failure listen notification, try again in "
                                        + RETRY_PERIOD + " ms" + ", withError = " + e.toString());
                    } catch (NullPointerException d) {
                        //ignored
                    } catch (Exception d) {
                        //ignored
                    }
                }, 1000);
            }
            QiscusErrorLogger.print(TAG, "Failure listen notification, try again in " + RETRY_PERIOD + " ms");
            connect();
            scheduledListenNotification = QiscusAndroidUtil.runOnBackgroundThread(fallBackListenNotification, RETRY_PERIOD);
        }
    }

    @Deprecated
    public void listenRoom(QChatRoom qChatRoom) {
        QiscusLogger.print(TAG, "Listening room...");
        fallBackListenRoom = () -> listenRoom(qChatRoom);
        try {
            long roomId = qChatRoom.getId();
            if (!qChatRoom.getType().equals("channel")) {
                mqttAndroidClient.subscribe("r/" + roomId + "/+/+/t", 2);
                mqttAndroidClient.subscribe("r/" + roomId + "/+/+/d", 2);
                mqttAndroidClient.subscribe("r/" + roomId + "/+/+/r", 2);

                QiscusAndroidUtil.runOnBackgroundThread(() -> eventReport("MQTT", "LISTEN_ROOM",
                        "r/" + roomId + "/+/+/t/d/r"), 1000);
            } else {
                mqttAndroidClient.subscribe(QiscusCore.getAppId() + "/" + qChatRoom.getUniqueId() + "/c", 2);
                QiscusAndroidUtil.runOnBackgroundThread(() -> eventReport("MQTT",
                        "LISTEN_ROOM", QiscusCore.getAppId()
                                + "/" + qChatRoom.getUniqueId() + "/c"), 1000);
            }
        } catch (MqttException e) {
            //Do nothing
        } catch (NullPointerException | IllegalArgumentException e) {
            if (e != null) {
                QiscusAndroidUtil.runOnBackgroundThread(() -> {
                    try {
                        eventReport("MQTT", "LISTEN_ROOM", e.toString());
                    } catch (NullPointerException d) {
                        //ignored
                    } catch (Exception d) {
                        //ignored
                    }
                }, 1000);
            }
            QiscusErrorLogger.print(TAG, "Failure listen room, try again in " + RETRY_PERIOD + " ms");
            connect();
            scheduledListenRoom = QiscusAndroidUtil.runOnBackgroundThread(fallBackListenRoom, RETRY_PERIOD);
        }
    }

    public void subscribeChatRoom(QChatRoom qChatRoom) {
        QiscusLogger.print(TAG, "Listening room...");
        fallBackListenRoom = () -> subscribeChatRoom(qChatRoom);
        try {
            long roomId = qChatRoom.getId();
            if (!qChatRoom.getType().equals("channel")) {
                mqttAndroidClient.subscribe("r/" + roomId + "/+/+/t", 2);
                mqttAndroidClient.subscribe("r/" + roomId + "/+/+/d", 2);
                mqttAndroidClient.subscribe("r/" + roomId + "/+/+/r", 2);

                QiscusAndroidUtil.runOnBackgroundThread(() -> eventReport("MQTT", "LISTEN_ROOM",
                        "r/" + roomId + "/+/+/t/d/r"), 1000);
            } else {
                mqttAndroidClient.subscribe(QiscusCore.getAppId() + "/" + qChatRoom.getUniqueId() + "/c", 2);
                QiscusAndroidUtil.runOnBackgroundThread(() -> eventReport("MQTT",
                        "LISTEN_ROOM", QiscusCore.getAppId()
                                + "/" + qChatRoom.getUniqueId() + "/c"), 1000);
            }
        } catch (MqttException e) {
            //Do nothing
        } catch (NullPointerException | IllegalArgumentException e) {
            if (e != null) {
                QiscusAndroidUtil.runOnBackgroundThread(() -> {
                    try {
                        eventReport("MQTT", "LISTEN_ROOM", e.toString());
                    } catch (NullPointerException d) {
                        //ignored
                    } catch (Exception d) {
                        //ignored
                    }
                }, 1000);
            }
            QiscusErrorLogger.print(TAG, "Failure listen room, try again in " + RETRY_PERIOD + " ms");
            connect();
            scheduledListenRoom = QiscusAndroidUtil.runOnBackgroundThread(fallBackListenRoom, RETRY_PERIOD);
        }
    }

    @Deprecated
    public void unListenRoom(QChatRoom qChatRoom) {
        try {
            long roomId = qChatRoom.getId();
            mqttAndroidClient.unsubscribe("r/" + roomId + "/+/+/t");
            mqttAndroidClient.unsubscribe("r/" + roomId + "/+/+/d");
            mqttAndroidClient.unsubscribe("r/" + roomId + "/+/+/r");
            mqttAndroidClient.unsubscribe(QiscusCore.getAppId() + "/" + qChatRoom.getUniqueId() + "/c");
        } catch (MqttException | NullPointerException | IllegalArgumentException e) {
            //Do nothing
        }
        if (scheduledListenRoom != null) {
            scheduledListenRoom.cancel(true);
            scheduledListenRoom = null;
        }
        fallBackListenRoom = null;
    }


    public void unsubsribeChatRoom(QChatRoom qChatRoom) {
        try {
            long roomId = qChatRoom.getId();
            mqttAndroidClient.unsubscribe("r/" + roomId + "/+/+/t");
            mqttAndroidClient.unsubscribe("r/" + roomId + "/+/+/d");
            mqttAndroidClient.unsubscribe("r/" + roomId + "/+/+/r");
            mqttAndroidClient.unsubscribe(QiscusCore.getAppId() + "/" + qChatRoom.getUniqueId() + "/c");
        } catch (MqttException | NullPointerException | IllegalArgumentException e) {
            //Do nothing
        }
        if (scheduledListenRoom != null) {
            scheduledListenRoom.cancel(true);
            scheduledListenRoom = null;
        }
        fallBackListenRoom = null;
    }

    @Deprecated
    public void listenUserStatus(String user) {
        fallBackListenUserStatus = () -> listenUserStatus(user);
        try {
            mqttAndroidClient.subscribe("u/" + user + "/s", 2);
        } catch (MqttException e) {
            //Do nothing
        } catch (NullPointerException | IllegalArgumentException e) {
            connect();
            scheduledListenUserStatus = QiscusAndroidUtil.runOnBackgroundThread(fallBackListenUserStatus, RETRY_PERIOD);
        }
    }

    public void subscribeUserOnlinePresence(String userId) {
        fallBackListenUserStatus = () -> subscribeUserOnlinePresence(userId);
        try {
            mqttAndroidClient.subscribe("u/" + userId + "/s", 2);
        } catch (MqttException e) {
            //Do nothing
        } catch (NullPointerException | IllegalArgumentException e) {
            connect();
            scheduledListenUserStatus = QiscusAndroidUtil.runOnBackgroundThread(fallBackListenUserStatus, RETRY_PERIOD);
        }
    }

    @Deprecated
    public void unListenUserStatus(String user) {
        try {
            mqttAndroidClient.unsubscribe("u/" + user + "/s");
        } catch (MqttException | NullPointerException | IllegalArgumentException e) {
            //Do nothing
        }
        if (scheduledListenUserStatus != null) {
            scheduledListenUserStatus.cancel(true);
            scheduledListenUserStatus = null;
        }
        fallBackListenUserStatus = null;
    }

    public void unsubscribeUserOnlinePresence(String userId) {
        try {
            mqttAndroidClient.unsubscribe("u/" + userId + "/s");
        } catch (MqttException | NullPointerException | IllegalArgumentException e) {
            //Do nothing
        }
        if (scheduledListenUserStatus != null) {
            scheduledListenUserStatus.cancel(true);
            scheduledListenUserStatus = null;
        }
        fallBackListenUserStatus = null;
    }

    @Deprecated
    private void setUserStatus(boolean online) {
        try {
            if (!isConnected() && connecting != true) {
                connect();
            } else {
                try {
                    MqttMessage message = new MqttMessage();
                    message.setPayload(online ? "1".getBytes() : "0".getBytes());
                    message.setQos(1);
                    message.setRetained(true);
                    mqttAndroidClient.publish("u/" + qAccount.getId() + "/s", message);
                } catch (MqttException | NullPointerException | IllegalArgumentException e) {
                    //Do nothing
                }
            }
        } catch (NullPointerException e) {
            connect();
        } catch (Exception ignored) {
            //ignored
        }

    }

    public void publishOnlinePresence(boolean isOnline) {
        try {
            if (!isConnected() && connecting != true) {
                connect();
            } else {
                try {
                    MqttMessage message = new MqttMessage();
                    message.setPayload(isOnline ? "1".getBytes() : "0".getBytes());
                    message.setQos(1);
                    message.setRetained(true);
                    mqttAndroidClient.publish("u/" + qAccount.getId() + "/s", message);
                } catch (MqttException | NullPointerException | IllegalArgumentException e) {
                    //Do nothing
                }
            }
        } catch (NullPointerException e) {
            connect();
        } catch (Exception ignored) {
            //ignored
        }

    }

    @Deprecated
    public void setUserTyping(long roomId, boolean typing) {
        checkAndConnect();
        try {
            MqttMessage message = new MqttMessage();
            message.setPayload((typing ? "1" : "0").getBytes());
            mqttAndroidClient.publish("r/" + roomId + "/" + roomId + "/"
                    + qAccount.getId() + "/t", message);
        } catch (MqttException | NullPointerException | IllegalArgumentException e) {
            //Do nothing
        }
    }

    public void publishTyping(long roomId, boolean isTyping) {
        checkAndConnect();
        try {
            MqttMessage message = new MqttMessage();
            message.setPayload((isTyping ? "1" : "0").getBytes());
            mqttAndroidClient.publish("r/" + roomId + "/" + roomId + "/"
                    + qAccount.getId() + "/t", message);
        } catch (MqttException | NullPointerException | IllegalArgumentException e) {
            //Do nothing
        }
    }

    @Deprecated
    public void setUserRead(long roomId, long commentId) {
        Observable.fromCallable(() -> QiscusCore.getDataStore().getChatRoom(roomId))
                .filter(room -> room != null)
                .flatMap(room -> QiscusApi.getInstance().updateCommentStatus(roomId, commentId, 0))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aVoid -> {
                }, QiscusErrorLogger::print);
    }

    @Deprecated
    public void setUserDelivery(long roomId, long commentId) {
        Observable.fromCallable(() -> QiscusCore.getDataStore().getChatRoom(roomId))
                .filter(room -> room != null)
                .filter(room -> !room.getType().equals("channel"))
                .flatMap(room -> QiscusApi.getInstance().updateCommentStatus(roomId, 0, commentId))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aVoid -> {
                }, QiscusErrorLogger::print);
    }

    public void markAsRead(long roomId, long commentId) {
        Observable.fromCallable(() -> QiscusCore.getDataStore().getChatRoom(roomId))
                .filter(room -> room != null)
                .flatMap(room -> QiscusApi.getInstance().updateCommentStatus(roomId, commentId, 0))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aVoid -> {
                }, QiscusErrorLogger::print);
    }

    public void markAsDelivered(long roomId, long commentId) {
        Observable.fromCallable(() -> QiscusCore.getDataStore().getChatRoom(roomId))
                .filter(room -> room != null)
                .filter(room -> !room.getType().equals("channel"))
                .flatMap(room -> QiscusApi.getInstance().updateCommentStatus(roomId, 0, commentId))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aVoid -> {
                }, QiscusErrorLogger::print);
    }

    @Deprecated
    public void setEvent(long roomId, JSONObject data) {
        checkAndConnect();
        try {
            JSONObject payload = new JSONObject();
            payload.put("sender", qAccount.getId());
            payload.put("data", data);

            MqttMessage message = new MqttMessage();
            message.setPayload((payload.toString().getBytes()));

            mqttAndroidClient.publish("r/" + roomId + "/" + roomId + "/e", message);
        } catch (MqttException | NullPointerException | IllegalArgumentException | JSONException e) {
            //Do nothing
        }
    }

    public void publishCustomEvent(long roomId, JSONObject data) {
        checkAndConnect();
        try {
            JSONObject payload = new JSONObject();
            payload.put("sender", qAccount.getId());
            payload.put("data", data);

            MqttMessage message = new MqttMessage();
            message.setPayload((payload.toString().getBytes()));

            mqttAndroidClient.publish("r/" + roomId + "/" + roomId + "/e", message);
        } catch (MqttException | NullPointerException | IllegalArgumentException | JSONException e) {
            //Do nothing
        }
    }

    @Deprecated
    public void listenEvent(long roomId) {
        QiscusLogger.print(TAG, "Listening event...");
        fallbackListenEvent = () -> listenEvent(roomId);
        try {
            mqttAndroidClient.subscribe("r/" + roomId + "/" + roomId + "/e", 2);
        } catch (MqttException e) {
            //Do nothing
        } catch (NullPointerException | IllegalArgumentException e) {
            connect();
            scheduledListenEvent = QiscusAndroidUtil.runOnBackgroundThread(fallbackListenEvent, RETRY_PERIOD);
        }
    }

    public void subsribeCustomEvent(long roomId) {
        QiscusLogger.print(TAG, "Listening event...");
        fallbackListenEvent = () -> subsribeCustomEvent(roomId);
        try {
            mqttAndroidClient.subscribe("r/" + roomId + "/" + roomId + "/e", 2);
        } catch (MqttException e) {
            //Do nothing
        } catch (NullPointerException | IllegalArgumentException e) {
            connect();
            scheduledListenEvent = QiscusAndroidUtil.runOnBackgroundThread(fallbackListenEvent, RETRY_PERIOD);
        }
    }

    @Deprecated
    public void unlistenEvent(long roomId) {
        try {
            mqttAndroidClient.unsubscribe("r/" + roomId + "/" + roomId + "/e");
        } catch (MqttException | NullPointerException | IllegalArgumentException e) {
            //Do nothing
        }
        if (scheduledListenEvent != null) {
            scheduledListenEvent.cancel(true);
            scheduledListenEvent = null;
        }
        fallbackListenEvent = null;
    }

    public void unsubsribeCustomEvent(long roomId) {
        try {
            mqttAndroidClient.unsubscribe("r/" + roomId + "/" + roomId + "/e");
        } catch (MqttException | NullPointerException | IllegalArgumentException e) {
            //Do nothing
        }
        if (scheduledListenEvent != null) {
            scheduledListenEvent.cancel(true);
            scheduledListenEvent = null;
        }
        fallbackListenEvent = null;
    }

    private void checkAndConnect() {
        try {
            if (!isConnected()) {
                connect();
            }
        } catch (NullPointerException e) {
            connect();
        } catch (Exception ignored) {
            //ignored
        }
    }

    @Override
    public void connectionLost(Throwable cause) {
        if (reconnectCounter == 0) {
            getMqttBrokerUrlFromLB();
        }

        EventBus.getDefault().post(QiscusMqttStatusEvent.DISCONNECTED);
        reconnectCounter++;
        if (cause != null) {
            try {
                eventReport("MQTT", "CONNECTION_LOST", cause.toString());
            } catch (NullPointerException d) {
                //ignored
            } catch (Exception d) {
                //ignored
            }
        } else {
            eventReport("MQTT", "CONNECTION_LOST",
                    "Lost connection, will try reconnect in " + RETRY_PERIOD + " ms");
        }

        QiscusErrorLogger.print(TAG, "Lost connection, will try reconnect in "
                + RETRY_PERIOD + " ms");
        connecting = false;
        scheduledConnect = QiscusAndroidUtil.runOnBackgroundThread(fallbackConnect, RETRY_PERIOD);
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        try {
            handleMessage(topic, new String(message.getPayload()));
        } catch (Exception ignored) {
            //Do nothing
        }
    }

    private void handleMessage(String topic, String message) {
        if (topic.equals(qAccount.getToken() + "/n")) {
            try {
                handleNotification(new JSONObject(message));
            } catch (JSONException e) {
                QiscusLogger.print(e.getMessage());
            }
        } else if (topic.equals(qAccount.getToken() + "/c")
                || (topic.startsWith(QiscusCore.getAppId()) && topic.endsWith("/c"))) {
            QMessage qiscusMessage = jsonToComment(message);
            if (qiscusMessage == null) {
                return;
            }
            handleReceivedComment(qiscusMessage);
        } else if (topic.startsWith("r/") && topic.endsWith("/t")) {
            String[] data = topic.split("/");
            if (!data[3].equals(qAccount.getId())) {
                QiscusChatRoomEvent event = new QiscusChatRoomEvent()
                        .setRoomId(Long.parseLong(data[1]))
                        .setUser(data[3])
                        .setEvent(QiscusChatRoomEvent.Event.TYPING)
                        .setTyping("1".equals(message));
                EventBus.getDefault().post(event);
            }
        } else if (topic.startsWith("r/") && topic.endsWith("/d")) {
            String[] data = topic.split("/");
            if (!data[3].equals(qAccount.getId())) {
                String[] payload = message.split(":");
                QiscusChatRoomEvent event = new QiscusChatRoomEvent()
                        .setRoomId(Long.parseLong(data[1]))
                        .setUser(data[3])
                        .setEvent(QiscusChatRoomEvent.Event.DELIVERED)
                        .setCommentId(Long.parseLong(payload[0]))
                        .setCommentUniqueId(payload[1]);
                EventBus.getDefault().post(event);
            }
        } else if (topic.startsWith("r/") && topic.endsWith("/r")) {
            String[] data = topic.split("/");
            if (!data[3].equals(qAccount.getId())) {
                String[] payload = message.split(":");
                QiscusChatRoomEvent event = new QiscusChatRoomEvent()
                        .setRoomId(Long.parseLong(data[1]))
                        .setUser(data[3])
                        .setEvent(QiscusChatRoomEvent.Event.READ)
                        .setCommentId(Long.parseLong(payload[0]))
                        .setCommentUniqueId(payload[1]);
                EventBus.getDefault().post(event);
            }
        } else if (topic.startsWith("u/") && topic.endsWith("/s")) {
            String[] data = topic.split("/");
            if (!data[1].equals(qAccount.getId())) {
                String[] status = message.split(":");
                Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                calendar.setTimeInMillis(Long.parseLong(status[1].substring(0, 13)));
                QiscusUserStatusEvent event = new QiscusUserStatusEvent(data[1], "1".equals(status[0]),
                        calendar.getTime());
                EventBus.getDefault().post(event);
            }
        } else if (topic.startsWith("r/") && topic.endsWith("/e")) {
            String[] data = topic.split("/");
            JSONObject eventPayload = parseEventData(message);
            try {
                if (eventPayload != null &&
                        !eventPayload.getString("sender").equals(qAccount.getId())) {
                    QiscusChatRoomEvent event = new QiscusChatRoomEvent()
                            .setRoomId(Long.parseLong(data[1]))
                            .setUser(eventPayload.getString("sender"))
                            .setEvent(QiscusChatRoomEvent.Event.CUSTOM)
                            .setEventData(eventPayload.getJSONObject("data"));
                    EventBus.getDefault().post(event);
                }
            } catch (JSONException e) {
                QiscusErrorLogger.print(e);
            }
        }
    }

    private JSONObject parseEventData(String message) {
        try {
            return new JSONObject(message);
        } catch (JSONException e) {
            QiscusErrorLogger.print(e);
        }
        return null;
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
    }

    @Override
    public void connectComplete(boolean reconnect, String serverUri) {
        if (!isConnected()) {
            QiscusCore.setSyncInterval(DISCONNECTED_SYNC_INTERVAL);
            connecting = false;
            reconnectCounter = 0;
            connect();
        } else {
            QiscusCore.setSyncInterval(CONNECTED_SYNC_INTERVAL);
            // if connected, update flag to true
            QiscusCore.setCacheMqttBrokerUrl(QiscusCore.getMqttBrokerUrl(), true);

            QiscusLogger.print(TAG, "Connected..." + QiscusCore.getMqttBrokerUrl());
            eventReport("MQTT", "CONNECTED", "connectComplete... " + QiscusCore.getMqttBrokerUrl());
            EventBus.getDefault().post(QiscusMqttStatusEvent.CONNECTED);
            try {
                connecting = false;
                reconnectCounter = 0;
                listenComment();
                listenNotification();
                if (fallBackListenRoom != null) {
                    scheduledListenRoom = QiscusAndroidUtil.runOnBackgroundThread(fallBackListenRoom);
                }
                if (fallBackListenUserStatus != null) {
                    scheduledListenUserStatus = QiscusAndroidUtil.runOnBackgroundThread(fallBackListenUserStatus);
                }
                if (scheduledConnect != null) {
                    scheduledConnect.cancel(true);
                    scheduledConnect = null;
                }
                scheduleUserStatus();
            } catch (NullPointerException e) {
                //ignored
            } catch (IllegalArgumentException ignored) {
                //Do nothing
                if (ignored != null) {
                    try {
                        QiscusLogger.print(TAG, "Connected..." + ignored.toString());
                        eventReport("MQTT", "CONNECTED", "Failed Connected... " + ignored.toString());
                    } catch (NullPointerException e) {
                        //ignored
                    } catch (Exception e) {
                        //ignored
                    }
                }
            }
        }
    }

    @Override
    public void onSuccess(IMqttToken asyncActionToken) {
        QiscusCore.setSyncInterval(CONNECTED_SYNC_INTERVAL);
    }

    @Override
    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
        QiscusCore.setSyncInterval(DISCONNECTED_SYNC_INTERVAL);
        if (reconnectCounter == 0) {
            getMqttBrokerUrlFromLB();
        }

        EventBus.getDefault().post(QiscusMqttStatusEvent.DISCONNECTED);
        if (exception != null) {
            try {
                eventReport("MQTT", "FAILURE_TO_CONNECT", exception.toString());
            } catch (NullPointerException e) {
                //ignored
            } catch (Exception e) {
                //ignored
            }
        } else {
            eventReport("MQTT", "FAILURE_TO_CONNECT", "Failure to connect, try again in "
                    + RETRY_PERIOD + " ms");
        }

        reconnectCounter++;
        QiscusErrorLogger.print(TAG, "Failure to connect, try again in " + RETRY_PERIOD + " ms");
        connecting = false;
        scheduledConnect = QiscusAndroidUtil.runOnBackgroundThread(fallbackConnect, RETRY_PERIOD);
    }

    @Subscribe
    public void onUserEvent(QiscusUserEvent userEvent) {
        switch (userEvent) {
            case LOGOUT:
                disconnect();
                break;
        }
    }

    private void scheduleUserStatus() {
        scheduledUserStatus = QiscusCore.getTaskExecutor()
                .scheduleWithFixedDelay(() -> {
                    if (QiscusCore.hasSetupUser()) {
                        if (QiscusCore.isOnForeground()) {
                            QiscusResendCommentHelper.tryResendPendingComment();
                        }
                        if (isConnected()) {
                            if (QiscusCore.isOnForeground()) {
                                setOfflineCounter = 0;
                                publishOnlinePresence(true);
                            } else {
                                if (setOfflineCounter <= 2) {
                                    publishOnlinePresence(false);
                                    setOfflineCounter++;
                                }
                            }
                        }
                    } else {
                        stopUserStatus();
                    }
                }, 0, 10, TimeUnit.SECONDS);
    }

    private void stopUserStatus() {
        if (scheduledUserStatus != null) {
            scheduledUserStatus.cancel(true);
        }
    }
}
