package com.qiscus.sdk.chat.core.data.model;

import static org.junit.Assert.*;

import com.qiscus.sdk.chat.core.InstrumentationBaseTest;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;

public class QiscusImageCompressionConfigTest extends InstrumentationBaseTest {

    @Before
    public void setUp() throws Exception {
        super.setUp();
        super.setupEngine();

        QiscusImageCompressionConfig config = new QiscusImageCompressionConfig(100,100,100);
        config.getMaxHeight();
        config.getMaxWidth();
        config.setMaxHeight(100);
        config.setMaxWidth(100);
        config.getQuality();
        config.setQuality(100);
    }
}