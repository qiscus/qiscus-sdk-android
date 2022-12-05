package com.qiscus.sdk.chat.core.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.qiscus.sdk.chat.core.BuildConfig;
import com.qiscus.sdk.chat.core.QiscusCore;
import com.qiscus.sdk.chat.core.data.model.QiscusChatRoom;
import com.qiscus.sdk.chat.core.data.model.QiscusComment;

import java.util.HashMap;
import java.util.List;

public class QiscusHashMapUtil {

    public static HashMap<String, Object> eventReport(String moduleName, String event, String message) {
        HashMap<String, Object> hashMap = new HashMap<>();

        hashMap.put("module_name", moduleName);
        hashMap.put("event", event);
        hashMap.put("message", message);

        return hashMap;
    }

    public static HashMap<String, Object> login(String identityToken) {
        HashMap<String, Object> hashMap = new HashMap<>();

        hashMap.put("identity_token", identityToken);

        return hashMap;
    }

    public static HashMap<String, Object> loginOrRegister(String email, String password, String username, String avatarUrl, String extras) {
        HashMap<String, Object> hashMap = new HashMap<>();

        JsonObject jsonExtras = null;
        if (extras != null && !extras.equals("")) {
            jsonExtras = new JsonParser().parse(extras).getAsJsonObject();
        }

        hashMap.put("email", email);
        hashMap.put("password", password);
        hashMap.put("username", username);
        hashMap.put("avatar_url", avatarUrl);
        hashMap.put("extras", jsonExtras);

        return hashMap;
    }

    public static HashMap<String, Object> updateProfile(String username, String avatarUrl, String extras) {
        HashMap<String, Object> hashMap = new HashMap<>();

        JsonObject jsonExtras = null;
        if (extras != null && !extras.equals("")) {
            jsonExtras = new JsonParser().parse(extras).getAsJsonObject();
        }

        hashMap.put("name", username);
        hashMap.put("avatar_url", avatarUrl);
        hashMap.put("extras", jsonExtras);

        return hashMap;
    }

    public static HashMap<String, Object> getChatRoom(Object withEmail, String options) {
        HashMap<String, Object> hashMap = new HashMap<>();

        hashMap.put("emails", withEmail);
        hashMap.put("options", options);

        return hashMap;
    }

    public static HashMap<String, Object> createGroupChatRoom(String name, List<String> emails, String avatarUrl, String options) {
        HashMap<String, Object> hashMap = new HashMap<>();

        hashMap.put("name", name);
        hashMap.put("participants", emails);
        hashMap.put("avatar_url", avatarUrl);
        hashMap.put("options", options);

        return hashMap;
    }

    public static HashMap<String, Object> getGroupChatRoom(String uniqueId, String name, String avatarUrl, String options) {
        HashMap<String, Object> hashMap = new HashMap<>();

        hashMap.put("unique_id", uniqueId);
        hashMap.put("name", name);
        hashMap.put("avatar_url", avatarUrl);
        hashMap.put("options", options);

        return hashMap;
    }

    public static HashMap<String, Object> postComment(QiscusComment qiscusComment) {
        HashMap<String, Object> hashMap = new HashMap<>();

        JsonObject jsonPayload = null;
        JsonObject jsonExtras = null;

        if (qiscusComment.getExtraPayload() != null && !qiscusComment.getExtraPayload().equals("")) {
            jsonPayload = new JsonParser().parse(qiscusComment.getExtraPayload()).getAsJsonObject();
        }

        if (qiscusComment.getExtras() != null) {
            jsonExtras = new JsonParser().parse(qiscusComment.getExtras().toString()).getAsJsonObject();
        }

        hashMap.put("comment", qiscusComment.getMessage());
        hashMap.put("topic_id", String.valueOf(qiscusComment.getRoomId()));
        hashMap.put("unique_temp_id", qiscusComment.getUniqueId());
        hashMap.put("type", qiscusComment.getRawType());
        hashMap.put("payload", jsonPayload);
        hashMap.put("extras", jsonExtras);

        return hashMap;
    }

    public static HashMap<String, Object> updateComment(QiscusComment qiscusComment) {
        HashMap<String, Object> hashMap = new HashMap<>();

        JsonObject jsonPayload = null;
        JsonObject jsonExtras = null;

        if (qiscusComment.getExtraPayload() != null && !qiscusComment.getExtraPayload().equals("")) {
            jsonPayload = new JsonParser().parse(qiscusComment.getExtraPayload()).getAsJsonObject();
        }

        if (qiscusComment.getExtras() != null) {
            jsonExtras = new JsonParser().parse(qiscusComment.getExtras().toString()).getAsJsonObject();
        }

        hashMap.put("comment", qiscusComment.getMessage());
        hashMap.put("unique_id", qiscusComment.getUniqueId());
        hashMap.put("token", QiscusCore.getQiscusAccount().getToken());
        hashMap.put("payload", jsonPayload);
        hashMap.put("extras", jsonExtras);

        return hashMap;
    }

    public static HashMap<String, Object> updateChatRoom(String roomId, String name, String avatarUrl, String options) {
        HashMap<String, Object> hashMap = new HashMap<>();

        hashMap.put("id", roomId);
        hashMap.put("room_name", name);
        hashMap.put("avatar_url", avatarUrl);
        hashMap.put("options", options);

        return hashMap;
    }

    public static HashMap<String, Object> updateCommentStatus(String roomId, String lastReadId, String lastReceivedId) {
        HashMap<String, Object> hashMap = new HashMap<>();

        hashMap.put("room_id", roomId);
        hashMap.put("last_comment_read_id", lastReadId);
        hashMap.put("last_comment_received_id", lastReceivedId);

        return hashMap;
    }

    public static HashMap<String, Object> registerOrRemoveFcmToken(String fcmToken) {
        HashMap<String, Object> hashMap = new HashMap<>();

        hashMap.put("device_platform", "android");
        hashMap.put("device_token", fcmToken);
        hashMap.put("is_development", BuildConfig.DEBUG);

        return hashMap;
    }

    public static HashMap<String, Object> getChatRooms(Object roomIds, Object roomUniqueIds, boolean showParticipants,
                                                       boolean showRemoved) {
        HashMap<String, Object> hashMap = new HashMap<>();


        if (roomIds != null) {
            hashMap.put("room_id", roomIds);
        }

        if (roomUniqueIds != null) {
            hashMap.put("room_unique_id", roomUniqueIds);
        }

        hashMap.put("show_participants", showParticipants);
        hashMap.put("show_removed", showRemoved);

        return hashMap;
    }

    public static HashMap<String, Object> addRoomMember(String roomId, List<String> emails) {
        HashMap<String, Object> hashMap = new HashMap<>();

        hashMap.put("room_id", roomId);
        hashMap.put("emails", emails);

        return hashMap;
    }

    public static HashMap<String, Object> removeRoomMember(String roomId, List<String> emails) {
        HashMap<String, Object> hashMap = new HashMap<>();

        hashMap.put("room_id", roomId);
        hashMap.put("emails", emails);

        return hashMap;
    }

    public static HashMap<String, Object> blockUser(String userEmail) {
        HashMap<String, Object> hashMap = new HashMap<>();

        hashMap.put("user_email", userEmail);

        return hashMap;
    }

    public static HashMap<String, Object> unblockUser(String userEmail) {
        HashMap<String, Object> hashMap = new HashMap<>();

        hashMap.put("user_email", userEmail);

        return hashMap;
    }

    public static HashMap<String, Object> getRealtimeStatus(String topic) {
        HashMap<String, Object> hashMap = new HashMap<>();

        hashMap.put("topic", topic);

        return hashMap;
    }

    public static HashMap<String, Object> getChannelsInfo(List<String> uniqueIds) {
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("unique_ids", uniqueIds);

        return hashMap;
    }

    public static HashMap<String, Object> joinChannels(List<String> uniqueIds) {
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("unique_ids", uniqueIds);

        return hashMap;
    }

    public static HashMap<String, Object> leaveChannels(List<String> uniqueIds) {
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("unique_ids", uniqueIds);

        return hashMap;
    }

    public static HashMap<String, Object> usersPresence(List<String> userIds) {
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("user_ids", userIds);

        return hashMap;
    }

    public static HashMap<String, Object> fileList(List<String> roomIds, String fileType,
                                                   String userId, List<String> includeExtensions,
                                                   List<String> excludeExtensions, int page, int limit) {
        HashMap<String, Object> hashMap = new HashMap<>();
        if (roomIds.size() != 0) {
            hashMap.put("room_ids", roomIds);
        }

        if (fileType != null && userId.isEmpty()) {
            hashMap.put("file_type", fileType);
        }

        hashMap.put("page", page);
        hashMap.put("limit", limit);

        if (userId != null && userId.isEmpty()) {
            hashMap.put("sender", userId);
        }

        if (includeExtensions != null && includeExtensions.size() != 0) {
            hashMap.put("include_extensions", includeExtensions);
        }

        if (excludeExtensions != null && excludeExtensions.size() != 0) {
            hashMap.put("exclude_extensions", excludeExtensions);
        }

        return hashMap;
    }

    public static HashMap<String, Object> searchMessage(String query, List<String> roomIds, String userId,
                                                        List<String> type, QiscusChatRoom.RoomType roomType, int page, int limit) {
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("query", query);
        hashMap.put("room_ids", roomIds);
        hashMap.put("type", type);
        hashMap.put("sender", userId);
        hashMap.put("page", page);
        hashMap.put("limit", limit);

        if (roomType != null) {
            if (roomType == QiscusChatRoom.RoomType.SINGLE) {
                hashMap.put("room_type", "single");
                hashMap.put("is_public", false);
            } else if (roomType == QiscusChatRoom.RoomType.GROUP) {
                hashMap.put("room_type", "group");
                hashMap.put("is_public", false);
            } else if (roomType == QiscusChatRoom.RoomType.CHANNEL)  {
                hashMap.put("room_type", "group");
                hashMap.put("is_public", true);
            }
        }

        return hashMap;
    }

    public static HashMap<String, Object> refreshToken(String userId, String refreshToken) {
        HashMap<String, Object> hashMap = new HashMap<>();

        hashMap.put("user_id", userId);
        hashMap.put("refresh_token", refreshToken);

        return hashMap;
    }

    public static HashMap<String, Object> logout(String userId, String token) {
        HashMap<String, Object> hashMap = new HashMap<>();

        hashMap.put("user_id", userId);
        hashMap.put("token", token);

        return hashMap;
    }
}