package com.qiscus.sdk.chat.core.data.model;

import android.content.Context;

import java.util.List;

/**
 * @author Yuana andhikayuana@gmail.com
 * @since Aug, Tue 14 2018 10.00
 **/
public interface DeleteMessageListener {

    void onHandleDeletedCommentNotification(Context context, List<QMessage> messages, boolean hardDelete);
}
