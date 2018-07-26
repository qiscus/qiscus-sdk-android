package com.qiscus.sdk.data.model;

import com.qiscus.sdk.chat.core.data.model.QiscusComment;

/**
 * @author yuana
 * @since 10/20/17
 */

public interface QiscusCommentSendingInterceptor {
    QiscusComment sendComment(QiscusComment qiscusComment);
}
