package com.qiscus.sdk.data.model;

import android.content.Context;

/**
 * Created by rajapulau on 5/25/17.
 */

public interface ReplyNotificationHandler {
    void onSend(Context context, QiscusComment qiscusComment);
}
