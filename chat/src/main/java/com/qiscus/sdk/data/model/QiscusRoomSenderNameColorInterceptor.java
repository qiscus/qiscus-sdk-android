package com.qiscus.sdk.data.model;

import android.support.annotation.ColorRes;

import com.qiscus.sdk.chat.core.data.model.QiscusComment;

/**
 * @author yuana <andhikayuana@gmail.com>
 * @since 1/10/18
 */

public interface QiscusRoomSenderNameColorInterceptor {
    @ColorRes
    int getColor(QiscusComment qiscusComment);
}
