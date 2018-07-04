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

package com.qiscus.dragonfly;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.qiscus.nirmana.Nirmana;
import com.qiscus.sdk.Qiscus;
import com.qiscus.sdk.data.model.QiscusAccount;
import com.qiscus.sdk.data.model.QiscusChatRoom;
import com.qiscus.sdk.data.model.QiscusRoomMember;
import com.qiscus.sdk.ui.QiscusBaseChatActivity;
import com.qiscus.sdk.ui.fragment.QiscusBaseChatFragment;
import com.qiscus.sdk.ui.fragment.QiscusChatFragment;
import com.qiscus.sdk.ui.view.QiscusCircularImageView;
import com.qiscus.sdk.util.QiscusDateUtil;

import java.util.Date;

/**
 * Created on : September 09, 2016
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
public class ChatActivity extends QiscusBaseChatActivity {
    private Toolbar toolbar;
    private TextView tvTitle;
    private TextView tvSubtitle;
    private QiscusCircularImageView ivAvatar;

    private QiscusAccount qiscusAccount;

    public static Intent generateIntent(Context context, QiscusChatRoom qiscusChatRoom) {
        Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtra(CHAT_ROOM_DATA, qiscusChatRoom);
        return intent;
    }

    @Override
    protected int getResourceLayout() {
        return R.layout.activity_chat;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onLoadView() {
        toolbar = findViewById(com.qiscus.sdk.R.id.toolbar);
        tvTitle = findViewById(com.qiscus.sdk.R.id.tv_title);
        tvSubtitle = findViewById(com.qiscus.sdk.R.id.tv_subtitle);
        ivAvatar = findViewById(com.qiscus.sdk.R.id.profile_picture);
        setSupportActionBar(toolbar);
    }

    @Override
    protected QiscusBaseChatFragment onCreateChatFragment() {
        return QiscusChatFragment.newInstance(qiscusChatRoom);
    }

    @Override
    protected void onViewReady(Bundle savedInstanceState) {
        qiscusAccount = Qiscus.getQiscusAccount();
        super.onViewReady(savedInstanceState);
    }

    @Override
    protected void applyChatConfig() {
        toolbar.setBackgroundResource(chatConfig.getAppBarColor());
        tvTitle.setTextColor(ContextCompat.getColor(this, chatConfig.getTitleColor()));
        tvSubtitle.setTextColor(ContextCompat.getColor(this, chatConfig.getSubtitleColor()));
    }

    @Override
    protected void binRoomData() {
        super.binRoomData();
        tvTitle.setText(qiscusChatRoom.getName());
        showRoomImage();
    }

    protected void showRoomImage() {
        for (QiscusRoomMember member : qiscusChatRoom.getMember()) {
            if (!member.getEmail().equalsIgnoreCase(qiscusAccount.getEmail())) {
                Nirmana.getInstance().get().load(member.getAvatar())
                        .error(com.qiscus.sdk.R.drawable.ic_qiscus_avatar)
                        .placeholder(com.qiscus.sdk.R.drawable.ic_qiscus_avatar)
                        .dontAnimate()
                        .into(ivAvatar);
                break;
            }
        }
    }

    @Override
    public void onUserTyping(String user, boolean typing) {
        tvSubtitle.setText(typing ? "Typing..." : "Online");
        tvSubtitle.setVisibility(View.VISIBLE);
    }

    @Override
    public void onUserStatusChanged(String user, boolean online, Date lastActive) {
        String last = QiscusDateUtil.getRelativeTimeDiff(lastActive);
        tvSubtitle.setText(online ? "Online" : "Last seen " + last);
        tvSubtitle.setVisibility(View.VISIBLE);
    }
}
