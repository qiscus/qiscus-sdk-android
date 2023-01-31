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
import com.qiscus.sdk.chat.core.data.model.QAccount;
import com.qiscus.sdk.chat.core.data.model.QChatRoom;
import com.qiscus.sdk.chat.core.data.model.QMessage;
import com.qiscus.sdk.chat.core.data.model.QParticipant;
import com.qiscus.sdk.chat.core.data.model.QUser;
import com.qiscus.sdk.chat.core.event.QMessageReceivedEvent;
import com.qiscus.sdk.chat.core.event.QMessageUpdateEvent;
import com.qiscus.sdk.chat.core.event.QiscusChatRoomEvent;
import com.qiscus.sdk.chat.core.event.QiscusMqttStatusEvent;
import com.qiscus.sdk.chat.core.event.QiscusUserEvent;
import com.qiscus.sdk.chat.core.event.QiscusUserStatusEvent;
import com.qiscus.sdk.chat.core.util.QiscusAndroidUtil;
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

public class QiscusPusherApi implements MqttCallbackExtended, IMqttActionListener {

    private static final String TAG = QiscusPusherApi.class.getSimpleName();
    private static final long RETRY_PERIOD = 4000;

    private Gson gson;
    private long reconnectCounter;

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
    private boolean reporting = true;
    private ScheduledFuture<?> scheduledUserStatus;
    private int setOfflineCounter;
    private QiscusCore qiscusCore;
    private Runnable fallbackConnect = this::restartConnection;
    private Runnable fallBackListenComment = this::listenComment;
    private Runnable fallBackListenNotification = this::listenNotification;

    public QiscusPusherApi(QiscusCore qiscusCore) {
        this.qiscusCore = qiscusCore;
        gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").create();
        qiscusCore.getLogger().print("QiscusPusherApi", "Creating...");
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

        clientId = qiscusCore.getApps().getPackageName() + "-";
        clientId += Settings.Secure.getString(qiscusCore.getApps().getContentResolver(), Settings.Secure.ANDROID_ID);

        buildClient();

        connecting = false;
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public void handleReceivedComment(QMessage qMessage) {
        QiscusAndroidUtil.runOnBackgroundThread(() -> handleComment(qMessage, false));
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public void handleUpdateComment(QMessage qiscusComment) {
        QiscusAndroidUtil.runOnBackgroundThread(() -> handleComment(qiscusComment, true));
    }


    private void handleComment(QMessage qMessage, Boolean isMessageUpdate) {
        QMessage savedComment = qiscusCore.getDataStore().getComment(qMessage.getUniqueId());
        if (savedComment != null && savedComment.areContentsTheSame(qMessage)) {
            return;
        }

        if (!qMessage.getSender().getId().equals(qiscusCore.getQiscusAccount().getId())) {
            markAsDelivered(qMessage.getChatRoomId(), qMessage.getId());
        }

        if (isMessageUpdate == false) {
            if (qiscusCore.getChatConfig().getNotificationListener() != null) {
                qiscusCore.getChatConfig().getNotificationListener()
                        .onHandlePushNotification(qiscusCore.getApps(), qMessage);
            }

            QiscusAndroidUtil.runOnUIThread(() -> EventBus.getDefault().post(new QMessageReceivedEvent(qMessage)));
        } else {
            QiscusAndroidUtil.runOnUIThread(() -> EventBus.getDefault().post(new QMessageUpdateEvent(qMessage)));
        }

    }

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public void handleNotification(JSONObject jsonObject) {
        long eventId = jsonObject.optLong("id");

        qiscusCore.getEventCache().setLastEventId(eventId);

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

            qiscusCore.getDeleteCommentHandler().handle(deletedCommentsData);
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

            QiscusClearCommentsHandler.ClearCommentsData clearCommentsData
                    = new QiscusClearCommentsHandler.ClearCommentsData();
            //timestamp is in nano seconds format, convert it to milliseconds by divide it
            clearCommentsData.setTimestamp(jsonObject.optLong("timestamp") / 1000000L);
            clearCommentsData.setActor(actor);
            clearCommentsData.setRoomIds(roomIds);

            qiscusCore.getClearCommentsHandler().handle(clearCommentsData);
        } else if (jsonObject.optString("action_topic").equals("delivered")) {
            JSONObject payload = jsonObject.optJSONObject("payload");
            JSONObject dataJson = payload.optJSONObject("data");

            Long commentId = dataJson.optLong("comment_id");
            String commentUniqueID = dataJson.optString("comment_unique_id");
            Long roomId = dataJson.optLong("room_id");
            String sender = dataJson.optString("email");

            QMessage savedComment = qiscusCore.getDataStore().getComment(commentUniqueID);
            QAccount qAccount = qiscusCore.getQiscusAccount();

            if (savedComment != null && savedComment.getStatus() != QMessage.STATE_READ &&
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

            QAccount qAccount = qiscusCore.getQiscusAccount();
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
    public QMessage jsonToComment(JsonObject jsonObject) {
        try {
            QMessage qMessage = new QMessage();
            qMessage.setId(jsonObject.get("id").getAsLong());
            qMessage.setChatRoomId(jsonObject.get("room_id").getAsLong());
            qMessage.setUniqueId(jsonObject.get("unique_temp_id").getAsString());
            qMessage.setPreviousMessageId(jsonObject.get("comment_before_id").getAsLong());
            qMessage.setText(jsonObject.get("message").getAsString());
            if (jsonObject.has("app_code")){
                qMessage.setAppId(jsonObject.get("app_code").getAsString());
            }

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
            qMessage.setSender(qUser);

            qMessage.getSender().setId(jsonObject.get("email").getAsString());
            qMessage.getSender().setAvatarUrl(jsonObject.get("user_avatar").getAsString());

            //timestamp is in nano seconds format, convert it to milliseconds by divide it
            long timestamp = jsonObject.get("unix_nano_timestamp").getAsLong() / 1000000L;
            qMessage.setTimestamp(new Date(timestamp));
            qMessage.setStatus(QMessage.STATE_SENT);

            if (jsonObject.has("type")) {
                qMessage.setRawType(jsonObject.get("type").getAsString());
                if (jsonObject.has("payload") && !jsonObject.get("payload").isJsonNull()) {
                    qMessage.setPayload(jsonObject.get("payload").toString());
                }

                if (qMessage.getType() == QMessage.Type.BUTTONS
                        || qMessage.getType() == QMessage.Type.REPLY
                        || qMessage.getType() == QMessage.Type.CARD) {
                    JsonObject payload = jsonObject.get("payload").getAsJsonObject();
                    if (payload.has("text")) {
                        String text = payload.get("text").getAsString();
                        if (QiscusTextUtil.isNotBlank(text)) {
                            qMessage.setText(text.trim());
                        }
                    }
                }
            }

            if (jsonObject.has("extras") && !jsonObject.get("extras").isJsonNull()) {
                qMessage.setExtras(new JSONObject(jsonObject.get("extras").getAsJsonObject().toString()));
            }

            return qMessage;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Nullable
    public QMessage jsonToComment(String json) {
        return jsonToComment(gson.fromJson(json, JsonObject.class));
    }

    public void buildClient() {
        Long epochTimeLong = System.currentTimeMillis() / 1000;
        String epochTime = epochTimeLong.toString();

        mqttAndroidClient = null;
        try {
            mqttAndroidClient = new MqttAndroidClient(qiscusCore.getApps().getApplicationContext(),
                    qiscusCore.getMqttBrokerUrl(), clientId + epochTime, new MemoryPersistence());
            mqttAndroidClient.setCallback(this);
            mqttAndroidClient.setTraceEnabled(false);
        } catch (NullPointerException e) {
            mqttAndroidClient = new MqttAndroidClient(qiscusCore.getApps().getApplicationContext(),
                    qiscusCore.getMqttBrokerUrl(), clientId + epochTime);
            mqttAndroidClient.setCallback(this);
            mqttAndroidClient.setTraceEnabled(false);

        }

    }

    /**
     * If isEnableMqttLB = true, MQTT broker url is from own MQTT_LB and save to shared pref
     */
    private void getMqttBrokerUrlFromLB() {
        qiscusCore.getLogger().print(TAG, "isEnableMqttLB : " + qiscusCore.isEnableMqttLB());
        qiscusCore.getLogger().print(TAG, "urlLB : " + qiscusCore.getBaseURLLB());

        boolean isValid = qiscusCore.isEnableMqttLB() &&
                qiscusCore.willGetNewNodeMqttBrokerUrl() &&
                qiscusCore.getAndroidUtil().isNetworkAvailable();

        if (isValid) {
            qiscusCore.getApi()
                    .getMqttBaseUrl()
                    .map(s -> String.format("ssl://%s:1885", s))
                    .doOnNext(node -> qiscusCore.setCacheMqttBrokerUrl(node, false))
                    .map(node -> qiscusCore.getMqttBrokerUrl())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(mqttBaseUrl -> {
                                qiscusCore.getLogger().print(TAG, "New MQTT Broker URL = " + mqttBaseUrl);
                                buildClient();
                            },
                            qiscusCore.getErrorLogger()::print);
        }
    }

    private void eventReport(String moduleName, String event, String message) {
        if (qiscusCore.hasSetupUser() && reporting == true && qiscusCore.getEnableEventReport() == true) {
            qiscusCore.getApi()
                    .eventReport(moduleName, event, message)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(aVoid -> {
                        reporting = false;
                    }, qiscusCore.getErrorLogger()::print);
        }
    }

    public void connect() {
        if (qiscusCore.hasSetupUser() && !connecting
                && qiscusCore.getAndroidUtil().isNetworkAvailable()
                && qiscusCore.getEnableRealtime() && qiscusCore.getStatusRealtimeEnableDisable() && !QiscusCore.getIsExactAlarmDisable()) {
            connecting = true;
            qAccount = qiscusCore.getQiscusAccount();
            MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
            mqttConnectOptions.setAutomaticReconnect(false);
            mqttConnectOptions.setCleanSession(false);
            mqttConnectOptions.setWill("u/" + qAccount.getId()
                    + "/s", ("0:" + Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTimeInMillis())
                    .getBytes(), 2, true);
            EventBus.getDefault().post(QiscusMqttStatusEvent.RECONNETING);
            try {
                mqttAndroidClient.connect(mqttConnectOptions, null, this);
                qiscusCore.getLogger().print(TAG, "Connecting...");
            } catch (MqttException | IllegalStateException e) {
                connecting = false;
                if (e != null) {
                    try {
                        qiscusCore.getLogger().print(TAG, "Connecting... error" + e.toString());
                    } catch (NullPointerException d) {
                        //ignored
                    } catch (Exception d) {
                        //ignored
                    }
                } else {
                    qiscusCore.getLogger().print(TAG, "Connecting... " + "Failure to connecting");
                }
            } catch (NullPointerException | IllegalArgumentException e) {
                connecting = false;
                if (e != null) {
                    try {
                        qiscusCore.getLogger().print(TAG, "Connecting... error" + e.toString());
                    } catch (NullPointerException d) {
                        //ignored
                    } catch (Exception d) {
                        //ignored
                    }
                } else {
                    qiscusCore.getLogger().print(TAG, "Connecting... " + "Failure to connecting");
                }
                restartConnection();
            }
        }
    }

    public boolean isConnected() {
        try {
            if (mqttAndroidClient != null) {
                return mqttAndroidClient.isConnected();
            } else {
                return false;
            }
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public void restartConnection() {
        if (isConnected()) {
            qiscusCore.getLogger().print(TAG, "Connected... " + "connectCompleteFromRestartConnection");
            return;
        }

        if (!qiscusCore.getEnableRealtime()) {
            return;
        }

        if (!qiscusCore.getStatusRealtimeEnableDisable()) {
            qiscusCore.getLogger().print(TAG, "QiscusPusherApi... " + "Disconnect manually from client");
            return;
        }

        if (qiscusCore.getIsExactAlarmDisable()){
            qiscusCore.getLogger().print(TAG, "QiscusPusherApi... " + "Disconnect manually from client (exact alarm is false)");
            return;
        }

        if (connecting) {
            qiscusCore.getLogger().print(TAG, "Connecting... " + "connectingFromRestartConnection");
            return;
        }

        getMqttBrokerUrlFromLB();
        qiscusCore.getLogger().print("QiscusPusherApi", "Restart connection...");
        try {
            connecting = false;
            mqttAndroidClient.disconnect();
            mqttAndroidClient.close();

        } catch (MqttException | NullPointerException | IllegalArgumentException e) {
            //Do nothing
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
        qiscusCore.getLogger().print(TAG, "Disconnecting...");
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
        if (!qiscusCore.getEnableRealtime() || !qiscusCore.getStatusRealtimeEnableDisable()) {
            return;
        }

        qiscusCore.getLogger().print(TAG, "Listening comment...");
        try {
            mqttAndroidClient.subscribe(qAccount.getToken() + "/c", 2);
            mqttAndroidClient.subscribe(qAccount.getToken() + "/update", 2);
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
            qiscusCore.getErrorLogger().print(TAG, "Failure listen comment, try again in " + RETRY_PERIOD + " ms");
            connect();
            scheduledListenComment = qiscusCore.getAndroidUtil().runOnBackgroundThread(fallBackListenComment, RETRY_PERIOD);
        }
    }

    private void listenNotification() {
        qiscusCore.getLogger().print(TAG, "Listening notification...");
        try {
            mqttAndroidClient.subscribe(qAccount.getToken() + "/n", 2);
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
            qiscusCore.getErrorLogger().print(TAG, "Failure listen notification, try again in " + RETRY_PERIOD + " ms");
            connect();
            scheduledListenNotification = qiscusCore.getAndroidUtil().runOnBackgroundThread(fallBackListenNotification, RETRY_PERIOD);
        }
    }

    @Deprecated
    public void listenRoom(QChatRoom qChatRoom) {
        if (!qiscusCore.getEnableRealtime() || !qiscusCore.getStatusRealtimeEnableDisable()) {
            return;
        }
        qiscusCore.getLogger().print(TAG, "Listening room...");
        fallBackListenRoom = () -> listenRoom(qChatRoom);
        try {
            long roomId = qChatRoom.getId();
            if (!qChatRoom.getType().equals("channel")) {
                mqttAndroidClient.subscribe("r/" + roomId + "/+/+/t", 2);
                mqttAndroidClient.subscribe("r/" + roomId + "/+/+/d", 2);
                mqttAndroidClient.subscribe("r/" + roomId + "/+/+/r", 2);

            } else {
                mqttAndroidClient.subscribe(qiscusCore.getAppId() + "/" + qChatRoom.getUniqueId() + "/c", 2);
            }
        } catch (MqttException e) {
            //Do nothing
        } catch (NullPointerException | IllegalArgumentException e) {
            qiscusCore.getErrorLogger().print(TAG, "Failure listen room, try again in " + RETRY_PERIOD + " ms");
            //connect();
            scheduledListenRoom = qiscusCore.getAndroidUtil().runOnBackgroundThread(fallBackListenRoom, RETRY_PERIOD);
        }
    }

    public void subscribeChatRoom(QChatRoom qChatRoom) {
        if (!qiscusCore.getEnableRealtime() || !qiscusCore.getStatusRealtimeEnableDisable()) {
            return;
        }
        qiscusCore.getLogger().print(TAG, "Listening room...");
        fallBackListenRoom = () -> subscribeChatRoom(qChatRoom);
        try {
            long roomId = qChatRoom.getId();
            if (!qChatRoom.getType().equals("channel")) {
                mqttAndroidClient.subscribe("r/" + roomId + "/+/+/t", 2);
                mqttAndroidClient.subscribe("r/" + roomId + "/+/+/d", 2);
                mqttAndroidClient.subscribe("r/" + roomId + "/+/+/r", 2);

            } else {
                mqttAndroidClient.subscribe(qiscusCore.getAppId() + "/" + qChatRoom.getUniqueId() + "/c", 2);

            }
        } catch (MqttException e) {
            //Do nothing
        } catch (NullPointerException | IllegalArgumentException e) {
            qiscusCore.getErrorLogger().print(TAG, "Failure listen room, try again in " + RETRY_PERIOD + " ms");
            //connect();
            scheduledListenRoom = qiscusCore.getAndroidUtil().runOnBackgroundThread(fallBackListenRoom, RETRY_PERIOD);
        }
    }

    @Deprecated
    public void unListenRoom(QChatRoom qChatRoom) {
        if (!qiscusCore.getEnableRealtime() || !qiscusCore.getStatusRealtimeEnableDisable()) {
            return;
        }

        try {
            long roomId = qChatRoom.getId();
            mqttAndroidClient.unsubscribe("r/" + roomId + "/+/+/t");
            mqttAndroidClient.unsubscribe("r/" + roomId + "/+/+/d");
            mqttAndroidClient.unsubscribe("r/" + roomId + "/+/+/r");
            mqttAndroidClient.unsubscribe(qiscusCore.getAppId() + "/" + qChatRoom.getUniqueId() + "/c");
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
        if (!qiscusCore.getEnableRealtime() || !qiscusCore.getStatusRealtimeEnableDisable()) {
            return;
        }

        try {
            long roomId = qChatRoom.getId();
            mqttAndroidClient.unsubscribe("r/" + roomId + "/+/+/t");
            mqttAndroidClient.unsubscribe("r/" + roomId + "/+/+/d");
            mqttAndroidClient.unsubscribe("r/" + roomId + "/+/+/r");
            mqttAndroidClient.unsubscribe(qiscusCore.getAppId() + "/" + qChatRoom.getUniqueId() + "/c");
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
        if (!qiscusCore.getEnableRealtime() || !qiscusCore.getStatusRealtimeEnableDisable()) {
            return;
        }

        fallBackListenUserStatus = () -> listenUserStatus(user);
        try {
            mqttAndroidClient.subscribe("u/" + user + "/s", 2);
        } catch (MqttException e) {
            //Do nothing
        } catch (NullPointerException | IllegalArgumentException e) {
            //connect();
            scheduledListenUserStatus = qiscusCore.getAndroidUtil().runOnBackgroundThread(fallBackListenUserStatus, RETRY_PERIOD);
        }
    }

    public void subscribeUserOnlinePresence(String userId) {
        if (!qiscusCore.getEnableRealtime() || !qiscusCore.getStatusRealtimeEnableDisable()) {
            return;
        }

        fallBackListenUserStatus = () -> subscribeUserOnlinePresence(userId);
        try {
            mqttAndroidClient.subscribe("u/" + userId + "/s", 2);
        } catch (MqttException e) {
            //Do nothing
        } catch (NullPointerException | IllegalArgumentException e) {
            //connect();
            scheduledListenUserStatus = qiscusCore.getAndroidUtil().runOnBackgroundThread(fallBackListenUserStatus, RETRY_PERIOD);
        }
    }

    @Deprecated
    public void unListenUserStatus(String user) {
        if (!qiscusCore.getEnableRealtime() || !qiscusCore.getStatusRealtimeEnableDisable()) {
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
        if (!qiscusCore.getEnableRealtime() || !qiscusCore.getStatusRealtimeEnableDisable()) {
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
        if (!qiscusCore.getEnableRealtime() || !qiscusCore.getStatusRealtimeEnableDisable()) {
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
        if (!qiscusCore.getEnableRealtime() || !qiscusCore.getStatusRealtimeEnableDisable()) {
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
        if (!qiscusCore.getEnableRealtime() || !qiscusCore.getStatusRealtimeEnableDisable()) {
            return;
        }

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
        if (!qiscusCore.getEnableRealtime() || !qiscusCore.getStatusRealtimeEnableDisable()) {
            return;
        }

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
        Observable.fromCallable(() -> qiscusCore.getDataStore().getChatRoom(roomId))
                .filter(room -> room != null)
                .flatMap(room -> qiscusCore.getApi().updateCommentStatus(roomId, commentId, 0))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aVoid -> {
                }, qiscusCore.getErrorLogger()::print);
    }

    @Deprecated
    public void setUserDelivery(long roomId, long commentId) {
        Observable.fromCallable(() -> qiscusCore.getDataStore().getChatRoom(roomId))
                .filter(room -> room != null)
                .filter(room -> !room.getType().equals("channel"))
                .flatMap(room -> qiscusCore.getApi().updateCommentStatus(roomId, 0, commentId))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aVoid -> {
                }, qiscusCore.getErrorLogger()::print);
    }

    public void markAsRead(long roomId, long commentId) {
        Observable.fromCallable(() -> qiscusCore.getDataStore().getChatRoom(roomId))
                .filter(room -> room != null)
                .flatMap(room -> qiscusCore.getApi().updateCommentStatus(roomId, commentId, 0))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aVoid -> {
                }, qiscusCore.getErrorLogger()::print);
    }

    public void markAsDelivered(long roomId, long commentId) {
        Observable.fromCallable(() -> qiscusCore.getDataStore().getChatRoom(roomId))
                .filter(room -> room != null)
                .filter(room -> !room.getType().equals("channel"))
                .flatMap(room -> qiscusCore.getApi().updateCommentStatus(roomId, 0, commentId))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aVoid -> {
                }, qiscusCore.getErrorLogger()::print);
    }

    @Deprecated
    public void setEvent(long roomId, JSONObject data) {
        if (!qiscusCore.getEnableRealtime() || !qiscusCore.getStatusRealtimeEnableDisable()) {
            return;
        }

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
        if (!qiscusCore.getEnableRealtime() || !qiscusCore.getStatusRealtimeEnableDisable()) {
            return;
        }

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
        if (!qiscusCore.getEnableRealtime() || !qiscusCore.getStatusRealtimeEnableDisable()) {
            return;
        }

        qiscusCore.getLogger().print(TAG, "Listening event...");
        fallbackListenEvent = () -> listenEvent(roomId);
        try {
            mqttAndroidClient.subscribe("r/" + roomId + "/" + roomId + "/e", 2);
        } catch (MqttException e) {
            //Do nothing
        } catch (NullPointerException | IllegalArgumentException e) {
            connect();
            scheduledListenEvent = qiscusCore.getAndroidUtil().runOnBackgroundThread(fallbackListenEvent, RETRY_PERIOD);
        }
    }

    public void subsribeCustomEvent(long roomId) {
        if (!qiscusCore.getEnableRealtime() || !qiscusCore.getStatusRealtimeEnableDisable()) {
            return;
        }
        
        qiscusCore.getLogger().print(TAG, "Listening event...");
        fallbackListenEvent = () -> subsribeCustomEvent(roomId);
        try {
            mqttAndroidClient.subscribe("r/" + roomId + "/" + roomId + "/e", 2);
        } catch (MqttException e) {
            //Do nothing
        } catch (NullPointerException | IllegalArgumentException e) {
            connect();
            scheduledListenEvent = qiscusCore.getAndroidUtil().runOnBackgroundThread(fallbackListenEvent, RETRY_PERIOD);
        }
    }

    @Deprecated
    public void unlistenEvent(long roomId) {
        if (!qiscusCore.getEnableRealtime() || !qiscusCore.getStatusRealtimeEnableDisable()) {
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
        if (!qiscusCore.getEnableRealtime() || !qiscusCore.getStatusRealtimeEnableDisable()) {
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
            if (!isConnected() && !connecting) {
                buildClient();
                connect();
            }
        } catch (NullPointerException e) {
            if (!isConnected() && !connecting) {
                buildClient();
                connect();
            }
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

        qiscusCore.getErrorLogger().print(TAG, "Lost connection, will try reconnect in "
                + RETRY_PERIOD + " ms");
        connecting = false;
        scheduledConnect = qiscusCore.getAndroidUtil().runOnBackgroundThread(fallbackConnect, RETRY_PERIOD);
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
                qiscusCore.getLogger().print(e.getMessage());
            }
        } else if (topic.equals(qAccount.getToken() + "/c")
                || (topic.startsWith(qiscusCore.getAppId()) && topic.endsWith("/c"))) {
            QMessage qMessage = jsonToComment(message);
            if (qMessage == null) {
                return;
            }
            handleReceivedComment(qMessage);
        } else if (topic.equals(qAccount.getToken() + "/update")
                || (topic.startsWith(qiscusCore.getAppId()) && topic.endsWith("/update"))) {
            QMessage qMessage = jsonToComment(message);
            if (qMessage == null) {
                return;
            }
            handleUpdateComment(qMessage);
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
                qiscusCore.getErrorLogger().print(e);
            }
        }
    }

    private JSONObject parseEventData(String message) {
        try {
            return new JSONObject(message);
        } catch (JSONException e) {
            qiscusCore.getErrorLogger().print(e);
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
            qiscusCore.setCacheMqttBrokerUrl(qiscusCore.getMqttBrokerUrl(), true);

            if (!qiscusCore.getEnableRealtime()) {
                disconnect();
                return;
            }

            qiscusCore.getLogger().print(TAG, "Connected..." + mqttAndroidClient.getClientId() + " " + qiscusCore.getMqttBrokerUrl());
            EventBus.getDefault().post(QiscusMqttStatusEvent.CONNECTED);
            reporting = true;
            try {
                connecting = false;
                reconnectCounter = 0;
                listenComment();
                listenNotification();
                if (fallBackListenRoom != null) {
                    scheduledListenRoom = qiscusCore.getAndroidUtil().runOnBackgroundThread(fallBackListenRoom);
                }
                if (fallBackListenUserStatus != null) {
                    scheduledListenUserStatus = qiscusCore.getAndroidUtil().runOnBackgroundThread(fallBackListenUserStatus);
                }
                if (scheduledConnect != null) {
                    scheduledConnect.cancel(true);
                    scheduledConnect = null;
                }
                scheduleUserStatus();
            } catch (NullPointerException e) {
                //ignored
                qiscusCore.getErrorLogger().print("QiscusPusherApi 2", "listen nullpointer: " + e);
            } catch (IllegalArgumentException e1) {
                //Do nothing
                qiscusCore.getErrorLogger().print("QiscusPusherApi 2", "listen nullpointer: " + e1);
                if (e1 != null) {
                    try {
                        qiscusCore.getLogger().print(TAG, "Connected..." + e1.toString());
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

    }

    @Override
    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
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
        qiscusCore.getErrorLogger().print(TAG, "Failure to connect, try again in " + RETRY_PERIOD + " ms");
        connecting = false;
        scheduledConnect = qiscusCore.getAndroidUtil().runOnBackgroundThread(fallbackConnect, RETRY_PERIOD);
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
        scheduledUserStatus = qiscusCore.getTaskExecutor()
                .scheduleWithFixedDelay(() -> {
                    if (qiscusCore.hasSetupUser()) {
                        if (qiscusCore.isOnForeground()) {
                            qiscusCore.getQiscusResendCommentHelper().tryResendPendingComment();
                        }
                        if (isConnected()) {
                            if (qiscusCore.isOnForeground()) {
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