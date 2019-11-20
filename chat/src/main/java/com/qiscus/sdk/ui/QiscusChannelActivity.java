/*
 * Copyright (c) 2016 Qiscus.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.qiscus.sdk.ui;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import com.qiscus.sdk.R;
import com.qiscus.sdk.chat.core.data.model.QChatRoom;
import com.qiscus.sdk.chat.core.data.model.QiscusComment;
import com.qiscus.sdk.chat.core.data.remote.QiscusApi;
import com.qiscus.sdk.data.model.QiscusDeleteCommentConfig;
import com.qiscus.sdk.ui.fragment.QiscusBaseChatFragment;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AlertDialog;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created on : April 10, 2018
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
public class QiscusChannelActivity extends QiscusGroupChatActivity implements QiscusApi.MetaRoomMembersListener {
    protected String subtitle;

    public static Intent generateIntent(Context context, QChatRoom qChatRoom) {
        return generateIntent(context, qChatRoom, null, null,
                false, null, null);
    }

    public static Intent generateIntent(Context context, QChatRoom qChatRoom,
                                        String startingMessage, List<File> shareFiles,
                                        boolean autoSendExtra, List<QiscusComment> comments,
                                        QiscusComment scrollToComment) {

        if (qChatRoom.getType().equals("single")) {
            return QiscusChatActivity.generateIntent(context, qChatRoom, startingMessage,
                    shareFiles, autoSendExtra, comments, scrollToComment);
        }

        if (!qChatRoom.getType().equals("channel")) {
            return QiscusGroupChatActivity.generateIntent(context, qChatRoom, startingMessage,
                    shareFiles, autoSendExtra, comments, scrollToComment);
        }

        Intent intent = new Intent(context, QiscusChannelActivity.class);
        intent.putExtra(CHAT_ROOM_DATA, qChatRoom);
        intent.putExtra(EXTRA_STARTING_MESSAGE, startingMessage);
        intent.putExtra(EXTRA_SHARING_FILES, (Serializable) shareFiles);
        intent.putExtra(EXTRA_AUTO_SEND, autoSendExtra);
        intent.putParcelableArrayListExtra(EXTRA_FORWARD_COMMENTS, (ArrayList<QiscusComment>) comments);
        intent.putExtra(EXTRA_SCROLL_TO_COMMENT, scrollToComment);
        return intent;
    }


    @Override
    protected void generateSubtitle() {
        QiscusApi.getInstance().getParticipants(qChatRoom.getUniqueId(), 0, null, this)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();
    }

    @Override
    protected void binRoomData() {
        tvTitle.setText(qChatRoom.getName());
        generateSubtitle();
        tvSubtitle.setText(subtitle);
        tvSubtitle.setVisibility(View.VISIBLE);
        showRoomImage();
    }

    /**
     * Only can hard delete for every one
     */
    @Override
    protected void deleteComments(List<QiscusComment> selectedComments) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this)
                .setMessage(getResources().getQuantityString(R.plurals.qiscus_delete_comments_confirmation,
                        selectedComments.size(), selectedComments.size()))
                .setNegativeButton(R.string.qiscus_cancel, (dialog, which) -> dialog.dismiss())
                .setCancelable(true);

        alertDialogBuilder.setPositiveButton(R.string.qiscus_delete_for_everyone, (dialog, which) -> {
            QiscusBaseChatFragment fragment = (QiscusBaseChatFragment) getSupportFragmentManager()
                    .findFragmentByTag(QiscusBaseChatFragment.class.getName());
            if (fragment != null) {
                fragment.deleteCommentsForEveryone(selectedComments);
            }
            dialog.dismiss();
        });

        AlertDialog alertDialog = alertDialogBuilder.create();

        alertDialog.setOnShowListener(dialog -> {
            QiscusDeleteCommentConfig deleteConfig = chatConfig.getDeleteCommentConfig();
            alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(deleteConfig.getCancelButtonColor());
            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(deleteConfig.getDeleteForEveryoneButtonColor());
        });

        alertDialog.show();
    }

    @Override
    public void onMetaReceived(int currentOffset, int perPage, int total) {
        subtitle = getResources().getQuantityString(R.plurals.qiscus_channel_participant_count_subtitle,
                total, total);
    }
}
