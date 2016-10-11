/*
 * Copyright (c) 2016 Qiscus.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.qiscus.dragonfly;

import android.app.Application;

import com.qiscus.sdk.Qiscus;

import java.text.SimpleDateFormat;

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
    }
}
