package com.qiscus.sdk.chat.core.service;

import static org.junit.Assert.*;

import android.content.Intent;

import com.qiscus.sdk.chat.core.InstrumentationBaseTest;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class QiscusStartServiceReceiverTest extends InstrumentationBaseTest {

    private QiscusStartServiceReceiver receiver;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        receiver = new QiscusStartServiceReceiver();
    }

    @Test
    @Override
    public void setupEngine() {
        receiver.onReceive(
                context, new Intent()
        );

        super.setupEngine();
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
        receiver = null;
    }

    @Test
    public void onReceive() {
       receiver.onReceive(
                context, new Intent()
        );
    }
}