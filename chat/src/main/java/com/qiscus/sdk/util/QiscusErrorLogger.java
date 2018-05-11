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

import android.util.Log;

import com.qiscus.sdk.Qiscus;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.HttpException;

/**
 * Created on : August 24, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
public final class QiscusErrorLogger {

    private static final String TAG = Qiscus.class.getSimpleName();

    public static void print(Throwable throwable) {
        if (Qiscus.isEnableLog()) {
            Log.e(TAG, getMessage(throwable));
        }
    }

    public static void print(String tag, Throwable throwable) {
        if (Qiscus.isEnableLog()) {
            Log.e(tag, getMessage(throwable));
        }
    }

    public static void print(String tag, String errorMessage) {
        if (Qiscus.isEnableLog()) {
            Log.e(tag, errorMessage);
        }
    }

    public static String getMessage(Throwable throwable) {
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
        return throwable.getMessage() == null ? throwable.getClass().getSimpleName() : throwable.getMessage();
    }
}
