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
import com.qiscus.sdk.chat.core.data.model.QiscusChatRoom;
import com.qiscus.sdk.chat.core.data.model.QiscusComment;
import com.qiscus.sdk.chat.core.data.model.QiscusRoomMember;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

;

/**
 * Created on : November 24, 2016
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
public class QiscusGroupChatActivity extends QiscusChatActivity {

    protected String subtitle;

    public static Intent generateIntent(Context context, QiscusChatRoom qiscusChatRoom) {
        return generateIntent(context, qiscusChatRoom, null, null,
                false, null, null);
    }

    public static Intent generateIntent(Context context, QiscusChatRoom qiscusChatRoom,
                                        String startingMessage, List<File> shareFiles,
                                        boolean autoSendExtra, List<QiscusComment> comments,
                                        QiscusComment scrollToComment) {
        if (!qiscusChatRoom.isGroup()) {
            return QiscusChatActivity.generateIntent(context, qiscusChatRoom, startingMessage,
                    shareFiles, autoSendExtra, comments, scrollToComment);
        }

        if (qiscusChatRoom.isChannel()) {
            return QiscusChannelActivity.generateIntent(context, qiscusChatRoom, startingMessage,
                    shareFiles, autoSendExtra, comments, scrollToComment);
        }

        Intent intent = new Intent(context, QiscusGroupChatActivity.class);
        intent.putExtra(CHAT_ROOM_DATA, qiscusChatRoom);
        intent.putExtra(EXTRA_STARTING_MESSAGE, startingMessage);
        intent.putExtra(EXTRA_SHARING_FILES, (Serializable) shareFiles);
        intent.putExtra(EXTRA_AUTO_SEND, autoSendExtra);
        intent.putParcelableArrayListExtra(EXTRA_FORWARD_COMMENTS, (ArrayList<QiscusComment>) comments);
        intent.putExtra(EXTRA_SCROLL_TO_COMMENT, scrollToComment);
        return intent;
    }

    @Override
    protected void binRoomData() {
        tvTitle.setText(qiscusChatRoom.getName());
        generateSubtitle();
        tvSubtitle.setText(subtitle);
        tvSubtitle.setVisibility(View.VISIBLE);
        showRoomImage();
    }

    protected void generateSubtitle() {
        subtitle = "";
        int count = 0;
        for (QiscusRoomMember member : qiscusChatRoom.getMember()) {
            if (!member.getEmail().equalsIgnoreCase(Qiscus.getQiscusAccount().getEmail())) {
                count++;
                subtitle += member.getUsername().split(" ")[0];
                if (count < qiscusChatRoom.getMember().size() - 1) {
                    subtitle += ", ";
                }
            }
            if (count >= 10) {
                break;
            }
        }
        subtitle += String.format(" %s", getString(R.string.qiscus_group_member_closing));
        if (count == 0) subtitle = getString(R.string.qiscus_group_member_only_you);
    }

    @Override
    public void onUserStatusChanged(String user, boolean online, Date lastActive) {

    }

    @Override
    public void onUserTyping(String user, boolean typing) {
        if (typing) {
            Observable.fromIterable(qiscusChatRoom.getMember())
                    .filter(qiscusRoomMember -> qiscusRoomMember.getEmail().equals(user))
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    //.compose(bindToLifecycle())
                    .subscribe(qiscusRoomMember -> tvSubtitle.setText(getString(R.string.qiscus_group_member_typing,
                            qiscusRoomMember.getUsername())), throwable -> {
                    });
        } else {
            tvSubtitle.setText(subtitle);
        }
    }
}
