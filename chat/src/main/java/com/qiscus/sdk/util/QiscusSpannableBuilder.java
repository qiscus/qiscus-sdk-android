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
import android.support.v4.content.ContextCompat;
import android.text.Spannable;

import com.qiscus.sdk.Qiscus;
import com.qiscus.sdk.R;
import com.qiscus.sdk.chat.core.data.model.MentionClickHandler;
import com.qiscus.sdk.chat.core.data.model.QiscusRoomMember;
import com.qiscus.sdk.chat.core.util.QiscusTextUtil;

import java.util.Map;

/**
 * Created on : September 15, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
public class QiscusSpannableBuilder {
    private String message;
    private Map<String, QiscusRoomMember> members;
    @ColorInt
    private int mentionAllColor = ContextCompat.getColor(Qiscus.getApps(), R.color.qiscus_mention_all);
    @ColorInt
    private int mentionOtherColor = ContextCompat.getColor(Qiscus.getApps(), R.color.qiscus_mention_other);
    @ColorInt
    private int mentionMeColor = ContextCompat.getColor(Qiscus.getApps(), R.color.qiscus_mention_me);
    private MentionClickHandler mentionClickListener;

    public QiscusSpannableBuilder(String message, Map<String, QiscusRoomMember> members) {
        this.message = message;
        this.members = members;
    }

    public QiscusSpannableBuilder setMentionAllColor(@ColorInt int mentionAllColor) {
        this.mentionAllColor = mentionAllColor;
        return this;
    }

    public QiscusSpannableBuilder setMentionOtherColor(@ColorInt int mentionOtherColor) {
        this.mentionOtherColor = mentionOtherColor;
        return this;
    }

    public QiscusSpannableBuilder setMentionMeColor(@ColorInt int mentionMeColor) {
        this.mentionMeColor = mentionMeColor;
        return this;
    }

    public QiscusSpannableBuilder setMentionClickListener(MentionClickHandler mentionClickListener) {
        this.mentionClickListener = mentionClickListener;
        return this;
    }

    public Spannable build() {
        return QiscusTextUtil.createQiscusSpannableText(
                message,
                members,
                mentionAllColor,
                mentionOtherColor,
                mentionMeColor,
                mentionClickListener
        );
    }
}
