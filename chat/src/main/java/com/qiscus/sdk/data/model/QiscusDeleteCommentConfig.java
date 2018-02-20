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

import android.support.annotation.ColorInt;
import android.support.v4.content.ContextCompat;

import com.qiscus.sdk.Qiscus;
import com.qiscus.sdk.R;

/**
 * Created on : February 20, 2018
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
public class QiscusDeleteCommentConfig {
    private boolean enableDeleteComment = false;
    private boolean enableHardDelete = false;

    private int deleteForMeButtonColor = ContextCompat.getColor(Qiscus.getApps(), R.color.qiscus_accent);
    private int deleteForEveryoneButtonColor = ContextCompat.getColor(Qiscus.getApps(), R.color.qiscus_accent);
    private int cancelButtonColor = ContextCompat.getColor(Qiscus.getApps(), R.color.qiscus_accent);

    public boolean isEnableDeleteComment() {
        return enableDeleteComment;
    }

    public QiscusDeleteCommentConfig setEnableDeleteComment(boolean enableDeleteComment) {
        this.enableDeleteComment = enableDeleteComment;
        return this;
    }

    public boolean isEnableHardDelete() {
        return enableHardDelete;
    }

    public QiscusDeleteCommentConfig setEnableHardDelete(boolean enableHardDelete) {
        this.enableHardDelete = enableHardDelete;
        return this;
    }

    @ColorInt
    public int getDeleteForMeButtonColor() {
        return deleteForMeButtonColor;
    }

    public QiscusDeleteCommentConfig setDeleteForMeButtonColor(@ColorInt int deleteForMeButtonColor) {
        this.deleteForMeButtonColor = deleteForMeButtonColor;
        return this;
    }

    @ColorInt
    public int getDeleteForEveryoneButtonColor() {
        return deleteForEveryoneButtonColor;
    }

    public QiscusDeleteCommentConfig setDeleteForEveryoneButtonColor(@ColorInt int deleteForEveryoneButtonColor) {
        this.deleteForEveryoneButtonColor = deleteForEveryoneButtonColor;
        return this;
    }

    @ColorInt
    public int getCancelButtonColor() {
        return cancelButtonColor;
    }

    public QiscusDeleteCommentConfig setCancelButtonColor(@ColorInt int cancelButtonColor) {
        this.cancelButtonColor = cancelButtonColor;
        return this;
    }
}
