package com.qiscus.sdk.chat.core.util;


import static net.bytebuddy.matcher.ElementMatchers.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import net.bytebuddy.matcher.ElementMatcher;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

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