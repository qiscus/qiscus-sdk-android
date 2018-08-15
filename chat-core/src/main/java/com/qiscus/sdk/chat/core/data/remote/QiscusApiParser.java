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

import android.support.v4.util.Pair;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.qiscus.sdk.chat.core.QiscusCore;
import com.qiscus.sdk.chat.core.data.model.QiscusAccount;
import com.qiscus.sdk.chat.core.data.model.QiscusChatRoom;
import com.qiscus.sdk.chat.core.data.model.QiscusComment;
import com.qiscus.sdk.chat.core.data.model.QiscusNonce;
import com.qiscus.sdk.chat.core.data.model.QiscusRoomMember;
import com.qiscus.sdk.chat.core.util.QiscusTextUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

    static QiscusAccount parseQiscusAccount(JsonElement jsonElement) {
        JsonObject jsonAccount = jsonElement.getAsJsonObject().get("results").getAsJsonObject().get("user").getAsJsonObject();
        return parseQiscusAccount(jsonAccount, true);
    }

    static QiscusAccount parseQiscusAccount(JsonObject jsonAccount, Boolean isSelf) {
        QiscusAccount qiscusAccount = new QiscusAccount();
        qiscusAccount.setId(jsonAccount.get("id").getAsInt());
        qiscusAccount.setUsername(jsonAccount.get("username").getAsString());
        qiscusAccount.setEmail(jsonAccount.get("email").getAsString());
        qiscusAccount.setAvatar(jsonAccount.get("avatar_url").getAsString());
        try {
            qiscusAccount.setExtras(jsonAccount.get("extras").isJsonNull() ? null :
                    new JSONObject(jsonAccount.get("extras").getAsString()));
        } catch (JSONException ignored) {
            //Do nothing
        }
        if (isSelf) {
            qiscusAccount.setToken(jsonAccount.get("token").getAsString());
        }
        return qiscusAccount;
    }

    static QiscusChatRoom parseQiscusChatRoom(JsonElement jsonElement) {
        if (jsonElement != null) {
            JsonObject jsonChatRoom = jsonElement.getAsJsonObject().get("results").getAsJsonObject().get("room").getAsJsonObject();
            QiscusChatRoom qiscusChatRoom = new QiscusChatRoom();
            qiscusChatRoom.setId(jsonChatRoom.get("id").getAsLong());
            qiscusChatRoom.setGroup(!"single".equals(jsonChatRoom.get("chat_type").getAsString()));
            qiscusChatRoom.setName(jsonChatRoom.get("room_name").getAsString());

            if (qiscusChatRoom.isGroup()) {
                qiscusChatRoom.setDistinctId(jsonChatRoom.get("unique_id").getAsString());
            } else {
                qiscusChatRoom.setDistinctId(jsonChatRoom.get("raw_room_name").getAsString());
            }

            qiscusChatRoom.setUniqueId(jsonChatRoom.get("unique_id").getAsString());
            try {
                qiscusChatRoom.setOptions(jsonChatRoom.get("options").isJsonNull() ? null :
                        new JSONObject(jsonChatRoom.get("options").getAsString()));
            } catch (JSONException ignored) {
                //Do nothing
            }
            qiscusChatRoom.setAvatarUrl(jsonChatRoom.get("avatar_url").getAsString());

            if (jsonChatRoom.has("is_public_channel")) {
                qiscusChatRoom.setChannel(jsonChatRoom.get("is_public_channel").getAsBoolean());
            }

            if (jsonChatRoom.has("room_total_participants")) {
                qiscusChatRoom.setMemberCount(jsonChatRoom.get("room_total_participants").getAsInt());
            }

            JsonElement participants = jsonChatRoom.get("participants");
            List<QiscusRoomMember> members = new ArrayList<>();
            if (participants.isJsonArray()) {
                JsonArray jsonMembers = participants.getAsJsonArray();
                for (JsonElement jsonMember : jsonMembers) {
                    members.add(parseQiscusRoomMember(jsonMember.getAsJsonObject()));
                }
            }
            qiscusChatRoom.setMember(members);

            JsonArray comments = jsonElement.getAsJsonObject().get("results")
                    .getAsJsonObject().get("comments").getAsJsonArray();

            if (comments.size() > 0) {
                QiscusComment latestComment = parseQiscusComment(comments.get(0), qiscusChatRoom.getId());
                determineCommentState(latestComment, members);
                qiscusChatRoom.setLastComment(latestComment);
            }
            return qiscusChatRoom;
        }

        return null;
    }

    static QiscusRoomMember parseQiscusRoomMember(JsonObject jsonMember) {
        QiscusRoomMember member = new QiscusRoomMember();
        member.setEmail(jsonMember.get("email").getAsString());
        member.setAvatar(jsonMember.get("avatar_url").getAsString());
        member.setUsername(jsonMember.get("username").getAsString());

        try {
            member.setExtras(jsonMember.get("extras").isJsonNull() ? null :
                    new JSONObject(jsonMember.get("extras").getAsString()));
        } catch (JSONException ignored) {
            //Do nothing
        }

        if (jsonMember.getAsJsonObject().has("last_comment_received_id")) {
            member.setLastDeliveredCommentId(jsonMember.getAsJsonObject().get("last_comment_received_id").getAsInt());
        }
        if (jsonMember.getAsJsonObject().has("last_comment_read_id")) {
            member.setLastReadCommentId(jsonMember.getAsJsonObject().get("last_comment_read_id").getAsInt());
        }
        return member;
    }

    static List<QiscusChatRoom> parseQiscusChatRoomInfo(JsonElement jsonElement) {
        List<QiscusChatRoom> qiscusChatRooms = new ArrayList<>();
        if (jsonElement != null) {
            JsonArray jsonRoomInfo = jsonElement.getAsJsonObject().get("results").getAsJsonObject().get("rooms_info").getAsJsonArray();
            for (JsonElement item : jsonRoomInfo) {
                JsonObject jsonChatRoom = item.getAsJsonObject();
                QiscusChatRoom qiscusChatRoom = new QiscusChatRoom();
                qiscusChatRoom.setId(jsonChatRoom.get("id").getAsLong());
                qiscusChatRoom.setGroup(!"single".equals(jsonChatRoom.get("chat_type").getAsString()));
                qiscusChatRoom.setName(jsonChatRoom.get("room_name").getAsString());

                if (qiscusChatRoom.isGroup()) {
                    qiscusChatRoom.setDistinctId(jsonChatRoom.get("unique_id").getAsString());
                } else {
                    qiscusChatRoom.setDistinctId(jsonChatRoom.get("raw_room_name").getAsString());
                }

                qiscusChatRoom.setUniqueId(jsonChatRoom.get("unique_id").getAsString());
                try {
                    qiscusChatRoom.setOptions(jsonChatRoom.get("options").isJsonNull() ? null :
                            new JSONObject(jsonChatRoom.get("options").getAsString()));
                } catch (JSONException ignored) {
                    //Do nothing
                }
                qiscusChatRoom.setAvatarUrl(jsonChatRoom.get("avatar_url").getAsString());
                qiscusChatRoom.setUnreadCount(jsonChatRoom.get("unread_count").getAsInt());

                if (jsonChatRoom.has("is_public_channel")) {
                    qiscusChatRoom.setChannel(jsonChatRoom.get("is_public_channel").getAsBoolean());
                }

                if (jsonChatRoom.has("room_total_participants")) {
                    qiscusChatRoom.setMemberCount(jsonChatRoom.get("room_total_participants").getAsInt());
                }

                List<QiscusRoomMember> members = new ArrayList<>();
                if (jsonChatRoom.has("participants") && jsonChatRoom.get("participants").isJsonArray()) {
                    JsonArray jsonMembers = jsonChatRoom.get("participants").getAsJsonArray();
                    for (JsonElement jsonMember : jsonMembers) {
                        QiscusRoomMember member = new QiscusRoomMember();
                        member.setEmail(jsonMember.getAsJsonObject().get("email").getAsString());
                        member.setAvatar(jsonMember.getAsJsonObject().get("avatar_url").getAsString());
                        member.setUsername(jsonMember.getAsJsonObject().get("username").getAsString());
                        if (jsonMember.getAsJsonObject().has("last_comment_received_id")) {
                            member.setLastDeliveredCommentId(jsonMember.getAsJsonObject().get("last_comment_received_id").getAsLong());
                        }
                        if (jsonMember.getAsJsonObject().has("last_comment_read_id")) {
                            member.setLastReadCommentId(jsonMember.getAsJsonObject().get("last_comment_read_id").getAsLong());
                        }
                        members.add(member);
                    }
                }
                qiscusChatRoom.setMember(members);

                QiscusComment latestComment = parseQiscusComment(jsonChatRoom.get("last_comment"), qiscusChatRoom.getId());
                if (qiscusChatRoom.getMember() != null) {
                    determineCommentState(latestComment, qiscusChatRoom.getMember());
                }
                qiscusChatRoom.setLastComment(latestComment);

                qiscusChatRooms.add(qiscusChatRoom);
            }
            return qiscusChatRooms;
        }
        return qiscusChatRooms;
    }

    static Pair<QiscusChatRoom, List<QiscusComment>> parseQiscusChatRoomWithComments(JsonElement jsonElement) {
        if (jsonElement != null) {
            QiscusChatRoom qiscusChatRoom = parseQiscusChatRoom(jsonElement);

            JsonArray comments = jsonElement.getAsJsonObject().get("results").getAsJsonObject().get("comments").getAsJsonArray();
            List<QiscusComment> qiscusComments = new ArrayList<>();
            for (JsonElement jsonComment : comments) {
                qiscusComments.add(parseQiscusComment(jsonComment, qiscusChatRoom.getId()));
            }

            return Pair.create(qiscusChatRoom, qiscusComments);
        }

        return null;
    }

    static QiscusComment parseQiscusComment(JsonElement jsonElement, long roomId) {
        QiscusComment qiscusComment = new QiscusComment();
        JsonObject jsonComment = jsonElement.getAsJsonObject();
        qiscusComment.setRoomId(roomId);
        qiscusComment.setId(jsonComment.get("id").getAsLong());
        qiscusComment.setCommentBeforeId(jsonComment.get("comment_before_id").getAsLong());
        qiscusComment.setMessage(jsonComment.get("message").getAsString());
        qiscusComment.setSender(jsonComment.get("username").getAsString());
        qiscusComment.setSenderEmail(jsonComment.get("email").getAsString());
        qiscusComment.setSenderAvatar(jsonComment.get("user_avatar_url").getAsString());
        qiscusComment.setState(QiscusComment.STATE_ON_QISCUS);

        //timestamp is in nano seconds format, convert it to milliseconds by divide it
        long timestamp = jsonComment.get("unix_nano_timestamp").getAsLong() / 1000000L;
        qiscusComment.setTime(new Date(timestamp));

        if (jsonComment.has("is_deleted")) {
            qiscusComment.setDeleted(jsonComment.get("is_deleted").getAsBoolean());
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
                    if (QiscusTextUtil.isNotBlank(text)) {
                        qiscusComment.setMessage(text.trim());
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

        return qiscusComment;
    }

    private static Pair<Long, Long> getPairedLastState(List<QiscusRoomMember> members) {
        long lastDelivered = Long.MAX_VALUE;
        long lastRead = Long.MAX_VALUE;
        QiscusAccount account = QiscusCore.getQiscusAccount();
        for (QiscusRoomMember member : members) {
            if (!member.getEmail().equals(account.getEmail())) {
                if (member.getLastDeliveredCommentId() < lastDelivered) {
                    lastDelivered = member.getLastDeliveredCommentId();
                }

                if (member.getLastReadCommentId() < lastRead) {
                    lastRead = member.getLastReadCommentId();
                    if (lastRead > lastDelivered) {
                        lastDelivered = lastRead;
                    }
                }
            }
        }

        if (lastDelivered == Long.MAX_VALUE) {
            lastDelivered = 0;
        }

        if (lastRead == Long.MAX_VALUE) {
            lastRead = 0;
        }

        return Pair.create(lastDelivered, lastRead);
    }

    private static void determineCommentState(QiscusComment comment, List<QiscusRoomMember> members) {
        if (members == null || members.isEmpty()) {
            return;
        }
        Pair<Long, Long> lastMemberState = getPairedLastState(members);
        if (comment.getId() > lastMemberState.first) {
            comment.setState(QiscusComment.STATE_ON_QISCUS);
        } else if (comment.getId() > lastMemberState.second) {
            comment.setState(QiscusComment.STATE_DELIVERED);
        } else {
            comment.setState(QiscusComment.STATE_READ);
        }
    }
}
