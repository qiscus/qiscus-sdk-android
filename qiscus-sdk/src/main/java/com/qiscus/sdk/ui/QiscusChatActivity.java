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

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.qiscus.sdk.Qiscus;
import com.qiscus.sdk.R;
import com.qiscus.sdk.data.local.QiscusCacheManager;
import com.qiscus.sdk.data.model.QiscusChatConfig;
import com.qiscus.sdk.data.model.QiscusChatRoom;
import com.qiscus.sdk.data.model.QiscusComment;
import com.qiscus.sdk.ui.fragment.QiscusBaseChatFragment;
import com.qiscus.sdk.ui.fragment.QiscusChatFragment;
import com.trello.rxlifecycle.components.support.RxAppCompatActivity;

import java.util.List;

public class QiscusChatActivity extends RxAppCompatActivity implements QiscusBaseChatFragment.CommentSelectedListener, ActionMode.Callback {
    protected static final String CHAT_ROOM_DATA = "chat_room_data";

    protected Toolbar toolbar;
    protected TextView tvTitle;
    protected TextView tvSubtitle;

    protected QiscusChatConfig chatConfig;
    protected QiscusChatRoom qiscusChatRoom;
    private ActionMode actionMode;

    public static Intent generateIntent(Context context, QiscusChatRoom qiscusChatRoom) {
        Intent intent = new Intent(context, QiscusChatActivity.class);
        intent.putExtra(CHAT_ROOM_DATA, qiscusChatRoom);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        chatConfig = Qiscus.getChatConfig();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, chatConfig.getStatusBarColor()));
        }
        setContentView(R.layout.activity_qiscus_chat);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        tvTitle = (TextView) findViewById(R.id.tv_title);
        tvSubtitle = (TextView) findViewById(R.id.tv_subtitle);

        setSupportActionBar(toolbar);

        onViewReady(savedInstanceState);
    }

    protected void onViewReady(Bundle savedInstanceState) {
        resolveChatRoom(savedInstanceState);

        applyChatConfig();

        tvTitle.setText(qiscusChatRoom.getName());
        tvSubtitle.setText(qiscusChatRoom.getSubtitle());
        tvSubtitle.setVisibility(qiscusChatRoom.getSubtitle().isEmpty() ? View.GONE : View.VISIBLE);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, QiscusChatFragment.newInstance(qiscusChatRoom), QiscusChatFragment.class.getName())
                    .commit();
        }
    }

    private void applyChatConfig() {
        toolbar.setBackgroundResource(chatConfig.getAppBarColor());
        tvTitle.setTextColor(ContextCompat.getColor(this, chatConfig.getTitleColor()));
        tvSubtitle.setTextColor(ContextCompat.getColor(this, chatConfig.getSubtitleColor()));
    }

    private void resolveChatRoom(Bundle savedInstanceState) {
        qiscusChatRoom = getIntent().getParcelableExtra(CHAT_ROOM_DATA);
        if (qiscusChatRoom == null && savedInstanceState != null) {
            qiscusChatRoom = savedInstanceState.getParcelable(CHAT_ROOM_DATA);
        }

        if (qiscusChatRoom == null) {
            finish();
            return;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        QiscusCacheManager.getInstance().setLastChatActivity(true, qiscusChatRoom.getId());
    }

    @Override
    protected void onPause() {
        super.onPause();
        QiscusCacheManager.getInstance().setLastChatActivity(false, qiscusChatRoom.getId());
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(CHAT_ROOM_DATA, qiscusChatRoom);
    }

    @Override
    protected void onStop() {
        super.onStop();
        QiscusCacheManager.getInstance().setLastChatActivity(false, 0);
    }

    @Override
    public void onCommentSelected(List<QiscusComment> selectedComments) {
        boolean hasCheckedItems = selectedComments.size() > 0;
        if (hasCheckedItems && actionMode == null) {
            actionMode = startSupportActionMode(this);
        } else if (!hasCheckedItems && actionMode != null) {
            actionMode.finish();
        }

        if (actionMode != null) {
            actionMode.setTitle(selectedComments.size() + " selected");
        }
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        mode.getMenuInflater().inflate(R.menu.comment_action, menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        if (Build.VERSION.SDK_INT < 11) {
            MenuItemCompat.setShowAsAction(menu.findItem(R.id.action_copy), MenuItemCompat.SHOW_AS_ACTION_NEVER);
            MenuItemCompat.setShowAsAction(menu.findItem(R.id.action_share), MenuItemCompat.SHOW_AS_ACTION_NEVER);
        } else {
            menu.findItem(R.id.action_copy).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            menu.findItem(R.id.action_share).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        }

        return true;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        QiscusChatFragment fragment = (QiscusChatFragment) getSupportFragmentManager()
                .findFragmentByTag(QiscusChatFragment.class.getName());
        if (fragment == null) {
            mode.finish();
            return false;
        }

        List<QiscusComment> selectedComments = fragment.getSelectedComments();
        int i = item.getItemId();
        String text = "";
        for (QiscusComment qiscusComment : selectedComments) {
            text += qiscusComment.getSender() + ": ";
            text += qiscusComment.isAttachment() ? qiscusComment.getAttachmentName() : qiscusComment.getMessage();
            text += "\n";
        }

        if (i == R.id.action_copy) {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText(getString(R.string.chat_activity_label_clipboard), text);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, selectedComments.size() + " messages copied!", Toast.LENGTH_SHORT).show();
            mode.finish();
        } else if (i == R.id.action_share) {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_SUBJECT, "Messages");
            intent.putExtra(Intent.EXTRA_TEXT, text);
            startActivity(Intent.createChooser(intent, "Share"));
            mode.finish();
        }
        return false;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        actionMode = null;
        QiscusChatFragment fragment = (QiscusChatFragment) getSupportFragmentManager()
                .findFragmentByTag(QiscusChatFragment.class.getName());
        if (fragment != null) {
            fragment.clearSelectedComments();
        }
    }
}
