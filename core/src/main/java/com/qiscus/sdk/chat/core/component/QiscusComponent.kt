package com.qiscus.sdk.chat.core.component

import android.app.Application
import com.qiscus.sdk.chat.core.AppWatcher
import com.qiscus.sdk.chat.data.util.ApplicationWatcher

/**
 * Created on : August 30, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
class QiscusComponent
(
        val application: Application,
        private val serverBaseUrl: String,
        val appWatcher: ApplicationWatcher = AppWatcher(),
        val executorComponent: ExecutorComponent = ExecutorComponent(),
        val dataComponent: DataComponent = DataComponent(application, appWatcher, serverBaseUrl)
)