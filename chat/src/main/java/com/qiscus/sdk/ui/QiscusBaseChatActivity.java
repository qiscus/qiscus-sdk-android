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
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.qiscus.sdk.Qiscus;
import com.qiscus.sdk.R;
import com.qiscus.sdk.data.local.QiscusCacheManager;
import com.qiscus.sdk.data.model.ForwardCommentHandler;
import com.qiscus.sdk.data.model.QiscusChatConfig;
import com.qiscus.sdk.data.model.QiscusChatRoom;
import com.qiscus.sdk.data.model.QiscusComment;
import com.qiscus.sdk.data.model.QiscusRoomMember;
import com.qiscus.sdk.presenter.QiscusUserStatusPresenter;
import com.qiscus.sdk.ui.fragment.QiscusBaseChatFragment;
import com.qiscus.sdk.ui.fragment.QiscusChatFragment;
import com.qiscus.sdk.util.QiscusSpannableBuilder;
import com.trello.rxlifecycle.components.support.RxAppCompatActivity;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created on : December 08, 2016
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
public abstract class QiscusBaseChatActivity extends RxAppCompatActivity implements QiscusBaseChatFragment.RoomChangedListener,
        QiscusBaseChatFragment.CommentSelectedListener, ActionMode.Callback, QiscusChatFragment.UserTypingListener,
        QiscusUserStatusPresenter.View {
    protected static final String CHAT_ROOM_DATA = "chat_room_data";
    protected static final String EXTRA_STARTING_MESSAGE = "extra_starting_message";
    protected static final String EXTRA_SHARING_FILE = "extra_share_file";
    protected static final String EXTRA_AUTO_SEND = "auto_send";
    protected static final String EXTRA_FORWARD_COMMENTS = "extra_forward_comments";
    protected static final String EXTRA_SCROLL_TO_COMMENT = "extra_scroll_to_comment";

    protected QiscusChatConfig chatConfig;
    protected QiscusChatRoom qiscusChatRoom;
    protected String startingMessage;
    protected File shareFile;
    protected boolean autoSendExtra;
    protected List<QiscusComment> forwardComments;
    protected QiscusComment scrollToComment;

    private Map<String, QiscusRoomMember> roomMembers;
    private ActionMode actionMode;
    private QiscusUserStatusPresenter userStatusPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        chatConfig = onLoadChatConfig();
        onSetStatusBarColor();
        setContentView(getResourceLayout());
        onLoadView();
        userStatusPresenter = new QiscusUserStatusPresenter(this);
        onViewReady(savedInstanceState);
    }

    protected QiscusChatConfig onLoadChatConfig() {
        return Qiscus.getChatConfig();
    }

    protected void onSetStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, chatConfig.getStatusBarColor()));
        }
    }

    protected abstract int getResourceLayout();

    protected abstract void onLoadView();

    protected void applyChatConfig() {

    }

    protected void onViewReady(Bundle savedInstanceState) {
        resolveChatRoom(savedInstanceState);
        resolveStartingMessage();
        resolveShareFile();
        resolveAutoSendExtra();
        resolveForwardComments();
        resolveScrollToComment();

        binRoomData();

        applyChatConfig();

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, onCreateChatFragment(), QiscusBaseChatFragment.class.getName())
                    .commit();
        }
    }

    protected void resolveChatRoom(Bundle savedInstanceState) {
        qiscusChatRoom = getIntent().getParcelableExtra(CHAT_ROOM_DATA);
        if (qiscusChatRoom == null && savedInstanceState != null) {
            qiscusChatRoom = savedInstanceState.getParcelable(CHAT_ROOM_DATA);
        }

        if (qiscusChatRoom == null) {
            finish();
            return;
        }
    }

    protected void resolveStartingMessage() {
        if (getIntent().hasExtra(EXTRA_STARTING_MESSAGE)) {
            startingMessage = getIntent().getStringExtra(EXTRA_STARTING_MESSAGE);
            getIntent().removeExtra(EXTRA_STARTING_MESSAGE);
        }
    }

    protected void resolveShareFile() {
        if (getIntent().hasExtra(EXTRA_SHARING_FILE)) {
            shareFile = (File) getIntent().getSerializableExtra(EXTRA_SHARING_FILE);
            getIntent().removeExtra(EXTRA_SHARING_FILE);
        }
    }

    protected void resolveAutoSendExtra() {
        if (getIntent().hasExtra(EXTRA_AUTO_SEND)) {
            autoSendExtra = getIntent().getBooleanExtra(EXTRA_AUTO_SEND, true);
            getIntent().removeExtra(EXTRA_AUTO_SEND);
        }
    }

    protected void resolveForwardComments() {
        if (getIntent().hasExtra(EXTRA_FORWARD_COMMENTS)) {
            forwardComments = getIntent().getParcelableArrayListExtra(EXTRA_FORWARD_COMMENTS);
            getIntent().removeExtra(EXTRA_FORWARD_COMMENTS);
        }
    }

    protected void resolveScrollToComment() {
        if (getIntent().hasExtra(EXTRA_SCROLL_TO_COMMENT)) {
            scrollToComment = getIntent().getParcelableExtra(EXTRA_SCROLL_TO_COMMENT);
            getIntent().removeExtra(EXTRA_SCROLL_TO_COMMENT);
        }
    }

    protected void binRoomData() {
        for (QiscusRoomMember member : qiscusChatRoom.getMember()) {
            if (!member.getEmail().equals(Qiscus.getQiscusAccount().getEmail())) {
                userStatusPresenter.listenUser(member.getEmail());
            }
        }
    }

    protected abstract QiscusBaseChatFragment onCreateChatFragment();

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
        QiscusCacheManager.getInstance().setLastChatActivity(false, "0");
    }

    @Override
    public void onRoomUpdated(QiscusChatRoom qiscusChatRoom) {
        this.qiscusChatRoom = qiscusChatRoom;
        binRoomData();
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
            actionMode.setTitle(getString(R.string.qiscus_selected_comment, selectedComments.size()));
            actionMode.getMenu().findItem(R.id.action_forward)
                    .setVisible(Qiscus.getChatConfig().isEnableForwardComment());
            if (selectedComments.size() == 1 && selectedComments.get(0).getState() >= QiscusComment.STATE_ON_QISCUS) {
                actionMode.getMenu().findItem(R.id.action_reply).setVisible(true);
                QiscusComment qiscusComment = selectedComments.get(0);
                File localPath = Qiscus.getDataStore().getLocalPath(qiscusComment.getId());
                if (localPath != null) {
                    actionMode.getMenu().findItem(R.id.action_share).setVisible(true);
                } else {
                    actionMode.getMenu().findItem(R.id.action_share).setVisible(false);
                }
            } else {
                actionMode.getMenu().findItem(R.id.action_reply).setVisible(false);
                actionMode.getMenu().findItem(R.id.action_share).setVisible(false);
            }

            if (onlyTextOrLinkType(selectedComments)) {
                actionMode.getMenu().findItem(R.id.action_copy).setVisible(true);
            } else {
                actionMode.getMenu().findItem(R.id.action_copy).setVisible(false);
            }
        }
    }

    private boolean onlyTextOrLinkType(List<QiscusComment> selectedComments) {
        for (QiscusComment selectedComment : selectedComments) {
            if (selectedComment.getType() != QiscusComment.Type.TEXT
                    && selectedComment.getType() != QiscusComment.Type.LINK
                    && selectedComment.getType() != QiscusComment.Type.REPLY
                    && selectedComment.getType() != QiscusComment.Type.CONTACT
                    && selectedComment.getType() != QiscusComment.Type.LOCATION) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        mode.getMenuInflater().inflate(R.menu.comment_action, menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        menu.findItem(R.id.action_reply).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        menu.findItem(R.id.action_copy).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        menu.findItem(R.id.action_share).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        menu.findItem(R.id.action_forward).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return true;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        QiscusBaseChatFragment fragment = (QiscusBaseChatFragment) getSupportFragmentManager()
                .findFragmentByTag(QiscusBaseChatFragment.class.getName());
        if (fragment == null) {
            mode.finish();
            return false;
        }

        onSelectedCommentsAction(mode, item, fragment.getSelectedComments());
        return false;
    }

    protected void onSelectedCommentsAction(ActionMode mode, MenuItem item, List<QiscusComment> selectedComments) {
        int i = item.getItemId();
        if (i == R.id.action_copy) {
            if (roomMembers == null) {
                roomMembers = new HashMap<>();
                for (QiscusRoomMember member : qiscusChatRoom.getMember()) {
                    roomMembers.put(member.getEmail(), member);
                }
            }
            String text = "";
            if (selectedComments.size() == 1) {
                QiscusComment qiscusComment = selectedComments.get(0);
                text = qiscusComment.isAttachment() ? qiscusComment.getAttachmentName() :
                        new QiscusSpannableBuilder(qiscusComment.getMessage(), roomMembers)
                                .build().toString();
            } else {
                for (QiscusComment qiscusComment : selectedComments) {
                    text += qiscusComment.getSender() + ": ";
                    text += qiscusComment.isAttachment() ? qiscusComment.getAttachmentName() :
                            new QiscusSpannableBuilder(qiscusComment.getMessage(), roomMembers)
                                    .build().toString();
                    text += "\n";
                }
            }

            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText(getString(R.string.qiscus_chat_activity_label_clipboard), text);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, getString(R.string.qiscus_copied_message, selectedComments.size()), Toast.LENGTH_SHORT).show();
        } else if (i == R.id.action_share) {
            if (selectedComments.size() > 0) {
                QiscusComment qiscusComment = selectedComments.get(0);
                String mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(qiscusComment.getExtension());
                File file = Qiscus.getDataStore().getLocalPath(qiscusComment.getId());
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType(mime);
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                    intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
                } else {
                    intent.putExtra(Intent.EXTRA_STREAM,
                            FileProvider.getUriForFile(this, Qiscus.getProviderAuthorities(), file));
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                }
                startActivity(Intent.createChooser(intent, getString(R.string.qiscus_share_image_title)));
            }
        } else if (i == R.id.action_reply) {
            QiscusBaseChatFragment fragment = (QiscusBaseChatFragment) getSupportFragmentManager()
                    .findFragmentByTag(QiscusBaseChatFragment.class.getName());
            if (fragment != null && selectedComments.size() > 0) {
                fragment.replyComment(selectedComments.get(0));
            }
        } else if (i == R.id.action_forward) {
            ForwardCommentHandler forwardCommentHandler = Qiscus.getChatConfig().getForwardCommentHandler();
            if (forwardCommentHandler == null) {
                throw new NullPointerException("Please set forward handler before.\n" +
                        "Set it using this method Qiscus.getChatConfig().setForwardCommentHandler()");
            }
            forwardCommentHandler.forward(selectedComments);
        }
        mode.finish();
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        actionMode = null;
        QiscusBaseChatFragment fragment = (QiscusBaseChatFragment) getSupportFragmentManager()
                .findFragmentByTag(QiscusBaseChatFragment.class.getName());
        if (fragment != null) {
            fragment.clearSelectedComments();
        }
    }

    @Override
    public void showError(String errorMessage) {

    }

    @Override
    public void showLoading() {

    }

    @Override
    public void dismissLoading() {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        userStatusPresenter.detachView();
    }
}
