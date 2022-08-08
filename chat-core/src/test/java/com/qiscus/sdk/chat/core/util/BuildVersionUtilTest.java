package com.qiscus.sdk.chat.core.util;

import static org.junit.Assert.*;

import android.os.Build;

import org.junit.Test;

public class BuildVersionUtilTest {

    @Test
    public void isOreoLower() {
        assertTrue(BuildVersionUtil.isOreoLower());
    }

    @Test
    public void isOreoOrHigher() {
        assertTrue(BuildVersionUtil.isOreoOrHigher());
    }

    @Test
    public void isNougatOrHigher() {
        assertTrue(BuildVersionUtil.isNougatOrHigher());
    }

    @Test
    public void isAtLeastNMR1() {
        assertTrue(BuildVersionUtil.isAtLeastNMR1());
    }
}