package com.qiscus.sdk.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.qiscus.sdk.Qiscus;
import com.qiscus.sdk.data.remote.QiscusResendCommentHelper;
import com.qiscus.sdk.util.QiscusAndroidUtil;
import com.qiscus.sdk.util.QiscusLogger;

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
        if (Qiscus.hasSetupUser() && Qiscus.getDataStore().getPendingComments().size() > 0) {
            QiscusAndroidUtil.runOnBackgroundThread(() -> {
                QiscusResendCommentHelper.cancelAll();
                QiscusResendCommentHelper.tryResendPendingComment();
            });
        }
    }

}