package com.qiscus.sdk.chat.core;

import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import com.qiscus.sdk.chat.core.util.QiscusLogger;
import com.qiscus.sdk.chat.core.util.QiscusServiceUtil;

enum BackgroundForegroundListener implements DefaultLifecycleObserver {
    INSTANCE;
    private static boolean foreground;
    @Override
    public void onCreate(@NonNull LifecycleOwner owner) {
        DefaultLifecycleObserver.super.onCreate(owner);
        QiscusLogger.print("BackgroundForegroundListener", "onCreate");
    }

    @Override
    public void onStart(@NonNull LifecycleOwner owner) {
        DefaultLifecycleObserver.super.onStart(owner);
        QiscusLogger.print("BackgroundForegroundListener", "onStart");
        foreground = true;
        if (!QiscusServiceUtil.isMyServiceRunning() && !QiscusCore.isSyncServiceDisabledManually()) {
            QiscusCore.startSyncService();
        }
    }

    @Override
    public void onResume(@NonNull LifecycleOwner owner) {
        DefaultLifecycleObserver.super.onResume(owner);
        QiscusLogger.print("BackgroundForegroundListener", "onResume");
    }

    @Override
    public void onPause(@NonNull LifecycleOwner owner) {
        DefaultLifecycleObserver.super.onPause(owner);
        QiscusLogger.print("BackgroundForegroundListener", "onPause");
    }

    @Override
    public void onStop(@NonNull LifecycleOwner owner) {
        DefaultLifecycleObserver.super.onStop(owner);
        QiscusLogger.print("BackgroundForegroundListener", "onStop");
        foreground = false;
    }

    @Override
    public void onDestroy(@NonNull LifecycleOwner owner) {
        DefaultLifecycleObserver.super.onDestroy(owner);
        foreground = false;
        QiscusLogger.print("BackgroundForegroundListener", "onDestroy");
    }

    boolean isForeground() {
        return foreground;
    }

    public static void setAppActiveOrForground(){
        foreground = true;
    }
}