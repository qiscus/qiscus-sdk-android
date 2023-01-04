package com.qiscus.sdk.chat.core.util;

import android.content.Context;

import com.qiscus.sdk.chat.core.InstrumentationBaseTest;
import com.qiscus.sdk.chat.core.QiscusCore;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class QiscusAndroidUtilTest extends InstrumentationBaseTest {

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
    public void runOnUIThreadTest() {
        QiscusAndroidUtil.runOnBackgroundThread(() -> {

        }, 10);
    }

    @Test
    public void compareTest() {
        QiscusAndroidUtil.compare(2, 1);
        QiscusAndroidUtil.compare(1, 2);
        QiscusAndroidUtil.compare(1, 1);
    }

    @Test
    public void testCompareTest() {
        QiscusAndroidUtil.compare(2L, 1L);
        QiscusAndroidUtil.compare(1L, 2L);
        QiscusAndroidUtil.compare(1L, 1L);
    }
}