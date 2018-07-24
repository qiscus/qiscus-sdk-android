package com.qiscus.sdk.service;

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
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.qiscus.sdk.Qiscus;
import com.qiscus.sdk.util.QiscusLogger;

import static android.net.ConnectivityManager.CONNECTIVITY_ACTION;

/**
 * @author Yuana andhikayuana@gmail.com
 * @since Jul, Mon 23 2018 14.37
 **/
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class QiscusNetworkCheckerJobService extends JobService {

    private static final String TAG = QiscusNetworkCheckerJobService.class.getSimpleName();

    private QiscusNetworkStateReceiver networkStateReceiver;

    public static void scheduleJob(Context context) {
        Log.d(TAG, "scheduleJob: ");
        ComponentName componentName = new ComponentName(context, QiscusNetworkCheckerJobService.class);
        JobInfo jobInfo = new JobInfo.Builder(Qiscus.getQiscusAccount().getId(), componentName)
                .setRequiresCharging(true)
                .setMinimumLatency(5 * 1000)
                .setOverrideDeadline(2000)
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
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(CONNECTIVITY_ACTION);
        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        registerReceiver(networkStateReceiver, intentFilter);
        return true; //tell to the system to keep this job
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        QiscusLogger.print(TAG, "onStopJob: ");
        unregisterReceiver(networkStateReceiver);
        return true;  //the system not drop this job
    }

    @Override
    public void onCreate() {
        super.onCreate();
        QiscusLogger.print(TAG, "onCreate: ");
        if (Qiscus.hasSetupUser()) {
            scheduleJob(this);
        }
        networkStateReceiver = new QiscusNetworkStateReceiver();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        QiscusLogger.print(TAG, "onStartCommand: ");
        return START_STICKY;
    }
}
