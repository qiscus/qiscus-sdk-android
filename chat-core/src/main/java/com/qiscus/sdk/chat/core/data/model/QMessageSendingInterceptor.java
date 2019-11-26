package com.qiscus.sdk.chat.core.data.model;

/**
 * @author yuana
 * @since 10/20/17
 */

public interface QMessageSendingInterceptor {
    QMessage sendComment(QMessage qiscusMessage);
}
