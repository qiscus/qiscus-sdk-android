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

import static com.qiscus.dragonfly.BuildConfig.QISCUS_SDK_APP_ID;

import androidx.multidex.MultiDexApplication;

import com.facebook.stetho.Stetho;
import com.qiscus.sdk.Qiscus;
import com.qiscus.sdk.data.model.QiscusDeleteCommentConfig;

/**
 * Created on : August 18, 2016
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
public class SampleApps extends MultiDexApplication {

    @Override
    public void onCreate() {
        super.onCreate();

//        Qiscus.setup(this, QISCUS_SDK_APP_ID);

        // for test refresh token
        Qiscus.setupWithCustomServer(
                this, "dragongo", "https://dragongo.qiscus.com",
                "ssl://realtime-stag.qiscus.com", null
        );

        Qiscus.getChatConfig()
                .enableDebugMode(true)
                .setEnableAddLocation(false)
                .setDeleteCommentConfig(new QiscusDeleteCommentConfig()
                        .setEnableDeleteComment(true)
                        .setEnableHardDelete(true));

        Stetho.initializeWithDefaults(this);
    }
}
