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

import android.util.Log;

import com.github.pwittchen.reactivenetwork.library.ConnectivityStatus;
import com.github.pwittchen.reactivenetwork.library.ReactiveNetwork;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.pusher.client.Pusher;
import com.pusher.client.channel.Channel;
import com.pusher.client.connection.ConnectionEventListener;
import com.pusher.client.connection.ConnectionState;
import com.pusher.client.connection.ConnectionStateChange;
import com.qiscus.sdk.Qiscus;
import com.qiscus.sdk.data.model.QiscusComment;
import com.qiscus.sdk.util.QiscusDateUtil;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public enum QiscusPusherApi implements ConnectionEventListener {
    INSTANCE;
    private static final String TAG = QiscusPusherApi.class.getSimpleName();

    private final Pusher pusher;
    private final Gson gson;
    private final Channel channel;

    QiscusPusherApi() {
        pusher = new Pusher(Qiscus.getQiscusAccount().getRtKey());
        gson = new Gson();

        connectPusherIfNeeded();
        channel = pusher.subscribe(Qiscus.getToken());

        new ReactiveNetwork().observeNetworkConnectivity(Qiscus.getApps())
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<ConnectivityStatus>() {
                    @Override
                    public void call(ConnectivityStatus status) {
                        Log.d(TAG, "Network state changed to: " + status);
                        switch (status) {
                            case MOBILE_CONNECTED:
                            case WIFI_CONNECTED:
                                connectPusherIfNeeded();
                                break;
                        }
                    }
                }, Throwable::printStackTrace);
    }

    public static QiscusPusherApi getInstance() {
        return INSTANCE;
    }

    private void connectPusherIfNeeded() {
        Log.d(TAG, "Connect pusher if needed....");
        ConnectionState pusherConnectionState = pusher.getConnection().getState();
        if (!(pusherConnectionState.equals(ConnectionState.CONNECTED) ||
                pusherConnectionState.equals(ConnectionState.CONNECTING))) {
            pusher.connect(this, ConnectionState.ALL);
        } else {
            Log.d(TAG, "Pusher was connected....");
        }
    }


    @Override
    public void onConnectionStateChange(ConnectionStateChange connectionStateChange) {
        switch (connectionStateChange.getCurrentState()) {
            case CONNECTING:
                Log.d(TAG, "Connecting qiscus pusher....");
                break;
            case CONNECTED:
                Log.d(TAG, "Qiscus pusher connected....");
                break;
            case DISCONNECTING:
                Log.d(TAG, "Disconnecting qiscus pusher....");
                break;
            case DISCONNECTED:
                Log.d(TAG, "Qiscus pusher disconnected....");
                break;
        }
    }

    @Override
    public void onError(String s, String s1, Exception e) {
        if (e != null) {
            e.printStackTrace();
        }
        Log.e(TAG, "Failed to connect qiscus pusher...");
        connectPusherIfNeeded();
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
            qiscusComment.setTime(QiscusDateUtil.parseIsoFormat(jsonObject.get("created_at").getAsString()));
            return qiscusComment;
        } catch (Exception e) {
            e.printStackTrace();
        }
        throw new RuntimeException("Unable to parse the JSON QiscusComment");
    }

    public Observable<QiscusComment> listenNewComment() {
        connectPusherIfNeeded();
        return Observable.create(subscriber -> {
            channel.bind("newmessage", (channel, event, data) -> {
                if ("newmessage".equals(event)) {
                    subscriber.onNext(jsonToComment(gson.fromJson(data, JsonObject.class)));
                }
            });
        });
    }

}
