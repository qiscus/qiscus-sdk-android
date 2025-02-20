package com.qiscus.sdk.chat.core;

import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import com.qiscus.sdk.chat.core.util.QiscusLogger;

public class BackgroundForegroundListener implements DefaultLifecycleObserver {

    private QiscusCore qiscusCore;
    private static boolean foreground;

    public BackgroundForegroundListener(QiscusCore qiscusCore) {
        this.qiscusCore = qiscusCore;
    }

    @Override
    public void onCreate(@NonNull LifecycleOwner owner) {
        DefaultLifecycleObserver.super.onCreate(owner);
        qiscusCore.getLogger().print("BackgroundForegroundListener", "onCreate");
    }

    @Override
    public void onStart(@NonNull LifecycleOwner owner) {
        DefaultLifecycleObserver.super.onStart(owner);
        qiscusCore.getLogger().print("BackgroundForegroundListener", "onStart");
        foreground = true;
        if (!qiscusCore.getAndroidUtil().isMyServiceRunning() && !qiscusCore.isSyncServiceDisabledManually()) {
            qiscusCore.getQiscusMediator().getSyncTimer().startSchedule();
        }
    }

    @Override
    public void onResume(@NonNull LifecycleOwner owner) {
        DefaultLifecycleObserver.super.onResume(owner);
        qiscusCore.getLogger().print("BackgroundForegroundListener", "onResume");
    }

    @Override
    public void onPause(@NonNull LifecycleOwner owner) {
        DefaultLifecycleObserver.super.onPause(owner);
        qiscusCore.getLogger().print("BackgroundForegroundListener", "onPause");
    }

    @Override
    public void onStop(@NonNull LifecycleOwner owner) {
        DefaultLifecycleObserver.super.onStop(owner);
        qiscusCore.getLogger().print("BackgroundForegroundListener", "onStop");
        foreground = false;
    }

    @Override
    public void onDestroy(@NonNull LifecycleOwner owner) {
        DefaultLifecycleObserver.super.onDestroy(owner);
        foreground = false;
        qiscusCore.getLogger().print("BackgroundForegroundListener", "onDestroy");
    }

    boolean isForeground() {
        return foreground;
    }

    public static void setAppActiveOrForground(){
        foreground = true;
    }
}