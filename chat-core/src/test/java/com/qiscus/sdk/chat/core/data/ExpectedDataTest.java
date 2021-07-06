package com.qiscus.sdk.chat.core.data;

import com.qiscus.sdk.chat.core.data.model.QiscusComment;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

public class ExpectedDataTest {

    public static QiscusComment qiscusCommentForTest() {
        QiscusComment qiscusComment = new QiscusComment();

        qiscusComment.setRoomId(10185397L);
        qiscusComment.setId(575122484L);
        qiscusComment.setUniqueId("android_1619573289531tfiiz2y02f07eb75ff434c18");
        qiscusComment.setCommentBeforeId(575122483L);
        qiscusComment.setMessage("Ya");
        qiscusComment.setSender("arief92");
        qiscusComment.setSenderEmail("arief92");
        qiscusComment.setSenderAvatar("https://robohash.org/arief92/bgset_bg2/3.14160?set=set4");
        qiscusComment.setState(QiscusComment.STATE_READ);

        //timestamp is in nano seconds format, convert it to milliseconds by divide it
        long timestamp = 1619573296926964000L / 1000000L;
        qiscusComment.setTime(new Date(timestamp));

        qiscusComment.setDeleted(false);

        qiscusComment.setRoomName("arief92 arief93");

        qiscusComment.setGroupMessage(false);

        qiscusComment.setUniqueId("android_1619573289531tfiiz2y02f07eb75ff434c18");

        qiscusComment.setRawType("text");
        qiscusComment.setExtraPayload("{}");

        qiscusComment.setExtras(new JSONObject());

        try {
            JSONObject jsonUserExtras = new JSONObject();
            jsonUserExtras.put("alamat", "jogja");
            jsonUserExtras.put("comment", "the comment");
            jsonUserExtras.put("name", "name nya ini");
            qiscusComment.setUserExtras(jsonUserExtras);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return qiscusComment;
    }
}