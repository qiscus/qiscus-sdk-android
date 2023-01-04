package com.qiscus.sdk.chat.core.event;

import static org.junit.Assert.*;

import com.qiscus.sdk.chat.core.InstrumentationBaseTest;
import com.qiscus.sdk.chat.core.QiscusCore;
import com.qiscus.sdk.chat.core.data.model.QiscusAccount;
import com.qiscus.sdk.chat.core.event.QiscusClearCommentsEvent;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;

import java.util.Date;

public class QiscusClearCommentsEventTest extends InstrumentationBaseTest {

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

    }

    @Test
    public void testEvent(){
        QiscusClearCommentsEvent event = new QiscusClearCommentsEvent(123);
        event.getRoomId();
        event.getTimestamp();

        QiscusClearCommentsEvent event2 = new QiscusClearCommentsEvent(123,1234566);
    }
}