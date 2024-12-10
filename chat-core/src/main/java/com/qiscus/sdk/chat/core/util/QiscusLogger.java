package com.qiscus.sdk.chat.core.util;

import androidx.annotation.RestrictTo;
import android.util.Log;

import com.qiscus.sdk.chat.core.QiscusCore;

import java.util.concurrent.Executors;

/**
 * Created on : November 02, 2017
 * Author     : adicatur
 * Name       : Catur Adi Nugroho
 * GitHub     : https://github.com/adicatur
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public final class QiscusLogger {

    private static final String TAG = "Qiscus";

    public static void print(String message) {
        print(TAG, message);
    }

    public static void print(String tag, String message) {
        Executors.newSingleThreadExecutor().execute(() -> {
            if (QiscusCore.getChatConfig().isEnableLog()) {
                Log.d(tag, message);
            }
        });
    }
}
