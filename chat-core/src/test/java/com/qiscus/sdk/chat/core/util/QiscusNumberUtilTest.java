package com.qiscus.sdk.chat.core.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class QiscusNumberUtilTest {
    @Test
    public void convertToInt() {
        assertEquals(QiscusNumberUtil.convertToInt(19L), 19);
    }

    @Test
    public void testConvertToInt() {
        assertEquals(QiscusNumberUtil.convertToInt(19L), 19);
    }
}