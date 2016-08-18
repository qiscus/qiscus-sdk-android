package com.qiscus.library.chat.sample;

import android.app.Application;

import com.qiscus.library.chat.Qiscus;

/**
 * Created on : August 18, 2016
 * Author     : zetbaitsu
 * Name       : Zetra
 * Email      : zetra@mail.ugm.ac.id
 * GitHub     : https://github.com/zetbaitsu
 * LinkedIn   : https://id.linkedin.com/in/zetbaitsu
 */
public class SampleApps extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Qiscus.init(this);
    }
}
