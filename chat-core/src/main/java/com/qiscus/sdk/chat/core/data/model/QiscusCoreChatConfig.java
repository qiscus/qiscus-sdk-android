package com.qiscus.sdk.chat.core.data.model;

import com.qiscus.sdk.chat.core.QiscusAppComponent;

/**
 * @author Yuana andhikayuana@gmail.com
 * @since Jul, Thu 26 2018 11.50
 **/
public class QiscusCoreChatConfig {

    private static QiscusCoreChatConfig INSTANCE;

    public static QiscusCoreChatConfig getInstance() {
        if (INSTANCE == null) {
            synchronized (QiscusCoreChatConfig.class) {
                if (INSTANCE == null) {
                   INSTANCE = new QiscusCoreChatConfig();
                }
            }
        }
        return INSTANCE;
    }

    private boolean enableLog = false;
    private boolean enableFcmPushNotification = false;
    private QiscusCommentSendingInterceptor qiscusCommentSendingInterceptor = qiscusComment -> qiscusComment;
    private QiscusImageCompressionConfig qiscusImageCompressionConfig = new QiscusImageCompressionConfig();
    private NotificationListener notificationListener;
    private DeleteCommentListener deleteCommentListener;

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

    public NotificationListener getNotificationListener() {
        return notificationListener;
    }

    public QiscusCoreChatConfig setNotificationListener(NotificationListener notificationListener) {
        this.notificationListener = notificationListener;
        return this;
    }

    public DeleteCommentListener getDeleteCommentListener() {
        return deleteCommentListener;
    }

    public QiscusCoreChatConfig setDeleteCommentListener(DeleteCommentListener deleteCommentListener) {
        this.deleteCommentListener = deleteCommentListener;
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
