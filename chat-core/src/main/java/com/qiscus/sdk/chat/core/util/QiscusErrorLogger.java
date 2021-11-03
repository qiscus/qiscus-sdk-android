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

import android.util.Log;

import androidx.annotation.RestrictTo;

import com.qiscus.sdk.chat.core.QiscusCore;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.HttpException;

/**
 * Created on : August 24, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public final class QiscusErrorLogger {

    private static final String TAG = "Qiscus";
    private QiscusCore qiscusCore;

    public QiscusErrorLogger(QiscusCore qiscusCore) {
        this.qiscusCore = qiscusCore;
    }

    public void print(Throwable throwable) {
        if (qiscusCore.getChatConfig().isEnableLog()) {
            try {
                Log.e(TAG, qiscusCore.getAppId() + "-" + getMessage(throwable));
            } catch (NullPointerException n) {
                Log.e(TAG, qiscusCore.getAppId() + "-" + "error with no message");
            }
        }
    }

    public void print(String tag, Throwable throwable) {
        if (qiscusCore.getChatConfig().isEnableLog()) {
            try {
                Log.e(tag, qiscusCore.getAppId() + "-" + getMessage(throwable));
            } catch (NullPointerException n) {
                Log.e(tag, qiscusCore.getAppId() + "-" + "error with no message");
            }
        }
    }

    public void print(String tag, String errorMessage) {
        if (qiscusCore.getChatConfig().isEnableLog()) {
            Log.e(tag, qiscusCore.getAppId() + "-" + errorMessage);
        }
    }

    public String getMessage(Throwable throwable) {
        if (throwable instanceof HttpException) { //Error response from server
            HttpException e = (HttpException) throwable;
            try {
                ResponseBody responseBody = e.response().errorBody();
                if (responseBody != null) {
                    return e.code() + ": " + responseBody.string();
                }
            } catch (IOException e1) {
                return e1.getMessage();
            }
        } else if (throwable instanceof IOException) { //Error from network
            return "Can not connect to qiscus server!";
        }
        return throwable.getMessage();
    }
}
