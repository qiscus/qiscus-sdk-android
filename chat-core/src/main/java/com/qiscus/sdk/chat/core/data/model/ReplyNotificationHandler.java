package com.qiscus.sdk.chat.core.data.model;

import android.content.Context;

/**
 * Created on : May 25, 2017
 * Author     : rajapulau
 * Name       : Ganjar Widiatmansyah
 * GitHub     : https://github.com/rajapulau
 */

public interface ReplyNotificationHandler {
    void onSend(Context context, QiscusComment qiscusComment);
}
