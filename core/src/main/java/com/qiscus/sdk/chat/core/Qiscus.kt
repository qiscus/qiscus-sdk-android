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
import com.qiscus.sdk.chat.data.pubsub.FcmHandler
import com.qiscus.sdk.chat.data.pusher.FcmHandlerImpl
import com.qiscus.sdk.chat.domain.common.MessageFactory
import com.qiscus.sdk.chat.domain.common.MessageFactoryImpl
import com.qiscus.sdk.chat.domain.interactor.Action
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

/**
 * Created on : August 30, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
class Qiscus private constructor(val component: QiscusComponent) {

    val useCaseFactory: UseCaseFactory by lazy {
        UseCaseFactoryImpl(component)
    }

    val messageFactory: MessageFactory by lazy {
        MessageFactoryImpl(component.dataComponent.accountRepository)
    }

    val fcmHandler: FcmHandler by lazy {
        FcmHandlerImpl(
                component.dataComponent.accountLocal,
                component.dataComponent.messageLocal,
                component.dataComponent.messageRemote,
                component.dataComponent.pubSubClient,
                component.dataComponent.messagePayloadMapper
        )
    }

    companion object {
        @Volatile private var INSTANCE: Qiscus? = null

        @JvmOverloads
        fun init(application: Application, appId: String, serverBaseUrl: String = "https://$appId.qiscus.com",
                 mqttBrokerUrl: String = "ssl://mqtt.qiscus.com:1885") {
            INSTANCE = Qiscus(QiscusComponent(application, serverBaseUrl, mqttBrokerUrl))
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
        if (component.dataComponent.accountLocal.isAuthenticated()) {
            component.dataComponent.pubSubClient.restartConnection()
        }
    }

    @JvmOverloads
    fun registerFcmToken(fcmToken: String, onComplete: Action<Void?>? = null, onError: Action<Throwable>? = null) {
        component.dataComponent.restApi
                .registerFcmToken(component.dataComponent.accountLocal.getAccount().token, "android", fcmToken)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ onComplete?.call(null) }, { onError?.call(it) })
    }

    fun isAuthenticated(): Boolean {
        return component.dataComponent.accountLocal.isAuthenticated()
    }
}