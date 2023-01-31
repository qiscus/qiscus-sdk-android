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

import rx.schedulers.Schedulers;

public class QiscusSyncTimer {

    private static final String TAG = QiscusSyncTimer.class.getSimpleName();
    private QiscusCore qiscusCore;
    private Timer timer;

    public QiscusSyncTimer(QiscusCore qiscusCore) {
        this.qiscusCore = qiscusCore;

        syncJob(qiscusCore.getApps());
    }

    public void syncJob(Context context) {
        long period = qiscusCore.getHeartBeat();
        qiscusCore.getLogger().print(TAG, "syncTimer...");

        stopSync();

        if (qiscusCore.getStatusRealtimeEnableDisable()) {
            period = qiscusCore.getAutomaticHeartBeat();
        }

        try {
            timer = new Timer();
            timer.schedule(new TimerTask() {
                public void run() {
                    // time ran out.
                    newSchedule(context);
                }
            }, period);
        } catch (IllegalStateException e) {
            qiscusCore.getLogger().print(TAG, "Error timer canceled");
        } catch (Exception e) {
            qiscusCore.getLogger().print(TAG, "Error timer exception");
        }
    }

    private void newSchedule(Context context) {
        qiscusCore.getLogger().print(TAG, "Job started...");

        if (qiscusCore.hasSetupUser() && !qiscusCore.getPusherApi().isConnected()) {
            if (qiscusCore.getEnableRealtime()) {
                if (qiscusCore.getStatusRealtimeEnableDisable()){
                    QiscusAndroidUtil.runOnUIThread(() -> qiscusCore.getPusherApi().restartConnection());
                    checkPendingMessage();
                    scheduleSync();

                }else{
                    checkPendingMessage();
                    scheduleSync();
                }
            }else{
                checkPendingMessage();
                scheduleSync();
            }



        }else{
            if (qiscusCore.hasSetupUser()) {
                checkPendingMessage();
                scheduleSync();
            }
        }


        syncJob(context);
    }

    public void startSchedule(){
        if (qiscusCore != null) {
            syncJob(qiscusCore.getApps());
        }
    }

    private void checkPendingMessage(){
        boolean isConnected = qiscusCore.getAndroidUtil().isNetworkAvailable();

        if (isConnected && qiscusCore.getDataStore().getPendingComments().size() > 0) {
            qiscusCore.getQiscusResendCommentHelper().tryResendPendingComment();
        }
    }

    private void scheduleSync() {
        if (qiscusCore.isOnForeground()) {
            if (qiscusCore.getEnableSync()) {
                syncComments();
            }

            if (qiscusCore.getEnableSyncEvent()){
                syncEvents();
            }
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
                .doOnSubscribe(() -> {
                    EventBus.getDefault().post((QiscusSyncEvent.STARTED));
                    qiscusCore.getLogger().print("Sync started...");
                })
                .doOnCompleted(() -> {
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
            try {
                timer.cancel();
                timer.purge();
            } catch (NullPointerException e) {
                // do nothing
            } catch (RuntimeException e) {
                // do nothing
            } catch (Exception e) {
                // do nothing
            }

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
