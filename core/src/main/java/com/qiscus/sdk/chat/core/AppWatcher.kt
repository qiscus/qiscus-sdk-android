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

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.qiscus.sdk.chat.data.util.ApplicationWatcher
import com.qiscus.sdk.chat.domain.util.runOnBackgroundThread
import java.util.concurrent.ScheduledFuture


/**
 * Created on : September 21, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
class AppWatcher : ApplicationWatcher, Application.ActivityLifecycleCallbacks {
    private val maxActivityTransitionTime: Long = 2000

    private var activityTransition: ScheduledFuture<*>? = null
    private var foreground: Boolean = false

    override fun isOnForeground(): Boolean {
        return foreground
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}

    override fun onActivityStarted(activity: Activity) {}

    override fun onActivityResumed(activity: Activity) {
        stopActivityTransitionTimer()
    }

    override fun onActivityPaused(activity: Activity) {
        startActivityTransitionTimer()
    }

    override fun onActivityStopped(activity: Activity) {}

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle?) {}

    override fun onActivityDestroyed(activity: Activity) {}

    private fun startActivityTransitionTimer() {
        activityTransition = runOnBackgroundThread(Runnable { foreground = false }, maxActivityTransitionTime)
    }

    private fun stopActivityTransitionTimer() {
        if (activityTransition != null) {
            activityTransition!!.cancel(true)
        }

        foreground = true
    }
}