package com.qiscus.sdk.chat.core.event;

import static org.junit.Assert.*;

import com.qiscus.sdk.chat.core.InstrumentationBaseTest;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;

public class QiscusRefreshTokenEventTest extends InstrumentationBaseTest {

    @Before
    public void setUp() throws Exception {
        super.setUp();

        super.setupEngine();

        QiscusRefreshTokenEvent event = new QiscusRefreshTokenEvent(403, "Unauthorized. Token is expired");
        event.getCode();
        event.getMessage();
        event.isTokenExpired();
        event.isUnauthorized();

        QiscusRefreshTokenEvent event2 = new QiscusRefreshTokenEvent(401, "Unauthorized");
        event2.getCode();
        event2.getMessage();
        event2.isTokenExpired();
        event2.isUnauthorized();

        QiscusRefreshTokenEvent event3 = new QiscusRefreshTokenEvent(500, "Error");
        event3.getCode();
        event3.getMessage();
        event3.isTokenExpired();
        event3.isUnauthorized();

        QiscusRefreshTokenEvent event4 = new QiscusRefreshTokenEvent(403, "Unauthorized");
        event4.getCode();
        event4.getMessage();
        event4.isTokenExpired();
        event4.isUnauthorized();

    }
}