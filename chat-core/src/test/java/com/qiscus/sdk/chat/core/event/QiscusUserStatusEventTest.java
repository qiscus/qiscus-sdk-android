package com.qiscus.sdk.chat.core.event;

import static org.junit.Assert.*;

import org.junit.Test;

import java.util.Date;

public class QiscusUserStatusEventTest {

    private QiscusUserStatusEvent event = new QiscusUserStatusEvent("user", true, new Date());

    @Test
    public void getUserTest() {
        assertNotNull(
                event.getUser()
        );
    }

    @Test
    public void isOnlineTest() {
        assertNotNull(
                event.isOnline()
        );
    }

    @Test
    public void getLastActiveTest() {
        assertNotNull(
                event.getLastActive()
        );
    }
}