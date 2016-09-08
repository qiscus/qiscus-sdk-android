package com.qiscus.sdk.data.remote;

import android.util.Log;
import android.util.Pair;

import com.github.pwittchen.reactivenetwork.library.ConnectivityStatus;
import com.github.pwittchen.reactivenetwork.library.ReactiveNetwork;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.pusher.client.Pusher;
import com.pusher.client.channel.ChannelEventListener;
import com.pusher.client.connection.ConnectionEventListener;
import com.pusher.client.connection.ConnectionState;
import com.pusher.client.connection.ConnectionStateChange;
import com.qiscus.sdk.Qiscus;
import com.qiscus.sdk.data.model.QiscusComment;
import com.qiscus.sdk.util.QiscusDateUtil;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Action2;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

public enum QiscusPusherApi implements ConnectionEventListener {
    INSTANCE;
    private static final String TAG = QiscusPusherApi.class.getSimpleName();

    private final Pusher pusher;
    private final Gson gson;

    private final Map<String, PublishSubject<Pair<RoomEvent, JsonObject>>> roomPublishSubjects;
    private final PublishSubject<Pair<UserEvent, JsonObject>> userPublishSubject;
    private final PublishSubject<Pair<TokenEvent, JsonObject>> tokenPublishSubject;
    private final PublishSubject<Pair<ChannelEvent, JsonObject>> channelPublishSubject;

    private final Set<String> willBeSubscribedChannel = new HashSet<>();
    private final Set<String> subscribedChannels = new HashSet<>();

    private static final Map<String, RoomEvent> ROOM_EVENTS = new HashMap<>();
    private static final Map<String, UserEvent> USER_EVENTS = new HashMap<>();
    private static final Map<String, TokenEvent> TOKEN_EVENTS = new HashMap<>();
    private static final Map<String, ChannelEvent> CHANNEL_EVENTS = new HashMap<>();

    static {
        ROOM_EVENTS.put("postcomment", RoomEvent.COMMENT_POSTED);
        ROOM_EVENTS.put("delete", RoomEvent.COMMENT_DELETED);
        ROOM_EVENTS.put("newtopic", RoomEvent.TOPIC_CREATED);
        ROOM_EVENTS.put("deleteTopic", RoomEvent.TOPIC_DELETED);
        ROOM_EVENTS.put("newPeople", RoomEvent.PARTICIPANT_JOINED);

        USER_EVENTS.put("newRoom", UserEvent.ROOM_JOINED);
        USER_EVENTS.put("deleteRoom", UserEvent.ROOM_DELETED);
        USER_EVENTS.put("clearNotifTopic", UserEvent.TOPIC_READ);
        USER_EVENTS.put("newmessage", UserEvent.INCOMING_COMMENT);

        TOKEN_EVENTS.put("callnewmessage", TokenEvent.BEING_CALLED);
        TOKEN_EVENTS.put("postcomment", TokenEvent.COMMENT_POSTED);

        CHANNEL_EVENTS.put("call_ringing", ChannelEvent.CALLEE_RINGED);
        CHANNEL_EVENTS.put("call_reject", ChannelEvent.CALLEE_REJECTED);
    }

    QiscusPusherApi() {
        pusher = new Pusher(Qiscus.getQiscusAccount().getRtKey());
        gson = new GsonBuilder().create();

        roomPublishSubjects = new HashMap<>();
        userPublishSubject = PublishSubject.create();
        tokenPublishSubject = PublishSubject.create();
        channelPublishSubject = PublishSubject.create();

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

    @Override
    public void onConnectionStateChange(ConnectionStateChange connectionStateChange) {
        switch (connectionStateChange.getCurrentState()) {
            case CONNECTING:
                Log.d(TAG, "Connecting pusher....");
                break;
            case CONNECTED:
                Log.d(TAG, "Pusher connected....");
                break;
            case DISCONNECTING:
                Log.d(TAG, "Disconnecting pusher....");
                break;
            case DISCONNECTED:
                Log.d(TAG, "Pusher disconnected....");
                break;
        }
    }

    @Override
    public void onError(String s, String s1, Exception e) {
        if (e != null) {
            e.printStackTrace();
        }
        Log.e(TAG, "Failed to connect pusher...");
        Log.d(TAG, "Reconnecting pusher...");
        connectPusherIfNeeded();
    }

    public Observable<Pair<UserEvent, JsonObject>> getUserEvents(final String userCode) {
        return startSubscribeUser(userCode);
    }

    public Observable<Pair<TokenEvent, JsonObject>> getTokenEvents(final String tokenCode) {
        return startSubscribeToken(tokenCode);
    }

    public Observable<Pair<RoomEvent, JsonObject>> getRoomEvents(final String roomCode) {
        return startSubscribeRoom(roomCode);
    }

    public Observable<Pair<ChannelEvent, JsonObject>> getChannelEvents(String channelCode) {
        return startSubscribeChannel(channelCode);
    }

    private Observable<Pair<UserEvent, JsonObject>> startSubscribeUser(String userCode) {
        String[] userEventNames = USER_EVENTS.keySet().toArray(new String[USER_EVENTS.size()]);
        startSubscribeIfNeeded(userCode, userEventNames, (event, data) -> {
            UserEvent userEvent = USER_EVENTS.get(event);
            JsonObject dataEvent = gson.fromJson(data, JsonObject.class);

            userPublishSubject.onNext(new Pair<>(userEvent, dataEvent));
        });

        return userPublishSubject.asObservable();
    }

    private Observable<Pair<TokenEvent, JsonObject>> startSubscribeToken(String tokenCode) {
        String[] tokenEventNames = TOKEN_EVENTS.keySet().toArray(new String[TOKEN_EVENTS.size()]);
        startSubscribeIfNeeded(tokenCode, tokenEventNames, (event, data) -> {
            TokenEvent tokenEvent = TOKEN_EVENTS.get(event);
            JsonObject dataEvent = gson.fromJson(data, JsonObject.class);

            tokenPublishSubject.onNext(new Pair<>(tokenEvent, dataEvent));
        });

        return tokenPublishSubject.asObservable();
    }

    private Observable<Pair<RoomEvent, JsonObject>> startSubscribeRoom(final String roomCode) {
        PublishSubject roomPublishSubject = roomPublishSubjects.get(roomCode);
        if (roomPublishSubject == null) {
            roomPublishSubject = PublishSubject.create();
            roomPublishSubjects.put(roomCode, roomPublishSubject);
        }

        String[] roomEventNames = ROOM_EVENTS.keySet().toArray(new String[ROOM_EVENTS.size()]);
        final PublishSubject finalRoomPublishSubject = roomPublishSubject;
        startSubscribeIfNeeded(roomCode, roomEventNames, (event, data) -> {
            RoomEvent roomEvent = ROOM_EVENTS.get(event);
            JsonObject dataEvent = gson.fromJson(data, JsonObject.class);
            finalRoomPublishSubject.onNext(new Pair<>(roomEvent, dataEvent));
        });

        return finalRoomPublishSubject.asObservable();
    }

    private Observable<Pair<ChannelEvent, JsonObject>> startSubscribeChannel(String channelCode) {
        Log.d(TAG, "Pusher subscribe channel: " + channelCode);
        String[] channelEventNames = CHANNEL_EVENTS.keySet().toArray(new String[CHANNEL_EVENTS.size()]);
        startSubscribeIfNeeded(channelCode, channelEventNames, (event, data) -> {
            ChannelEvent channelEvent = CHANNEL_EVENTS.get(event);
            JsonObject dataEvent = gson.fromJson(data, JsonObject.class);

            channelPublishSubject.onNext(new Pair<>(channelEvent, dataEvent));
        });

        return channelPublishSubject.asObservable();
    }

    private void startSubscribeIfNeeded(String channelName, String[] eventNames,
                                        final Action2<String, String> eventCallback) {
        connectPusherIfNeeded();

        if (subscribedChannels.contains(channelName)) {
            return;
        }

        if (willBeSubscribedChannel.contains(channelName)) {
            return;
        }
        willBeSubscribedChannel.add(channelName);

        pusher.subscribe(channelName, new ChannelEventListener() {
            @Override
            public void onSubscriptionSucceeded(String channel) {
                willBeSubscribedChannel.remove(channel);
                subscribedChannels.add(channel);
            }

            @Override
            public void onEvent(String channel, String event, String data) {
                eventCallback.call(event, data);
            }
        }, eventNames);
    }

    private void connectPusherIfNeeded() {
        Log.d(TAG, "Connect pusher if needed....");
        ConnectionState pusherConnectionState = pusher.getConnection().getState();
        if (!(pusherConnectionState.equals(ConnectionState.CONNECTED) ||
                pusherConnectionState.equals(ConnectionState.CONNECTING))) {
            pusher.connect(this);
        } else {
            Log.d(TAG, "Pusher was connected....");
        }
    }

    public static QiscusComment jsonToComment(JsonObject jsonObject) {
        Log.d(TAG, "Got comment: " + jsonObject);
        try {
            QiscusComment qiscusComment = new QiscusComment();
            qiscusComment.setId(jsonObject.get("comment_id").getAsInt());
            qiscusComment.setTopicId(jsonObject.get("topic_id").getAsInt());
            qiscusComment.setRoomId(jsonObject.get("room_id").getAsInt());
            qiscusComment.setUniqueId(jsonObject.get("unique_id").getAsString());
            qiscusComment.setCommentBeforeId(jsonObject.get("comment_before_id").getAsInt());
            qiscusComment.setMessage(jsonObject.get("qiscusComment").getAsString());
            qiscusComment.setSender(jsonObject.get("username").isJsonNull() ? null : jsonObject.get("username").getAsString());
            qiscusComment.setSenderEmail(jsonObject.get("username_real").getAsString());
            qiscusComment.setTime(QiscusDateUtil.parseIsoFormat(jsonObject.get("created_at").getAsString()));
            return qiscusComment;
        } catch (Exception e) {
            e.printStackTrace();
        }
        throw new RuntimeException("Unable to parse the JSON QiscusComment");
    }

    public enum RoomEvent {
        COMMENT_POSTED, COMMENT_DELETED, TOPIC_CREATED, TOPIC_DELETED, PARTICIPANT_JOINED
    }

    public enum UserEvent {
        ROOM_JOINED, ROOM_DELETED, TOPIC_READ, INCOMING_COMMENT
    }

    public enum TokenEvent {
        BEING_CALLED, COMMENT_POSTED
    }

    public enum ChannelEvent {
        CALLEE_RINGED, CALLEE_REJECTED
    }
}
