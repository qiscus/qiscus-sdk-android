package com.qiscus.sdk.util;

import android.util.Log;

import com.qiscus.sdk.Qiscus;

/**
 * Created by adicatur on 11/2/17.
 */

public class QiscusLogger {

    private static final String TAG = Qiscus.class.getSimpleName();

    public static void print(String message) {
        if (Qiscus.isEnableLog()) {
            Log.d(TAG, message);
        }
    }

    public static void print(String tag,String message) {
        if (Qiscus.isEnableLog()) {
            Log.i(tag, message);
        }
    }

}
