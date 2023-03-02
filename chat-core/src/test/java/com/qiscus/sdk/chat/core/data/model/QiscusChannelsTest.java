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


    @Test
    public void testEqual(){
       QiscusChannels qiscusChannels = new QiscusChannels();
       qiscusChannels.setAvatarUrl("https://");
       qiscusChannels.setRoomId(1L);
       qiscusChannels.setUniqueId("123avc");
       qiscusChannels.setName("name");
       qiscusChannels.setJoined(true);

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

        qiscusChannels.setCreatedAt("1234");
        qiscusChannels.setAvatarUrl("https://");



        QiscusChannels qiscusChannels2 = new QiscusChannels();
        qiscusChannels2.setAvatarUrl("https://qiscus.com");
        qiscusChannels2.setRoomId(1L);
        qiscusChannels2.setUniqueId("123avca");
        qiscusChannels2.setName("named");
        qiscusChannels2.setJoined(true);

        String json_string2 = "{\n" +
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
            qiscusChannels2.setExtras(new JSONObject(json_string2));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        qiscusChannels2.setCreatedAt("123425");
        qiscusChannels2.setAvatarUrl("https://qiscus.com");



        QiscusChannels qiscusChannels3 = new QiscusChannels();
        qiscusChannels3.setAvatarUrl(null);
        qiscusChannels3.setRoomId(null);
        qiscusChannels3.setUniqueId(null);
        qiscusChannels3.setName(null);
        qiscusChannels3.setJoined(null);
        qiscusChannels3.setExtras(null);
        qiscusChannels3.setCreatedAt(null);
        qiscusChannels3.setAvatarUrl(null);


        QiscusChannels qiscusChannels4 = new QiscusChannels();
        qiscusChannels4.setAvatarUrl("https://");
        qiscusChannels4.setRoomId(1L);
        qiscusChannels4.setUniqueId("123avc");
        qiscusChannels4.setName("name");
        qiscusChannels4.setJoined(true);

        String json_string4 = "{\n" +
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
            qiscusChannels4.setExtras(new JSONObject(json_string4));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        qiscusChannels4.setCreatedAt("1234");
        qiscusChannels4.setAvatarUrl("https://");

        qiscusChannels.equals(qiscusChannels3);
        qiscusChannels.equals(qiscusChannels2);
        qiscusChannels.equals(qiscusChannels);
        qiscusChannels.equals(qiscusChannels4);


    }
}