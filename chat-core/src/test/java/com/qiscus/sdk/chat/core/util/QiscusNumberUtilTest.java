package com.qiscus.sdk.chat.core.util;

import static org.junit.Assert.assertEquals;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class QiscusNumberUtilTest {
    protected static QiscusNumberUtil qiscusNumberUtil;

    @BeforeClass
    public static void beforeClass() throws Exception {
        qiscusNumberUtil = new QiscusNumberUtil();
    }

    @AfterClass
    public static void afterClass() throws Exception {
        qiscusNumberUtil = null;
    }

    @Test
    public void convertToInt() {
        assertEquals(qiscusNumberUtil.convertToInt(19L), 19);
    }

    @Test
    public void testConvertToInt() {
        assertEquals(qiscusNumberUtil.convertToInt(19L), 19);
    }
}