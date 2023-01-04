package com.qiscus.sdk.chat.core.service;

import static org.junit.Assert.*;

import android.content.Intent;

import com.qiscus.sdk.chat.core.InstrumentationBaseTest;
import com.qiscus.sdk.chat.core.QiscusCore;
import com.qiscus.sdk.chat.core.service.QiscusNetworkStateReceiver;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class QiscusNetworkStateReceiverTest extends InstrumentationBaseTest {

    private QiscusNetworkStateReceiver receiver;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        receiver = new QiscusNetworkStateReceiver();
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Override
    public void setupEngine() {
        super.setupEngine();
    }

    @Test
    public void onReceiveTest() {
        receiver.onReceive(context, new Intent());
    }
}