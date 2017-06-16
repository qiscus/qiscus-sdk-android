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

package com.qiscus.sdk.util;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.qiscus.sdk.Qiscus;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ScheduledFuture;
import java.util.regex.Matcher;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * Created on : May 31, 2016
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
public final class QiscusAndroidUtil {

    private static final Random random = new Random();

    private QiscusAndroidUtil() {
    }

    public static void runOnUIThread(Runnable runnable) {
        runOnUIThread(runnable, 0);
    }

    public static void runOnUIThread(Runnable runnable, long delay) {
        if (delay == 0) {
            Qiscus.getAppsHandler().post(runnable);
        } else {
            Qiscus.getAppsHandler().postDelayed(runnable, delay);
        }
    }

    public static void cancelRunOnUIThread(Runnable runnable) {
        Qiscus.getAppsHandler().removeCallbacks(runnable);
    }

    public static ScheduledFuture<?> runOnBackgroundThread(Runnable runnable) {
        return runOnBackgroundThread(runnable, 0);
    }

    public static ScheduledFuture<?> runOnBackgroundThread(Runnable runnable, long delay) {
        if (delay == 0) {
            return Qiscus.getTaskExecutor().schedule(runnable, 0, MILLISECONDS);
        }
        return Qiscus.getTaskExecutor().schedule(runnable, delay, MILLISECONDS);
    }

    public static boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager)
                Qiscus.getApps().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    public static int compare(int x, int y) {
        return (x < y) ? -1 : ((x == y) ? 0 : 1);
    }

    public static boolean isUrl(String s) {
        return QiscusPatterns.AUTOLINK_WEB_URL.matcher(s).matches();
    }

    public static List<String> extractUrl(String text) {
        List<String> urls = new ArrayList<>();
        Matcher matcher = QiscusPatterns.AUTOLINK_WEB_URL.matcher(text);
        while (matcher.find()) {
            int start = matcher.start();
            if (start > 0 && text.charAt(start - 1) == '@') {
                continue;
            }
            int end = matcher.end();
            if (end < text.length() && text.charAt(end) == '@') {
                continue;
            }

            String url = matcher.group();
            if (!url.startsWith("http")) {
                url = "http://" + url;
            }
            urls.add(url);
        }
        return urls;
    }

    public static int getRandomColor() {
        return Color.argb(100, random.nextInt(256), random.nextInt(256), random.nextInt(256));
    }

    public static float dp2px(Resources resources, float dp) {
        final float scale = resources.getDisplayMetrics().density;
        return dp * scale + 0.5f;
    }

    public static float sp2px(Resources resources, float sp) {
        final float scale = resources.getDisplayMetrics().scaledDensity;
        return sp * scale;
    }

    public static void showKeyboard(Context context, View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (view.requestFocus()) {
            inputMethodManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    public static void hideKeyboard(Context context, View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @NonNull
    public static String getString(@StringRes int resId) {
        return Qiscus.getApps().getString(resId);
    }

    @NonNull
    public static String getString(@StringRes int resId, Object... formatArgs) {
        return Qiscus.getApps().getString(resId, formatArgs);
    }
}
