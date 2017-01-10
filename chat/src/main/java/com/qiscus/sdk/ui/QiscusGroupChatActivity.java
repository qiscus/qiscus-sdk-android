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
import com.qiscus.sdk.data.model.QiscusChatRoom;
import com.qiscus.sdk.data.model.QiscusRoomMember;

import java.util.Date;

/**
 * Created on : November 24, 2016
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
public class QiscusGroupChatActivity extends QiscusChatActivity {

    protected String subtitle;

    public static Intent generateIntent(Context context, QiscusChatRoom qiscusChatRoom) {
        qiscusChatRoom.setGroup(true);
        Intent intent = new Intent(context, QiscusGroupChatActivity.class);
        intent.putExtra(CHAT_ROOM_DATA, qiscusChatRoom);
        return intent;
    }

    @Override
    protected void binRoomData() {
        super.binRoomData();
        generateSubtitle();
        tvSubtitle.setText(subtitle);
        tvSubtitle.setVisibility(View.VISIBLE);
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
        }
        subtitle += " and you";
    }

    @Override
    public void onUserStatusChanged(String user, boolean online, Date lastActive) {

    }

    @Override
    public void onUserTyping(String user, boolean typing) {
        if (typing) {
            for (QiscusRoomMember member : qiscusChatRoom.getMember()) {
                if (member.getEmail().equals(user)) {
                    tvSubtitle.setText(String.format("%s is typing...", member.getUsername()));
                    break;
                }
            }
        } else {
            tvSubtitle.setText(subtitle);
        }
    }
}
