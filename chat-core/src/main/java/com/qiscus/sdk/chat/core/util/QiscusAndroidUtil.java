/*
 * Copyright (c) 2016 Qiscus.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.qiscus.sdk.chat.core.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.qiscus.sdk.chat.core.QiscusCore;

import java.util.Random;
import java.util.concurrent.ScheduledFuture;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * Created on : May 31, 2016
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
public final class QiscusAndroidUtil {

    private static final Random random = new Random();

    private static QiscusCore qiscusCore;

    public QiscusAndroidUtil(QiscusCore qiscusCore) {
        QiscusAndroidUtil.qiscusCore = qiscusCore;
    }

    public static void runOnUIThread(Runnable runnable) {
        runOnUIThread(runnable, 0);
    }

    public static void runOnUIThread(Runnable runnable, long delay) {
        if (delay == 0) {
            qiscusCore.getAppsHandler().post(runnable);
        } else {
            qiscusCore.getAppsHandler().postDelayed(runnable, delay);
        }
    }

    public static void cancelRunOnUIThread(Runnable runnable) {
        qiscusCore.getAppsHandler().removeCallbacks(runnable);
    }

    public static ScheduledFuture<?> runOnBackgroundThread(Runnable runnable) {
        return runOnBackgroundThread(runnable, 0);
    }

    public static ScheduledFuture<?> runOnBackgroundThread(Runnable runnable, long delay) {
        if (delay == 0) {
            return qiscusCore.getTaskExecutor().schedule(runnable, 0, MILLISECONDS);
        }
        return qiscusCore.getTaskExecutor().schedule(runnable, delay, MILLISECONDS);
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager)
                qiscusCore.getApps().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm != null ? cm.getActiveNetworkInfo() : null;
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    public int compare(int x, int y) {
        return (x < y) ? -1 : ((x == y) ? 0 : 1);
    }

    public int compare(long x, long y) {
        return (x < y) ? -1 : ((x == y) ? 0 : 1);
    }
}
