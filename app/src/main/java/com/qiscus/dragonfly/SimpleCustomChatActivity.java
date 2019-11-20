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
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import com.qiscus.sdk.chat.core.data.model.QChatRoom;
import com.qiscus.sdk.ui.QiscusBaseChatActivity;
import com.qiscus.sdk.ui.fragment.QiscusBaseChatFragment;

import java.util.Date;

/**
 * Created on : September 09, 2016
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
public class SimpleCustomChatActivity extends QiscusBaseChatActivity {
    private TextView mTitle;

    public static Intent generateIntent(Context context, QChatRoom qChatRoom) {
        Intent intent = new Intent(context, SimpleCustomChatActivity.class);
        intent.putExtra(CHAT_ROOM_DATA, qChatRoom);
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
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mTitle = findViewById(R.id.tv_title);
    }

    @Override
    protected QiscusBaseChatFragment onCreateChatFragment() {
        return SimpleCustomChatFragment.newInstance(qChatRoom);
    }

    @Override
    protected void onViewReady(Bundle savedInstanceState) {
        super.onViewReady(savedInstanceState);
        mTitle.setText(qChatRoom.getName());
    }

    @Override
    public void onUserStatusChanged(String user, boolean online, Date lastActive) {

    }

    @Override
    public void onUserTyping(String user, boolean typing) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.custom_chat_activity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else if (item.getItemId() == R.id.menu_clear) {
            actionClearMenu();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }

    }

    private void actionClearMenu() {
        SimpleCustomChatFragment customChatFragment = (SimpleCustomChatFragment)
                getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (customChatFragment != null) {
            customChatFragment.actionClearComments();
        }
    }
}
