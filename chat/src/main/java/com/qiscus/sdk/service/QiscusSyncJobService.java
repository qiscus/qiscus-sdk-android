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
import com.qiscus.sdk.data.local.QiscusEventCache;
import com.qiscus.sdk.data.model.QiscusComment;
import com.qiscus.sdk.data.remote.QiscusApi;
import com.qiscus.sdk.data.remote.QiscusPusherApi;
import com.qiscus.sdk.event.QiscusSyncEvent;
import com.qiscus.sdk.event.QiscusUserEvent;
import com.qiscus.sdk.util.QiscusAndroidUtil;
import com.qiscus.sdk.util.QiscusErrorLogger;
import com.qiscus.sdk.util.QiscusLogger;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

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

    public static void syncJob(Context context) {
        QiscusLogger.print(TAG, "syncJob...");

        ComponentName componentName = new ComponentName(context, QiscusSyncJobService.class);
        JobInfo jobInfo = new JobInfo.Builder(Qiscus.getQiscusAccount().getId(), componentName)
                .setMinimumLatency(Qiscus.getHeartBeat())
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setPersisted(true)
                .build();

        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        if (jobScheduler != null) {
            jobScheduler.schedule(jobInfo);
        }

    }

    @Override
    public void onCreate() {
        super.onCreate();

        QiscusLogger.print(TAG, "Creating...");
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

        if (Qiscus.hasSetupUser()) {
            QiscusAndroidUtil.runOnUIThread(() -> QiscusPusherApi.getInstance().restartConnection());
            syncJob(this);
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
        QiscusApi.getInstance().sync()
                .doOnNext(qiscusComment -> {
                    QiscusComment savedQiscusComment = Qiscus.getDataStore().getComment(qiscusComment.getUniqueId());

                    if (savedQiscusComment != null && savedQiscusComment.isDeleted()) {
                        return;
                    }

                    if (!qiscusComment.isMyComment()) {
                        QiscusPusherApi.getInstance()
                                .setUserDelivery(qiscusComment.getRoomId(), qiscusComment.getId());
                    }

                    if (savedQiscusComment != null && savedQiscusComment.getState() > qiscusComment.getState()) {
                        qiscusComment.setState(savedQiscusComment.getState());
                    }
                })
                .doOnSubscribe(() -> {
                    EventBus.getDefault().post((QiscusSyncEvent.STARTED));
                    QiscusLogger.print("Sync started...");
                })
                .doOnCompleted(() -> {
                    EventBus.getDefault().post((QiscusSyncEvent.COMPLETED));
                    QiscusLogger.print("Sync completed...");
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(QiscusPusherApi::handleReceivedComment, throwable -> {
                    QiscusErrorLogger.print(throwable);
                    EventBus.getDefault().post(QiscusSyncEvent.FAILED);
                    QiscusLogger.print("Sync failed...");
                });
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
                syncJob(this);
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
            syncJob(this);
        }

        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        QiscusLogger.print(TAG, "Job stopped...");

        return true;
    }

}
