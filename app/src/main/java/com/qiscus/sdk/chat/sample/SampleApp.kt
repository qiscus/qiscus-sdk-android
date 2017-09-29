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

package com.qiscus.sdk.chat.sample

import android.app.Application
import com.facebook.stetho.Stetho
import com.qiscus.jupuk.Jupuk
import com.qiscus.sdk.chat.core.Qiscus

/**
 * Created on : August 19, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
class SampleApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Qiscus.init(this, "dragongo")
        Stetho.initializeWithDefaults(this)
        Jupuk.init(this)
    }
}