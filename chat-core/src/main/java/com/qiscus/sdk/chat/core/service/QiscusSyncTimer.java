package com.qiscus.sdk.chat.core.service;

import android.content.Context;

import com.qiscus.sdk.chat.core.QiscusCore;
import com.qiscus.sdk.chat.core.event.QiscusSyncEvent;
import com.qiscus.sdk.chat.core.event.QiscusUserEvent;
import com.qiscus.sdk.chat.core.util.QiscusAndroidUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.Timer;
import java.util.TimerTask;

import io.reactivex.schedulers.Schedulers;


public class QiscusSyncTimer {

    private static final String TAG = QiscusSyncTimer.class.getSimpleName();
    private QiscusCore qiscusCore;
    private Timer timer;

    public QiscusSyncTimer(QiscusCore qiscusCore) {
        this.qiscusCore = qiscusCore;

        syncJob(qiscusCore.getApps());
    }

    public void syncJob(Context context) {
        qiscusCore.getLogger().print(TAG, "syncTimer...");

        stopSync();

        timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                // time ran out.
                newSchedule(context);
            }
        }, qiscusCore.getHeartBeat());
    }

    private void newSchedule(Context context) {
        qiscusCore.getLogger().print(TAG, "Job started...");

        if (qiscusCore.hasSetupUser() && !qiscusCore.getPusherApi().isConnected()) {
            QiscusAndroidUtil.runOnUIThread(() -> qiscusCore.getPusherApi().restartConnection());
            scheduleSync();
            checkPendingMessage();
        }

        syncJob(context);
    }

    private void checkPendingMessage(){
        boolean isConnected = qiscusCore.getAndroidUtil().isNetworkAvailable();

        if (isConnected && qiscusCore.getDataStore().getPendingComments().size() > 0) {
            qiscusCore.getQiscusResendCommentHelper().tryResendPendingComment();
        }
    }

    private void scheduleSync() {
        if (qiscusCore.isOnForeground()) { syncComments();
            syncEvents();
        }
    }

    private void syncEvents() {
        qiscusCore.getApi().synchronizeEvent(qiscusCore.getEventCache().getLastEventId())
                .subscribeOn(Schedulers.io())
                .subscribe(events -> {
                }, qiscusCore.getErrorLogger()::print);
    }

    private void syncComments() {
        qiscusCore.getApi().sync()
                .doOnSubscribe(disposable -> {
                    EventBus.getDefault().post((QiscusSyncEvent.STARTED));
                    qiscusCore.getLogger().print("Sync started...");
                })
                .doOnComplete(() -> {
                    EventBus.getDefault().post((QiscusSyncEvent.COMPLETED));
                    qiscusCore.getLogger().print("Sync completed...");
                })
                .subscribeOn(Schedulers.io())
                .subscribe(qiscusCore.getPusherApi()::handleReceivedComment, throwable -> {
                    qiscusCore.getErrorLogger().print(throwable);
                    EventBus.getDefault().post(QiscusSyncEvent.FAILED);
                    qiscusCore.getLogger().print("Sync failed...");
                });
    }

    private void stopSync() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    @Subscribe
    public void onUserEvent(QiscusUserEvent userEvent) {
        switch (userEvent) {
            case LOGIN:
                QiscusAndroidUtil.runOnUIThread(() -> qiscusCore.getPusherApi().connect());
                syncJob(qiscusCore.getApps());
                break;
            case LOGOUT:
                stopSync();
                break;
        }
    }
}
