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

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.core.util.PatternsCompat;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;

import com.qiscus.sdk.chat.core.QiscusCore;
import com.qiscus.sdk.chat.core.data.model.MentionClickHandler;
import com.qiscus.sdk.chat.core.data.model.QAccount;
import com.qiscus.sdk.chat.core.data.model.QiscusRoomMember;

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

    private QiscusTextUtil() {

    }

    @NonNull
    public static String getString(@StringRes int resId) {
        return QiscusCore.getApps().getString(resId);
    }

    @NonNull
    public static String getString(@StringRes int resId, Object... formatArgs) {
        return QiscusCore.getApps().getString(resId, formatArgs);
    }

    public static String getRandomString(int length) {
        char[] buf = new char[length];
        for (int i = 0; i < buf.length; i++) {
            buf[i] = symbols[random.nextInt(symbols.length)];
        }
        return new String(buf);
    }

    public static boolean isBlank(final CharSequence cs) {
        int strLen;
        if (cs == null || (strLen = cs.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(cs.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static boolean isNotBlank(final CharSequence cs) {
        return !isBlank(cs);
    }

    public static boolean isUrl(String s) {
        return PatternsCompat.AUTOLINK_WEB_URL.matcher(s).matches();
    }

    public static List<String> extractUrl(String text) {
        List<String> urls = new ArrayList<>();
        Matcher matcher = PatternsCompat.AUTOLINK_WEB_URL.matcher(text);
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

        if (message == null) {
            return new SpannableString("");
        }

        QAccount qAccount = QiscusCore.getQiscusAccount();

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
                            if (mentionClickListener != null) {
                                mentionClickListener.onMentionClick(mentionedUser);
                            }
                        }

                        @Override
                        public void updateDrawState(TextPaint ds) {
                            if (mentionedUserId.equals("all")) {
                                ds.setColor(mentionAllColor);
                            } else if (mentionedUserId.equals(qAccount.getId())) {
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
