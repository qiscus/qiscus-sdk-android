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
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            context.startService(new Intent(context, QiscusPusherService.class));
        } else if (intent.getAction().equals("com.qiscus.OPEN_COMMENT_PN")) {
            QiscusComment comment = intent.getParcelableExtra("data");
            Qiscus.getChatConfig().getNotificationClickListener().onClick(context, comment);
        }
    }
}
