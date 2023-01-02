package com.qiscus.sdk.chat.core.data.model;

import static org.junit.Assert.*;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

public class QiscusChannelsTest {

    QiscusChannels qiscusChannels;
    @Before
    public void setUp() throws Exception {
        qiscusChannels = new QiscusChannels();
    }

    @Test
    public void testToString() {
        setAvatarUrl();
        setCreatedAt();
        setExtras();
        setJoined();
        setName();
        setUniqueId();

        qiscusChannels.toString();
    }

    @Test
    public void testEquals() {
    }

    @Test
    public void testHashCode() {
        setAvatarUrl();
        setCreatedAt();
        setExtras();
        setJoined();
        setName();
        setUniqueId();
        setRoomId();
        qiscusChannels.hashCode();
        qiscusChannels.equals(qiscusChannels);
    }

    @Test
    public void testHashCode2() {
        qiscusChannels.setAvatarUrl(null);
        qiscusChannels.setCreatedAt(null);
        qiscusChannels.setExtras(null);
        qiscusChannels.setJoined(null);
        qiscusChannels.setName(null);
        qiscusChannels.setUniqueId(null);
        qiscusChannels.setRoomId(null);

        qiscusChannels.hashCode();
    }

    @Test
    public void getAvatarUrl() {
        qiscusChannels.getAvatarUrl();
    }

    @Test
    public void setAvatarUrl() {
        qiscusChannels.setAvatarUrl("https://");
    }

    @Test
    public void getCreatedAt() {
        qiscusChannels.getCreatedAt();
    }

    @Test
    public void setCreatedAt() {
        qiscusChannels.setCreatedAt("1234");
    }

    @Test
    public void getExtras() {
        qiscusChannels.getExtras();
    }

    @Test
    public void setExtras() {
        String json_string = "{\n" +
                "  \"title\":\"JSONParserTutorial\",\n" +
                "  \"array\":[\n" +
                "    {\n" +
                "    \"company\":\"Google\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"company\":\"Facebook\"\n" +
                "    },\n" +
                "    {\n" +
                "    \"company\":\"LinkedIn\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"company\" : \"Microsoft\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"company\": \"Apple\"\n" +
                "    }\n" +
                "    ],\n" +
                "    \"nested\":{\n" +
                "    \"flag\": true,\n" +
                "    \"random_number\":1\n" +
                "    }\n" +
                "}";
        try {
            qiscusChannels.setExtras(new JSONObject(json_string));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void getJoined() {
        qiscusChannels.getJoined();
    }

    @Test
    public void setJoined() {
        qiscusChannels.setJoined(true);
    }

    @Test
    public void getName() {
        qiscusChannels.getName();
    }

    @Test
    public void setName() {
        qiscusChannels.setName("name");
    }

    @Test
    public void getUniqueId() {
        qiscusChannels.getUniqueId();
    }

    @Test
    public void setUniqueId() {
        qiscusChannels.setUniqueId("123avc");
    }

    @Test
    public void getRoomId() {
        qiscusChannels.getRoomId();
    }

    @Test
    public void setRoomId() {
        qiscusChannels.setRoomId(1L);
    }
}