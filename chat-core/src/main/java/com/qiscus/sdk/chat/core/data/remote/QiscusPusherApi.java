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

import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.qiscus.sdk.chat.core.QiscusCore;
import com.qiscus.sdk.chat.core.data.local.QiscusEventCache;
import com.qiscus.sdk.chat.core.data.model.QiscusAccount;
import com.qiscus.sdk.chat.core.data.model.QiscusChatRoom;
import com.qiscus.sdk.chat.core.data.model.QiscusComment;
import com.qiscus.sdk.chat.core.data.model.QiscusRoomMember;
import com.qiscus.sdk.chat.core.event.QiscusChatRoomEvent;
import com.qiscus.sdk.chat.core.event.QiscusCommentReceivedEvent;
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
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.SQLOutput;
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
    private static final long RETRY_PERIOD = 2000;

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
    public static void handleReceivedComment(QiscusComment qiscusComment) {
        QiscusAndroidUtil.runOnBackgroundThread(() -> handleComment(qiscusComment));
    }

    private static void handleComment(QiscusComment qiscusComment) {
        QiscusComment savedComment = QiscusCore.getDataStore().getComment(qiscusComment.getUniqueId());
        if (savedComment != null && (savedComment.isDeleted() || savedComment.areContentsTheSame(qiscusComment))) {
            return;
        }

        if (!qiscusComment.isMyComment()) {
            QiscusPusherApi.getInstance().setUserDelivery(qiscusComment.getRoomId(), qiscusComment.getId());
        }

        if (QiscusCore.getChatConfig().getNotificationListener() != null) {
            QiscusCore.getChatConfig().getNotificationListener()
                    .onHandlePushNotification(QiscusCore.getApps(), qiscusComment);
        }

        QiscusAndroidUtil.runOnUIThread(() -> EventBus.getDefault().post(new QiscusCommentReceivedEvent(qiscusComment)));
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public static void handleNotification(JSONObject jsonObject) {
        long eventId = jsonObject.optLong("id");
        if (eventId <= QiscusEventCache.getInstance().getLastEventId()) {
            return;
        }

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
        mqttAndroidClient = null;
        mqttAndroidClient = new MqttAndroidClient(QiscusCore.getApps().getApplicationContext(),
                QiscusCore.getMqttBrokerUrl(), clientId);
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
            qiscusAccount = QiscusCore.getQiscusAccount();
            MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
            mqttConnectOptions.setAutomaticReconnect(false);
            mqttConnectOptions.setCleanSession(false);
            mqttConnectOptions.setWill("u/" + qiscusAccount.getEmail()
                    + "/s", ("0:" + Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTimeInMillis())
                    .getBytes(), 2, true);
            try {
                mqttAndroidClient.connect(mqttConnectOptions, null, this);
                QiscusLogger.print(TAG, "Connecting...");
                eventReport("MQTT", "CONNECTING", "Connecting...");
            } catch (MqttException | IllegalStateException e) {
                //Do nothing
                if (e != null) {
                    eventReport("MQTT", "CONNECTING", e.toString());
                    QiscusLogger.print(TAG, "Connecting... error" + e.toString());
                } else {
                    QiscusLogger.print(TAG, "Connecting... " + "Failure to connecting");
                    eventReport("MQTT", "CONNECTING", "Failure to connecting");
                }
            } catch (NullPointerException | IllegalArgumentException e) {
                if (e != null) {
                    QiscusLogger.print(TAG, "Connecting... error" + e.toString());
                    eventReport("MQTT", "CONNECTING", e.toString());
                } else {
                    QiscusLogger.print(TAG, "Connecting... " + "Failure to connecting");
                    eventReport("MQTT", "CONNECTING", "Failure to connecting");
                }
                restartConnection();
            }
        }
    }

    public boolean isConnected() {
        return mqttAndroidClient != null && mqttAndroidClient.isConnected();
    }

    public void restartConnection() {
        if (mqttAndroidClient != null && mqttAndroidClient.isConnected()) {
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
            QiscusAndroidUtil.runOnBackgroundThread(new Runnable() {
                @Override
                public void run() {
                    eventReport("MQTT", "RESTART_CONNECTION", "Restart connection...");
                }
            }, 1000);
        } catch (MqttException | NullPointerException | IllegalArgumentException e) {
            //Do nothing
            if (e != null) {
                QiscusAndroidUtil.runOnBackgroundThread(new Runnable() {
                    @Override
                    public void run() {
                        eventReport("MQTT", "RESTART_CONNECTION", e.getCause().toString());
                    }
                }, 1000);
            }
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
        setUserStatus(false);
        try {
            connecting = false;
            mqttAndroidClient.disconnect();
            mqttAndroidClient.close();
            eventReport("MQTT", "DISCONNECT", "Disconnect");
        } catch (MqttException | NullPointerException | IllegalArgumentException e) {
            //Do nothing
            if (e != null) {
                eventReport("MQTT", "DISCONNECT", e.toString());
            }
        }
        clearTasks();
        stopUserStatus();
    }

    private void listenComment() {
        QiscusLogger.print(TAG, "Listening comment...");
        try {
            mqttAndroidClient.subscribe(qiscusAccount.getToken() + "/c", 2);
            eventReport("MQTT", "LISTEN_COMMENT", qiscusAccount.getToken() + "/c");
        } catch (MqttException e) {
            //Do nothing
        } catch (NullPointerException | IllegalArgumentException e) {
            if (e != null) {
                eventReport("MQTT", "LISTEN_COMMENT", "Failure listen comment, try again in "
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
            mqttAndroidClient.subscribe(qiscusAccount.getToken() + "/n", 2);
            QiscusAndroidUtil.runOnBackgroundThread(new Runnable() {
                @Override
                public void run() {
                    eventReport("MQTT", "LISTEN_NOTIFICATION", qiscusAccount.getToken() + "/n");
                }
            }, 1000);
        } catch (MqttException e) {
            //Do nothing
        } catch (NullPointerException | IllegalArgumentException e) {
            if (e != null) {
                QiscusAndroidUtil.runOnBackgroundThread(new Runnable() {
                    @Override
                    public void run() {
                        eventReport("MQTT", "LISTEN_NOTIFICATION", "Failure listen notification, try again in "
                                + RETRY_PERIOD + " ms" + ", withError = " + e.toString());
                    }
                }, 1000);
            }
            QiscusErrorLogger.print(TAG, "Failure listen notification, try again in " + RETRY_PERIOD + " ms");
            connect();
            scheduledListenNotification = QiscusAndroidUtil.runOnBackgroundThread(fallBackListenNotification, RETRY_PERIOD);
        }
    }

    public void listenRoom(QiscusChatRoom qiscusChatRoom) {
        QiscusLogger.print(TAG, "Listening room...");
        fallBackListenRoom = () -> listenRoom(qiscusChatRoom);
        try {
            long roomId = qiscusChatRoom.getId();
            if (!qiscusChatRoom.isChannel()) {
                mqttAndroidClient.subscribe("r/" + roomId + "/+/+/t", 2);
                mqttAndroidClient.subscribe("r/" + roomId + "/+/+/d", 2);
                mqttAndroidClient.subscribe("r/" + roomId + "/+/+/r", 2);

                QiscusAndroidUtil.runOnBackgroundThread(new Runnable() {
                    @Override
                    public void run() {
                        eventReport("MQTT", "LISTEN_ROOM", "r/" + roomId + "/+/+/t/d/r");
                    }
                }, 1000);
            } else {
                mqttAndroidClient.subscribe(QiscusCore.getAppId() + "/" + qiscusChatRoom.getUniqueId() + "/c", 2);
                QiscusAndroidUtil.runOnBackgroundThread(new Runnable() {
                    @Override
                    public void run() {
                        eventReport("MQTT", "LISTEN_ROOM", QiscusCore.getAppId()
                                + "/" + qiscusChatRoom.getUniqueId() + "/c");
                    }
                }, 1000);
            }
        } catch (MqttException e) {
            //Do nothing
        } catch (NullPointerException | IllegalArgumentException e) {
            if (e != null) {
                QiscusAndroidUtil.runOnBackgroundThread(new Runnable() {
                    @Override
                    public void run() {
                        eventReport("MQTT", "LISTEN_ROOM", e.toString());
                    }
                }, 1000);
            }
            QiscusErrorLogger.print(TAG, "Failure listen room, try again in " + RETRY_PERIOD + " ms");
            connect();
            scheduledListenRoom = QiscusAndroidUtil.runOnBackgroundThread(fallBackListenRoom, RETRY_PERIOD);
        }
    }

    public void unListenRoom(QiscusChatRoom qiscusChatRoom) {
        try {
            long roomId = qiscusChatRoom.getId();
            mqttAndroidClient.unsubscribe("r/" + roomId + "/+/+/t");
            mqttAndroidClient.unsubscribe("r/" + roomId + "/+/+/d");
            mqttAndroidClient.unsubscribe("r/" + roomId + "/+/+/r");
            mqttAndroidClient.unsubscribe(QiscusCore.getAppId() + "/" + qiscusChatRoom.getUniqueId() + "/c");
        } catch (MqttException | NullPointerException | IllegalArgumentException e) {
            //Do nothing
        }
        if (scheduledListenRoom != null) {
            scheduledListenRoom.cancel(true);
            scheduledListenRoom = null;
        }
        fallBackListenRoom = null;
    }

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

    private void setUserStatus(boolean online) {
        checkAndConnect();
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

    public void setUserTyping(long roomId, boolean typing) {
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

    public void setUserRead(long roomId, long commentId) {
        Observable.fromCallable(() -> QiscusCore.getDataStore().getChatRoom(roomId))
                .filter(room -> room != null)
                .flatMap(room -> QiscusApi.getInstance().updateCommentStatus(roomId, commentId, 0))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aVoid -> {
                }, QiscusErrorLogger::print);
    }

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

    public void setEvent(long roomId, JSONObject data) {
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

    private void checkAndConnect() {
        try {
            if (!mqttAndroidClient.isConnected()) {
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
            eventReport("MQTT", "CONNECTION_LOST", cause.toString());
        } else {
            eventReport("MQTT", "CONNECTION_LOST", "Lost connection, will try reconnect in " + RETRY_PERIOD * 1 + " ms");
        }

        QiscusErrorLogger.print(TAG, "Lost connection, will try reconnect in " + RETRY_PERIOD * 1 + " ms");
        connecting = false;
        scheduledConnect = QiscusAndroidUtil.runOnBackgroundThread(fallbackConnect, RETRY_PERIOD * 1);
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
        if (!mqttAndroidClient.isConnected()) {
            connecting = false;
            reconnectCounter = 0;
            connect();
        } else {
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
            } catch (NullPointerException | IllegalArgumentException ignored) {
                //Do nothing
                if (ignored != null) {
                    QiscusLogger.print(TAG, "Connected..." + ignored.toString());
                    eventReport("MQTT", "CONNECTED", "Failed Connected... " + ignored.toString());
                }
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
        }

        EventBus.getDefault().post(QiscusMqttStatusEvent.DISCONNECTED);
        if (exception != null) {
            eventReport("MQTT", "FAILURE_TO_CONNECT", exception.getCause().toString());
        } else {
            eventReport("MQTT", "FAILURE_TO_CONNECT", "Failure to connect, try again in " + RETRY_PERIOD * 1 + " ms");
        }
        reconnectCounter++;
        QiscusErrorLogger.print(TAG, "Failure to connect, try again in " + RETRY_PERIOD * 1 + " ms");
        connecting = false;
        scheduledConnect = QiscusAndroidUtil.runOnBackgroundThread(fallbackConnect, RETRY_PERIOD * 1);
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
                                setUserStatus(true);
                            } else {
                                if (setOfflineCounter <= 2) {
                                    setUserStatus(false);
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
