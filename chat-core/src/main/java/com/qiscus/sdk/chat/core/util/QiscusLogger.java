package com.qiscus.sdk.chat.core.util;

import android.support.annotation.RestrictTo;
import android.util.Log;

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
    private static boolean enableLog = QiscusCore.getChatConfig().isEnableLog();

    public static void print(String message) {
        if (enableLog) {
            Log.i(TAG, message);
        }
    }

    public static void print(String tag, String message) {
        if (enableLog) {
            Log.i(tag, message);
        }
    }
}
