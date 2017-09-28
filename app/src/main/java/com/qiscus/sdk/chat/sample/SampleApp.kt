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