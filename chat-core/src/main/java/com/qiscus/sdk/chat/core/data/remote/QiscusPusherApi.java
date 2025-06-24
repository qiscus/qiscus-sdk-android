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
import com.qiscus.sdk.chat.core.data.local.QiscusDataStore;
import com.qiscus.sdk.chat.core.data.local.QiscusEventCache;
import com.qiscus.sdk.chat.core.data.model.QiscusAccount;
import com.qiscus.sdk.chat.core.data.model.QiscusChatRoom;
import com.qiscus.sdk.chat.core.data.model.QiscusComment;
import com.qiscus.sdk.chat.core.data.model.QiscusRoomMember;
import com.qiscus.sdk.chat.core.event.QiscusChatRoomEvent;
import com.qiscus.sdk.chat.core.event.QiscusChatRoomTypingAIEvent;
import com.qiscus.sdk.chat.core.event.QiscusCommentReceivedEvent;
import com.qiscus.sdk.chat.core.event.QiscusCommentUpdateEvent;
import com.qiscus.sdk.chat.core.event.QiscusMqttStatusEvent;
import com.qiscus.sdk.chat.core.event.QiscusUserEvent;
import com.qiscus.sdk.chat.core.event.QiscusUserStatusEvent;
import com.qiscus.sdk.chat.core.util.QiscusAndroidUtil;
import com.qiscus.sdk.chat.core.util.QiscusErrorLogger;
import com.qiscus.sdk.chat.core.util.QiscusFileUtil;
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

    private static Gson gson;
    private static long reconnectCounter;

    static {
        gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").create();
    }

    private String clientId;
    private MqttAndroidClient mqttAndroidClient;
    private QiscusAccount qiscusAccount;
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
    private boolean reporting = true;
    private Runnable fallbackConnect = this::restartConnection;
    private Runnable fallBackListenNotification = this::listenNotification;
    private ScheduledFuture<?> scheduledUserStatus;
    private Runnable fallBackListenComment = this::listenComment;
    private int setOfflineCounter;

    QiscusPusherApi() {
        if (!QiscusCore.getEnableRealtime()) {
            return;
        }
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
    public static void handleReceivedComment(QiscusComment qiscusComment) {
        QiscusAndroidUtil.runOnBackgroundThread(() -> handleComment(qiscusComment, false));
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public static void handleUpdateComment(QiscusComment qiscusComment) {
        QiscusAndroidUtil.runOnBackgroundThread(() -> handleComment(qiscusComment, true));
    }

    private static void handleComment(QiscusComment qiscusComment, Boolean isMessageUpdate) {
        QiscusComment savedComment = QiscusCore.getDataStore().getComment(qiscusComment.getUniqueId());
        if (savedComment != null && (savedComment.isDeleted() || savedComment.areContentsTheSame(qiscusComment))) {
            return;
        }

        if (!qiscusComment.isMyComment()) {
            QiscusPusherApi.getInstance().markAsDelivered(qiscusComment.getRoomId(), qiscusComment.getId());
        }

        if (isMessageUpdate == false) {
            if (QiscusCore.getChatConfig().getNotificationListener() != null) {
                QiscusCore.getChatConfig().getNotificationListener()
                        .onHandlePushNotification(QiscusCore.getApps(), qiscusComment);
            }

            QiscusAndroidUtil.runOnUIThread(() -> EventBus.getDefault().post(new QiscusCommentReceivedEvent(qiscusComment)));
        } else {
            QiscusAndroidUtil.runOnUIThread(() -> EventBus.getDefault().post(new QiscusCommentUpdateEvent(qiscusComment)));
        }

    }

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public static void handleNotification(JSONObject jsonObject) {
        long eventId = jsonObject.optLong("id");

        QiscusEventCache.getInstance().setLastEventId(eventId);

        if (jsonObject.optString("action_topic").equals("delete_message")) {
            JSONObject payload = jsonObject.optJSONObject("payload");

            JSONObject actorJson = payload.optJSONObject("actor");
            QiscusRoomMember actor = new QiscusRoomMember();
            actor.setEmail(actorJson.optString("email"));
            actor.setUsername(actorJson.optString("name"));

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
            QiscusRoomMember actor = new QiscusRoomMember();
            actor.setEmail(actorJson.optString("email"));
            actor.setUsername(actorJson.optString("name"));

            List<Long> roomIds = new ArrayList<>();
            JSONObject dataJson = payload.optJSONObject("data");
            JSONArray clearedRoomsJson = dataJson.optJSONArray("deleted_rooms");
            int clearedRoomsJsonSize = clearedRoomsJson.length();
            for (int i = 0; i < clearedRoomsJsonSize; i++) {
                JSONObject clearedRoomJson = clearedRoomsJson.optJSONObject(i);
                roomIds.add(clearedRoomJson.optLong("id"));
            }

            QiscusClearCommentsHandler.ClearCommentsData clearCommentsData
                    = new QiscusClearCommentsHandler.ClearCommentsData();
            //timestamp is in nano seconds format, convert it to milliseconds by divide it
            clearCommentsData.setTimestamp(jsonObject.optLong("timestamp") / 1000000L);
            clearCommentsData.setActor(actor);
            clearCommentsData.setRoomIds(roomIds);

            QiscusClearCommentsHandler.handle(clearCommentsData);
        } else if (jsonObject.optString("action_topic").equals("delivered")) {
            JSONObject payload = jsonObject.optJSONObject("payload");
            JSONObject dataJson = payload.optJSONObject("data");

            Long commentId = dataJson.optLong("comment_id");
            String commentUniqueID = dataJson.optString("comment_unique_id");
            Long roomId = dataJson.optLong("room_id");
            String sender = dataJson.optString("email");

            QiscusComment savedComment = QiscusCore.getDataStore().getComment(commentUniqueID);
            QiscusAccount qiscusAccount = QiscusCore.getQiscusAccount();

            if (savedComment != null && savedComment.getState() != QiscusComment.STATE_READ &&
                    !sender.equals(qiscusAccount.getEmail())) {

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

            QiscusAccount qiscusAccount = QiscusCore.getQiscusAccount();
            if (!sender.equals(qiscusAccount.getEmail())) {
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
    public static QiscusComment jsonToComment(JsonObject jsonObject) {
        try {
            QiscusComment qiscusComment = new QiscusComment();
            qiscusComment.setId(jsonObject.get("id").getAsLong());
            qiscusComment.setRoomId(jsonObject.get("room_id").getAsLong());
            qiscusComment.setUniqueId(jsonObject.get("unique_temp_id").getAsString());
            qiscusComment.setCommentBeforeId(jsonObject.get("comment_before_id").getAsLong());
            qiscusComment.setMessage(jsonObject.get("message").getAsString());
            qiscusComment.setSender(jsonObject.get("username").isJsonNull() ? null : jsonObject.get("username").getAsString());
            qiscusComment.setSenderEmail(jsonObject.get("email").getAsString());
            qiscusComment.setSenderAvatar(jsonObject.get("user_avatar").getAsString());

            //timestamp is in nano seconds format, convert it to milliseconds by divide it
            long timestamp = jsonObject.get("unix_nano_timestamp").getAsLong() / 1000000L;
            qiscusComment.setTime(new Date(timestamp));
            qiscusComment.setState(QiscusComment.STATE_ON_QISCUS);

            if (jsonObject.has("is_deleted")) {
                qiscusComment.setDeleted(jsonObject.get("is_deleted").getAsBoolean());
            }

            qiscusComment.setRoomName(jsonObject.get("room_name").isJsonNull() ?
                    qiscusComment.getSender() : jsonObject.get("room_name").getAsString());
            if (jsonObject.has("room_avatar")) {
                qiscusComment.setRoomAvatar(jsonObject.get("room_avatar").getAsString());
            }

            qiscusComment.setGroupMessage(!"single".equals(jsonObject.get("chat_type").getAsString()));
            if (!qiscusComment.isGroupMessage()) {
                qiscusComment.setRoomName(qiscusComment.getSender());
            }
            if (jsonObject.has("type")) {
                qiscusComment.setRawType(jsonObject.get("type").getAsString());
                qiscusComment.setExtraPayload(jsonObject.get("payload").toString());
                if (qiscusComment.getType() == QiscusComment.Type.BUTTONS
                        || qiscusComment.getType() == QiscusComment.Type.REPLY
                        || qiscusComment.getType() == QiscusComment.Type.CARD) {
                    JsonObject payload = jsonObject.get("payload").getAsJsonObject();
                    if (payload.has("text")) {
                        String text = payload.get("text").getAsString();
                        if (QiscusTextUtil.isNotBlank(text)) {
                            qiscusComment.setMessage(text.trim());
                        }
                    }
                }
            }

            if (jsonObject.has("extras") && !jsonObject.get("extras").isJsonNull()) {
                qiscusComment.setExtras(new JSONObject(jsonObject.get("extras").getAsJsonObject().toString()));
            }

            if (jsonObject.has("user_extras") && !jsonObject.get("user_extras").isJsonNull()) {
                qiscusComment.setUserExtras(new JSONObject(jsonObject.get("user_extras").getAsJsonObject().toString()));
            }

            return qiscusComment;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Nullable
    public static QiscusComment jsonToComment(String json) {
        return jsonToComment(gson.fromJson(json, JsonObject.class));
    }

    private void buildClient() {
        Long epochTimeLong = System.currentTimeMillis() / 1000;
        String epochTime = epochTimeLong.toString();

        mqttAndroidClient = null;

        try {
            mqttAndroidClient = new MqttAndroidClient(QiscusCore.getApps().getApplicationContext(),
                    QiscusCore.getMqttBrokerUrl(), clientId + epochTime, new MemoryPersistence());
            mqttAndroidClient.setCallback(this);
            mqttAndroidClient.setTraceEnabled(false);
        } catch (NullPointerException n) {
            mqttAndroidClient = new MqttAndroidClient(QiscusCore.getApps().getApplicationContext(),
                    QiscusCore.getMqttBrokerUrl(), clientId + epochTime);
            mqttAndroidClient.setCallback(this);
            mqttAndroidClient.setTraceEnabled(false);
        }

    }

    /**
     * If isEnableMqttLB = true, MQTT broker url is from own MQTT_LB and save to shared pref
     */
    private void getMqttBrokerUrlFromLB() {
        QiscusLogger.print(TAG, "isEnableMqttLB : " + QiscusCore.isEnableMqttLB());
        QiscusLogger.print(TAG, "urlLB : " + QiscusCore.getBaseURLLB());

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
                                QiscusLogger.print(TAG, "New MQTT Broker URL = " + mqttBaseUrl);
                            },
                            QiscusErrorLogger::print);
        }
    }

    private void eventReport(String moduleName, String event, String message) {
        if (QiscusCore.hasSetupUser() && reporting == true && QiscusCore.getEnableEventReport() == true) {
            QiscusApi.getInstance()
                    .eventReport(moduleName, event, message)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(aVoid -> {
                        reporting = false;
                    }, QiscusErrorLogger::print);
        }
    }

    public void connect() {
        if (QiscusCore.hasSetupUser() && !connecting && QiscusAndroidUtil.isNetworkAvailable()
                && QiscusCore.getEnableRealtime() && QiscusCore.getStatusRealtimeEnableDisable()) {
            connecting = true;
            qiscusAccount = QiscusCore.getQiscusAccount();
            MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
            mqttConnectOptions.setAutomaticReconnect(false);
            mqttConnectOptions.setCleanSession(false);

            try {
                mqttConnectOptions.setWill("u/" + qiscusAccount.getEmail()
                        + "/s", ("0:" + Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTimeInMillis())
                        .getBytes(), 2, true);
                EventBus.getDefault().post(QiscusMqttStatusEvent.RECONNETING);
                mqttAndroidClient.connect(mqttConnectOptions, null, this);
                QiscusLogger.print(TAG, "Connecting...");
            } catch (MqttException | IllegalStateException e) {
                connecting = false;
                if (e != null) {
                    try {
                        QiscusLogger.print(TAG, "Connecting... error" + e.toString());
                    } catch (NullPointerException d) {
                        //ignored
                    } catch (Exception d) {
                        //ignored
                    }
                } else {
                    QiscusLogger.print(TAG, "Connecting... " + "Failure to connecting");
                }
            } catch (NullPointerException | IllegalArgumentException e) {
                connecting = false;
                if (e != null) {
                    try {
                        QiscusLogger.print(TAG, "Connecting... error" + e.toString());
                    } catch (NullPointerException d) {
                        //ignored
                    } catch (Exception d) {
                        //ignored
                    }
                } else {
                    QiscusLogger.print(TAG, "Connecting... " + "Failure to connecting");
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
            if (!QiscusCore.getEnableRealtime()) {
                disconnect();
            }
            return;
        }

        if (!QiscusCore.getEnableRealtime()) {
            QiscusLogger.print("QiscusPusherApi", "Disconnect from AppConfig.");
            return;
        }

        if (!QiscusCore.getStatusRealtimeEnableDisable()) {
            QiscusLogger.print(TAG, "QiscusPusherApi... " + "Disconnect manually from client");
            return;
        }


        if (connecting) {
            QiscusLogger.print(TAG, "Connecting... " + "connectingFromRestartConnection");
            return;
        }

        getMqttBrokerUrlFromLB();
        QiscusLogger.print("QiscusPusherApi", "Restart connection...");
        try {
            connecting = false;
            mqttAndroidClient.disconnect();
            mqttAndroidClient.close();

        } catch (MqttException | NullPointerException | IllegalArgumentException e) {
            //Do nothing
            connecting = false;
        } catch (RuntimeException e) {
            connecting = false;
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
        if (mqttAndroidClient == null) {
            return;
        }
        publishOnlinePresence(false);
        try {
            connecting = false;
            mqttAndroidClient.disconnect();
            mqttAndroidClient.close();
        } catch (MqttException | NullPointerException d) {
            //Do nothing
        } catch (IllegalArgumentException e) {
            //Do nothing
        }
        clearTasks();
        stopUserStatus();
    }

    private void listenComment() {
        if (!QiscusCore.getEnableRealtime() || !QiscusCore.getStatusRealtimeEnableDisable()) {
            return;
        }
        QiscusLogger.print(TAG, "Listening comment...");
        try {
            mqttAndroidClient.subscribe(qiscusAccount.getToken() + "/c", 2);
            mqttAndroidClient.subscribe(qiscusAccount.getToken() + "/update", 2);
        } catch (MqttException e) {
            try {
                eventReport("MQTT", "FAILED_LISTEN_COMMENT", e.toString());
            } catch (NullPointerException d) {
                //ignored
            } catch (Exception d) {
                //ignored
            }
            disconnect();
            restartConnection();
        } catch (NullPointerException | IllegalArgumentException e) {
            try {
                eventReport("MQTT", "FAILED_LISTEN_COMMENT", e.toString());
            } catch (NullPointerException d) {
                //ignored
            } catch (Exception d) {
                //ignored
            }
            QiscusErrorLogger.print(TAG, "Failure listen comment, try again in " + RETRY_PERIOD + " ms");
            connect();
            scheduledListenComment = QiscusAndroidUtil.runOnBackgroundThread(fallBackListenComment, RETRY_PERIOD);
        }
    }

    private void listenNotification() {
        QiscusLogger.print(TAG, "Listening notification...");
        try {
            mqttAndroidClient.subscribe(qiscusAccount.getToken() + "/n", 2);
        } catch (MqttException e) {
            try {
                eventReport("MQTT", "FAILED_LISTEN_NOTIFICATION", e.toString());
            } catch (NullPointerException d) {
                //ignored
            } catch (Exception d) {
                //ignored
            }
        } catch (NullPointerException | IllegalArgumentException e) {
            try {
                eventReport("MQTT", "FAILED_LISTEN_NOTIFICATION", e.toString());
            } catch (NullPointerException d) {
                //ignored
            } catch (Exception d) {
                //ignored
            }
            QiscusErrorLogger.print(TAG, "Failure listen notification, try again in " + RETRY_PERIOD + " ms");
            connect();
            scheduledListenNotification = QiscusAndroidUtil.runOnBackgroundThread(fallBackListenNotification, RETRY_PERIOD);
        }
    }

    @Deprecated
    public void listenRoom(QiscusChatRoom qiscusChatRoom) {
        if (!QiscusCore.getEnableRealtime() || !QiscusCore.getStatusRealtimeEnableDisable()) {
            return;
        }
        QiscusLogger.print(TAG, "Listening room...");
        fallBackListenRoom = () -> listenRoom(qiscusChatRoom);
        try {
            long roomId = qiscusChatRoom.getId();
            if (!qiscusChatRoom.isChannel()) {
                mqttAndroidClient.subscribe("r/" + roomId + "/+/+/t", 2);
                mqttAndroidClient.subscribe("r/" + roomId + "/+/+/d", 2);
                mqttAndroidClient.subscribe("r/" + roomId + "/+/+/r", 2);
                mqttAndroidClient.subscribe("r/" + roomId + "/typing", 2);
            } else {
                mqttAndroidClient.subscribe(QiscusCore.getAppId() + "/" + qiscusChatRoom.getUniqueId() + "/c", 2);
                mqttAndroidClient.subscribe("r/" + roomId + "/typing", 2);
            }
        } catch (MqttException e) {
            //Do nothing
        } catch (NullPointerException | IllegalArgumentException e) {
            QiscusErrorLogger.print(TAG, "Failure listen room, try again in " + RETRY_PERIOD + " ms");
            connect();
            scheduledListenRoom = QiscusAndroidUtil.runOnBackgroundThread(fallBackListenRoom, RETRY_PERIOD);
        }
    }

    public void subscribeChatRoom(QiscusChatRoom qiscusChatRoom) {
        if (!QiscusCore.getEnableRealtime() || !QiscusCore.getStatusRealtimeEnableDisable()) {
            return;
        }
        QiscusLogger.print(TAG, "Listening room...");
        fallBackListenRoom = () -> subscribeChatRoom(qiscusChatRoom);
        try {
            long roomId = qiscusChatRoom.getId();
            if (!qiscusChatRoom.isChannel()) {
                mqttAndroidClient.subscribe("r/" + roomId + "/+/+/t", 2);
                mqttAndroidClient.subscribe("r/" + roomId + "/+/+/d", 2);
                mqttAndroidClient.subscribe("r/" + roomId + "/+/+/r", 2);
                mqttAndroidClient.subscribe("r/" + roomId + "/typing", 2);

            } else {
                mqttAndroidClient.subscribe(QiscusCore.getAppId() + "/" + qiscusChatRoom.getUniqueId() + "/c", 2);
                mqttAndroidClient.subscribe("r/" + roomId + "/typing", 2);
            }
        } catch (MqttException e) {
            //Do nothing
        } catch (NullPointerException | IllegalArgumentException e) {
            QiscusErrorLogger.print(TAG, "Failure listen room, try again in " + RETRY_PERIOD + " ms");
            connect();
            scheduledListenRoom = QiscusAndroidUtil.runOnBackgroundThread(fallBackListenRoom, RETRY_PERIOD);
        }
    }

    @Deprecated
    public void unListenRoom(QiscusChatRoom qiscusChatRoom) {
        if (!QiscusCore.getEnableRealtime() || !QiscusCore.getStatusRealtimeEnableDisable()) {
            return;
        }

        try {
            long roomId = qiscusChatRoom.getId();
            mqttAndroidClient.unsubscribe("r/" + roomId + "/+/+/t");
            mqttAndroidClient.unsubscribe("r/" + roomId + "/+/+/d");
            mqttAndroidClient.unsubscribe("r/" + roomId + "/+/+/r");
            mqttAndroidClient.unsubscribe(QiscusCore.getAppId() + "/" + qiscusChatRoom.getUniqueId() + "/c");
            mqttAndroidClient.unsubscribe("r/" + roomId + "/typing");
        } catch (MqttException | NullPointerException | IllegalArgumentException e) {
            //Do nothing
        }
        if (scheduledListenRoom != null) {
            scheduledListenRoom.cancel(true);
            scheduledListenRoom = null;
        }
        fallBackListenRoom = null;
    }


    public void unsubsribeChatRoom(QiscusChatRoom qiscusChatRoom) {
        if (!QiscusCore.getEnableRealtime() || !QiscusCore.getStatusRealtimeEnableDisable()) {
            return;
        }

        try {
            long roomId = qiscusChatRoom.getId();
            mqttAndroidClient.unsubscribe("r/" + roomId + "/+/+/t");
            mqttAndroidClient.unsubscribe("r/" + roomId + "/+/+/d");
            mqttAndroidClient.unsubscribe("r/" + roomId + "/+/+/r");
            mqttAndroidClient.unsubscribe(QiscusCore.getAppId() + "/" + qiscusChatRoom.getUniqueId() + "/c");
            mqttAndroidClient.unsubscribe("r/" + roomId + "/typing");
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
        if (!QiscusCore.getEnableRealtime() || !QiscusCore.getStatusRealtimeEnableDisable()) {
            return;
        }

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
        if (!QiscusCore.getEnableRealtime() || !QiscusCore.getStatusRealtimeEnableDisable()) {
            return;
        }

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
        if (!QiscusCore.getEnableRealtime() || !QiscusCore.getStatusRealtimeEnableDisable()) {
            return;
        }

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
        if (!QiscusCore.getEnableRealtime() || !QiscusCore.getStatusRealtimeEnableDisable()) {
            return;
        }

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
        if (!QiscusCore.getEnableRealtime() || !QiscusCore.getStatusRealtimeEnableDisable()) {
            return;
        }

        try {
            if (!isConnected() && connecting != true) {
                connect();
            } else {
                try {
                    MqttMessage message = new MqttMessage();
                    message.setPayload(online ? "1".getBytes() : "0".getBytes());
                    message.setQos(1);
                    message.setRetained(true);
                    mqttAndroidClient.publish("u/" + qiscusAccount.getEmail() + "/s", message);
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
        if (!QiscusCore.getEnableRealtime() || !QiscusCore.getStatusRealtimeEnableDisable()) {
            return;
        }

        try {
            if (!isConnected() && connecting != true) {
                connect();
            } else {
                try {
                    MqttMessage message = new MqttMessage();
                    message.setPayload(isOnline ? "1".getBytes() : "0".getBytes());
                    message.setQos(1);
                    message.setRetained(true);
                    mqttAndroidClient.publish("u/" + qiscusAccount.getEmail() + "/s", message);
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
        if (!QiscusCore.getEnableRealtime() || !QiscusCore.getStatusRealtimeEnableDisable()) {
            return;
        }

        checkAndConnect();
        try {
            MqttMessage message = new MqttMessage();
            message.setPayload((typing ? "1" : "0").getBytes());
            mqttAndroidClient.publish("r/" + roomId + "/" + roomId + "/"
                    + qiscusAccount.getEmail() + "/t", message);
        } catch (MqttException | NullPointerException | IllegalArgumentException e) {
            //Do nothing
        }
    }

    public void publishTyping(long roomId, boolean isTyping) {
        if (!QiscusCore.getEnableRealtime() || !QiscusCore.getStatusRealtimeEnableDisable()) {
            return;
        }

        checkAndConnect();
        try {
            MqttMessage message = new MqttMessage();
            message.setPayload((isTyping ? "1" : "0").getBytes());
            mqttAndroidClient.publish("r/" + roomId + "/" + roomId + "/"
                    + qiscusAccount.getEmail() + "/t", message);
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
                .filter(room -> !room.isChannel())
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
                .filter(room -> !room.isChannel())
                .flatMap(room -> QiscusApi.getInstance().updateCommentStatus(roomId, 0, commentId))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aVoid -> {
                }, QiscusErrorLogger::print);
    }

    @Deprecated
    public void setEvent(long roomId, JSONObject data) {
        if (!QiscusCore.getEnableRealtime() || !QiscusCore.getStatusRealtimeEnableDisable()) {
            return;
        }

        checkAndConnect();
        try {
            JSONObject payload = new JSONObject();
            payload.put("sender", qiscusAccount.getEmail());
            payload.put("data", data);

            MqttMessage message = new MqttMessage();
            message.setPayload((payload.toString().getBytes()));

            mqttAndroidClient.publish("r/" + roomId + "/" + roomId + "/e", message);
        } catch (MqttException | NullPointerException | IllegalArgumentException | JSONException e) {
            //Do nothing
        }
    }

    public void publishCustomEvent(long roomId, JSONObject data) {
        if (!QiscusCore.getEnableRealtime() || !QiscusCore.getStatusRealtimeEnableDisable()) {
            return;
        }

        checkAndConnect();
        try {
            JSONObject payload = new JSONObject();
            payload.put("sender", qiscusAccount.getEmail());
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
        if (!QiscusCore.getEnableRealtime() || !QiscusCore.getStatusRealtimeEnableDisable()) {
            return;
        }
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
        if (!QiscusCore.getEnableRealtime() || !QiscusCore.getStatusRealtimeEnableDisable()) {
            return;
        }

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
        if (!QiscusCore.getEnableRealtime() || !QiscusCore.getStatusRealtimeEnableDisable()) {
            return;
        }

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
        if (!QiscusCore.getEnableRealtime() || !QiscusCore.getStatusRealtimeEnableDisable()) {
            return;
        }

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
        }else if (reconnectCounter >= 3){
            connecting = false;
            QiscusCore.setEnableDisableRealtime(false);
            EventBus.getDefault().post(QiscusMqttStatusEvent.DISCONNECTED);
            QiscusLogger.print(TAG, "Realtime using sync");
            return;
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
        if (topic.equals(qiscusAccount.getToken() + "/n")) {
            try {
                handleNotification(new JSONObject(message));
            } catch (JSONException e) {
                QiscusLogger.print(e.getMessage());
            }
        } else if (topic.equals(qiscusAccount.getToken() + "/c")
                || (topic.startsWith(QiscusCore.getAppId()) && topic.endsWith("/c"))) {
            QiscusComment qiscusComment = jsonToComment(message);
            if (qiscusComment == null) {
                return;
            }
            handleReceivedComment(qiscusComment);
        } else if (topic.equals(qiscusAccount.getToken() + "/update")
                || (topic.startsWith(QiscusCore.getAppId()) && topic.endsWith("/update"))) {
            QiscusComment qiscusComment = jsonToComment(message);
            if (qiscusComment == null) {
                return;
            }
            handleUpdateComment(qiscusComment);
        } else if (topic.startsWith("r/") && topic.endsWith("/t")) {
            String[] data = topic.split("/");
            if (!data[3].equals(qiscusAccount.getEmail())) {
                QiscusChatRoomEvent event = new QiscusChatRoomEvent()
                        .setRoomId(Long.parseLong(data[1]))
                        .setUser(data[3])
                        .setEvent(QiscusChatRoomEvent.Event.TYPING)
                        .setTyping("1".equals(message));
                EventBus.getDefault().post(event);
            }
        } else if (topic.startsWith("r/") && topic.endsWith("/typing")) {
            String[] data = topic.split("/");
            try {
                JSONObject dataPayload = new JSONObject(message);
                String senderId = dataPayload.optString("sender_id");
                String senderName = dataPayload.optString("sender_name");
                String textMessage = dataPayload.optString("text");
                Boolean statusTyping = dataPayload.optInt("status") == 1;

                QiscusChatRoomTypingAIEvent event = new QiscusChatRoomTypingAIEvent()
                        .setRoomId(Long.parseLong(data[1]))
                        .setSenderId(senderId)
                        .setSenderName(senderName)
                        .setTyping(statusTyping)
                        .setTextMessage(textMessage);

                EventBus.getDefault().post(event);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }

        } else if (topic.startsWith("r/") && topic.endsWith("/d")) {
            String[] data = topic.split("/");
            if (!data[3].equals(qiscusAccount.getEmail())) {
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
            if (!data[3].equals(qiscusAccount.getEmail())) {
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
            if (!data[1].equals(qiscusAccount.getEmail())) {
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
                        !eventPayload.getString("sender").equals(qiscusAccount.getEmail())) {
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
            connecting = false;
            reconnectCounter = 0;
            restartConnection();
        } else {
            // if connected, update flag to true
            QiscusCore.setCacheMqttBrokerUrl(QiscusCore.getMqttBrokerUrl(), true);

            if (!QiscusCore.getEnableRealtime()) {
                disconnect();
                return;
            }

            try {
                QiscusLogger.print(TAG, "Connected..." + mqttAndroidClient.getClientId() + " " + QiscusCore.getMqttBrokerUrl());
                EventBus.getDefault().post(QiscusMqttStatusEvent.CONNECTED);
                reporting = true;

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
                QiscusErrorLogger.print("QiscusPusherApi 2", " nullpointer: " + e);
            } catch (IllegalArgumentException e1) {
                //Do nothing
                QiscusErrorLogger.print("QiscusPusherApi 2", " IllegalArgumentException: " + e1);
                if (e1 != null) {
                    try {
                        QiscusLogger.print(TAG, "Connected..." + e1.toString());
                    } catch (NullPointerException e) {
                        //ignored
                    } catch (Exception e) {
                        //ignored
                    }
                }
            } catch (Exception e) {
                //ignored
                QiscusErrorLogger.print("QiscusPusherApi 2", " Exception: " + e);
            }
        }
    }

    @Override
    public void onSuccess(IMqttToken asyncActionToken) {

    }

    @Override
    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
        if (reconnectCounter == 0) {
            getMqttBrokerUrlFromLB();
        }else if (reconnectCounter >= 3){
            QiscusCore.setEnableDisableRealtime(false);
            connecting = false;
            EventBus.getDefault().post(QiscusMqttStatusEvent.DISCONNECTED);
            QiscusLogger.print(TAG, "Realtime using sync");
            return;
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