package com.qiscus.sdk.chat.core

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.qiscus.sdk.chat.data.util.ApplicationWatcher
import com.qiscus.sdk.chat.domain.common.runOnBackgroundThread
import java.util.concurrent.ScheduledFuture


/**
 * Created on : September 21, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
class AppWatcher : ApplicationWatcher, Application.ActivityLifecycleCallbacks {
    private val MAX_ACTIVITY_TRANSITION_TIME: Long = 2000

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
        activityTransition = runOnBackgroundThread(Runnable { foreground = false }, MAX_ACTIVITY_TRANSITION_TIME)
    }

    private fun stopActivityTransitionTimer() {
        if (activityTransition != null) {
            activityTransition!!.cancel(true)
        }

        foreground = true
    }
}