package com.qiscus.sdk.chat.core.util;

import android.util.Log;

import com.qiscus.sdk.chat.core.QiscusCore;

/**
 * Created on : November 02, 2017
 * Author     : adicatur
 * Name       : Catur Adi Nugroho
 * GitHub     : https://github.com/adicatur
 */
public final class QiscusLogger {

    private static final String TAG = "Qiscus";

    public static void print(String message) {
        if (QiscusCore.isEnableLog()) {
            Log.i(TAG, message);
        }
    }

    public static void print(String tag, String message) {
        if (QiscusCore.isEnableLog()) {
            Log.i(tag, message);
        }
    }
}
