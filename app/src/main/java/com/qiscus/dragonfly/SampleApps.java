package com.qiscus.dragonfly;

import android.app.Application;

import com.qiscus.sdk.Qiscus;

import java.text.SimpleDateFormat;

import timber.log.Timber;

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
        Qiscus.init(this, "dragonfly");

        Qiscus.getChatConfig()
                .setStatusBarColor(R.color.accent)
                .setAppBarColor(R.color.accent)
                .setTitleColor(R.color.qiscus_dark_white)
                .setLeftBubbleColor(R.color.accent)
                .setRightBubbleColor(R.color.primary)
                .setRightBubbleTextColor(R.color.qiscus_white)
                .setRightBubbleTimeColor(R.color.primary_light)
                .setTimeFormat(date -> new SimpleDateFormat("HH:mm").format(date));

        Timber.plant(new Timber.DebugTree());
    }
}
