package com.qiscus.sdk.data.model;

/**
 * @author yuana
 * @since 10/20/17
 */

public interface QiscusCommentSendingInterceptor {
    QiscusComment sendComment(QiscusComment qiscusComment);
}
