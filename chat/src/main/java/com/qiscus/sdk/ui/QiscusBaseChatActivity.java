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
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.qiscus.sdk.Qiscus;
import com.qiscus.sdk.R;
import com.qiscus.sdk.chat.core.data.model.MessageInfoHandler;
import com.qiscus.sdk.chat.core.data.model.ForwardMessageHandler;
import com.qiscus.sdk.chat.core.data.model.QAccount;
import com.qiscus.sdk.chat.core.data.model.QChatRoom;
import com.qiscus.sdk.chat.core.data.model.QMessage;
import com.qiscus.sdk.chat.core.data.model.QParticipant;
import com.qiscus.sdk.data.model.QiscusChatConfig;
import com.qiscus.sdk.data.model.QiscusDeleteMessageConfig;
import com.qiscus.sdk.data.model.QiscusMentionConfig;
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
    protected static final String EXTRA_SHARING_FILES = "extra_share_files";
    protected static final String EXTRA_AUTO_SEND = "auto_send";
    protected static final String EXTRA_FORWARD_COMMENTS = "extra_forward_comments";
    protected static final String EXTRA_SCROLL_TO_COMMENT = "extra_scroll_to_comment";

    protected QiscusChatConfig chatConfig;
    protected QChatRoom qChatRoom;
    protected String startingMessage;
    protected List<File> shareFiles;
    protected boolean autoSendExtra;
    protected List<QMessage> forwardCommentsData;
    protected QMessage scrollToComment;

    private Map<String, QParticipant> roomMembers;
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
        resolveShareFiles();
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
        qChatRoom = getIntent().getParcelableExtra(CHAT_ROOM_DATA);
        if (qChatRoom == null && savedInstanceState != null) {
            qChatRoom = savedInstanceState.getParcelable(CHAT_ROOM_DATA);
        }

        if (qChatRoom == null) {
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

    protected void resolveShareFiles() {
        if (getIntent().hasExtra(EXTRA_SHARING_FILES)) {
            shareFiles = (List<File>) getIntent().getSerializableExtra(EXTRA_SHARING_FILES);
            getIntent().removeExtra(EXTRA_SHARING_FILES);
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
            forwardCommentsData = getIntent().getParcelableArrayListExtra(EXTRA_FORWARD_COMMENTS);
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
        for (QParticipant member : qChatRoom.getParticipants()) {
            if (!member.getId().equals(Qiscus.getQiscusAccount().getId())) {
                userStatusPresenter.listenUser(member.getId());
            }
        }
    }

    protected abstract QiscusBaseChatFragment onCreateChatFragment();

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(CHAT_ROOM_DATA, qChatRoom);
    }

    @Override
    public void onRoomUpdated(QChatRoom qChatRoom) {
        this.qChatRoom = qChatRoom;
        binRoomData();
    }

    @Override
    public void onCommentSelected(List<QMessage> selectedComments) {
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
            if (selectedComments.size() == 1 && selectedComments.get(0).getState() >= QMessage.STATE_ON_QISCUS) {
                QMessage qiscusMessage = selectedComments.get(0);

                actionMode.getMenu().findItem(R.id.action_reply).setVisible(true);

                if (qChatRoom.getType().equals("group") && qiscusMessage.isMyComment() && !qChatRoom.getType().equals("channel")) {
                    actionMode.getMenu().findItem(R.id.action_info)
                            .setVisible(Qiscus.getChatConfig().isEnableCommentInfo());
                } else {
                    actionMode.getMenu().findItem(R.id.action_info).setVisible(false);
                }

                File localPath = Qiscus.getDataStore().getLocalPath(qiscusMessage.getId());
                if (localPath != null) {
                    actionMode.getMenu().findItem(R.id.action_share)
                            .setVisible(Qiscus.getChatConfig().isEnableShareMedia());
                } else {
                    actionMode.getMenu().findItem(R.id.action_share).setVisible(false);
                }
            } else {
                actionMode.getMenu().findItem(R.id.action_reply).setVisible(false);
                actionMode.getMenu().findItem(R.id.action_share).setVisible(false);
                actionMode.getMenu().findItem(R.id.action_info).setVisible(false);
            }

            if (onlyTextOrLinkType(selectedComments)) {
                actionMode.getMenu().findItem(R.id.action_copy).setVisible(true);
            } else {
                actionMode.getMenu().findItem(R.id.action_copy).setVisible(false);
            }

            if (chatConfig.getDeleteCommentConfig().isEnableDeleteComment()) {
                actionMode.getMenu().findItem(R.id.action_delete)
                        .setVisible(allMyComments(selectedComments) && deleteable(selectedComments));
            } else {
                actionMode.getMenu().findItem(R.id.action_delete).setVisible(false);
            }
        }
    }

    private boolean onlyTextOrLinkType(List<QMessage> selectedComments) {
        for (QMessage selectedComment : selectedComments) {
            if (selectedComment.getType() != QMessage.Type.TEXT
                    && selectedComment.getType() != QMessage.Type.LINK
                    && selectedComment.getType() != QMessage.Type.REPLY
                    && selectedComment.getType() != QMessage.Type.CONTACT
                    && selectedComment.getType() != QMessage.Type.LOCATION) {
                return false;
            }
        }
        return true;
    }

    private boolean deleteable(List<QMessage> selectedComments) {
        for (QMessage selectedComment : selectedComments) {
            if (selectedComment.isDeleted()) {
                return false;
            }
        }
        return true;
    }

    private boolean allMyComments(List<QMessage> selectedComments) {
        QAccount account = Qiscus.getQiscusAccount();
        for (QMessage selectedComment : selectedComments) {
            if (!selectedComment.getSenderEmail().equals(account.getId())) {
                return false;
            }
        }
        return true;
    }

    private QiscusBaseChatFragment getChatFragment() {
        return (QiscusBaseChatFragment) getSupportFragmentManager()
                .findFragmentByTag(QiscusBaseChatFragment.class.getName());
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        mode.getMenuInflater().inflate(R.menu.qiscus_comment_action, menu);
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
        QiscusBaseChatFragment fragment = getChatFragment();
        if (fragment == null) {
            mode.finish();
            return false;
        }

        onSelectedCommentsAction(mode, item, fragment.getSelectedComments());
        return false;
    }

    protected void onSelectedCommentsAction(ActionMode mode, MenuItem item, List<QMessage> selectedComments) {
        int i = item.getItemId();
        if (i == R.id.action_copy) {
            copyComments(selectedComments);
        } else if (i == R.id.action_share) {
            if (selectedComments.size() > 0) {
                shareComment(selectedComments.get(0));
            }
        } else if (i == R.id.action_reply) {
            if (selectedComments.size() > 0) {
                replyComment(selectedComments.get(0));
            }
        } else if (i == R.id.action_forward) {
            forwardComments(selectedComments);
        } else if (i == R.id.action_info && selectedComments.size() > 0) {
            showCommentInfo(selectedComments.get(0));
        } else if (i == R.id.action_delete && selectedComments.size() > 0) {
            deleteComments(selectedComments);
        }
        mode.finish();
    }

    protected void copyComments(List<QMessage> selectedComments) {
        if (roomMembers == null) {
            roomMembers = new HashMap<>();
            for (QParticipant member : qChatRoom.getParticipants()) {
                roomMembers.put(member.getId(), member);
            }
        }
        QiscusMentionConfig mentionConfig = Qiscus.getChatConfig().getMentionConfig();
        String textCopied;
        if (selectedComments.size() == 1) {
            QMessage qiscusMessage = selectedComments.get(0);
            if (mentionConfig.isEnableMention()) {
                textCopied = (qiscusMessage.isAttachment() ? qiscusMessage.getAttachmentName() :
                        new QiscusSpannableBuilder(qiscusMessage.getMessage(), roomMembers)
                                .build().toString());
            } else {
                textCopied = qiscusMessage.isAttachment() ? qiscusMessage.getAttachmentName() : qiscusMessage.getMessage();
            }
        } else {
            StringBuilder text = new StringBuilder();
            if (mentionConfig.isEnableMention()) {
                for (QMessage qiscusMessage : selectedComments) {
                    text.append(qiscusMessage.getSender()).append(": ");
                    text.append(qiscusMessage.isAttachment() ? qiscusMessage.getAttachmentName() :
                            new QiscusSpannableBuilder(qiscusMessage.getMessage(), roomMembers)
                                    .build().toString());
                    text.append('\n');
                }
            } else {
                for (QMessage qiscusMessage : selectedComments) {
                    text.append(qiscusMessage.getSender()).append(": ");
                    text.append(qiscusMessage.isAttachment() ? qiscusMessage.getAttachmentName() :
                            qiscusMessage.getMessage());
                    text.append('\n');
                }
            }
            textCopied = text.toString();
        }

        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(getString(R.string.qiscus_chat_activity_label_clipboard), textCopied);
        if (clipboard != null) {
            clipboard.setPrimaryClip(clip);
        }
        Toast.makeText(this, getString(R.string.qiscus_copied_message, selectedComments.size()), Toast.LENGTH_SHORT).show();
    }

    protected void shareComment(QMessage qiscusMessage) {
        String mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(qiscusMessage.getExtension());
        File file = Qiscus.getDataStore().getLocalPath(qiscusMessage.getId());
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

    protected void replyComment(QMessage qiscusMessage) {
        QiscusBaseChatFragment fragment = (QiscusBaseChatFragment) getSupportFragmentManager()
                .findFragmentByTag(QiscusBaseChatFragment.class.getName());
        if (fragment != null) {
            fragment.replyComment(qiscusMessage);
        }
    }

    protected void forwardComments(List<QMessage> selectedComments) {
        ForwardMessageHandler forwardMessageHandler = Qiscus.getChatConfig().getForwardMessageHandler();
        if (forwardMessageHandler == null) {
            throw new NullPointerException("Please set forward handler before.\n" +
                    "Set it using this method Qiscus.getChatConfig().setForwardMessageHandler()");
        }
        forwardMessageHandler.forward(selectedComments);
    }

    protected void showCommentInfo(QMessage qiscusMessage) {
        MessageInfoHandler messageInfoHandler = Qiscus.getChatConfig().getMessageInfoHandler();
        if (messageInfoHandler == null) {
            throw new NullPointerException("Please set comment info handler before.\n" +
                    "Set it using this method Qiscus.getChatConfig().setMessageInfoHandler()");
        }
        messageInfoHandler.showInfo(qiscusMessage);
    }

    protected void deleteComments(List<QMessage> selectedComments) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this)
                .setMessage(getResources().getQuantityString(R.plurals.qiscus_delete_comments_confirmation,
                        selectedComments.size(), selectedComments.size()))
                .setNegativeButton(R.string.qiscus_cancel, (dialog, which) -> dialog.dismiss())
                .setCancelable(true);

        boolean ableToDeleteForEveryone = allMyComments(selectedComments);
        if (ableToDeleteForEveryone) {
            alertDialogBuilder.setNeutralButton(R.string.qiscus_delete_for_everyone, (dialog, which) -> {
                QiscusBaseChatFragment fragment = (QiscusBaseChatFragment) getSupportFragmentManager()
                        .findFragmentByTag(QiscusBaseChatFragment.class.getName());
                if (fragment != null) {
                    fragment.deleteCommentsForEveryone(selectedComments);
                }
                dialog.dismiss();
            });
        }

        AlertDialog alertDialog = alertDialogBuilder.create();

        alertDialog.setOnShowListener(dialog -> {
            QiscusDeleteMessageConfig deleteConfig = chatConfig.getDeleteCommentConfig();
            alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(deleteConfig.getCancelButtonColor());
            if (ableToDeleteForEveryone) {
                alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setTextColor(deleteConfig.getDeleteForEveryoneButtonColor());
            }
        });

        alertDialog.show();
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
