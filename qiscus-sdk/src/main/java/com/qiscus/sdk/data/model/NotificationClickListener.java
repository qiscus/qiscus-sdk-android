package com.qiscus.sdk.data.model;

import android.content.Context;

/**
 * Created by zetra. on 9/19/16.
 */
public interface NotificationClickListener {
    void onClick(Context context, QiscusComment qiscusComment);
}
