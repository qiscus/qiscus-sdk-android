package com.qiscus.sdk.chat.core.data.model;

/**
 * @author Yuana andhikayuana@gmail.com
 * @since Jul, Thu 26 2018 11.50
 **/
public class QiscusCoreChatConfig {

    private boolean enableFcmPushNotification = false;
    private QiscusCommentSendingInterceptor qiscusCommentSendingInterceptor = qiscusComment -> qiscusComment;
    private QiscusImageCompressionConfig qiscusImageCompressionConfig = new QiscusImageCompressionConfig();
    private PushNotificationListener pushNotificationListener;

    public boolean isEnableFcmPushNotification() {
        return enableFcmPushNotification;
    }

    public QiscusCoreChatConfig setEnableFcmPushNotification(boolean enableFcmPushNotification) {
        this.enableFcmPushNotification = enableFcmPushNotification;
        return this;
    }

    public QiscusCommentSendingInterceptor getCommentSendingInterceptor() {
        return qiscusCommentSendingInterceptor;
    }

    public QiscusCoreChatConfig setCommentSendingInterceptor(QiscusCommentSendingInterceptor
                                                                     qiscusCommentSendingInterceptor) {
        this.qiscusCommentSendingInterceptor = qiscusCommentSendingInterceptor;
        return this;
    }

    public QiscusImageCompressionConfig getQiscusImageCompressionConfig() {
        return qiscusImageCompressionConfig;
    }

    public QiscusCoreChatConfig setQiscusImageCompressionConfig(QiscusImageCompressionConfig qiscusImageCompressionConfig) {
        this.qiscusImageCompressionConfig = qiscusImageCompressionConfig;
        return this;
    }

    public PushNotificationListener getPushNotificationListener() {
        return pushNotificationListener;
    }

    public QiscusCoreChatConfig setPushNotificationListener(PushNotificationListener pushNotificationListener) {
        this.pushNotificationListener = pushNotificationListener;
        return this;
    }
}
