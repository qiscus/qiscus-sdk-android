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

import android.support.v4.util.Pair;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.qiscus.sdk.data.model.QiscusAccount;
import com.qiscus.sdk.data.model.QiscusChatRoom;
import com.qiscus.sdk.data.model.QiscusComment;
import com.qiscus.sdk.data.model.QiscusNonce;
import com.qiscus.sdk.data.model.QiscusRoomMember;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created on : February 02, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
final class QiscusApiParser {
    private static DateFormat dateFormat;

    static {
        dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    static QiscusNonce parseNonce(JsonElement jsonElement) {
        JsonObject result = jsonElement.getAsJsonObject().get("results").getAsJsonObject();
        return new QiscusNonce(new Date(result.get("expired_at").getAsLong() * 1000L),
                result.get("nonce").getAsString());
    }

    static QiscusAccount parseQiscusAccount(JsonElement jsonElement) {
        JsonObject jsonAccount = jsonElement.getAsJsonObject().get("results").getAsJsonObject().get("user").getAsJsonObject();
        QiscusAccount qiscusAccount = new QiscusAccount();
        qiscusAccount.setId(jsonAccount.get("id").getAsInt());
        qiscusAccount.setUsername(jsonAccount.get("username").getAsString());
        qiscusAccount.setEmail(jsonAccount.get("email").getAsString());
        qiscusAccount.setToken(jsonAccount.get("token").getAsString());
        qiscusAccount.setRtKey(jsonAccount.get("rtKey").getAsString());
        qiscusAccount.setAvatar(jsonAccount.get("avatar_url").getAsString());
        return qiscusAccount;
    }

    static QiscusChatRoom parseQiscusChatRoom(JsonElement jsonElement) {
        if (jsonElement != null) {
            JsonObject jsonChatRoom = jsonElement.getAsJsonObject().get("results").getAsJsonObject().get("room").getAsJsonObject();
            QiscusChatRoom qiscusChatRoom = new QiscusChatRoom();
            qiscusChatRoom.setId(jsonChatRoom.get("id").getAsInt());
            //TODO minta server ngasih tau distinctId biar bisa disimpen
            //qiscusChatRoom.setDistinctId("default");
            qiscusChatRoom.setGroup(!"single".equals(jsonChatRoom.get("chat_type").getAsString()));
            qiscusChatRoom.setName(jsonChatRoom.get("room_name").getAsString());
            if (qiscusChatRoom.isGroup()) {
                qiscusChatRoom.setDistinctId(qiscusChatRoom.getId() + "");
            }
            qiscusChatRoom.setLastCommentId(jsonChatRoom.get("last_comment_id").getAsInt());
            qiscusChatRoom.setLastCommentMessage(jsonChatRoom.get("last_comment_message").getAsString());
            qiscusChatRoom.setLastTopicId(jsonChatRoom.get("last_topic_id").getAsInt());
            qiscusChatRoom.setOptions(jsonChatRoom.get("options").isJsonNull() ? null : jsonChatRoom.get("options").getAsString());
            qiscusChatRoom.setAvatarUrl(jsonChatRoom.get("avatar_url").getAsString());

            JsonArray jsonMembers = jsonElement.getAsJsonObject().get("results")
                    .getAsJsonObject().get("room").getAsJsonObject().get("participants").getAsJsonArray();
            List<QiscusRoomMember> members = new ArrayList<>();
            for (JsonElement jsonMember : jsonMembers) {
                QiscusRoomMember member = new QiscusRoomMember();
                member.setEmail(jsonMember.getAsJsonObject().get("email").getAsString());
                member.setAvatar(jsonMember.getAsJsonObject().get("avatar_url").getAsString());
                member.setUsername(jsonMember.getAsJsonObject().get("username").getAsString());
                if (jsonMember.getAsJsonObject().has("last_comment_received_id")) {
                    member.setLastDeliveredCommentId(jsonMember.getAsJsonObject().get("last_comment_received_id").getAsInt());
                }
                if (jsonMember.getAsJsonObject().has("last_comment_read_id")) {
                    member.setLastReadCommentId(jsonMember.getAsJsonObject().get("last_comment_read_id").getAsInt());
                }
                members.add(member);
            }
            qiscusChatRoom.setMember(members);

            JsonArray comments = jsonElement.getAsJsonObject().get("results")
                    .getAsJsonObject().get("comments").getAsJsonArray();

            if (comments.size() > 0) {
                JsonObject lastComment = comments.get(0).getAsJsonObject();
                qiscusChatRoom.setLastCommentSender(lastComment.get("username").getAsString());
                qiscusChatRoom.setLastCommentSenderEmail(lastComment.get("email").getAsString());
                try {
                    qiscusChatRoom.setLastCommentTime(dateFormat.parse(lastComment.get("timestamp").getAsString()));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            return qiscusChatRoom;
        }

        return null;
    }

    static List<QiscusChatRoom> parseQiscusChatRoomInfo(JsonElement jsonElement) {

        if (jsonElement != null) {
            JsonArray jsonRoomInfo = jsonElement.getAsJsonObject().get("results").getAsJsonObject().get("rooms_info").getAsJsonArray();
            List<QiscusChatRoom> qiscusChatRooms = new ArrayList<>();
            for (JsonElement jsonMember : jsonRoomInfo) {
                QiscusChatRoom qiscusChatRoom = new QiscusChatRoom();
                qiscusChatRoom.setLastCommentId(jsonMember.getAsJsonObject().get("last_comment_id").getAsInt());
                qiscusChatRoom.setAvatarUrl(jsonMember.getAsJsonObject().get("room_avatar_url").getAsString());
                qiscusChatRoom.setId(jsonMember.getAsJsonObject().get("room_id").getAsInt());
                qiscusChatRoom.setName(jsonMember.getAsJsonObject().get("room_name").getAsString());
                qiscusChatRoom.setLastCommentMessage(jsonMember.getAsJsonObject().get("last_comment_message").getAsString());
                qiscusChatRoom.setUnreadCount(jsonMember.getAsJsonObject().get("unread_count").getAsInt());

                try {
                    qiscusChatRoom.setLastCommentTime(dateFormat.parse(jsonMember.getAsJsonObject()
                            .get("last_comment_timestamp").getAsString()));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                qiscusChatRooms.add(qiscusChatRoom);
            }
            return qiscusChatRooms;
        }
        return null;
    }

    static Pair<QiscusChatRoom, List<QiscusComment>> parseQiscusChatRoomWithComments(JsonElement jsonElement) {
        if (jsonElement != null) {
            QiscusChatRoom qiscusChatRoom = parseQiscusChatRoom(jsonElement);

            JsonArray comments = jsonElement.getAsJsonObject().get("results").getAsJsonObject().get("comments").getAsJsonArray();
            List<QiscusComment> qiscusComments = new ArrayList<>();
            for (JsonElement jsonComment : comments) {
                qiscusComments.add(parseQiscusComment(jsonComment, qiscusChatRoom.getId(), qiscusChatRoom.getLastTopicId()));
            }

            return Pair.create(qiscusChatRoom, qiscusComments);
        }

        return null;
    }

    static QiscusComment parseQiscusComment(JsonElement jsonElement, int roomId, int topicId) {
        QiscusComment qiscusComment = new QiscusComment();
        JsonObject jsonComment = jsonElement.getAsJsonObject();
        qiscusComment.setTopicId(topicId);
        qiscusComment.setRoomId(roomId);
        qiscusComment.setId(jsonComment.get("id").getAsInt());
        qiscusComment.setCommentBeforeId(jsonComment.get("comment_before_id").getAsInt());
        qiscusComment.setMessage(jsonComment.get("message").getAsString());
        qiscusComment.setSender(jsonComment.get("username").getAsString());
        qiscusComment.setSenderEmail(jsonComment.get("email").getAsString());
        qiscusComment.setSenderAvatar(jsonComment.get("user_avatar_url").getAsString());
        qiscusComment.setState(QiscusComment.STATE_ON_QISCUS);

        try {
            qiscusComment.setTime(dateFormat.parse(jsonComment.get("timestamp").getAsString()));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (jsonComment.has("room_name")) {
            qiscusComment.setRoomName(jsonComment.get("room_name").getAsString());
        }

        if (jsonComment.has("chat_type")) {
            qiscusComment.setGroupMessage(!"single".equals(jsonComment.get("chat_type").getAsString()));
        }

        if (jsonComment.has("unique_id")) {
            qiscusComment.setUniqueId(jsonComment.get("unique_id").getAsString());
        } else if (jsonComment.has("unique_temp_id")) {
            qiscusComment.setUniqueId(jsonComment.get("unique_temp_id").getAsString());
        } else {
            qiscusComment.setUniqueId(String.valueOf(qiscusComment.getId()));
        }

        if (jsonComment.has("type")) {
            qiscusComment.setRawType(jsonComment.get("type").getAsString());
            qiscusComment.setExtraPayload(jsonComment.get("payload").toString());
            if (qiscusComment.getType() == QiscusComment.Type.BUTTONS
                    || qiscusComment.getType() == QiscusComment.Type.REPLY
                    || qiscusComment.getType() == QiscusComment.Type.CARD) {
                JsonObject payload = jsonComment.get("payload").getAsJsonObject();
                if (payload.has("text")) {
                    String text = payload.get("text").getAsString();
                    if (text != null && !text.trim().isEmpty()) {
                        qiscusComment.setMessage(text.trim());
                    }
                }
            }
        }

        return qiscusComment;
    }
}
