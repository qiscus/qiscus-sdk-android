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

package com.qiscus.sdk.chat.core.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.qiscus.sdk.chat.core.QiscusCore;
import com.qiscus.sdk.chat.core.data.remote.QiscusResendCommentHelper;
import com.qiscus.sdk.chat.core.util.QiscusAndroidUtil;
import com.qiscus.sdk.chat.core.util.QiscusLogger;

/**
 * @author Yuana andhikayuana@gmail.com
 * @since Jul, Mon 23 2018 14.45
 **/
public class QiscusNetworkStateReceiver extends BroadcastReceiver {

    private static final String TAG = QiscusNetworkStateReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        boolean isConnected = QiscusAndroidUtil.isNetworkAvailable();
        QiscusLogger.print(TAG, "isConnected : " + isConnected);
        QiscusAndroidUtil.runOnBackgroundThread(() -> {
            if (needResend(isConnected)) {
                QiscusResendCommentHelper.cancelAll();
                QiscusResendCommentHelper.tryResendPendingComment();
            }
        });
    }

    private boolean needResend(boolean isConnected) {
        return isConnected && QiscusCore.hasSetupUser() && QiscusCore.getDataStore().getPendingComments().size() > 0;
    }

}