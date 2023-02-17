package com.qiscus.sdk.chat.core.data.local;

import static org.junit.Assert.*;

import com.qiscus.sdk.chat.core.InstrumentationBaseTest;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;

public class QiscusEventCacheTest extends InstrumentationBaseTest {

    @Before
    public void setUp() throws Exception {
        super.setUp();
        super.setupEngine();
    }

    @Test
    public void saveLastEventId(){
        QiscusEventCache.getInstance().saveLastEventId(999999999);
    }


}