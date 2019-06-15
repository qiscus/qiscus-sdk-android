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

package com.qiscus.sdk.chat.core.service;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.Build;
import androidx.annotation.RequiresApi;

import com.qiscus.sdk.chat.core.event.QiscusUserEvent;
import com.qiscus.sdk.chat.core.util.QiscusLogger;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.concurrent.TimeUnit;

import static android.net.ConnectivityManager.CONNECTIVITY_ACTION;

/**
 * @author Yuana andhikayuana@gmail.com
 * @since Jul, Mon 23 2018 14.37
 **/
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class QiscusNetworkCheckerJobService extends JobService {

    private static final String TAG = QiscusNetworkCheckerJobService.class.getSimpleName();
    private static final int STATIC_JOB_ID = 200;
    private QiscusNetworkStateReceiver networkStateReceiver;

    public static void scheduleJob(Context context) {
        QiscusLogger.print(TAG, "scheduleJob: ");
        ComponentName componentName = new ComponentName(context, QiscusNetworkCheckerJobService.class);
        JobInfo jobInfo = new JobInfo.Builder(STATIC_JOB_ID, componentName)
                .setMinimumLatency(TimeUnit.SECONDS.toMillis(5))
                .setOverrideDeadline(TimeUnit.SECONDS.toMillis(10))
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setPersisted(true)
                .build();

        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        if (jobScheduler != null) {
            jobScheduler.schedule(jobInfo);
        }

    }

    @Override
    public boolean onStartJob(JobParameters params) {
        QiscusLogger.print(TAG, "onStartJob: ");
        return true; //tell to the system to keep this job
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        QiscusLogger.print(TAG, "onStopJob: ");
        return true;  //the system not drop this job
    }

    @Override
    public void onCreate() {
        super.onCreate();
        QiscusLogger.print(TAG, "onCreate: ");

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

        networkStateReceiver = new QiscusNetworkStateReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(CONNECTIVITY_ACTION);
        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        registerReceiver(networkStateReceiver, intentFilter);
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(networkStateReceiver);
        QiscusLogger.print(TAG, "onDestroy");
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        QiscusLogger.print(TAG, "onStartCommand: ");
        return START_STICKY;
    }

    @Subscribe
    public void onUserEvent(QiscusUserEvent userEvent) {
        QiscusLogger.print(TAG, "onUserEvent");
        switch (userEvent) {
            case LOGIN:
                scheduleJob(this);
                break;
            case LOGOUT:
                stopJob();
                break;
        }
    }

    private void stopJob() {
        QiscusLogger.print(TAG, "stopJob");
        JobScheduler jobScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
        if (jobScheduler != null) {
            jobScheduler.cancel(STATIC_JOB_ID);
        }
    }
}
