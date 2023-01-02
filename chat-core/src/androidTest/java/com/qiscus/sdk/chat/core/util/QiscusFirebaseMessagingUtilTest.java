package com.qiscus.sdk.chat.core.util;

import android.os.Bundle;

import com.google.firebase.messaging.RemoteMessage;
import com.qiscus.sdk.chat.core.InstrumentationBaseTest;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class QiscusFirebaseMessagingUtilTest extends InstrumentationBaseTest {

    String jsonString = "{"
            + "\"id\": 0,"
            + "\"room_id\": 0,"
            + "\"unique_temp_id\": 02345678,"
            + "\"comment_before_id\": 123,"
            + "\"message\": \"msg\","
            + "\"email\": \"mail@mail.com\","
            + "\"user_avatar\": \"avatar\","
            + "\"unix_nano_timestamp\": 1000"
            + " }";

    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    @Override
    public void setupEngine() {
        Bundle b = new Bundle();
        b.putString("payload", "");
        b.putString("no_qiscus_sdk", "post_comment");
        RemoteMessage remoteMessage = new RemoteMessage(b);

        QiscusFirebaseMessagingUtil.handleMessageReceived(
                remoteMessage
        );
        super.setupEngine();
    }

    @Test
    public void postTest() {
        Bundle b = new Bundle();
        b.putString("payload", jsonString);
        b.putString("qiscus_sdk", "post_comment");
        RemoteMessage remoteMessage = new RemoteMessage(b);

        QiscusFirebaseMessagingUtil.handleMessageReceived(
                remoteMessage
        );
    }

    @Test
    public void deleteTest() {
        Bundle b = new Bundle();
        b.putString("payload", jsonString);
        b.putString("qiscus_sdk", "delete_message");
        RemoteMessage remoteMessage = new RemoteMessage(b);

        QiscusFirebaseMessagingUtil.handleMessageReceived(
                remoteMessage
        );
    }

    @Test
    public void clearTest() {
        Bundle b = new Bundle();
        b.putString("payload", jsonString);
        b.putString("qiscus_sdk", "clear_room");
        RemoteMessage remoteMessage = new RemoteMessage(b);

        QiscusFirebaseMessagingUtil.handleMessageReceived(
                remoteMessage
        );
    }

    @Test
    public void postNosdkTest() {
        Bundle b = new Bundle();
        b.putString("payload", "");
        b.putString("no_qiscus_sdk", "post_comment");
        RemoteMessage remoteMessage = new RemoteMessage(b);

        QiscusFirebaseMessagingUtil.handleMessageReceived(
                remoteMessage
        );
    }

    @Test
    public void postErrorTest() {
        Bundle b = new Bundle();
        b.putString("payload", "");
        b.putString("qiscus_sdk", "post_comment");
        RemoteMessage remoteMessage = new RemoteMessage(b);

        QiscusFirebaseMessagingUtil.handleMessageReceived(
                remoteMessage
        );
    }

    @Test
    public void deleteErrorTest() {
        Bundle b = new Bundle();
        b.putString("payload", "");
        b.putString("qiscus_sdk", "delete_message");
        RemoteMessage remoteMessage = new RemoteMessage(b);

        QiscusFirebaseMessagingUtil.handleMessageReceived(
                remoteMessage
        );
    }

    @Test
    public void clearErrorTest() {
        Bundle b = new Bundle();
        b.putString("payload", "");
        b.putString("qiscus_sdk", "clear_room");
        RemoteMessage remoteMessage = new RemoteMessage(b);

        QiscusFirebaseMessagingUtil.handleMessageReceived(
                remoteMessage
        );
    }
}