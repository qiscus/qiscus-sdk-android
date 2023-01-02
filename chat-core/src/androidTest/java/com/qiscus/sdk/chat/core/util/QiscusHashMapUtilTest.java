package com.qiscus.sdk.chat.core.util;

import static org.junit.Assert.*;

import com.qiscus.sdk.chat.core.InstrumentationBaseTest;
import com.qiscus.sdk.chat.core.QiscusCore;
import com.qiscus.sdk.chat.core.data.model.QiscusAccount;
import com.qiscus.sdk.chat.core.data.model.QiscusChatRoom;
import com.qiscus.sdk.chat.core.data.model.QiscusComment;
import com.qiscus.sdk.chat.core.event.QiscusChatRoomEvent;

import junit.framework.TestCase;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

public class QiscusHashMapUtilTest extends InstrumentationBaseTest {

    @Before
    public void setUp() throws Exception {
        super.setUp();
        QiscusCore.setup(application, "sdksample");

        QiscusCore.setUser("arief92", "arief92")
                .withUsername("arief92")
                .withAvatarUrl("https://")
                .withExtras(null)
                .save(new QiscusCore.SetUserListener() {
                    @Override
                    public void onSuccess(QiscusAccount qiscusAccount) {
                        //on success


                    }
                    @Override
                    public void onError(Throwable throwable) {
                        //on error
                    }});

        QiscusHashMapUtil hashMapUtil = new QiscusHashMapUtil();
    }

    @Test
    public void updateComment(){
        QiscusComment comment = QiscusComment.generateMessage(123,"test");

        try {
            comment.setExtras(new JSONObject("{}"));
            comment.setExtras(new JSONObject("{}"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        QiscusHashMapUtil.updateComment(comment);
    }

    @Test
    public void updateComment2(){
        QiscusComment comment = QiscusComment.generateMessage(123,"test");

        comment.setExtras(new JSONObject());
        comment.setExtras(new JSONObject());
        QiscusHashMapUtil.updateComment(comment);
    }

    @Test
    public void updateChatRoom(){
        QiscusHashMapUtil.updateChatRoom("123","name", "https://","{}");
    }

    @Test
    public void getRealtimeStatus(){
        QiscusHashMapUtil.getRealtimeStatus("topic/");
    }

    @Test
    public void searchMessage(){
        ArrayList<String> ids = new ArrayList<String>();
        ids.add(String.valueOf(Long.valueOf("1234")));

        ArrayList<String> type = new ArrayList<String>();
        type.add("type");

        QiscusHashMapUtil.searchMessage("test",ids,"arief94",type,  QiscusChatRoom.RoomType.GROUP,1,100);
    }

    @Test
    public void searchMessage2(){
        ArrayList<String> ids = new ArrayList<String>();
        ids.add(String.valueOf(Long.valueOf("1234")));

        ArrayList<String> type = new ArrayList<String>();
        type.add("type");

        QiscusHashMapUtil.searchMessage("test",ids,"arief94",type,  QiscusChatRoom.RoomType.CHANNEL,1,100);
    }
}