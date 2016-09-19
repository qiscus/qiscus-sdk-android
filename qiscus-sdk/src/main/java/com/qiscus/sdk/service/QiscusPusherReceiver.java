package com.qiscus.sdk.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.qiscus.sdk.Qiscus;
import com.qiscus.sdk.data.model.QiscusComment;

/**
 * Created by zetra. on 9/8/16.
 */
public class QiscusPusherReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        QiscusComment comment = intent.getParcelableExtra("data");
        Qiscus.getChatConfig().getNotificationClickListener().onClick(context, comment);
    }
}
