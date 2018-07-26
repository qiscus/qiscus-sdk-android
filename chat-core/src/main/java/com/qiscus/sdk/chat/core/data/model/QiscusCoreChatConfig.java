package com.qiscus.sdk.chat.core.data.model;

/**
 * @author Yuana andhikayuana@gmail.com
 * @since Jul, Thu 26 2018 11.50
 **/
public class QiscusCoreChatConfig {

    private boolean enableFcmPushNotification = false;

    public boolean isEnableFcmPushNotification() {
        return enableFcmPushNotification;
    }

    public QiscusCoreChatConfig setEnableFcmPushNotification(boolean enableFcmPushNotification) {
        this.enableFcmPushNotification = enableFcmPushNotification;
        return this;
    }
}
