package com.qiscus.sdk.data.model;

import com.qiscus.sdk.chat.core.data.model.QiscusComment;

/**
 * @author yuana <andhikayuana@gmail.com>
 * @since 10/17/17
 */

public interface QiscusRoomSenderNameInterceptor {
    String getSenderName(QiscusComment qiscusComment);
}
