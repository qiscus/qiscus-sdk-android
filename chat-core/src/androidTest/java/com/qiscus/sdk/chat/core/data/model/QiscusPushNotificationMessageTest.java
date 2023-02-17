package com.qiscus.sdk.chat.core.data.model;

import static org.junit.Assert.*;

import com.qiscus.sdk.chat.core.InstrumentationBaseTest;
import com.qiscus.sdk.chat.core.QiscusCore;
import com.qiscus.sdk.chat.core.util.QiscusLogger;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;

public class QiscusPushNotificationMessageTest extends InstrumentationBaseTest {

    @Before
    public void setUp() throws Exception {
        super.setUp();
        super.setupEngine();
    }

    @Test
    public void pn(){
        QiscusPushNotificationMessage pn = new QiscusPushNotificationMessage(1234,"test");
        pn.getCommentId();
        pn.hashCode();
        pn.toString();
        pn.describeContents();

    }

}