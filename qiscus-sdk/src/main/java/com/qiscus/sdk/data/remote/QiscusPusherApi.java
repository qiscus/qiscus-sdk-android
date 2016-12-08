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

package com.qiscus.sdk.data.remote;

import android.os.Handler;
import android.provider.Settings;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.qiscus.sdk.Qiscus;
import com.qiscus.sdk.data.model.QiscusAccount;
import com.qiscus.sdk.data.model.QiscusChatRoom;
import com.qiscus.sdk.data.model.QiscusComment;
import com.qiscus.sdk.event.QiscusChatRoomEvent;
import com.qiscus.sdk.event.QiscusCommentReceivedEvent;
import com.qiscus.sdk.event.QiscusUserEvent;
import com.qiscus.sdk.event.QiscusUserStatusEvent;
import com.qiscus.sdk.util.QiscusDateUtil;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.Calendar;
import java.util.TimeZone;

public enum QiscusPusherApi implements MqttCallback, IMqttActionListener {

    INSTANCE;
    private static final String TAG = QiscusPusherApi.class.getSimpleName();
    private static final int RETRY_PERIOD = 2000;

    private final Gson gson;
    private final MqttAndroidClient mqttAndroidClient;
    private QiscusAccount qiscusAccount;
    private final Handler handler;
    private Runnable fallbackConnect = this::connect;
    private Runnable fallBackListenComment = this::listenComment;
    private Runnable fallBackListenRoom;
    private Runnable fallBackListenUserStatus;
    private boolean connecting;

    QiscusPusherApi() {
        Log.i("QiscusPusherApi", "Creating...");
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

        gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").create();
        String clientId = Qiscus.getApps().getPackageName() + "-";
        clientId += Settings.Secure.getString(Qiscus.getApps().getContentResolver(), Settings.Secure.ANDROID_ID);
        String serverUri = "ssl://52.77.234.57:1885";
        mqttAndroidClient = new MqttAndroidClient(Qiscus.getApps().getApplicationContext(), serverUri, clientId);
        mqttAndroidClient.setCallback(this);
        handler = new Handler();
        connecting = false;

        if (Qiscus.hasSetupUser()) {
            connect();
        }
    }

    public static QiscusPusherApi getInstance() {
        return INSTANCE;
    }

    public void connect() {
        if (Qiscus.hasSetupUser() && !connecting) {
            Log.i(TAG, "Connecting...");
            connecting = true;
            qiscusAccount = Qiscus.getQiscusAccount();
            MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
            mqttConnectOptions.setAutomaticReconnect(false);
            mqttConnectOptions.setCleanSession(false);
            mqttConnectOptions.setWill("u/" + qiscusAccount.getEmail()
                    + "/s", ("0:" + Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTimeInMillis())
                    .getBytes(), 2, true);
            try {
                mqttAndroidClient.connect(mqttConnectOptions, null, this);
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }

    public void disconnect() {
        setUserStatus("0:" + Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTimeInMillis());
        try {
            connecting = false;
            mqttAndroidClient.disconnect();
        } catch (MqttException | NullPointerException e) {
            e.printStackTrace();
        }
    }

    private void listenComment() {
        Log.i(TAG, "Listening comment...");
        try {
            mqttAndroidClient.subscribe(qiscusAccount.getToken() + "/c", 2);
        } catch (MqttException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            Log.e(TAG, "Failure listen comment, try again in " + RETRY_PERIOD + " ms");
            connect();
            handler.postDelayed(fallBackListenComment, RETRY_PERIOD);
        }
    }

    public void listenRoom(QiscusChatRoom qiscusChatRoom) {
        Log.i(TAG, "Listening room...");
        fallBackListenRoom = () -> listenRoom(qiscusChatRoom);
        try {
            int roomId = qiscusChatRoom.getId();
            mqttAndroidClient.subscribe("r/" + roomId + "/+/+/t", 2);
            mqttAndroidClient.subscribe("r/" + roomId + "/+/+/d", 2);
            mqttAndroidClient.subscribe("r/" + roomId + "/+/+/r", 2);
        } catch (MqttException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            Log.e(TAG, "Failure listen room, try again in " + RETRY_PERIOD + " ms");
            connect();
            handler.postDelayed(fallBackListenRoom, RETRY_PERIOD);
        }
    }

    public void unListenRoom(QiscusChatRoom qiscusChatRoom) {
        try {
            int roomId = qiscusChatRoom.getId();
            mqttAndroidClient.unsubscribe("r/" + roomId + "/+/+/t");
            mqttAndroidClient.unsubscribe("r/" + roomId + "/+/+/d");
            mqttAndroidClient.unsubscribe("r/" + roomId + "/+/+/r");
        } catch (MqttException | NullPointerException e) {
            e.printStackTrace();
        }
        handler.removeCallbacks(fallBackListenRoom);
        fallBackListenRoom = null;
    }

    public void listenUserStatus(String user) {
        fallBackListenUserStatus = () -> listenUserStatus(user);
        try {
            mqttAndroidClient.subscribe("u/" + user + "/s", 2);
        } catch (MqttException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            connect();
            handler.postDelayed(fallBackListenUserStatus, RETRY_PERIOD);
        }
    }

    public void unListenUserStatus(String user) {
        try {
            mqttAndroidClient.unsubscribe("u/" + user + "/s");
        } catch (MqttException | NullPointerException e) {
            e.printStackTrace();
        }
        handler.removeCallbacks(fallBackListenUserStatus);
        fallBackListenUserStatus = null;
    }

    private void setUserStatus(String status) {
        if (!mqttAndroidClient.isConnected()) {
            connect();
        }
        try {
            MqttMessage message = new MqttMessage();
            message.setPayload(status.getBytes());
            message.setQos(2);
            message.setRetained(true);
            mqttAndroidClient.publish("u/" + qiscusAccount.getEmail() + "/s", message);
        } catch (MqttException | NullPointerException e) {
            e.printStackTrace();
        }
    }

    public void setUserTyping(int roomId, int topicId, boolean typing) {
        if (!mqttAndroidClient.isConnected()) {
            connect();
        }
        try {
            MqttMessage message = new MqttMessage();
            message.setPayload((typing ? "1" : "0").getBytes());
            mqttAndroidClient.publish("r/" + roomId + "/" + topicId + "/" + qiscusAccount.getEmail() + "/t", message);
        } catch (MqttException | NullPointerException e) {
            e.printStackTrace();
        }
    }


    public void setUserRead(int roomId, int topicId, int commentId, String commentUniqueId) {
        if (!mqttAndroidClient.isConnected()) {
            connect();
        }
        try {
            MqttMessage message = new MqttMessage();
            message.setPayload((commentId + ":" + commentUniqueId).getBytes());
            message.setQos(1);
            message.setRetained(true);
            mqttAndroidClient.publish("r/" + roomId + "/" + topicId + "/" + qiscusAccount.getEmail() + "/r", message);
        } catch (MqttException | NullPointerException e) {
            e.printStackTrace();
        }
    }

    public void setUserDelivery(int roomId, int topicId, int commentId, String commentUniqueId) {
        if (!mqttAndroidClient.isConnected()) {
            connect();
        }
        try {
            MqttMessage message = new MqttMessage();
            message.setPayload((commentId + ":" + commentUniqueId).getBytes());
            message.setQos(1);
            message.setRetained(true);
            mqttAndroidClient.publish("r/" + roomId + "/" + topicId + "/" + qiscusAccount.getEmail() + "/d", message);
        } catch (MqttException | NullPointerException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void connectionLost(Throwable cause) {
        Log.e(TAG, "Lost connection, will try reconnect in " + RETRY_PERIOD + " ms");
        connecting = false;
        if (cause != null) {
            cause.printStackTrace();
        }
        handler.postDelayed(fallbackConnect, RETRY_PERIOD);
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        if (topic.contains(qiscusAccount.getToken())) {
            QiscusComment qiscusComment = jsonToComment(gson.fromJson(new String(message.getPayload()), JsonObject.class));
            if (!qiscusComment.getSenderEmail().equals(qiscusAccount.getEmail())) {
                setUserDelivery(qiscusComment.getRoomId(), qiscusComment.getTopicId(), qiscusComment.getId(), qiscusComment.getUniqueId());
            }
            EventBus.getDefault().post(new QiscusCommentReceivedEvent(qiscusComment));
        } else if (topic.startsWith("r/") && topic.endsWith("/t")) {
            String data[] = topic.split("/");
            if (!data[3].equals(qiscusAccount.getEmail())) {
                QiscusChatRoomEvent event = new QiscusChatRoomEvent()
                        .setRoomId(Integer.parseInt(data[1]))
                        .setTopicId(Integer.parseInt(data[2]))
                        .setUser(data[3])
                        .setEvent(QiscusChatRoomEvent.Event.TYPING)
                        .setTyping("1".equals(new String(message.getPayload())));
                EventBus.getDefault().post(event);
            }
        } else if (topic.startsWith("r/") && topic.endsWith("/d")) {
            String data[] = topic.split("/");
            if (!data[3].equals(qiscusAccount.getEmail())) {
                String payload[] = new String(message.getPayload()).split(":");
                QiscusChatRoomEvent event = new QiscusChatRoomEvent()
                        .setRoomId(Integer.parseInt(data[1]))
                        .setTopicId(Integer.parseInt(data[2]))
                        .setUser(data[3])
                        .setEvent(QiscusChatRoomEvent.Event.DELIVERED)
                        .setCommentId(Integer.parseInt(payload[0]))
                        .setCommentUniqueId(payload[1]);
                EventBus.getDefault().post(event);
            }
        } else if (topic.startsWith("r/") && topic.endsWith("/r")) {
            String data[] = topic.split("/");
            if (!data[3].equals(qiscusAccount.getEmail())) {
                String payload[] = new String(message.getPayload()).split(":");
                QiscusChatRoomEvent event = new QiscusChatRoomEvent()
                        .setRoomId(Integer.parseInt(data[1]))
                        .setTopicId(Integer.parseInt(data[2]))
                        .setUser(data[3])
                        .setEvent(QiscusChatRoomEvent.Event.READ)
                        .setCommentId(Integer.parseInt(payload[0]))
                        .setCommentUniqueId(payload[1]);
                EventBus.getDefault().post(event);
            }
        } else if (topic.startsWith("u/") && topic.endsWith("/s")) {
            String data[] = topic.split("/");
            if (!data[1].equals(qiscusAccount.getEmail())) {
                String status[] = new String(message.getPayload()).split(":");
                Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                calendar.setTimeInMillis(Long.parseLong(status[1]));
                QiscusUserStatusEvent event = new QiscusUserStatusEvent(data[1], "1".equals(status[0]),
                        calendar.getTime());
                EventBus.getDefault().post(event);
            }
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {

    }

    @Override
    public void onSuccess(IMqttToken asyncActionToken) {
        Log.i(TAG, "Connected...");
        connecting = false;
        DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
        disconnectedBufferOptions.setBufferEnabled(true);
        disconnectedBufferOptions.setBufferSize(100);
        disconnectedBufferOptions.setPersistBuffer(false);
        disconnectedBufferOptions.setDeleteOldestMessages(false);
        mqttAndroidClient.setBufferOpts(disconnectedBufferOptions);
        setUserStatus("1:" + Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTimeInMillis());
        listenComment();
        if (fallBackListenRoom != null) {
            handler.post(fallBackListenRoom);
        }
        if (fallBackListenUserStatus != null) {
            handler.post(fallBackListenUserStatus);
        }
    }

    @Override
    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
        Log.e(TAG, "Failure to connect, try again in " + RETRY_PERIOD + " ms");
        if (exception != null) {
            exception.printStackTrace();
        }
        connecting = false;
        handler.postDelayed(fallbackConnect, RETRY_PERIOD);
    }

    @Subscribe
    public void onUserEvent(QiscusUserEvent userEvent) {
        switch (userEvent) {
            case LOGIN:
                connect();
                break;
            case LOGOUT:
                disconnect();
                break;
        }
    }

    private static QiscusComment jsonToComment(JsonObject jsonObject) {
        try {
            QiscusComment qiscusComment = new QiscusComment();
            qiscusComment.setId(jsonObject.get("id").getAsInt());
            qiscusComment.setTopicId(jsonObject.get("topic_id").getAsInt());
            qiscusComment.setRoomId(jsonObject.get("room_id").getAsInt());
            qiscusComment.setUniqueId(jsonObject.get("unique_temp_id").getAsString());
            qiscusComment.setCommentBeforeId(jsonObject.get("comment_before_id").getAsInt());
            qiscusComment.setMessage(jsonObject.get("message").getAsString());
            qiscusComment.setSender(jsonObject.get("username").isJsonNull() ? null : jsonObject.get("username").getAsString());
            qiscusComment.setSenderEmail(jsonObject.get("email").getAsString());
            qiscusComment.setSenderAvatar(jsonObject.get("user_avatar").getAsString());
            qiscusComment.setTime(QiscusDateUtil.parseIsoFormat(jsonObject.get("created_at").getAsString()));
            qiscusComment.setState(QiscusComment.STATE_ON_QISCUS);
            qiscusComment.setRoomName(jsonObject.get("room_name").isJsonNull() ? qiscusComment.getSender() : jsonObject.get("room_name").getAsString());
            qiscusComment.setGroupMessage(!"single".equals(jsonObject.get("chat_type").getAsString()));
            return qiscusComment;
        } catch (Exception e) {
            e.printStackTrace();
        }
        throw new RuntimeException("Unable to parse the JSON QiscusComment");
    }
}
