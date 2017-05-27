package com.qiscus.sdk.ui;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.qiscus.sdk.Qiscus;
import com.qiscus.sdk.data.local.QiscusCacheManager;
import com.qiscus.sdk.data.model.QiscusComment;
import com.qiscus.sdk.util.QiscusReplyDialog;

public class QiscusQuickReplyActivity extends AppCompatActivity {

    private static final String TAG = QiscusQuickReplyActivity.class.getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        QiscusComment comment = intent.getParcelableExtra("data");

        NotificationManager notificationManager = (NotificationManager) getSystemService(
                Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(comment.getRoomId());
        QiscusCacheManager.getInstance().clearMessageNotifItems(comment.getRoomId());

        String getRepliedTo = (comment.isGroupMessage()) ? comment.getRoomName() : comment.getSender();
        String getAvatar = (comment.isGroupMessage()) ? comment.getRoomAvatar() : comment.getSenderAvatar();
        
        final QiscusReplyDialog customDialog = new QiscusReplyDialog.Builder(getRepliedTo, comment.getMessage(),
                QiscusReplyDialog.ButtonOrientation.HORIZONTAL)
                .setResImage(getAvatar).build();
        customDialog.setDismissListener(dialog -> finish());
        customDialog.setOnClickSubmitListener(result -> {
            customDialog.dismiss();
            finish();
            QiscusComment qiscusComment = QiscusComment.generateMessage(result, comment.getRoomId(), comment.getTopicId());
            Qiscus.getChatConfig().sendReplyNotificationHandler().onSend(getApplicationContext(), qiscusComment);
        });
        customDialog.show(QiscusQuickReplyActivity.this.getFragmentManager(), TAG);
    }
}
