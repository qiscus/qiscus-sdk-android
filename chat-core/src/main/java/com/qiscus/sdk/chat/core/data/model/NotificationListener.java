package com.qiscus.sdk.chat.core.data.model;

import android.content.Context;

/**
 * @author Yuana andhikayuana@gmail.com
 * @since Aug, Mon 13 2018 16.29
 **/
public interface NotificationListener {

    void onHandlePushNotification(Context context, QMessage qiscusMessage);
}
