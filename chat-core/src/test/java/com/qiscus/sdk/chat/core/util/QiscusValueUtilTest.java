package com.qiscus.sdk.chat.core.util;

import static org.junit.Assert.*;

import org.junit.Test;

public class QiscusValueUtilTest {

    @Test
    public void getValueDataResults() {
        assertEquals(QiscusValueUtil.getValueDataResults(), "results");
    }
}