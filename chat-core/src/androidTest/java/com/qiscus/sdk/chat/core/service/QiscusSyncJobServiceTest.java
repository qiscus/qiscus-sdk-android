package com.qiscus.sdk.chat.core.service;

import com.qiscus.sdk.chat.core.InstrumentationBaseTest;
import com.qiscus.sdk.chat.core.QiscusCore;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class QiscusSyncJobServiceTest extends InstrumentationBaseTest {

    private QiscusSyncJobService service;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        service = new QiscusSyncJobService();
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
        service = null;
    }

    @Test
    @Override
    public void setupEngine() {
        super.setupEngine();
    }

    @Test
    public void syncJobTest() {
        QiscusCore.closeRealtimeConnection();
        service.syncJob(context);
        QiscusCore.setEnableDisableRealtime(false);
        service.syncJob(context);
    }

    @Test
    public void onStartJobTest() {
        service.onStartJob(null);
    }

    @Test
    public void onStopJobTest() {
        service.onStopJob(null);
    }
}