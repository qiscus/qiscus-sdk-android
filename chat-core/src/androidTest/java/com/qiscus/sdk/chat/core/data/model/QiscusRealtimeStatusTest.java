package com.qiscus.sdk.chat.core.data.model;

import static org.junit.Assert.*;

import com.qiscus.sdk.chat.core.InstrumentationBaseTest;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;

public class QiscusRealtimeStatusTest extends InstrumentationBaseTest {

    @Before
    public void setUp() throws Exception {
        super.setUp();
        super.setupEngine();

        QiscusRealtimeStatus status = new QiscusRealtimeStatus();
        status.setRealtimeStatus(true);
        status.getRealtimeStatus();
        status.toString();
    }
}