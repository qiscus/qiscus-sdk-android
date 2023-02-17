package com.qiscus.sdk.chat.core.util;

import static org.junit.Assert.*;

import com.qiscus.sdk.chat.core.InstrumentationBaseTest;
import com.qiscus.sdk.chat.core.QiscusCore;
import com.qiscus.sdk.chat.core.data.model.QiscusCoreChatConfig;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;

public class QiscusLoggerTest extends InstrumentationBaseTest {

    @Before
    public void setUp() throws Exception {
        super.setUp();
    }


    @Test
    @Override
    public void setupEngine() {
        super.setupEngine();
        QiscusCore.getChatConfig().setEnableLog(true);

        QiscusLogger logger = new QiscusLogger();

        logger.print(
                "test","true");
    }



}