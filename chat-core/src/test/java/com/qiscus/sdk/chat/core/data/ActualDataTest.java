package com.qiscus.sdk.chat.core.data;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import org.json.JSONException;
import org.json.JSONObject;

public class ActualDataTest {
//    public JSONObject jsonComment = new JSONObject();

    public static JSONObject sampleJsonComment() {
        JSONObject jsonComment = new JSONObject();

        try {
            jsonComment.put("comment_before_id", 575122483L);
            jsonComment.put("comment_before_id_str", "575122483");
            jsonComment.put("disable_link_preview", false);
            jsonComment.put("email", "arief92");
            jsonComment.put("extras", new JSONObject());
            jsonComment.put("id", 575122484L);
            jsonComment.put("id_str", "575122484");
            jsonComment.put("is_deleted", false);
            jsonComment.put("is_public_channel", false);
            jsonComment.put("message", "Ya");
            jsonComment.put("payload", new JSONObject());
            jsonComment.put("room_avatar", "https://d1edrlpyc25xu0.cloudfront.net/kiwari-prod/image/upload/75r6s_jOHa/1507541871-avatar-mine.png");
            jsonComment.put("room_id", 10185397L);
            jsonComment.put("room_id_str", "10185397");
            jsonComment.put("room_name", "arief92 arief93");
            jsonComment.put("room_type", "single");
            jsonComment.put("status", "read");
            jsonComment.put("timestamp", "2021-04-28T01:28:16Z");
            jsonComment.put("topic_id", 10185397L);
            jsonComment.put("topic_id_str", "10185397");
            jsonComment.put("type", "text");
            jsonComment.put("unique_temp_id", "android_1619573289531tfiiz2y02f07eb75ff434c18");
            jsonComment.put("unix_nano_timestamp", 1619573296926964000L);
            jsonComment.put("unix_timestamp", 1619573296L);

            JSONObject jsonUserAvatar = new JSONObject();
            JSONObject jsonAvatar = new JSONObject();
            jsonAvatar.put("url", "https://robohash.org/arief92/bgset_bg2/3.14160?set=set4");
            jsonUserAvatar.put("avatar", jsonAvatar);

            jsonComment.put("user_avatar", jsonUserAvatar);
            jsonComment.put("user_avatar_url", "https://robohash.org/arief92/bgset_bg2/3.14160?set=set4");

            JSONObject jsonUserExtras = new JSONObject();
            jsonUserAvatar.put("alamat", "jogja");
            jsonUserAvatar.put("comment", "the comment");
            jsonUserAvatar.put("name", "name nya ini");

            jsonComment.put("user_id", 85983395);
            jsonComment.put("user_id_str", "85983395");
            jsonComment.put("username", "arief92");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonComment;
    }


    public static JsonElement jsonCommentForTest() {
        return new Gson().fromJson(sampleJsonComment().toString(), JsonElement.class);
    }

}
