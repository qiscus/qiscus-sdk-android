package com.qiscus.sdk.chat.core.util;

import com.qiscus.sdk.chat.core.InstrumentationBaseTest;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class QiscusServiceUtilTest extends InstrumentationBaseTest {

    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    @Override
    public void setupEngine() {
        super.setupEngine();
    }

    @Test
    public void isMyServiceRunningTest() {
        QiscusServiceUtil.isMyServiceRunning();
    }

    @Test
    public void isValidUrlTest() {
        QiscusServiceUtil.isValidUrl("https://www.ok.com");
        QiscusServiceUtil.isValidUrl("no_url");
    }
}