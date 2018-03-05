package com.qiscus.sdk.service;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;

import com.qiscus.sdk.Qiscus;
import com.qiscus.sdk.data.QiscusSyncCommentHandler;
import com.qiscus.sdk.data.local.QiscusEventCache;
import com.qiscus.sdk.data.model.QiscusAccount;
import com.qiscus.sdk.data.remote.QiscusApi;
import com.qiscus.sdk.data.remote.QiscusPusherApi;
import com.qiscus.sdk.event.QiscusUserEvent;
import com.qiscus.sdk.util.QiscusAndroidUtil;
import com.qiscus.sdk.util.QiscusErrorLogger;
import com.qiscus.sdk.util.QiscusLogger;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created on : November 23, 2018
 * Author     : adicatur
 * Name       : Catur Adi Nugroho
 * GitHub     : https://github.com/adicatur
 */
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class QiscusSyncJobService extends JobService {
    private static final String TAG = QiscusSyncJobService.class.getSimpleName();

    private ComponentName componentName;

    @Override
    public void onCreate() {
        super.onCreate();

        QiscusLogger.print(TAG, "Creating...");
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

        if (Qiscus.hasSetupUser()) {
            QiscusAndroidUtil.runOnUIThread(() -> QiscusPusherApi.getInstance().restartConnection());
            componentName = new ComponentName(this, QiscusSyncJobService.class);
            syncJob();
        }

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    private void scheduleSync() {
        if (Qiscus.isOnForeground()) {
            syncComments();
            syncEvents();
        }
    }

    private void syncEvents() {
        QiscusApi.getInstance().getEvents(QiscusEventCache.getInstance().getLastEventId())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(events -> {
                }, QiscusErrorLogger::print);
    }

    private void syncComments() {
        QiscusSyncCommentHandler.sync();
    }

    private void stopSync() {
        JobScheduler jobScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
        if (jobScheduler != null) {
            jobScheduler.cancelAll();
        }
    }

    @Subscribe
    public void onUserEvent(QiscusUserEvent userEvent) {
        switch (userEvent) {
            case LOGIN:
                QiscusAndroidUtil.runOnUIThread(() -> QiscusPusherApi.getInstance().restartConnection());
                syncJob();
                break;
            case LOGOUT:
                stopSync();
                break;
        }
    }

    @Override
    public void onDestroy() {
        QiscusLogger.print(TAG, "Destroying...");
        EventBus.getDefault().unregister(this);
        stopSync();
        super.onDestroy();
    }

    @Override
    public boolean onStartJob(JobParameters params) {
        QiscusLogger.print(TAG, "Job started...");

        if (Qiscus.hasSetupUser()) {
            QiscusAndroidUtil.runOnUIThread(() -> QiscusPusherApi.getInstance().restartConnection());
            scheduleSync();
            jobFinished(params, true);
        }

        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {

        return false;
    }

    private void syncJob() {
        QiscusAccount qiscusAccount = Qiscus.getQiscusAccount();

        Random rand = new Random();
        int randomValue = rand.nextInt(50);

        JobInfo jobInfo = new JobInfo.Builder(qiscusAccount.getId() + randomValue, componentName)
                .setPeriodic(TimeUnit.MINUTES.toMillis(15))
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .build();

        JobScheduler jobScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
        if (jobScheduler != null) {
            jobScheduler.schedule(jobInfo);
        }

    }

}
