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

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;

import com.qiscus.sdk.chat.core.data.remote.QiscusPusherApi;
import com.qiscus.sdk.chat.core.util.QiscusAndroidUtil;

import java.util.concurrent.ScheduledFuture;

/**
 * Created on : February 09, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
public class QiscusActivityCallback implements Application.ActivityLifecycleCallbacks {

    private static final long MAX_ACTIVITY_TRANSITION_TIME = 2000;
    private static boolean foreground;
    private ScheduledFuture<?> activityTransition;
    private ScheduledFuture<?> activityTransition2;
    private QiscusCore qiscusCore;

    public QiscusActivityCallback(QiscusCore qiscusCore) {
        this.qiscusCore = qiscusCore;
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
    }

    @Override
    public void onActivityStarted(Activity activity) {
        AlarmManager alarmMgr = (AlarmManager) activity.getSystemService(Context.ALARM_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmMgr.canScheduleExactAlarms()) {
                qiscusCore.setIsExactAlarmDisable(true);
            } else {
                qiscusCore.setIsExactAlarmDisable(false);
            }
        }

        foreground = true;

//        if (!qiscusCore.getAndroidUtil().isMyServiceRunning() && !qiscusCore.isSyncServiceDisabledManually()) {
//            qiscusCore.startSyncService();
//        }
    }

    @Override
    public void onActivityResumed(Activity activity) {
        stopActivityTransitionTimer();
    }

    @Override
    public void onActivityPaused(Activity activity) {
        startActivityTransitionTimer();
    }

    @Override
    public void onActivityStopped(Activity activity) {
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
    }

    boolean isForeground() {
        return foreground;
    }

    public static void setAppActiveOrForground(){
        foreground = true;
    }

    private void startActivityTransitionTimer() {
        activityTransition = QiscusAndroidUtil.runOnBackgroundThread(() -> foreground = false,
                MAX_ACTIVITY_TRANSITION_TIME);

        activityTransition2 = QiscusAndroidUtil.runOnBackgroundThread(() -> check(), MAX_ACTIVITY_TRANSITION_TIME);
    }

    private void check(){
        if (!foreground) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                qiscusCore.getPusherApi().disconnect();
            }
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
