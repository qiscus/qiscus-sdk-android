package com.qiscus.sdk.chat.core.util;

import com.qiscus.sdk.chat.core.InstrumentationBaseTest;
import com.qiscus.sdk.chat.core.QiscusCore;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;

public class QiscusDateUtilTest extends InstrumentationBaseTest {

    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    @Override
    public void setupEngine() {
        super.setupEngine();
        QiscusCore.getChatConfig().enableDebugMode(true);
    }

    @Test
    public void toTodayOrDateTest() {
        QiscusDateUtil.toTodayOrDate(new Date());
    }

    @Test
    public void toHourTest() {
        QiscusDateUtil.toHour(new Date());
    }

    @Test
    public void isDateEqualIgnoreTimeTest() {
        QiscusDateUtil.isDateEqualIgnoreTime(new Date(), new Date(10000));
    }

    @Test
    public void getRelativeTimeDiffTest() {
        QiscusDateUtil.getRelativeTimeDiff(new Date());
    }

    @Test
    public void toFullDateFormatTest() {
        QiscusDateUtil.toFullDateFormat(new Date());
    }

    @Test
    public void getDateTimeSdfTest() {
        String stringDate = "2001-12-31'T'21:54:13'Z'";
        QiscusDateUtil.getDateTimeSdf(stringDate);
        QiscusDateUtil.getDateTimeSdf("");
    }

    @Test
    public void isBeforeADaySdfTest() {
        QiscusDateUtil.isBeforeADaySdf(10000);
    }

    @Test
    public void isPassingDateTimeSdfTest() {
        QiscusDateUtil.isPassingDateTimeSdf(10000);
    }
}