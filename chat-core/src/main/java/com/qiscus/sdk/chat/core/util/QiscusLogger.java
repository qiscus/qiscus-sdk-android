package com.qiscus.sdk.chat.core.util;

import android.util.Log;

import androidx.annotation.RestrictTo;

import com.qiscus.sdk.chat.core.QiscusCore;

/**
 * Created on : November 02, 2017
 * Author     : adicatur
 * Name       : Catur Adi Nugroho
 * GitHub     : https://github.com/adicatur
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public final class QiscusLogger {

    private static final String TAG = "Qiscus";
    private QiscusCore qiscusCore;

    public QiscusLogger(QiscusCore qiscusCore) {
        this.qiscusCore = qiscusCore;
    }

    public void print(String message) {
        print(TAG, message);
    }

    public void print(String tag, String message) {
        if (qiscusCore.getChatConfig().isEnableLog()) {
            Log.d(tag, message);
        }
    }
}
