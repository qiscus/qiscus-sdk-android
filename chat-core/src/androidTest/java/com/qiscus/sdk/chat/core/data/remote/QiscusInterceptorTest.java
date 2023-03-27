package com.qiscus.sdk.chat.core.data.remote;

import static com.qiscus.sdk.chat.core.event.QiscusRefreshTokenEvent.EXPIRED_TOKEN;
import static com.qiscus.sdk.chat.core.event.QiscusRefreshTokenEvent.TOKEN_EXPIRED_MESSAGE;
import static com.qiscus.sdk.chat.core.event.QiscusRefreshTokenEvent.UNAUTHORIZED;
import static com.qiscus.sdk.chat.core.event.QiscusRefreshTokenEvent.UNAUTHORIZED_MESSAGE;
import static org.junit.Assert.*;

import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner;

import com.qiscus.sdk.chat.core.InstrumentationBaseTest;
import com.qiscus.sdk.chat.core.QiscusCore;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import okhttp3.Request;

@RunWith(AndroidJUnit4ClassRunner.class)
public class QiscusInterceptorTest extends InstrumentationBaseTest {

    private QiscusInterceptor interceptor;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        setupEngine();
        interceptor = new QiscusInterceptor();
    }

    @Test
    public void getHeaderVersionParamTest() {
        try {
            QiscusCore.isBuiltIn(true);

            Method getHeaderVersionParam = extractMethode(
                    interceptor, "getHeaderVersionParam", null
            );
            getHeaderVersionParam.invoke(interceptor);
        } catch (IllegalAccessException | InvocationTargetException e) {
            // ignored
        }
    }

    @Test
    public void creteCustomHeaderTest() {
        try {
            JSONObject jsonCustomHeader = new JSONObject();
            jsonCustomHeader.put("key", "value");

            Request.Builder builder = new Request.Builder();

            QiscusCore.isBuiltIn(true);

            Method creteCustomHeader = extractMethode(
                    interceptor, "creteCustomHeader",
                    Request.Builder.class, JSONObject.class
            );
            creteCustomHeader.invoke(interceptor,builder , jsonCustomHeader);
        } catch (IllegalAccessException | InvocationTargetException | JSONException e) {
           // ignored
        }
    }

    @Test
    public void handleResponseTest() {
        try {
            JSONObject jsonResponse = new JSONObject();
            jsonResponse.put("success", "value");

            Method handleResponse = extractMethode(
                    interceptor, "handleResponse",
                    Integer.class, JSONObject.class
            );
            handleResponse.invoke(interceptor, 0 , jsonResponse);
        } catch (IllegalAccessException | InvocationTargetException | JSONException e) {
           // ignored
        }
    }

    @Test
    public void autoRefreshTokenTest() {
        try {
            JSONObject jsonResponse = new JSONObject();
            jsonResponse.put("message", TOKEN_EXPIRED_MESSAGE);

            autoRefreshToken(true, EXPIRED_TOKEN, jsonResponse);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void autoRefreshTokenNotRefreshTest() {
        try {
            JSONObject jsonResponse = new JSONObject();
            jsonResponse.put("message", TOKEN_EXPIRED_MESSAGE);

            autoRefreshToken(false, EXPIRED_TOKEN, jsonResponse);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void autoRefreshTokenUnAuthorizedTest() {
        try {
            JSONObject jsonResponse = new JSONObject();
            jsonResponse.put("message", TOKEN_EXPIRED_MESSAGE);

            autoRefreshToken(true, UNAUTHORIZED, jsonResponse);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void autoRefreshTokenUnAuthorizedNotRefreshTest() {
        try {
            JSONObject jsonResponse = new JSONObject();
            jsonResponse.put("message", TOKEN_EXPIRED_MESSAGE);

            autoRefreshToken(false, UNAUTHORIZED, jsonResponse);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void autoRefreshTokenUnAuthorizedMessageTest() {
        try {
            JSONObject jsonResponse = new JSONObject();
            jsonResponse.put("message", UNAUTHORIZED_MESSAGE);

            autoRefreshToken(true, EXPIRED_TOKEN, jsonResponse);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void autoRefreshTokenUnAuthorizedMessageNotRefreshTest() {
        try {
            JSONObject jsonResponse = new JSONObject();
            jsonResponse.put("message", UNAUTHORIZED_MESSAGE);

            autoRefreshToken(false, EXPIRED_TOKEN, jsonResponse);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void autoRefreshTokenUnAuthorizedMessageCodeNotRefreshTest() {
        try {
            JSONObject jsonResponse = new JSONObject();
            jsonResponse.put("message", UNAUTHORIZED_MESSAGE);

            autoRefreshToken(false, UNAUTHORIZED, jsonResponse);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private void autoRefreshToken(boolean isRefresh, int code, JSONObject jsonResponse) {
        try {
            Method autoRefreshToken = extractMethode(
                    interceptor, "autoRefreshToken",
                    Boolean.class, Integer.class, JSONObject.class
            );
            autoRefreshToken.invoke(interceptor, isRefresh, code , jsonResponse);
        } catch (IllegalAccessException | InvocationTargetException e) {
            // ignored
        }
    }

    @Test
    public void sendEventTest() {
        try {
            JSONObject jsonResponse = new JSONObject();
            jsonResponse.put("message", UNAUTHORIZED_MESSAGE);

            Method sendEvent = extractMethode(
                    interceptor, "sendEvent",
                    Integer.class, JSONObject.class
            );
            sendEvent.invoke(interceptor, EXPIRED_TOKEN , jsonResponse);
        } catch (IllegalAccessException | InvocationTargetException | JSONException e) {
           // ignored
        }
    }

}