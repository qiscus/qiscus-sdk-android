package com.qiscus.sdk.chat.core.service;

import static org.mockito.Mockito.mock;

import android.app.job.JobParameters;
import android.content.Intent;

import com.qiscus.sdk.chat.core.InstrumentationBaseTest;
import com.qiscus.sdk.chat.core.event.QiscusUserEvent;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

public class QiscusNetworkCheckerJobServiceTest extends InstrumentationBaseTest {

    private QiscusNetworkCheckerJobService receiver;
    private JobParameters params;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        MockitoAnnotations.openMocks(this);
        params = mock(JobParameters.class);
        receiver = new QiscusNetworkCheckerJobService();
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
    public void scheduleJobTest() {
        QiscusNetworkCheckerJobService.scheduleJob(context);
    }

    @Test
    public void onStartJobTest() {
        receiver.onStartJob(params);
    }

    @Test
    public void onStopJobTest() {
        receiver.onStopJob(params);
    }

    @Test
    public void onStartCommandTest() {
        receiver.onStartCommand(new Intent(), 0, 0);
    }

    @Test
    public void onUserEventTest() {
        receiver.onUserEvent(QiscusUserEvent.LOGIN);
//        receiver.onUserEvent(QiscusUserEvent.LOGOUT);
    }
}