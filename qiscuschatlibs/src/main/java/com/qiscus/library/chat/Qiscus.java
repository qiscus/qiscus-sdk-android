package com.qiscus.library.chat;

import android.app.Application;
import android.os.Handler;

import com.parse.Parse;
import com.qiscus.library.chat.data.model.QiscusConfig;

/**
 * Created on : August 18, 2016
 * Author     : zetbaitsu
 * Name       : Zetra
 * Email      : zetra@mail.ugm.ac.id
 * GitHub     : https://github.com/zetbaitsu
 * LinkedIn   : https://id.linkedin.com/in/zetbaitsu
 */
public class Qiscus {
    private static Application APP_INSTANCE;
    private static volatile Handler APP_HANDLER;

    public static void init(Application application) {
        APP_INSTANCE = application;
        APP_HANDLER = new Handler(APP_INSTANCE.getMainLooper());

        Parse.initialize(APP_INSTANCE, QiscusConfig.PARSE_APP_ID, QiscusConfig.PARSE_CLIENT_KEY);
    }

    public static Application getApps() {
        return APP_INSTANCE;
    }

    public static Handler getAppsHandler() {
        return APP_HANDLER;
    }
}
