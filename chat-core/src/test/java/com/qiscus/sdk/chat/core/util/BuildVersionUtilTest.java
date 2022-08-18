package com.qiscus.sdk.chat.core.util;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.os.Build;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class BuildVersionUtilTest {

    @Test
    public void isOreoLower() {
        if (BuildVersionUtil.isOreoLower()) {
            assertTrue(Build.VERSION.SDK_INT < Build.VERSION_CODES.O);
        }
    }

    @Test
    public void isOreoOrHigher() {
        if (BuildVersionUtil.isOreoOrHigher()) {
            assertEquals(Build.VERSION.SDK_INT, Build.VERSION_CODES.O);
            assertEquals(Build.VERSION.SDK_INT, Build.VERSION_CODES.O_MR1);
            assertEquals(Build.VERSION.SDK_INT, Build.VERSION_CODES.P);
            assertEquals(Build.VERSION.SDK_INT, Build.VERSION_CODES.Q);
            assertEquals(Build.VERSION.SDK_INT, Build.VERSION_CODES.R);
            assertEquals(Build.VERSION.SDK_INT, Build.VERSION_CODES.S);
            assertEquals(Build.VERSION.SDK_INT, Build.VERSION_CODES.S_V2);
        }
    }

    @Test
    public void isNougatOrHigher() {
        if (BuildVersionUtil.isNougatOrHigher()) {
            assertEquals(Build.VERSION.SDK_INT, Build.VERSION_CODES.O);
            assertEquals(Build.VERSION.SDK_INT, Build.VERSION_CODES.O_MR1);
            assertEquals(Build.VERSION.SDK_INT, Build.VERSION_CODES.P);
            assertEquals(Build.VERSION.SDK_INT, Build.VERSION_CODES.Q);
            assertEquals(Build.VERSION.SDK_INT, Build.VERSION_CODES.R);
            assertEquals(Build.VERSION.SDK_INT, Build.VERSION_CODES.S);
            assertEquals(Build.VERSION.SDK_INT, Build.VERSION_CODES.S_V2);
        }
    }

    @Test
    public void isAtLeastNMR1() {
        if (BuildVersionUtil.isAtLeastNMR1()) {
            assertEquals(Build.VERSION.SDK_INT, Build.VERSION_CODES.O);
            assertEquals(Build.VERSION.SDK_INT, Build.VERSION_CODES.O_MR1);
            assertEquals(Build.VERSION.SDK_INT, Build.VERSION_CODES.P);
            assertEquals(Build.VERSION.SDK_INT, Build.VERSION_CODES.Q);
            assertEquals(Build.VERSION.SDK_INT, Build.VERSION_CODES.R);
            assertEquals(Build.VERSION.SDK_INT, Build.VERSION_CODES.S);
            assertEquals(Build.VERSION.SDK_INT, Build.VERSION_CODES.S_V2);
        }
    }
}