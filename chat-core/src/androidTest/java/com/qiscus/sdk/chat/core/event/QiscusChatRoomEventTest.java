package com.qiscus.sdk.chat.core.event;

import static org.junit.Assert.*;

import com.qiscus.sdk.chat.core.InstrumentationBaseTest;
import com.qiscus.sdk.chat.core.QiscusCore;
import com.qiscus.sdk.chat.core.data.model.QiscusAccount;
import com.qiscus.sdk.chat.core.data.remote.QiscusApiParser;
import com.qiscus.sdk.chat.core.event.QiscusChatRoomEvent;

import junit.framework.TestCase;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

public class QiscusChatRoomEventTest extends InstrumentationBaseTest {

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

        QiscusChatRoomEvent event = new QiscusChatRoomEvent();
    }

    @Test
    public void test(){
        QiscusChatRoomEvent event = new QiscusChatRoomEvent()
                .setRoomId(123)
                .setEventData(new JSONObject())
                .setUser("arief94")
                .setEvent(QiscusChatRoomEvent.Event.DELIVERED)
                .setTyping(true)
                .setCommentId(1234)
                .setCommentUniqueId("12323abc");
        EventBus.getDefault().post(event);

        event.getEvent();
        event.getEventData();
        event.getRoomId();
        event.getCommentId();
        event.getUser();
        event.getCommentUniqueId();
        event.isTyping();
        event.toString();
        event.getClass();
    }

    @Test
    public void test2(){
        QiscusChatRoomEvent event = new QiscusChatRoomEvent()
                .setRoomId(123)
                .setEventData(new JSONObject())
                .setUser("arief94")
                .setEvent(QiscusChatRoomEvent.Event.DELIVERED)
                .setTyping(false)
                .setCommentId(1234)
                .setCommentUniqueId("12323abc");
        EventBus.getDefault().post(event);

        event.getEvent();
        event.getEventData();
        event.getRoomId();
        event.getCommentId();
        event.getUser();
        event.getCommentUniqueId();
        event.isTyping();
        event.toString();
        event.getClass();
    }
}