package com.qiscus.sdk.chat.core.data.model;

/**
 * @author Yuana andhikayuana@gmail.com
 * @since Jul, Thu 26 2018 11.50
 **/
public class QiscusCoreChatConfig {

    private boolean enableLog = false;
    private boolean enableFcmPushNotification = false;
    private QMessageSendingInterceptor qMessageSendingInterceptor = qMessage -> qMessage;
    private QiscusImageCompressionConfig qiscusImageCompressionConfig = new QiscusImageCompressionConfig();
    private NotificationListener notificationListener;
    private DeleteMessageListener deleteMessageListener;

    public boolean isEnableFcmPushNotification() {
        return enableFcmPushNotification;
    }

    public QiscusCoreChatConfig setEnableFcmPushNotification(boolean enableFcmPushNotification) {
        this.enableFcmPushNotification = enableFcmPushNotification;
        return this;
    }

    public QMessageSendingInterceptor getCommentSendingInterceptor() {
        return qMessageSendingInterceptor;
    }

    public QiscusCoreChatConfig setCommentSendingInterceptor(QMessageSendingInterceptor
                                                                     qMessageSendingInterceptor) {
        this.qMessageSendingInterceptor = qMessageSendingInterceptor;
        return this;
    }

    public QiscusImageCompressionConfig getQiscusImageCompressionConfig() {
        return qiscusImageCompressionConfig;
    }

    public QiscusCoreChatConfig setQiscusImageCompressionConfig(QiscusImageCompressionConfig qiscusImageCompressionConfig) {
        this.qiscusImageCompressionConfig = qiscusImageCompressionConfig;
        return this;
    }

    public NotificationListener getNotificationListener() {
        return notificationListener;
    }

    public QiscusCoreChatConfig setNotificationListener(NotificationListener notificationListener) {
        this.notificationListener = notificationListener;
        return this;
    }

    public DeleteMessageListener getDeleteMessageListener() {
        return deleteMessageListener;
    }

    public QiscusCoreChatConfig setDeleteMessageListener(DeleteMessageListener deleteMessageListener) {
        this.deleteMessageListener = deleteMessageListener;
        return this;
    }

    public boolean isEnableLog() {
        return enableLog;
    }

    @Deprecated
    public QiscusCoreChatConfig setEnableLog(boolean enableLog) {
        this.enableLog = enableLog;
        return this;
    }

    public QiscusCoreChatConfig enableDebugMode(boolean enableLog) {
        this.enableLog = enableLog;
        return this;
    }
}
