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
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import com.qiscus.sdk.data.model.QiscusChatRoom;
import com.qiscus.sdk.ui.QiscusBaseChatActivity;
import com.qiscus.sdk.ui.fragment.QiscusBaseChatFragment;
import com.qiscus.sdk.ui.fragment.QiscusChatFragment;

import java.util.Date;

/**
 * Created by zetra. on 9/19/16.
 */
public class ChatActivity extends QiscusBaseChatActivity {
    private TextView title;

    public static Intent generateIntent(Context context, QiscusChatRoom qiscusChatRoom, boolean simpleCustom) {
        Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtra(CHAT_ROOM_DATA, qiscusChatRoom);
        intent.putExtra("simple_custom", simpleCustom);
        return intent;
    }

    @Override
    protected void onSetStatusBarColor() {

    }

    @Override
    protected int getResourceLayout() {
        return R.layout.activity_chat;
    }

    @Override
    protected void onLoadView() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        title = (TextView) findViewById(R.id.tv_title);
    }

    @Override
    protected QiscusBaseChatFragment onCreateChatFragment() {
        return getIntent().getBooleanExtra("simple_custom", false) ? SimpleCustomChatFragment.newInstance(qiscusChatRoom)
                : QiscusChatFragment.newInstance(qiscusChatRoom);
    }

    @Override
    protected void onViewReady(Bundle savedInstanceState) {
        super.onViewReady(savedInstanceState);
        title.setText(qiscusChatRoom.getName());
    }

    @Override
    public void onUserStatusChanged(String user, boolean online, Date lastActive) {

    }

    @Override
    public void onUserTyping(String user, boolean typing) {

    }
}
