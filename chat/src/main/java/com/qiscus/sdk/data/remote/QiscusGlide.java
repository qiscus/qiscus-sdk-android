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

package com.qiscus.sdk.data.remote;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.qiscus.sdk.Qiscus;

/**
 * Created on : June 15, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
public enum QiscusGlide {
    INSTANCE;

    private RequestManager requestManager;

    QiscusGlide() {
        requestManager = Glide.with(Qiscus.getApps().getApplicationContext());
    }

    public static QiscusGlide getInstance() {
        return INSTANCE;
    }

    public RequestManager get() {
        return requestManager;
    }
}
