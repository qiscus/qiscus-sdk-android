package com.qiscus.sdk.chat.core;


import android.app.Activity;
import android.util.Log;

class MyAppLifecycleObserver : ApplicationLifecycleObserver() {
    override fun onAppForeground(owner: LifecycleOwner) {
        Log.d(this::class.simpleName, "onAppForeground: Application resumed")
    }

    override fun onAppBackground(owner: LifecycleOwner) {
        Log.d(this::class.simpleName, "onAppBackground: Application paused")
    }
}