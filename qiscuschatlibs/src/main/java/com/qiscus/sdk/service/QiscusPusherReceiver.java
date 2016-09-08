package com.qiscus.sdk.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.qiscus.sdk.Qiscus;
import com.qiscus.sdk.data.model.QiscusComment;

/**
 * Created by zetra. on 9/8/16.
 */
public class QiscusPusherReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        QiscusComment comment = intent.getParcelableExtra("data");
        Qiscus.buildChatWith(comment.getSenderEmail())
                .withTitle(comment.getSender())
                .build(context, new Qiscus.ChatActivityBuilderListener() {
                    @Override
                    public void onSuccess(Intent intent) {
                        context.startActivity(intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                                | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        throwable.printStackTrace();
                        Toast.makeText(context, throwable.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
