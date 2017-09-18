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

import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;

import com.qiscus.sdk.Qiscus;
import com.qiscus.sdk.data.model.MentionClickHandler;
import com.qiscus.sdk.data.model.QiscusAccount;
import com.qiscus.sdk.data.model.QiscusRoomMember;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

    public static Spannable createQiscusSpannableText(
            String message,
            Map<String, QiscusRoomMember> members,
            @ColorInt int mentionAllColor, @ColorInt int mentionOtherColor,
            @ColorInt int mentionMeColor, MentionClickHandler mentionClickListener) {

        QiscusAccount qiscusAccount = Qiscus.getQiscusAccount();

        SpannableStringBuilder spannable = new SpannableStringBuilder();
        int length = message.length();
        int lastNotMention = 0;
        int startPosition = 0;
        boolean ongoing = false;
        for (int i = 0; i < length; i++) {
            if (!ongoing && i < length - 1 && message.charAt(i) == '@' && message.charAt(i + 1) == '[') {
                ongoing = true;
                startPosition = i;
            }

            if (ongoing && message.charAt(i) == ']') {
                String mentionedUserId = message.substring(startPosition + 2, i);
                QiscusRoomMember mentionedUser = members.get(mentionedUserId);
                if (mentionedUser != null) {
                    SpannableString mention = new SpannableString("@" + mentionedUser.getUsername());
                    mention.setSpan(new ClickableSpan() {
                        @Override
                        public void onClick(View widget) {
                            mentionClickListener.onMentionClick(mentionedUser);
                        }

                        @Override
                        public void updateDrawState(TextPaint ds) {
                            if (mentionedUserId.equals("all")) {
                                ds.setColor(mentionAllColor);
                            } else if (mentionedUserId.equals(qiscusAccount.getEmail())) {
                                ds.setColor(mentionMeColor);
                            } else {
                                ds.setColor(mentionOtherColor);
                            }
                        }
                    }, 0, mention.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    if (lastNotMention != startPosition) {
                        spannable.append(message.substring(lastNotMention, startPosition));
                    }
                    spannable.append(mention);
                    lastNotMention = i + 1;
                }
                ongoing = false;
            }
        }
        if (lastNotMention < length) {
            spannable.append(message.substring(lastNotMention, length));
        }
        return spannable;
    }
}
