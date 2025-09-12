package com.qiscus.sdk.chat.core.data.remote;

import static com.qiscus.sdk.chat.core.event.QiscusRefreshTokenEvent.*;

import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;

import com.qiscus.sdk.chat.core.BuildConfig;
import com.qiscus.sdk.chat.core.QiscusCore;
import com.qiscus.sdk.chat.core.data.model.QiscusRefreshToken;
import com.qiscus.sdk.chat.core.event.QiscusRefreshTokenEvent;
import com.qiscus.sdk.chat.core.util.BuildVersionUtil;
import com.qiscus.sdk.chat.core.util.QiscusLogger;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Iterator;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.HttpException;

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
            builder.addHeader(VERSION, ANDROID_PARAM + "_" +
                    BuildConfig.CHAT_CORE_VERSION_MAJOR + "." +
                    BuildConfig.CHAT_CORE_VERSION_MINOR + "." +
                    BuildConfig.CHAT_CORE_VERSION_PATCH);
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
    }

    private static void autoRefreshToken(int code, JSONObject jsonObject) throws JSONException {
        if (QiscusCore.isAutoRefreshToken()
                && code == EXPIRED_TOKEN
                && jsonObject.getString("message").equals(TOKEN_EXPIRED_MESSAGE)
        ) {
            QiscusCore.refreshToken(new QiscusCore.SetRefreshTokenListener() {
                @Override
                public void onSuccess(QiscusRefreshToken refreshToken) {
                    EventBus.getDefault().post(
                            new QiscusRefreshTokenEvent(
                                    200, "Success"
                            )
                    );
                }

                @Override
                public void onError(Throwable throwable) {
                    try {
                        if (throwable instanceof HttpException) {
                            HttpException httpEx = (HttpException) throwable;

                            retrofit2.Response<?> response = httpEx.response(); // ini dari retrofit2.Response
                            if (response != null && response.errorBody() != null) {
                                String errorJson = response.errorBody().string(); // hanya sekali bisa dipanggil

                                JSONObject jsonObject = new JSONObject(errorJson);
                                JSONObject errorObj = jsonObject.getJSONObject("error");
                                String message = errorObj.getString("message");
                                int status = jsonObject.getInt("status");

                                if (status == 401 && "refresh token invalid".equals(message)) {
                                    // ignored
                                } else {
                                    // need to relogin
                                    EventBus.getDefault().post(
                                            new QiscusRefreshTokenEvent(
                                                    401, "Unauthorized"
                                            )
                                    );
                                }
                            } else {
                                // fallback kalau errorBody kosong
                                EventBus.getDefault().post(
                                        new QiscusRefreshTokenEvent(
                                                httpEx.code(), "Unauthorized"
                                        )
                                );
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        EventBus.getDefault().post(
                                new QiscusRefreshTokenEvent(
                                        403, "Unauthorized"
                                )
                        );
                    }



                }
            });
        }else{
            sendEvent(code, jsonObject);
        }
    }

    private static void sendEvent(int code, JSONObject jsonObject)  throws JSONException {
        if (jsonObject.getString("message").contains(UNAUTHORIZED_MESSAGE) || jsonObject.getString("message").contains(TOKEN_EXPIRED_MESSAGE)) {
            EventBus.getDefault().post(
                    new QiscusRefreshTokenEvent(
                            code, jsonObject.getString("message")
                    )
            );
        }
    }

    public static HttpLoggingInterceptor makeLoggingInterceptor(boolean isDebug) {
        HttpLoggingInterceptor.Logger customLogger = message -> {
            if (message.startsWith("-->")) {
                // Request mulai
                logLong("Qiscus API_REQUEST",
                        "\n==============================\n" +
                                "🚀 REQUEST START\n" +
                                message);
            } else if (message.startsWith("<--")) {
                // Response mulai
                logLong("Qiscus API_RESPONSE",
                        "\n==============================\n" +
                                "📥 RESPONSE START\n" +
                                message);
            } else if (message.startsWith("{") || message.startsWith("[")) {
                // Body JSON
                logLong("Qiscus API_BODY", message);
            } else if (message.contains("END HTTP")) {
                // End request/response
                logLong("Qiscus API_LOG", message +
                        "\n✅ END\n==============================");
            } else {
                // Header, size, dsb.
                logLong("Qiscus API_LOG", message);
            }
        };

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor(customLogger);
        logging.setLevel(isDebug ? HttpLoggingInterceptor.Level.BODY : HttpLoggingInterceptor.Level.NONE);
        return logging;
    }

    /**
     * Helper untuk cetak log panjang agar tidak terpotong di Logcat
     */
    private static void logLong(String tag, String message) {
        int maxLogSize = 2000; // limit per baris agar tidak terpotong
        for (int i = 0; i <= message.length() / maxLogSize; i++) {
            int start = i * maxLogSize;
            int end = Math.min((i + 1) * maxLogSize, message.length());
            if (start < end) {
                QiscusLogger.printRed(tag, message.substring(start, end));
            }
        }
    }

}
