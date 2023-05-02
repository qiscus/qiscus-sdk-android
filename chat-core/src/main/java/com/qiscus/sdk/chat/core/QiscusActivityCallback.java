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

package com.qiscus.sdk.chat.core;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

import com.qiscus.sdk.chat.core.data.remote.QiscusPusherApi;
import com.qiscus.sdk.chat.core.util.QiscusAndroidUtil;
import com.qiscus.sdk.chat.core.util.QiscusServiceUtil;

import java.util.concurrent.ScheduledFuture;

/**
 * Created on : February 09, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
enum QiscusActivityCallback implements LifecycleObserver {
    INSTANCE;

    private static final long MAX_ACTIVITY_TRANSITION_TIME = 2000;
    private static boolean foreground;
    private ScheduledFuture<?> activityTransition;
    private ScheduledFuture<?> activityTransition2;

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    public void onCreate() {

    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void onActivityStarted() {

        foreground = true;

        if (!QiscusServiceUtil.isMyServiceRunning() && !QiscusCore.isSyncServiceDisabledManually()) {
            QiscusCore.startSyncService();
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    public void onActivityResumed() {
        stopActivityTransitionTimer();
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    public void onActivityPaused() {
        startActivityTransitionTimer();
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onActivityStopped() {
    }

    boolean isForeground() {
        return foreground;
    }

    public static void setAppActiveOrForground(){
        foreground = true;
    }

    private void startActivityTransitionTimer() {
        activityTransition = QiscusAndroidUtil.runOnBackgroundThread(() ->{
            foreground = false ;
        }, MAX_ACTIVITY_TRANSITION_TIME);

        activityTransition2 = QiscusAndroidUtil.runOnBackgroundThread(() -> check(), MAX_ACTIVITY_TRANSITION_TIME);
    }

    private void check(){
        if (!foreground) {
            QiscusPusherApi.getInstance().disconnect();
        }
    }

    private void stopActivityTransitionTimer() {
        if (activityTransition != null) {
            activityTransition.cancel(true);
        }

        if (activityTransition2 != null) {
            activityTransition2.cancel(true);
        }

        foreground = true;
    }
}