package com.qiscus.dragonfly;

public class FirebaseUtil {

    public static void sendCurrentToken() {
        MyFirebaseService.getCurrentDeviceToken();
    }
}