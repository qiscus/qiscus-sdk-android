package com.qiscus.sdk.chat.core.data.model;

import android.content.Context;

import java.util.List;

/**
 * @author Yuana andhikayuana@gmail.com
 * @since Aug, Mon 13 2018 16.29
 **/
public interface PushNotificationListener {

    void onHandlePushNotification(Context context, QiscusComment qiscusComment);

    void onHandleDeletedCommentNotification(Context context, List<QiscusComment> comments, boolean hardDelete);
}
