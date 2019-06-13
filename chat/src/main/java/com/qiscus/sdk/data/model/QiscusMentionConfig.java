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

package com.qiscus.sdk.data.model;

import androidx.annotation.ColorInt;
import androidx.core.content.ContextCompat;

import com.qiscus.sdk.Qiscus;
import com.qiscus.sdk.R;
import com.qiscus.sdk.chat.core.data.model.MentionClickHandler;

/**
 * Created on : September 12, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
public class QiscusMentionConfig {
    private int leftMentionAllColor = ContextCompat.getColor(Qiscus.getApps(), R.color.qiscus_mention_all);
    private int leftMentionOtherColor = ContextCompat.getColor(Qiscus.getApps(), R.color.qiscus_mention_other);
    private int leftMentionMeColor = ContextCompat.getColor(Qiscus.getApps(), R.color.qiscus_mention_me);

    private int rightMentionAllColor = ContextCompat.getColor(Qiscus.getApps(), R.color.qiscus_mention_all);
    private int rightMentionOtherColor = ContextCompat.getColor(Qiscus.getApps(), R.color.qiscus_mention_other);
    private int rightMentionMeColor = ContextCompat.getColor(Qiscus.getApps(), R.color.qiscus_mention_me);

    private int editTextMentionAllColor = ContextCompat.getColor(Qiscus.getApps(), R.color.qiscus_mention_all);
    private int editTextMentionOtherColor = ContextCompat.getColor(Qiscus.getApps(), R.color.qiscus_mention_other);
    private int editTextMentionMeColor = ContextCompat.getColor(Qiscus.getApps(), R.color.qiscus_mention_me);

    private MentionClickHandler mentionClickHandler;
    private boolean enableMention = false;

    public int getLeftMentionAllColor() {
        return leftMentionAllColor;
    }

    public QiscusMentionConfig setLeftMentionAllColor(@ColorInt int leftMentionAllColor) {
        this.leftMentionAllColor = leftMentionAllColor;
        return this;
    }

    @ColorInt
    public int getLeftMentionOtherColor() {
        return leftMentionOtherColor;
    }

    public QiscusMentionConfig setLeftMentionOtherColor(@ColorInt int leftMentionOtherColor) {
        this.leftMentionOtherColor = leftMentionOtherColor;
        return this;
    }

    @ColorInt
    public int getLeftMentionMeColor() {
        return leftMentionMeColor;
    }

    public QiscusMentionConfig setLeftMentionMeColor(@ColorInt int leftMentionMeColor) {
        this.leftMentionMeColor = leftMentionMeColor;
        return this;
    }

    @ColorInt
    public int getRightMentionAllColor() {
        return rightMentionAllColor;
    }

    public QiscusMentionConfig setRightMentionAllColor(@ColorInt int rightMentionAllColor) {
        this.rightMentionAllColor = rightMentionAllColor;
        return this;
    }

    @ColorInt
    public int getRightMentionOtherColor() {
        return rightMentionOtherColor;
    }

    public QiscusMentionConfig setRightMentionOtherColor(@ColorInt int rightMentionOtherColor) {
        this.rightMentionOtherColor = rightMentionOtherColor;
        return this;
    }

    @ColorInt
    public int getRightMentionMeColor() {
        return rightMentionMeColor;
    }

    public QiscusMentionConfig setRightMentionMeColor(@ColorInt int rightMentionMeColor) {
        this.rightMentionMeColor = rightMentionMeColor;
        return this;
    }

    @ColorInt
    public int getEditTextMentionAllColor() {
        return editTextMentionAllColor;
    }

    public QiscusMentionConfig setEditTextMentionAllColor(@ColorInt int editTextMentionAllColor) {
        this.editTextMentionAllColor = editTextMentionAllColor;
        return this;
    }

    @ColorInt
    public int getEditTextMentionOtherColor() {
        return editTextMentionOtherColor;
    }

    public QiscusMentionConfig setEditTextMentionOtherColor(@ColorInt int editTextMentionOtherColor) {
        this.editTextMentionOtherColor = editTextMentionOtherColor;
        return this;
    }

    @ColorInt
    public int getEditTextMentionMeColor() {
        return editTextMentionMeColor;
    }

    public QiscusMentionConfig setEditTextMentionMeColor(@ColorInt int editTextMentionMeColor) {
        this.editTextMentionMeColor = editTextMentionMeColor;
        return this;
    }

    public MentionClickHandler getMentionClickHandler() {
        return mentionClickHandler;
    }

    public QiscusMentionConfig setMentionClickHandler(MentionClickHandler mentionClickHandler) {
        this.mentionClickHandler = mentionClickHandler;
        return this;
    }

    public boolean isEnableMention() {
        return enableMention;
    }

    public QiscusMentionConfig setEnableMention(boolean enableMention) {
        this.enableMention = enableMention;
        return this;
    }
}
