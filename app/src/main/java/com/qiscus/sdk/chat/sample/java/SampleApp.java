package com.qiscus.sdk.chat.sample.java;

import android.app.Application;

import com.qiscus.sdk.chat.core.Qiscus;

/**
 * Created on : September 20, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
public class SampleApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Qiscus.Companion.init(this, "dragongo");
    }
}
