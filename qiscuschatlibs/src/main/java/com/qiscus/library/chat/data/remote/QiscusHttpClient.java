package com.qiscus.library.chat.data.remote;

import com.squareup.okhttp.OkHttpClient;

import java.util.concurrent.TimeUnit;

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
        httpClient = new OkHttpClient();
        httpClient.setConnectTimeout(60, TimeUnit.SECONDS);
        httpClient.setReadTimeout(60, TimeUnit.SECONDS);
    }

    public static QiscusHttpClient getInstance() {
        return INSTANCE;
    }

    public OkHttpClient getHttpClient() {
        return httpClient;
    }
}
