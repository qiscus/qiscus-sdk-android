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

import com.qiscus.sdk.Qiscus;
import com.qiscus.sdk.R;
import com.qiscus.sdk.chat.core.data.model.QChatRoom;
import com.qiscus.sdk.chat.core.data.model.QParticipant;
import com.qiscus.sdk.chat.core.data.model.QMessage;
import com.qiscus.sdk.chat.core.data.remote.QiscusApi;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created on : November 24, 2016
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
public class QiscusGroupChatActivity extends QiscusChatActivity {

    protected String subtitle;

    public static Intent generateIntent(Context context, QChatRoom qChatRoom) {
        return generateIntent(context, qChatRoom, null, null,
                false, null, null);
    }

    public static Intent generateIntent(Context context, QChatRoom qChatRoom,
                                        String startingMessage, List<File> shareFiles,
                                        boolean autoSendExtra, List<QMessage> comments,
                                        QMessage scrollToComment) {
        if (!qChatRoom.getType().equals("group")) {
            return QiscusChatActivity.generateIntent(context, qChatRoom, startingMessage,
                    shareFiles, autoSendExtra, comments, scrollToComment);
        }

        if (qChatRoom.getType().equals("channel")) {
            return QiscusChannelActivity.generateIntent(context, qChatRoom, startingMessage,
                    shareFiles, autoSendExtra, comments, scrollToComment);
        }

        Intent intent = new Intent(context, QiscusGroupChatActivity.class);
        intent.putExtra(CHAT_ROOM_DATA, qChatRoom);
        intent.putExtra(EXTRA_STARTING_MESSAGE, startingMessage);
        intent.putExtra(EXTRA_SHARING_FILES, (Serializable) shareFiles);
        intent.putExtra(EXTRA_AUTO_SEND, autoSendExtra);
        intent.putParcelableArrayListExtra(EXTRA_FORWARD_COMMENTS, (ArrayList<QMessage>) comments);
        intent.putExtra(EXTRA_SCROLL_TO_COMMENT, scrollToComment);
        return intent;
    }

    @Override
    protected void binRoomData() {
        tvTitle.setText(qChatRoom.getName());
        generateSubtitle();
        tvSubtitle.setText(subtitle);
        tvSubtitle.setVisibility(View.VISIBLE);
        showRoomImage();
    }

    protected void generateSubtitle() {
        QiscusApi.getInstance().getParticipants(qChatRoom.getUniqueId(),0,"asc")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(participants -> {
                    qChatRoom.setParticipants(participants);
                    subtitle = "";
                    int count = 0;
                    for (QParticipant member : qChatRoom.getParticipants()) {
                        if (!member.getId().equalsIgnoreCase(Qiscus.getQiscusAccount().getId())) {
                            count++;
                            subtitle += member.getName().split(" ")[0];
                            if (count < qChatRoom.getParticipants().size() - 1) {
                                subtitle += ", ";
                            }
                        }
                        if (count >= 10) {
                            break;
                        }
                    }
                    subtitle += String.format(" %s", getString(R.string.qiscus_group_member_closing));
                    if (count == 0) subtitle = getString(R.string.qiscus_group_member_only_you);
                }, throwable -> {
                    throwable.printStackTrace();
                });

    }

    @Override
    public void onUserStatusChanged(String user, boolean online, Date lastActive) {

    }

    @Override
    public void onUserTyping(String user, boolean typing) {
        if (typing) {
            Observable.from(qChatRoom.getParticipants())
                    .filter(qiscusRoomMember -> qiscusRoomMember.getId().equals(user))
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .compose(bindToLifecycle())
                    .subscribe(qiscusRoomMember -> tvSubtitle.setText(getString(R.string.qiscus_group_member_typing,
                            qiscusRoomMember.getName())), throwable -> {
                    });
        } else {
            tvSubtitle.setText(subtitle);
        }
    }
}
