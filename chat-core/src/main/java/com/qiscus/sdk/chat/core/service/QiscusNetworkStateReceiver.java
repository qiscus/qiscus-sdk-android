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
import android.util.Log;

import com.qiscus.sdk.chat.core.QiscusCore;
import com.qiscus.sdk.chat.core.data.local.QiscusDataStore;
import com.qiscus.sdk.chat.core.data.model.QiscusComment;
import com.qiscus.sdk.chat.core.data.remote.QiscusResendCommentHelper;
import com.qiscus.sdk.chat.core.util.QiscusAndroidUtil;
import com.qiscus.sdk.chat.core.util.QiscusLogger;

import java.util.List;

/**
 * @author Yuana andhikayuana@gmail.com
 * @since Jul, Mon 23 2018 14.45
 **/
public class QiscusNetworkStateReceiver extends BroadcastReceiver {

    private static final String TAG = QiscusNetworkStateReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!QiscusCore.hasSetupAppID()) {
            return;
        }
        QiscusAndroidUtil.runOnBackgroundThread(() -> {
            handleNetworkChange();
        });
    }

    // ✅ All checks in background thread
    private void handleNetworkChange() {
        // ✅ Quick non-blocking check
        if (!isQiscusInitialized()) {
            QiscusLogger.print(TAG, "Qiscus not initialized, skipping network check");
            return;
        }

        boolean isConnected = QiscusAndroidUtil.isNetworkAvailable();
        QiscusLogger.print(TAG, "isConnected : " + isConnected);

        if (needResend(isConnected)) {
            QiscusResendCommentHelper.cancelAll();
            QiscusResendCommentHelper.tryResendPendingComment();
        }
    }

    // ✅ Safe initialization check
    private boolean isQiscusInitialized() {
        try {
            // ✅ This should now be non-blocking after fixing QiscusAppComponent
            return QiscusCore.hasSetupAppID();
        } catch (Exception e) {
            QiscusLogger.print(TAG, "Error checking Qiscus setup: " + e.getMessage());
            return false;
        }
    }

    // ✅ Safe check with null safety
    private boolean needResend(boolean isConnected) {
        if (!isConnected) {
            return false;
        }

        try {
            // ✅ Check user setup
            if (!QiscusCore.hasSetupUser()) {
                return false;
            }

            // ✅ Null-safe datastore check
            QiscusDataStore dataStore = QiscusCore.getDataStore();
            if (dataStore == null) {
                return false;
            }

            List<QiscusComment> pendingComments = dataStore.getPendingComments();
            return pendingComments != null && pendingComments.size() > 0;

        } catch (Exception e) {
            QiscusLogger.print(TAG, "Error checking resend needs: " + e.getMessage());
            return false;
        }
    }

}