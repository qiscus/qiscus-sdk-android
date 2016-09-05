package com.qiscus.library.chat.sample;

import android.app.Application;

import com.qiscus.sdk.Qiscus;

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
        Qiscus.init(this,
                "https://qvc-engine-staging.herokuapp.com",
                "3f27dc397124364ecc0f");

        Qiscus.getChatConfig()
                .setLeftBubbleColor(R.color.accent)
                .setRightBubbleColor(R.color.primary)
                .setRightBubbleTextColor(R.color.qiscus_white)
                .setRightBubbleTimeColor(R.color.primary_light);
    }
}
