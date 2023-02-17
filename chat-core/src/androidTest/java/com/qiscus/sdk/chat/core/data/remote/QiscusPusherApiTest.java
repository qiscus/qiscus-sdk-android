package com.qiscus.sdk.chat.core.data.remote;

import static org.junit.Assert.*;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.qiscus.sdk.chat.core.InstrumentationBaseTest;
import com.qiscus.sdk.chat.core.QiscusCore;
import com.qiscus.sdk.chat.core.data.model.QiscusChatRoom;
import com.qiscus.sdk.chat.core.data.model.QiscusComment;

import junit.framework.TestCase;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

public class QiscusPusherApiTest extends InstrumentationBaseTest {

    @Before
    public void setUp() throws Exception {
        super.setUp();
        super.setupEngine();
    }

    @Test
    public void handleUpdateComment(){
        QiscusComment qiscusComment = QiscusComment.generateMessage(roomId, "test");
        QiscusPusherApi.getInstance().handleUpdateComment(qiscusComment);
    }

    @Test
    public void handleUpdateComment2(){
        QiscusComment qiscusComment = QiscusComment.generateMessage(roomId, "test");
        QiscusCore.getDataStore().addOrUpdate(qiscusComment);
        QiscusPusherApi.getInstance().handleUpdateComment(qiscusComment);
    }

    @Test
    public void handleUpdateComment3(){
        QiscusComment qiscusComment = QiscusComment.generateMessage(roomId, "test");
        QiscusCore.getDataStore().addOrUpdate(qiscusComment);
        QiscusPusherApi.getInstance().handleComment(qiscusComment, false);
    }

    @Test
    public void handleUpdateComment4(){
        QiscusComment qiscusComment = QiscusComment.generateMessage(roomId, "test2");
        qiscusComment.setSenderEmail("arief95");
        qiscusComment.setSender("arief95");
        QiscusCore.getDataStore().addOrUpdate(qiscusComment);
        QiscusPusherApi.getInstance().handleComment(qiscusComment, false);
    }

    @Test
    public void handlePushNotificationClearRoom(){
        String json = "{\n" +
                "            \"id\": 1518505913641786359,\n" +
                "            \"action_topic\": \"clear_room\",\n" +
                "            \"payload\": {\n" +
                "                \"actor\": {\n" +
                "                    \"id\": \"144\",\n" +
                "                    \"email\": \"userid_108_6285868231412@kiwari-prod.com\",\n" +
                "                    \"name\": \"Yusufs\"\n" +
                "                },\n" +
                "                \"data\": {\n" +
                "                    \"deleted_rooms\": [\n" +
                "                        {\n" +
                "                            \"avatar_url\": \"https://res.cloudinary.com/qiscus/image/upload/v1490343786/kiwari-prod_user_id_201/sa6r61reovri6dtrajly.jpg\",\n" +
                "                            \"chat_type\": \"group\",\n" +
                "                            \"id\": 23254,\n" +
                "                            \"id_str\": \"23254\",\n" +
                "                            \"last_comment\": null,\n" +
                "                            \"options\": {},\n" +
                "                            \"raw_room_name\": \"Qiscus Demo\",\n" +
                "                            \"room_name\": \"Qiscus Demo\",\n" +
                "                            \"unique_id\": \"4c8af24f-258a-4169-9c5d-0a110d2eac2c\",\n" +
                "                            \"unread_count\": 0\n" +
                "                        }\n" +
                "                    ]\n" +
                "                }\n" +
                "            }\n" +
                "        }";

        try {
            QiscusPusherApi.getInstance().handleNotification(new JSONObject(json));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void handlePushNotificationDeleteMessage(){
        String json = "{\n" +
                "            \"id\": 1518503569096927668,\n" +
                "            \"action_topic\": \"delete_message\",\n" +
                "            \"payload\": {\n" +
                "                \"actor\": {\n" +
                "                    \"id\": \"144\",\n" +
                "                    \"email\": \"userid_108_6285868231412@kiwari-prod.com\",\n" +
                "                    \"name\": \"Yusufs\"\n" +
                "                },\n" +
                "                \"data\": {\n" +
                "                    \"deleted_messages\": [\n" +
                "                        {\n" +
                "                            \"message_unique_ids\": [\n" +
                "                                \"TZoM1BGrxsPNQAFoydQg\"\n" +
                "                            ],\n" +
                "                            \"room_id\": \"715\"\n" +
                "                        }\n" +
                "                    ],\n" +
                "                    \"is_hard_delete\": false\n" +
                "                }\n" +
                "            }\n" +
                "        }";

        try {
            QiscusPusherApi.getInstance().handleNotification(new JSONObject(json));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    @Test
    public void handlePushNotificationRead(){
        String json = "{\n" +
                "  \"id\": 1518503569096927668,\n" +
                "  \"action_topic\": \"read\",\n" +
                "  \"payload\": {\n" +
                "    \"actor\": {\n" +
                "      \"id\": \"144\",\n" +
                "      \"email\": \"userid_108_6285868231412@kiwari-prod.com\",\n" +
                "      \"name\": \"Yusufs\"\n" +
                "    },\n" +
                "    \"data\": {\n" +
                "      \"room_id\": \"715\",\n" +
                "      \"comment_id\": \"12345\",\n" +
                "      \"comment_unique_id\": \"asbdaksd34234432\",\n" +
                "      \"email\": \"arief96\"\n" +
                "    }\n" +
                "  }\n" +
                "}";

        try {
            QiscusPusherApi.getInstance().handleNotification(new JSONObject(json));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void handlePushNotificationDelivered(){
        QiscusComment comment = QiscusComment.generateMessage(roomId, "test");
        comment.setSender("arief96");
        comment.setSenderEmail("arief96");
        comment.setId(12345);

        QiscusCore.getDataStore().addOrUpdate(comment);
        String json = "{\n" +
                "  \"id\": 1518503569096927668,\n" +
                "  \"action_topic\": \"delivered\",\n" +
                "  \"payload\": {\n" +
                "    \"actor\": {\n" +
                "      \"id\": \"144\",\n" +
                "      \"email\": \"userid_108_6285868231412@kiwari-prod.com\",\n" +
                "      \"name\": \"Yusufs\"\n" +
                "    },\n" +
                "    \"data\": {\n" +
                "      \"room_id\": \"715\",\n" +
                "      \"comment_id\": \"12345\",\n" +
                "      \"comment_unique_id\": \""+ comment.getUniqueId() +"\",\n" +
                "      \"email\": \"arief96\"\n" +
                "    }\n" +
                "  }\n" +
                "}";

        try {
            QiscusPusherApi.getInstance().handleNotification(new JSONObject(json));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void jsonToComment(){
        String json = "{\n" +
                "  \"comment_before_id\": 3,\n" +
                "  \"comment_before_id_str\": \"3\",\n" +
                "  \"disable_link_preview\": false,\n" +
                "  \"email\": \"jarjit@mail.com\",\n" +
                "  \"extras\": {\n" +
                "    \n" +
                "  },\n" +
                "  \"id\": 4,\n" +
                "  \"id_str\": \"4\",\n" +
                "  \"is_deleted\": false,\n" +
                "  \"is_public_channel\": false,\n" +
                "  \"message\": \"wkwk halo\",\n" +
                "  \"payload\": {\n" +
                "    \"text\": \"new\"\n" +
                "  },\n" +
                "  \"room_avatar\": \"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/E2nVru1t25/1507541900-avatar.png\",\n" +
                "  \"room_id\": 1,\n" +
                "  \"room_id_str\": \"1\",\n" +
                "  \"room_name\": \"channelidrandomstring\",\n" +
                "  \"room_type\": \"group\",\n" +
                "  \"status\": \"sent\",\n" +
                "  \"timestamp\": \"2019-02-13T16:19:23Z\",\n" +
                "  \"topic_id\": 1,\n" +
                "  \"topic_id_str\": \"1\",\n" +
                "  \"type\": \"buttons\",\n" +
                "  \"unique_temp_id\": \"o2jHsxcI4shjrpOS7MuC\",\n" +
                "  \"unix_nano_timestamp\": 1550074763338823000,\n" +
                "  \"unix_timestamp\": 1550074763,\n" +
                "  \"user_avatar\": {\n" +
                "    \"avatar\": {\n" +
                "      \"url\": \"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\"\n" +
                "    }\n" +
                "  },\n" +
                "  \"user_avatar\": \"https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png\",\n" +
                "  \"user_id\": 13,\n" +
                "  \"user_id_str\": \"13\",\n" +
                "  \"username\": \"Jarjit singh\"\n" +
                "}";

        JsonObject jsonObject = new JsonParser().parse(json).getAsJsonObject();
        QiscusPusherApi.getInstance().jsonToComment(jsonObject);
    }

    @Test
    public void restartConnection(){
        QiscusPusherApi.getInstance().restartConnection();
    }

    @Test
    public void listenComment(){
        QiscusPusherApi.getInstance().listenComment();
    }

    @Test
    public void listenComment2(){
        QiscusCore.setEnableDisableRealtime(true);

        QiscusPusherApi.getInstance().listenComment();
    }

    @Test
    public void isConnected(){
        QiscusPusherApi.getInstance().isConnected();
    }

    @Test
    public void eventReport(){
        QiscusCore.setEnableEventReport(true);
        QiscusPusherApi.getInstance().eventReport("test", "error","error data");
    }

    @Test
    public void eventReport2(){
        QiscusCore.setEnableEventReport(false);
        QiscusPusherApi.getInstance().eventReport("test", "error","error data");
    }

    @Test
    public void getMqttBrokerUrlFromLB(){
        QiscusCore.setEnableDisableRealtime(true);
        QiscusCore.setWillGetNewNodeMqttBrokerUrl(true);
        QiscusPusherApi.getInstance().getMqttBrokerUrlFromLB();
    }

    @Test
    public void getMqttBrokerUrlFromLB2(){
        QiscusCore.setEnableDisableRealtime(false);
        QiscusCore.setWillGetNewNodeMqttBrokerUrl(false);
        QiscusPusherApi.getInstance().getMqttBrokerUrlFromLB();
    }


    @Test
    public void listenRoom(){
        QiscusCore.setEnableDisableRealtime(true);
        QiscusCore.setWillGetNewNodeMqttBrokerUrl(true);

        QiscusChatRoom qiscusChatRoom = new QiscusChatRoom();
        qiscusChatRoom.setId(1234);
        qiscusChatRoom.setGroup(true);
        qiscusChatRoom.setChannel(false);

        QiscusPusherApi.getInstance().listenRoom(qiscusChatRoom);
        QiscusPusherApi.getInstance().subscribeChatRoom(qiscusChatRoom);
    }

    @Test
    public void listenRoom2(){
        QiscusCore.setEnableDisableRealtime(true);
        QiscusCore.setWillGetNewNodeMqttBrokerUrl(true);

        QiscusChatRoom qiscusChatRoom = new QiscusChatRoom();
        qiscusChatRoom.setId(1234);
        qiscusChatRoom.setGroup(true);
        qiscusChatRoom.setChannel(true);

        QiscusPusherApi.getInstance().listenRoom(qiscusChatRoom);
        QiscusPusherApi.getInstance().subscribeChatRoom(qiscusChatRoom);
    }

    @Test
    public void unListenRoom(){
        QiscusCore.setEnableDisableRealtime(true);
        QiscusCore.setWillGetNewNodeMqttBrokerUrl(true);

        QiscusChatRoom qiscusChatRoom = new QiscusChatRoom();
        qiscusChatRoom.setId(1234);
        qiscusChatRoom.setGroup(true);
        qiscusChatRoom.setChannel(false);

        QiscusPusherApi.getInstance().unListenRoom(qiscusChatRoom);
        QiscusPusherApi.getInstance().unsubsribeChatRoom(qiscusChatRoom);
    }


    @Test
    public void listenUserStatus(){
        QiscusCore.setEnableDisableRealtime(true);
        QiscusPusherApi.getInstance().listenUserStatus("arief96");
    }

    @Test
    public void subscribeUserOnlinePresence(){
        QiscusCore.setEnableDisableRealtime(true);
        QiscusPusherApi.getInstance().subscribeUserOnlinePresence("arief96");
    }

    @Test
    public void unListenUserStatus(){
        QiscusCore.setEnableDisableRealtime(true);
        QiscusPusherApi.getInstance().unListenUserStatus("arief96");
    }

    @Test
    public void unsubscribeUserOnlinePresence(){
        QiscusCore.setEnableDisableRealtime(true);
        QiscusPusherApi.getInstance().unsubscribeUserOnlinePresence("arief96");
    }

    @Test
    public void setUserStatus(){
        QiscusCore.setEnableDisableRealtime(true);
        QiscusPusherApi.getInstance().setUserStatus(true);
    }

    @Test
    public void setUserTyping(){
        QiscusCore.setEnableDisableRealtime(true);
        QiscusPusherApi.getInstance().setUserTyping(roomId,true);
    }

    @Test
    public void publishTyping(){
        QiscusCore.setEnableDisableRealtime(true);
        QiscusPusherApi.getInstance().publishTyping(roomId,true);
    }

    @Test
    public void setUserRead(){
        QiscusCore.setEnableDisableRealtime(true);
        QiscusPusherApi.getInstance().setUserRead(roomId,123432);
    }

    @Test
    public void setUserDelivery(){
        QiscusCore.setEnableDisableRealtime(true);
        QiscusPusherApi.getInstance().setUserDelivery(roomId,123432);
    }

    @Test
    public void markAsRead(){
        QiscusCore.setEnableDisableRealtime(true);
        QiscusPusherApi.getInstance().markAsRead(roomId,123432);
    }

    @Test
    public void markAsDelivered(){
        QiscusCore.setEnableDisableRealtime(true);
        QiscusPusherApi.getInstance().markAsDelivered(roomId,123432);
    }


    @Test
    public void setEvent(){
        try {
            QiscusPusherApi.getInstance().setEvent(roomId,new JSONObject("{}"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void setEvent2(){
        QiscusCore.setEnableDisableRealtime(true);
        try {
            QiscusPusherApi.getInstance().setEvent(roomId,new JSONObject("{}"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void publishCustomEvent2(){
        try {
            QiscusPusherApi.getInstance().publishCustomEvent(roomId,new JSONObject("{}"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void publishCustomEvent(){
        QiscusCore.setEnableDisableRealtime(true);
        try {
            QiscusPusherApi.getInstance().publishCustomEvent(roomId,new JSONObject("{}"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void listenEvent(){
        QiscusCore.setEnableDisableRealtime(true);
        QiscusPusherApi.getInstance().listenEvent(roomId);
    }

    @Test
    public void listenEvent2(){
        QiscusPusherApi.getInstance().listenEvent(roomId);
    }

    @Test
    public void subsribeCustomEvent(){
        QiscusCore.setEnableDisableRealtime(true);
        QiscusPusherApi.getInstance().subsribeCustomEvent(roomId);
    }

    @Test
    public void subsribeCustomEvent2(){
        QiscusPusherApi.getInstance().subsribeCustomEvent(roomId);
    }

    @Test
    public void unlistenEvent(){
        QiscusPusherApi.getInstance().unlistenEvent(roomId);
    }

    @Test
    public void unlistenEvent2(){
        QiscusCore.setEnableDisableRealtime(true);
        QiscusPusherApi.getInstance().unlistenEvent(roomId);
    }

    @Test
    public void unsubsribeCustomEvent(){
        QiscusPusherApi.getInstance().unsubsribeCustomEvent(roomId);
    }

    @Test
    public void unsubsribeCustomEvent2(){
        QiscusCore.setEnableDisableRealtime(true);
        QiscusPusherApi.getInstance().unsubsribeCustomEvent(roomId);
    }

    @Test
    public void scheduleUserStatus(){
        QiscusPusherApi.getInstance().scheduleUserStatus();
    }

    @Test
    public void messageArrivedOnline(){

        try {
            QiscusPusherApi.getInstance().messageArrived("u/arief93/s", new MqttMessage(new byte[123]));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    @Test
    public void handleMessageTyping(){
        QiscusPusherApi.getInstance().handleMessage("r/96304367/96304367/arief93/t","1");
    }

    @Test
    public void handleMessageOnline(){
        QiscusPusherApi.getInstance().handleMessage("u/arief93/s","1:1674114394406");
    }

    @Test
    public void handleMessageNewComment(){
        QiscusPusherApi.getInstance().handleMessage("X0y1Nsd8u125k8gB6wiz1666680616/c","{\"app_code\":\"sdksample\",\"chat_type\":\"single\",\"comment_before_id\":1205442846,\"comment_before_id_str\":\"1205442846\",\"created_at\":\"2023-01-19T07:49:54.479861Z\",\"disable_link_preview\":false,\"email\":\"arief93\",\"extras\":{},\"id\":1207380921,\"id_str\":\"1207380921\",\"is_public_channel\":false,\"message\":\"abc\",\"payload\":{},\"raw_room_name\":\"testing34\",\"room_avatar\":\"https://robohash.org/arief93/bgset_bg2/3.14160?set=set4\",\"room_id\":96304367,\"room_id_str\":\"96304367\",\"room_name\":\"arief93\",\"room_options\":\"{}\",\"room_type\":\"single\",\"status\":\"sent\",\"timestamp\":\"2023-01-19T07:49:54Z\",\"topic_id\":96304367,\"topic_id_str\":\"96304367\",\"type\":\"text\",\"unique_temp_id\":\"javascript-1674114594284\",\"unix_nano_timestamp\":1674114594479861000,\"unix_timestamp\":1674114594,\"user_avatar\":\"https://robohash.org/arief93/bgset_bg2/3.14160?set=set4\",\"user_avatar_url\":\"https://robohash.org/arief93/bgset_bg2/3.14160?set=set4\",\"user_extras\":{},\"user_id\":133493390,\"user_id_str\":\"133493390\",\"username\":\"arief93\"}");
    }

    @Test
    public void handleMessageUpdate(){
        QiscusPusherApi.getInstance().handleMessage("X0y1Nsd8u125k8gB6wiz1666680616/update","{\"app_code\":\"sdksample\",\"chat_type\":\"single\",\"comment_before_id\":1205442846,\"comment_before_id_str\":\"1205442846\",\"created_at\":\"2023-01-19T07:49:54.479861Z\",\"disable_link_preview\":false,\"email\":\"arief93\",\"extras\":{},\"id\":1207380921,\"id_str\":\"1207380921\",\"is_public_channel\":false,\"message\":\"abc\",\"payload\":{},\"raw_room_name\":\"testing34\",\"room_avatar\":\"https://robohash.org/arief93/bgset_bg2/3.14160?set=set4\",\"room_id\":96304367,\"room_id_str\":\"96304367\",\"room_name\":\"arief93\",\"room_options\":\"{}\",\"room_type\":\"single\",\"status\":\"sent\",\"timestamp\":\"2023-01-19T07:49:54Z\",\"topic_id\":96304367,\"topic_id_str\":\"96304367\",\"type\":\"text\",\"unique_temp_id\":\"javascript-1674114594284\",\"unix_nano_timestamp\":1674114594479861000,\"unix_timestamp\":1674114594,\"user_avatar\":\"https://robohash.org/arief93/bgset_bg2/3.14160?set=set4\",\"user_avatar_url\":\"https://robohash.org/arief93/bgset_bg2/3.14160?set=set4\",\"user_extras\":{},\"user_id\":133493390,\"user_id_str\":\"133493390\",\"username\":\"arief93\"}");
    }

    @Test
    public void handleMessageDelivery(){
        QiscusPusherApi.getInstance().handleMessage("r/96304367/96304367/arief93/d","1207380921:javascript-1674114594284");
    }

    @Test
    public void handleMessageRead(){
        QiscusPusherApi.getInstance().handleMessage("r/96304367/96304367/arief93/r","1207380921:javascript-1674114594284");
    }

}