package com.qiscus.sdk.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.qiscus.sdk.chat.core.QiscusCore;
import com.qiscus.sdk.chat.core.data.model.QiscusChatRoom;
import com.qiscus.sdk.chat.core.data.model.QiscusComment;
import com.qiscus.sdk.chat.core.data.remote.QiscusApi;
import com.qiscus.sdk.ui.QiscusChatActivity;
import com.qiscus.sdk.ui.QiscusGroupChatActivity;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class NotificationClickReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        QiscusComment qiscusComment = intent.getParcelableExtra("data");
        QiscusApi.getInstance()
                .getChatRoom(qiscusComment.getRoomId())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(qiscusChatRoom -> QiscusCore.getDataStore().addOrUpdate(qiscusChatRoom))
                .map(qiscusChatRoom -> getChatRoomActivity(context, qiscusChatRoom))
                .subscribe(newIntent -> start(context, newIntent), throwable ->
                        Toast.makeText(context, throwable.getLocalizedMessage(), Toast.LENGTH_SHORT).show());
    }

    private Intent getChatRoomActivity(Context context, QiscusChatRoom qiscusChatRoom) {
        return qiscusChatRoom.isGroup() ? QiscusGroupChatActivity.generateIntent(context, qiscusChatRoom) :
                QiscusChatActivity.generateIntent(context, qiscusChatRoom);
    }

    private void start(Context context, Intent newIntent) {
        context.startActivity(newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
    }
}
