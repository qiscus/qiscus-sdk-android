package com.qiscus.sdk.chat.core.util;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class QiscusPushNotificationUtilTest extends QiscusFirebaseMessagingUtilTest {

    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }


    @Test
    public void runTest() {
        QiscusPushNotificationUtil.clearPushNotification(context, 100l);
    }
}