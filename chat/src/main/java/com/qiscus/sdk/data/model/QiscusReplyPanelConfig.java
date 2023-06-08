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
import androidx.annotation.DrawableRes;
import androidx.core.content.ContextCompat;

import com.qiscus.sdk.Qiscus;
import com.qiscus.sdk.R;

/**
 * Created on : March 12, 2018
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
public class QiscusReplyPanelConfig {
    private Integer backgroundColor = null;
    private Integer senderNameColor = null;
    private Integer messageColor = null;
    private Integer barColor = null;
    private Integer cancelIconResourceId = null;
    private Integer cancelIconTintColor = null;

    @ColorInt
    public int getBackgroundColor() {
        if (backgroundColor == null) {
            backgroundColor = ContextCompat.getColor(Qiscus.getApps(), R.color.qiscus_white);
        }
        return backgroundColor;
    }

    public QiscusReplyPanelConfig setBackgroundColor(@ColorInt int backgroundColor) {
        this.backgroundColor = backgroundColor;
        return this;
    }

    @ColorInt
    public int getSenderNameColor() {
        if (senderNameColor == null) {
            senderNameColor = ContextCompat.getColor(Qiscus.getApps(), R.color.qiscus_secondary_text);
        }
        return senderNameColor;
    }

    public QiscusReplyPanelConfig setSenderNameColor(@ColorInt int senderNameColor) {
        this.senderNameColor = senderNameColor;
        return this;
    }

    @ColorInt
    public int getMessageColor() {
        if (messageColor == null) {
            messageColor = ContextCompat.getColor(Qiscus.getApps(), R.color.qiscus_secondary_text);
        }
        return messageColor;
    }

    public QiscusReplyPanelConfig setMessageColor(@ColorInt int messageColor) {
        this.messageColor = messageColor;
        return this;
    }

    @ColorInt
    public int getBarColor() {
        if (barColor == null) {
            barColor = ContextCompat.getColor(Qiscus.getApps(), R.color.qiscus_primary);
        }
        return barColor;
    }

    public QiscusReplyPanelConfig setBarColor(@ColorInt int barColor) {
        this.barColor = barColor;
        return this;
    }

    @DrawableRes
    public int getCancelIconResourceId() {
        if (cancelIconResourceId == null) {
            cancelIconResourceId = R.drawable.ic_qiscus_action_close;
        }
        return cancelIconResourceId;
    }

    public QiscusReplyPanelConfig setCancelIconResourceId(@DrawableRes int cancelIconResourceId) {
        this.cancelIconResourceId = cancelIconResourceId;
        return this;
    }

    @ColorInt
    public int getCancelIconTintColor() {
        if (cancelIconTintColor == null) {
            cancelIconTintColor = ContextCompat.getColor(Qiscus.getApps(), R.color.qiscus_secondary_text);
        }
        return cancelIconTintColor;
    }

    public QiscusReplyPanelConfig setCancelIconTintColor(@ColorInt int cancelIconTintColor) {
        this.cancelIconTintColor = cancelIconTintColor;
        return this;
    }
}
