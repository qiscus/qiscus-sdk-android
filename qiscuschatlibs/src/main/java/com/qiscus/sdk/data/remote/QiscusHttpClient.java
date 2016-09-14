package com.qiscus.sdk.data.remote;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

/**
 * Created on : June 14, 2016
 * Author     : zetbaitsu
 * Name       : Zetra
 * Email      : zetra@mail.ugm.ac.id
 * GitHub     : https://github.com/zetbaitsu
 * LinkedIn   : https://id.linkedin.com/in/zetbaitsu
 */
public enum QiscusHttpClient {
    INSTANCE;

    private final OkHttpClient httpClient;

    QiscusHttpClient() {
        httpClient = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();
    }

    public static QiscusHttpClient getInstance() {
        return INSTANCE;
    }

    public OkHttpClient getHttpClient() {
        return httpClient;
    }
}
