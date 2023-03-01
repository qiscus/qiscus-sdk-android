package com.qiscus.dragonfly;

import android.util.Log;

import androidx.annotation.NonNull;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.qiscus.sdk.chat.core.QiscusCore;
import com.qiscus.sdk.chat.core.util.QiscusFirebaseMessagingUtil;

public class AppFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Log.d("Qiscus", "onMessageReceived " + remoteMessage.getData().toString());
        if (QiscusFirebaseMessagingUtil.handleMessageReceived(remoteMessage)) {
            return;
        }
    }

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);

        Log.d("Qiscus", "onNewToken " + s);
        QiscusCore.registerDeviceToken(s);
    }

    public static void getCurrentDeviceToken() {
        final String token = QiscusCore.getFcmToken();
        if (token != null) {
            FirebaseMessaging.getInstance().deleteToken()
                    .addOnCompleteListener(task -> {
                        QiscusCore.removeDeviceToken(token);
                        getTokenFcm();
                    })
                    .addOnFailureListener(e -> QiscusCore.registerDeviceToken(token));
        } else {
            getTokenFcm();
        }
    }

    private static void getTokenFcm() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        QiscusCore.registerDeviceToken(task.getResult());
                    } else {
                        Log.e("Qiscus", "getCurrentDeviceToken Failed : " +
                                task.getException());
                    }
                });
    }
}