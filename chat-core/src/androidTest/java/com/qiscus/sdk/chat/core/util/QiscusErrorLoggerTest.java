package com.qiscus.sdk.chat.core.util;

import com.qiscus.sdk.chat.core.InstrumentationBaseTest;
import com.qiscus.sdk.chat.core.QiscusCore;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import retrofit2.HttpException;
import retrofit2.Response;

public class QiscusErrorLoggerTest extends InstrumentationBaseTest {

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
        testPrint1Test();
        QiscusCore.getChatConfig().enableDebugMode(true);
    }

    @Test
    public void printTest() {
        new QiscusErrorLogger();
        String msg = null;
        QiscusErrorLogger.print(new Throwable(msg));
        QiscusErrorLogger.print(new Throwable("msg"));
    }

    @Test
    public void testPrintTest() {
        String msg = null;
        QiscusErrorLogger.print("error", new Throwable(msg));
        QiscusErrorLogger.print("error", new Throwable("msg"));
    }

    @Test
    public void testPrint1Test() {
        QiscusErrorLogger.print("error", "msg");
    }

    @Test
    public void getMessageTest() {
        QiscusErrorLogger.getMessage(httpError());
        QiscusErrorLogger.getMessage(new IOException("Something happened"));
    }

    private HttpException httpError() {
        ResponseBody rb = ResponseBody.create(MediaType.parse("plain/text"), "some content");
        Response<?> r = Response.error(500, rb);
        return new HttpException(r);
    }
}