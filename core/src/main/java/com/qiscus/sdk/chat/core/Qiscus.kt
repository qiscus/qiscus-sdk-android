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

package com.qiscus.sdk.chat.core

import android.app.Application
import com.qiscus.sdk.chat.core.component.QiscusComponent
import com.qiscus.sdk.chat.domain.common.CommentFactory
import com.qiscus.sdk.chat.domain.common.QiscusCommentFactory

/**
 * Created on : August 30, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
class Qiscus private constructor(val component: QiscusComponent) {
    val useCaseFactory: QiscusUseCaseFactory = QiscusQiscusUseCaseFactoryImpl(component)
    val commentFactory: CommentFactory = QiscusCommentFactory(component.dataComponent.accountRepository)

    companion object {
        @Volatile private var INSTANCE: Qiscus? = null

        fun init(application: Application, appId: String) {
            initWithCustomServer(application, "https://$appId.qiscus.com")
        }

        fun initWithCustomServer(application: Application, serverBaseUrl: String) {
            INSTANCE = Qiscus(QiscusComponent(application, serverBaseUrl = serverBaseUrl))
        }

        val instance: Qiscus
            get() {
                if (INSTANCE == null) {
                    synchronized(Qiscus::class.java) {
                        if (INSTANCE == null) {
                            throw RuntimeException("Please init Qiscus before!")
                        }
                    }
                }

                return INSTANCE!!
            }
    }

    init {
        watchApplication()
        startPubSubClient()
    }

    private fun watchApplication() {
        component.application.registerActivityLifecycleCallbacks(component.appWatcher as Application.ActivityLifecycleCallbacks)
    }

    private fun startPubSubClient() {
        if (component.dataComponent.accountLocal.isAuthenticate()) {
            component.dataComponent.pubSubClient.restartConnection()
        }
    }
}