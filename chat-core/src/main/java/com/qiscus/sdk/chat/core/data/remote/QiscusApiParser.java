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

import androidx.core.util.Pair;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.qiscus.sdk.chat.core.data.model.QAccount;
import com.qiscus.sdk.chat.core.data.model.QChatRoom;
import com.qiscus.sdk.chat.core.data.model.QMessage;
import com.qiscus.sdk.chat.core.data.model.QParticipant;
import com.qiscus.sdk.chat.core.data.model.QUser;
import com.qiscus.sdk.chat.core.data.model.QUserPresence;
import com.qiscus.sdk.chat.core.data.model.QiscusAppConfig;
import com.qiscus.sdk.chat.core.data.model.QiscusChannels;
import com.qiscus.sdk.chat.core.data.model.QiscusNonce;
import com.qiscus.sdk.chat.core.data.model.QiscusRealtimeStatus;
import com.qiscus.sdk.chat.core.util.QiscusTextUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * Created on : February 02, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
final class QiscusApiParser {

    static QiscusNonce parseNonce(JsonElement jsonElement) {
        JsonObject result = jsonElement.getAsJsonObject().get("results").getAsJsonObject();
        return new QiscusNonce(new Date(result.get("expired_at").getAsLong() * 1000L),
                result.get("nonce").getAsString());
    }

    static QAccount parseQiscusAccount(JsonElement jsonElement) {
        JsonObject jsonAccount = jsonElement.getAsJsonObject().get("results").getAsJsonObject().get("user").getAsJsonObject();
        return parseQiscusAccount(jsonAccount, true);
    }

    static QAccount parseQiscusAccount(JsonObject jsonAccount, Boolean isSelf) {
        QAccount qAccount = new QAccount();
        qAccount.setId(jsonAccount.get("email").getAsString());
        qAccount.setName(jsonAccount.get("username").getAsString());
        qAccount.setAvatarUrl(jsonAccount.get("avatar_url").getAsString());
        qAccount.setLastMessageId(jsonAccount.get("last_comment_id").getAsLong());
        qAccount.setLastSyncEventId(jsonAccount.get("last_sync_event_id").getAsLong());
        try {
            qAccount.setExtras(new JSONObject(jsonAccount.get("extras").getAsJsonObject().toString()));
        } catch (Exception ignored) {
            //Do nothing
        }
        if (isSelf) {
            qAccount.setToken(jsonAccount.get("token").getAsString());
        }
        return qAccount;
    }

    static QUser parseQUser(JsonElement jsonElement) {
        JsonObject jsonAccount = jsonElement.getAsJsonObject().get("results").getAsJsonObject().get("user").getAsJsonObject();
        return parseQUser(jsonAccount);
    }

    static QUser parseQUser(JsonObject jsonAccount) {
        QUser qUser = new QUser();
        qUser.setId(jsonAccount.get("email").getAsString());
        qUser.setName(jsonAccount.get("username").getAsString());
        qUser.setAvatarUrl(jsonAccount.get("avatar_url").getAsString());
        try {
            qUser.setExtras(new JSONObject(jsonAccount.get("extras").getAsJsonObject().toString()));
        } catch (Exception ignored) {
            //Do nothing
        }
        return qUser;
    }

    static QChatRoom parseQiscusChatRoom(JsonElement jsonElement) {
        if (jsonElement != null) {
            JsonObject jsonChatRoom = jsonElement.getAsJsonObject().get("results").getAsJsonObject().get("room").getAsJsonObject();
            QChatRoom qChatRoom = new QChatRoom();
            qChatRoom.setId(jsonChatRoom.get("id").getAsLong());
            String type = "single";
            type = jsonChatRoom.get("chat_type").getAsString();
            if (type.equals("group")) {
                qChatRoom.setType("group");
                if (jsonChatRoom.has("is_public_channel")) {
                    if (jsonChatRoom.get("is_public_channel").getAsBoolean() == true) {
                        qChatRoom.setType("channel");
                    }
                }
            } else {
                qChatRoom.setType("single");
            }

            qChatRoom.setName(jsonChatRoom.get("room_name").getAsString());

            qChatRoom.setUniqueId(jsonChatRoom.get("unique_id").getAsString());

            if (jsonChatRoom.getAsJsonObject().has("options") &&
                    !jsonChatRoom.getAsJsonObject().get("options").isJsonNull()) {
                try {
                    qChatRoom.setExtras(jsonChatRoom.get("options").isJsonNull() ? null :
                            new JSONObject(jsonChatRoom.get("options").getAsString()));
                } catch (JSONException ignored) {
                    //Do nothing
                }

            }

            qChatRoom.setAvatarUrl(jsonChatRoom.get("avatar_url").getAsString());

            if (jsonChatRoom.has("room_total_participants")) {
                qChatRoom.setTotalParticipants(jsonChatRoom.get("room_total_participants").getAsInt());
            }

            JsonElement participants = jsonChatRoom.get("participants");
            List<QParticipant> members = new ArrayList<>();
            if (participants.isJsonArray()) {
                JsonArray jsonMembers = participants.getAsJsonArray();
                for (JsonElement jsonMember : jsonMembers) {
                    members.add(parseQiscusRoomMember(jsonMember.getAsJsonObject()));
                }
            }
            qChatRoom.setParticipants(members);

            JsonArray comments = jsonElement.getAsJsonObject().get("results")
                    .getAsJsonObject().get("comments").getAsJsonArray();

            if (comments.size() > 0) {
                QMessage latestComment = parseQMessage(comments.get(0), qChatRoom.getId());
                qChatRoom.setLastMessage(latestComment);
            }
            return qChatRoom;
        }

        return null;
    }

    static QParticipant parseQiscusRoomMember(JsonObject jsonMember) {
        QParticipant participant = new QParticipant();
        participant.setId(jsonMember.get("email").getAsString());
        participant.setName(jsonMember.get("username").getAsString());
        if (jsonMember.has("avatar_url")) {
            participant.setAvatarUrl(jsonMember.get("avatar_url").getAsString());
        }

        try {
            if (jsonMember.has("extras")) {
                participant.setExtras(new JSONObject(jsonMember.get("extras").getAsJsonObject().toString()));
            }
        } catch (JSONException ignored) {
            //Do nothing
        }

        if (jsonMember.getAsJsonObject().has("last_comment_received_id")) {
            participant.setLastMessageDeliveredId(jsonMember.getAsJsonObject().get("last_comment_received_id").getAsInt());
        }
        if (jsonMember.getAsJsonObject().has("last_comment_read_id")) {
            participant.setLastMessageReadId(jsonMember.getAsJsonObject().get("last_comment_read_id").getAsInt());
        }
        return participant;
    }

    static List<QChatRoom> parseQiscusChatRoomInfo(JsonElement jsonElement) {
        List<QChatRoom> qChatRooms = new ArrayList<>();
        if (jsonElement != null) {
            JsonArray jsonRoomInfo = jsonElement.getAsJsonObject()
                    .get("results").getAsJsonObject().get("rooms_info").getAsJsonArray();
            for (JsonElement item : jsonRoomInfo) {
                JsonObject jsonChatRoom = item.getAsJsonObject();
                QChatRoom qChatRoom = new QChatRoom();
                qChatRoom.setId(jsonChatRoom.get("id").getAsLong());
                qChatRoom.setName(jsonChatRoom.get("room_name").getAsString());

                String type = "single";
                type = jsonChatRoom.get("chat_type").getAsString();
                if (type.equals("group")) {
                    qChatRoom.setType("group");
                    if (jsonChatRoom.has("is_public_channel")) {
                        if (jsonChatRoom.get("is_public_channel").getAsBoolean() == true) {
                            qChatRoom.setType("channel");
                        }
                    }
                } else {
                    qChatRoom.setType("single");
                }

                qChatRoom.setUniqueId(jsonChatRoom.get("unique_id").getAsString());


                if (jsonChatRoom.getAsJsonObject().has("options") &&
                        !jsonChatRoom.getAsJsonObject().get("options").isJsonNull()) {
                    try {
                        qChatRoom.setExtras(jsonChatRoom.get("options").isJsonNull() ? null :
                                new JSONObject(jsonChatRoom.get("options").getAsString()));
                    } catch (JSONException ignored) {
                        //Do nothing
                    }
                }

                qChatRoom.setAvatarUrl(jsonChatRoom.get("avatar_url").getAsString());
                qChatRoom.setUnreadCount(jsonChatRoom.get("unread_count").getAsInt());

                if (jsonChatRoom.has("room_total_participants")) {
                    qChatRoom.setTotalParticipants(jsonChatRoom.get("room_total_participants").getAsInt());
                }

                List<QParticipant> members = new ArrayList<>();
                if (jsonChatRoom.has("participants") && jsonChatRoom.get("participants").isJsonArray()) {
                    JsonArray jsonMembers = jsonChatRoom.get("participants").getAsJsonArray();
                    for (JsonElement jsonMember : jsonMembers) {
                        QParticipant qParticipant = new QParticipant();
                        qParticipant.setId(jsonMember.getAsJsonObject().get("email").getAsString());
                        qParticipant.setAvatarUrl(jsonMember.getAsJsonObject().get("avatar_url").getAsString());
                        qParticipant.setName(jsonMember.getAsJsonObject().get("username").getAsString());
                        if (jsonMember.getAsJsonObject().has("last_comment_received_id")) {
                            qParticipant.setLastMessageDeliveredId(jsonMember.getAsJsonObject().get("last_comment_received_id").getAsLong());
                        }
                        if (jsonMember.getAsJsonObject().has("last_comment_read_id")) {
                            qParticipant.setLastMessageReadId(jsonMember.getAsJsonObject().get("last_comment_read_id").getAsLong());
                        }

                        if (jsonMember.getAsJsonObject().has("extras") && !jsonMember.getAsJsonObject().get("extras").isJsonNull()) {
                            try {
                                qParticipant.setExtras(new JSONObject(jsonMember.getAsJsonObject().get("extras").getAsJsonObject().toString()));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        members.add(qParticipant);
                    }
                }
                qChatRoom.setParticipants(members);

                QMessage latestComment = parseQMessage(jsonChatRoom.get("last_comment"), qChatRoom.getId());
                qChatRoom.setLastMessage(latestComment);

                qChatRooms.add(qChatRoom);
            }
            return qChatRooms;
        }
        return qChatRooms;
    }

    static Pair<QChatRoom, List<QMessage>> parseQiscusChatRoomWithComments(JsonElement jsonElement) {
        if (jsonElement != null) {
            QChatRoom qChatRoom = parseQiscusChatRoom(jsonElement);

            JsonArray comments = jsonElement.getAsJsonObject().get("results").getAsJsonObject().get("comments").getAsJsonArray();
            List<QMessage> qiscusMessages = new ArrayList<>();
            for (JsonElement jsonComment : comments) {
                qiscusMessages.add(parseQMessage(jsonComment, qChatRoom.getId()));
            }

            return Pair.create(qChatRoom, qiscusMessages);
        }

        return null;
    }

    static QMessage parseQMessage(JsonElement jsonElement, long roomId) {
        QMessage qiscusMessage = new QMessage();
        JsonObject jsonComment = jsonElement.getAsJsonObject();
        qiscusMessage.setChatRoomId(roomId);
        qiscusMessage.setId(jsonComment.get("id").getAsLong());
        qiscusMessage.setPreviousMessageId(jsonComment.get("comment_before_id").getAsLong());
        qiscusMessage.setText(jsonComment.get("message").getAsString());

        QUser qUser = new QUser();
        qUser.setAvatarUrl(jsonComment.get("user_avatar_url").getAsString());
        qUser.setName(jsonComment.get("username").getAsString());
        qUser.setId(jsonComment.get("email").getAsString());
        if (jsonComment.has("user_extras") && !jsonComment.get("user_extras").isJsonNull()) {
            try {
                qUser.setExtras(new JSONObject(jsonComment.get("user_extras").getAsJsonObject().toString()));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        qiscusMessage.setSender(qUser);
        qiscusMessage.getSender().setId(jsonComment.get("email").getAsString());
        qiscusMessage.getSender().setAvatarUrl(jsonComment.get("user_avatar_url").getAsString());
        if (jsonComment.has("app_code")) {
            qiscusMessage.setAppId(jsonComment.get("app_code").getAsString());
        }
        determineCommentState(qiscusMessage, jsonComment.get("status").getAsString());

        //timestamp is in nano seconds format, convert it to milliseconds by divide it
        long timestamp = jsonComment.get("unix_nano_timestamp").getAsLong() / 1000000L;
        qiscusMessage.setTimestamp(new Date(timestamp));

        if (jsonComment.has("is_deleted")) {
            qiscusMessage.setDeleted(jsonComment.get("is_deleted").getAsBoolean());
        }

        if (jsonComment.has("unique_id")) {
            qiscusMessage.setUniqueId(jsonComment.get("unique_id").getAsString());
        } else if (jsonComment.has("unique_temp_id")) {
            qiscusMessage.setUniqueId(jsonComment.get("unique_temp_id").getAsString());
        } else {
            qiscusMessage.setUniqueId(String.valueOf(qiscusMessage.getId()));
        }

        if (jsonComment.has("type")) {
            qiscusMessage.setRawType(jsonComment.get("type").getAsString());
            if (jsonComment.has("payload") && !jsonComment.get("payload").isJsonNull()) {
                qiscusMessage.setPayload(jsonComment.get("payload").toString());
            }
            if (qiscusMessage.getType() == QMessage.Type.BUTTONS
                    || qiscusMessage.getType() == QMessage.Type.REPLY
                    || qiscusMessage.getType() == QMessage.Type.CARD) {
                JsonObject payload = jsonComment.get("payload").getAsJsonObject();
                if (payload.has("text")) {
                    String text = payload.get("text").getAsString();
                    if (QiscusTextUtil.isNotBlank(text)) {
                        qiscusMessage.setText(text.trim());
                    }
                }
            }
        }

        if (jsonComment.has("extras") && !jsonComment.get("extras").isJsonNull()) {
            try {
                qiscusMessage.setExtras(new JSONObject(jsonComment.get("extras").getAsJsonObject().toString()));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return qiscusMessage;
    }

    private static void determineCommentState(QMessage qiscusMessage, String status) {
        qiscusMessage.setStatus(QMessage.STATE_SENT);
        if (status != null && !status.isEmpty()) {
            switch (status) {
                case "sent":
                    qiscusMessage.setStatus(QMessage.STATE_SENT);
                    break;
                case "delivered":
                    qiscusMessage.setStatus(QMessage.STATE_DELIVERED);
                    break;
                case "read":
                    qiscusMessage.setStatus(QMessage.STATE_READ);
                    break;
            }
        }
    }

    public static HashMap<String, List<QParticipant>> parseQMessageInfo(JsonObject jsonResults) {
        HashMap<String, List<QParticipant>> commentInfo = new HashMap<>();
        List<QParticipant> listDeliveredTo = new ArrayList<>();
        List<QParticipant> listPending = new ArrayList<>();
        List<QParticipant> listReadBy = new ArrayList<>();

        JsonArray arrDeliveredTo = jsonResults.getAsJsonArray("delivered_to");
        JsonArray arrPending = jsonResults.getAsJsonArray("pending");
        JsonArray arrReadBy = jsonResults.getAsJsonArray("read_by");

        parseMemberAndAddToList(listDeliveredTo, arrDeliveredTo);
        parseMemberAndAddToList(listDeliveredTo, arrPending);
        parseMemberAndAddToList(listDeliveredTo, arrReadBy);

        commentInfo.put("delivered_to", listDeliveredTo);
        commentInfo.put("sent", listPending); // karena pending yang dimaksud sudah masuk server qiscus
        commentInfo.put("read_by", listReadBy);

        return commentInfo;
    }

    private static void parseMemberAndAddToList(List<QParticipant> memberList, JsonArray arr) {
        if (arr == null) {
            return;
        }
        for (JsonElement el : arr) {
            memberList.add(parseQiscusRoomMember(el.getAsJsonObject().getAsJsonObject("user")));
        }
    }

    static QiscusAppConfig parseQiscusAppConfig(JsonElement jsonElement) {
        if (jsonElement != null) {
            QiscusAppConfig appConfig = new QiscusAppConfig();
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            JsonObject results = jsonObject.getAsJsonObject("results");


            if (results.has("base_url")) {
                appConfig.setBaseURL(results.get("base_url").getAsString());
            } else {
                appConfig.setBaseURL("");
            }

            if (results.has("broker_lb_url")) {
                appConfig.setBrokerLBURL(results.get("broker_lb_url").getAsString());
            } else {
                appConfig.setBrokerLBURL("");
            }

            if (results.has("broker_url")) {
                appConfig.setBrokerURL(results.get("broker_url").getAsString());
            } else {
                appConfig.setBrokerURL("");
            }

            if (results.has("enable_event_report")) {
                appConfig.setEnableEventReport(results.get("enable_event_report").getAsBoolean());
            } else {
                appConfig.setEnableEventReport(false);
            }

            if (results.has("enable_realtime")) {
                appConfig.setEnableRealtime(results.get("enable_realtime").getAsBoolean());
            } else {
                appConfig.setEnableRealtime(true);
            }

            if (results.has("sync_interval")) {
                appConfig.setSyncInterval(results.get("sync_interval").getAsInt());
            } else {
                appConfig.setSyncInterval(5000);
            }

            if (results.has("sync_on_connect")) {
                appConfig.setSyncOnConnect(results.get("sync_on_connect").getAsInt());
            } else {
                appConfig.setSyncOnConnect(30000);
            }

            if (results.has("enable_realtime_check")) {
                appConfig.setEnableRealtimeCheck(results.get("enable_realtime_check").getAsBoolean());
            } else {
                appConfig.setEnableRealtimeCheck(false);
            }

            return appConfig;

        } else {
            return null;
        }
    }

    static QiscusRealtimeStatus parseQiscusRealtimeStatus(JsonElement jsonElement) {
        if (jsonElement != null) {
            QiscusRealtimeStatus realtimeStatus = new QiscusRealtimeStatus();
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            JsonObject results = jsonObject.getAsJsonObject("results");


            if (results.has("status")) {
                realtimeStatus.setRealtimeStatus(results.get("status").getAsBoolean());
            } else {
                realtimeStatus.setRealtimeStatus(false);
            }

            return realtimeStatus;
        } else {
            return null;
        }
    }

    static List<QiscusChannels> parseQiscusChannels(JsonElement jsonElement) {
        if (jsonElement != null) {

            JsonArray channels = jsonElement.getAsJsonObject().get("results").getAsJsonObject().get("channels").getAsJsonArray();
            List<QiscusChannels> qiscusChannels = new ArrayList<>();
            if (channels.isJsonArray()) {
                for (JsonElement channel : channels) {
                    qiscusChannels.add(parseQiscusChannel(channel.getAsJsonObject()));
                }
            }

            return qiscusChannels;
        } else {
            return null;
        }
    }

    static QiscusChannels parseQiscusChannel(JsonObject jsonChannel) {
        QiscusChannels channel = new QiscusChannels();
        if (jsonChannel.has("avatar_url")) {
            channel.setAvatarUrl(jsonChannel.get("avatar_url").getAsString());
        }

        if (jsonChannel.has("created_at")) {
            channel.setCreatedAt(jsonChannel.get("created_at").getAsString());
        }

        try {
            if (jsonChannel.has("extras")) {
                channel.setExtras(new JSONObject(jsonChannel.get("extras").getAsJsonObject().toString()));
            }
        } catch (JSONException ignored) {
            //Do nothing
        }

        if (jsonChannel.has("is_joined")) {
            channel.setJoined(jsonChannel.get("is_joined").getAsBoolean());
        }

        if (jsonChannel.has("name")) {
            channel.setName(jsonChannel.get("name").getAsString());
        }

        if (jsonChannel.has("unique_id")) {
            channel.setUniqueId(jsonChannel.get("unique_id").getAsString());
        }

        if (jsonChannel.has("id")) {
            channel.setRoomId(jsonChannel.get("id").getAsLong());
        }


        return channel;
    }

    static List<QUserPresence> parseQUsersPresence(JsonElement jsonElement) {
        if (jsonElement != null) {

            JsonArray userStatus = jsonElement.getAsJsonObject().get("results").getAsJsonObject().get("user_status").getAsJsonArray();
            List<QUserPresence> qiscusUserStatus = new ArrayList<>();
            if (userStatus.isJsonArray()) {
                for (JsonElement presence : userStatus) {
                    qiscusUserStatus.add(parseQUserPresence(presence.getAsJsonObject()));
                }
            }

            return qiscusUserStatus;
        } else {
            return null;
        }
    }

    static QUserPresence parseQUserPresence(JsonObject jsonPresence) {
        QUserPresence userStatus = new QUserPresence();
        if (jsonPresence.has("email")) {
            userStatus.setUserId(jsonPresence.get("email").getAsString());
        }

        if (jsonPresence.has("status")) {
            userStatus.setStatus(jsonPresence.get("status").getAsInt() == 1);
        }

        if (jsonPresence.has("timestamp")) {
            Calendar cal = Calendar.getInstance(Locale.getDefault());
            cal.setTimeInMillis(jsonPresence.get("timestamp").getAsLong() * 1000L);
            userStatus.setTimestamp(cal.getTime());
        }

        return userStatus;
    }

    static List<QMessage> parseFileListAndSearchMessage(JsonElement jsonElement) {
        if (jsonElement != null) {
            JsonArray comments = jsonElement.getAsJsonObject().get("results").getAsJsonObject().get("comments").getAsJsonArray();
            List<QMessage> qiscusComments = new ArrayList<>();
            for (JsonElement jsonComment : comments) {
                qiscusComments.add(parseFileListAndSearch(jsonComment));
            }

            return qiscusComments;
        }

        return null;
    }

    static QMessage parseFileListAndSearch(JsonElement jsonElement) {
        QMessage qiscusComment = new QMessage();
        JsonObject jsonComment = jsonElement.getAsJsonObject();

        if (jsonComment.has("room_id")) {
            qiscusComment.setChatRoomId(jsonComment.get("room_id").getAsLong());
        }

        qiscusComment.setId(jsonComment.get("id").getAsLong());
        qiscusComment.setPreviousMessageId(jsonComment.get("comment_before_id").getAsLong());
        qiscusComment.setText(jsonComment.get("message").getAsString());

        QUser qUser = new QUser();
        qUser.setAvatarUrl(jsonComment.get("user_avatar_url").getAsString());
        qUser.setName(jsonComment.get("username").getAsString());
        qUser.setId(jsonComment.get("email").getAsString());
        qiscusComment.setSender(qUser);
        determineCommentState(qiscusComment, jsonComment.get("status").getAsString());

        //timestamp is in nano seconds format, convert it to milliseconds by divide it
        long timestamp = jsonComment.get("unix_nano_timestamp").getAsLong() / 1000000L;
        qiscusComment.setTimestamp(new Date(timestamp));

        if (jsonComment.has("is_deleted")) {
            qiscusComment.setDeleted(jsonComment.get("is_deleted").getAsBoolean());
        }

//        if (jsonComment.has("room_name")) {
//            qiscusComment.setRoomName(jsonComment.get("room_name").getAsString());
//        }
//
//        if (jsonComment.has("room_avatar")) {
//            qiscusComment.setRoomAvatar(jsonComment.get("room_avatar").getAsString());
//        }
//
//        if (jsonComment.has("room_type")) {
//            qiscusComment.setGroupMessage(!"single".equals(jsonComment.get("room_type").getAsString()));
//        }

        if (jsonComment.has("unique_id")) {
            qiscusComment.setUniqueId(jsonComment.get("unique_id").getAsString());
        } else if (jsonComment.has("unique_temp_id")) {
            qiscusComment.setUniqueId(jsonComment.get("unique_temp_id").getAsString());
        } else {
            qiscusComment.setUniqueId(String.valueOf(qiscusComment.getId()));
        }

        if (jsonComment.has("type")) {
            qiscusComment.setRawType(jsonComment.get("type").getAsString());
            qiscusComment.setPayload(jsonComment.get("payload").toString());
            if (qiscusComment.getType() == QMessage.Type.BUTTONS
                    || qiscusComment.getType() == QMessage.Type.REPLY
                    || qiscusComment.getType() == QMessage.Type.CARD) {
                JsonObject payload = jsonComment.get("payload").getAsJsonObject();
                if (payload.has("text")) {
                    String text = payload.get("text").getAsString();
                    if (QiscusTextUtil.isNotBlank(text)) {
                        qiscusComment.setText(text.trim());
                    }
                }
            }
        }

        if (jsonComment.has("extras") && !jsonComment.get("extras").isJsonNull()) {
            try {
                qiscusComment.setExtras(new JSONObject(jsonComment.get("extras").getAsJsonObject().toString()));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        if (jsonComment.has("user_extras") && !jsonComment.get("user_extras").isJsonNull()) {
            try {
                qiscusComment.getSender().setExtras(new JSONObject(jsonComment.get("user_extras").getAsJsonObject().toString()));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return qiscusComment;
    }
}
