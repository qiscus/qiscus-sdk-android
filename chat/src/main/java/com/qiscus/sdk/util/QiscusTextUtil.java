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

import android.support.annotation.NonNull;
import android.support.annotation.StringRes;

import com.qiscus.sdk.Qiscus;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;

/**
 * Created on : September 12, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
public final class QiscusTextUtil {
    private static final Random random = new Random();
    private static final char[] symbols;

    private QiscusTextUtil() {

    }

    static {
        StringBuilder tmp = new StringBuilder();
        for (char ch = '0'; ch <= '9'; ch++) {
            tmp.append(ch);
        }
        for (char ch = 'a'; ch <= 'z'; ch++) {
            tmp.append(ch);
        }
        symbols = tmp.toString().toCharArray();
    }

    @NonNull
    public static String getString(@StringRes int resId) {
        return Qiscus.getApps().getString(resId);
    }

    @NonNull
    public static String getString(@StringRes int resId, Object... formatArgs) {
        return Qiscus.getApps().getString(resId, formatArgs);
    }

    public static String getRandomString(int length) {
        char[] buf = new char[length];
        for (int i = 0; i < buf.length; i++) {
            buf[i] = symbols[random.nextInt(symbols.length)];
        }
        return new String(buf);
    }

    public static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
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
}
