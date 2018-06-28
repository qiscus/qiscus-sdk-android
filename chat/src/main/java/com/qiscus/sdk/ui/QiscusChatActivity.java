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
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.qiscus.nirmana.Nirmana;
import com.qiscus.sdk.Qiscus;
import com.qiscus.sdk.R;
import com.qiscus.sdk.data.model.QiscusAccount;
import com.qiscus.sdk.data.model.QiscusChatRoom;
import com.qiscus.sdk.data.model.QiscusComment;
import com.qiscus.sdk.ui.fragment.QiscusBaseChatFragment;
import com.qiscus.sdk.ui.fragment.QiscusChatFragment;
import com.qiscus.sdk.ui.view.QiscusCircularImageView;
import com.qiscus.sdk.util.QiscusDateUtil;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class QiscusChatActivity extends QiscusBaseChatActivity {
    protected Toolbar toolbar;
    protected TextView tvTitle;
    protected TextView tvSubtitle;
    protected QiscusCircularImageView ivAvatar;

    protected QiscusAccount qiscusAccount;

    public static Intent generateIntent(Context context, QiscusChatRoom qiscusChatRoom) {
        return generateIntent(context, qiscusChatRoom, null, null,
                false, null, null);
    }

    public static Intent generateIntent(Context context, QiscusChatRoom qiscusChatRoom,
                                        String startingMessage, List<File> shareFiles,
                                        boolean autoSendExtra, List<QiscusComment> comments,
                                        QiscusComment scrollToComment) {
        if (qiscusChatRoom.isGroup()) {
            return QiscusGroupChatActivity.generateIntent(context, qiscusChatRoom, startingMessage,
                    shareFiles, autoSendExtra, comments, scrollToComment);
        }

        Intent intent = new Intent(context, QiscusChatActivity.class);
        intent.putExtra(CHAT_ROOM_DATA, qiscusChatRoom);
        intent.putExtra(EXTRA_STARTING_MESSAGE, startingMessage);
        intent.putExtra(EXTRA_SHARING_FILES, (Serializable) shareFiles);
        intent.putExtra(EXTRA_AUTO_SEND, autoSendExtra);
        intent.putParcelableArrayListExtra(EXTRA_FORWARD_COMMENTS, (ArrayList<QiscusComment>) comments);
        intent.putExtra(EXTRA_SCROLL_TO_COMMENT, scrollToComment);
        return intent;
    }

    @Override
    protected int getResourceLayout() {
        return R.layout.activity_qiscus_chat;
    }

    @Override
    protected void onLoadView() {
        toolbar = findViewById(R.id.toolbar);
        tvTitle = findViewById(R.id.tv_title);
        tvSubtitle = findViewById(R.id.tv_subtitle);
        ivAvatar = findViewById(R.id.profile_picture);
        setSupportActionBar(toolbar);
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
        Nirmana.getInstance().get().load(qiscusChatRoom.getAvatarUrl())
                .error(R.drawable.ic_qiscus_avatar)
                .placeholder(R.drawable.ic_qiscus_avatar)
                .dontAnimate()
                .into(ivAvatar);
    }

    @Override
    protected QiscusBaseChatFragment onCreateChatFragment() {
        return QiscusChatFragment.newInstance(qiscusChatRoom, startingMessage, shareFiles,
                autoSendExtra, forwardCommentsData, scrollToComment);
    }

    @Override
    public void onUserTyping(String user, boolean typing) {
        tvSubtitle.setText(typing ? getString(R.string.qiscus_typing) : getString(R.string.qiscus_online));
        tvSubtitle.setVisibility(View.VISIBLE);
    }

    @Override
    public void onUserStatusChanged(String user, boolean online, Date lastActive) {
        String last = QiscusDateUtil.getRelativeTimeDiff(lastActive);
        tvSubtitle.setText(online ? getString(R.string.qiscus_online) : getString(R.string.qiscus_last_seen, last));
        tvSubtitle.setVisibility(View.VISIBLE);
    }
}
