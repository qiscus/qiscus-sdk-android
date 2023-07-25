package com.qiscus.sdk.chat.core.data.remote;

import static com.qiscus.sdk.chat.core.event.QiscusRefreshTokenEvent.*;

import android.os.Build;

import androidx.annotation.NonNull;

import com.qiscus.sdk.chat.core.BuildConfig;
import com.qiscus.sdk.chat.core.QiscusCore;
import com.qiscus.sdk.chat.core.data.model.QiscusRefreshToken;
import com.qiscus.sdk.chat.core.event.QiscusRefreshTokenEvent;
import com.qiscus.sdk.chat.core.util.BuildVersionUtil;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Iterator;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

public class QiscusInterceptor {

    // headers params
    private static final String APP_ID = "QISCUS-SDK-APP-ID";
    private static final String TOKEN = "QISCUS-SDK-TOKEN";
    private static final String USER_EMAIL = "QISCUS-SDK-USER-EMAIL";
    private static final String VERSION = "QISCUS-SDK-VERSION";
    private static final String PLATFORM = "QISCUS-SDK-PLATFORM";
    private static final String DEVICE_BRAND = "QISCUS-SDK-DEVICE-BRAND";
    private static final String DEVICE_MODEL = "QISCUS-SDK-DEVICE-MODEL";
    private static final String DEVICE_OS_VERSION = "QISCUS-SDK-DEVICE-OS-VERSION";
    // headers Values
    private static final String ANDROID_PARAM = "ANDROID";

    @NonNull
    public static Response headersInterceptor(Interceptor.Chain chain) throws IOException {
        return refreshToken(chain, createNewBuilder(chain));
    }

    private static Request.Builder createNewBuilder(Interceptor.Chain chain) {
        Request.Builder builder = chain.request().newBuilder();
        JSONObject jsonCustomHeader = QiscusCore.getCustomHeader();

        builder.addHeader(APP_ID, QiscusCore.getAppId());
        builder.addHeader(TOKEN, QiscusCore.hasSetupUser() ? QiscusCore.getToken() : "");
        builder.addHeader(USER_EMAIL, QiscusCore.hasSetupUser() ? QiscusCore.getQiscusAccount().getEmail() : "");
        if (QiscusCore.getIsBuiltIn()) {
            builder.addHeader(VERSION, ANDROID_PARAM + "_" +
                    BuildConfig.CHAT_BUILT_IN_VERSION_MAJOR + "." +
                    BuildConfig.CHAT_BUILT_IN_VERSION_MINOR + "." +
                    BuildConfig.CHAT_BUILT_IN_VERSION_PATCH);
        } else {
//            builder.addHeader(VERSION, ANDROID_PARAM + "_" +
//                    BuildConfig.CHAT_CORE_VERSION_MAJOR + "." +
//                    BuildConfig.CHAT_CORE_VERSION_MINOR + "." +
//                    BuildConfig.CHAT_CORE_VERSION_PATCH);
            builder.addHeader(VERSION, ANDROID_PARAM + "_" +
                   "1.7.0-beta.3");
        }
        builder.addHeader(PLATFORM, ANDROID_PARAM);
        builder.addHeader(DEVICE_BRAND, Build.MANUFACTURER);
        builder.addHeader(DEVICE_MODEL, Build.MODEL);
        builder.addHeader(DEVICE_OS_VERSION, BuildVersionUtil.OS_VERSION_NAME);

        if (jsonCustomHeader != null) {
            Iterator<String> keys = jsonCustomHeader.keys();

            while (keys.hasNext()) {
                String key = keys.next();
                try {
                    Object customHeader = jsonCustomHeader.get(key);
                    if (customHeader != null) {
                        builder.addHeader(key, customHeader.toString());
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        return builder;
    }

    private static Response refreshToken(Interceptor.Chain chain, Request.Builder builder) throws IOException {
        Response initialResponse = chain.proceed(builder.build());

        if (initialResponse != null
                && (initialResponse.code() == UNAUTHORIZED
                || initialResponse.code() == EXPIRED_TOKEN)) {
            try {
                JSONObject jsonResponse = new JSONObject(initialResponse.body().string());
                handleResponse(
                        initialResponse.code(), jsonResponse
                );
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                initialResponse = chain.proceed(
                        createNewBuilder(chain).build()
                );
            }
        }

        return initialResponse;
    }

    private static void handleResponse(int code, JSONObject jsonResponse) throws JSONException {
        if (!jsonResponse.has("error")) return;
        JSONObject jsonObject = jsonResponse.getJSONObject("error");

        autoRefreshToken(code, jsonObject);
        sendEvent(code, jsonObject);
    }

    private static void autoRefreshToken(int code, JSONObject jsonObject) throws JSONException {
        if (QiscusCore.isAutoRefreshToken()
                && code == EXPIRED_TOKEN
                && jsonObject.getString("message").equals(TOKEN_EXPIRED_MESSAGE)
        ) {
            QiscusCore.refreshToken(null);
        }
    }

    private static void sendEvent(int code, JSONObject jsonObject)  throws JSONException {
        if (jsonObject.getString("message").contains(UNAUTHORIZED_MESSAGE)) {
            EventBus.getDefault().post(
                    new QiscusRefreshTokenEvent(
                            code, jsonObject.getString("message")
                    )
            );
        }
    }

    public static HttpLoggingInterceptor makeLoggingInterceptor(boolean isDebug) {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(isDebug ? HttpLoggingInterceptor.Level.BODY : HttpLoggingInterceptor.Level.NONE);
        return logging;
    }

}
