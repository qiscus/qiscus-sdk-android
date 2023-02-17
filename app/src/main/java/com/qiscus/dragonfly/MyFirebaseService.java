package com.qiscus.dragonfly;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.qiscus.sdk.Qiscus;
import com.qiscus.sdk.chat.core.QiscusCore;
import com.qiscus.sdk.chat.core.util.QiscusFirebaseMessagingUtil;
import com.qiscus.sdk.service.QiscusFirebaseService;

public class MyFirebaseService extends FirebaseMessagingService {

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
        Qiscus.setFcmToken(s);
    }

    public static void getCurrentDeviceToken() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.e("Qiscus", "getCurrentDeviceToken Failed : " +
                                    task.getException());
                            return;
                        }

                        if (task.getResult() != null) {
                            String currentToken = task.getResult();

                            Qiscus.setFcmToken(currentToken);
                        }

                    }
                });
    }
}