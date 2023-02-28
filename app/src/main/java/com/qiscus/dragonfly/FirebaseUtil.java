package com.qiscus.dragonfly;
import com.qiscus.dragonfly.AppFirebaseMessagingService;

public class FirebaseUtil {

    public static void sendCurrentToken() {
        AppFirebaseMessagingService.getCurrentDeviceToken();
    }
}
