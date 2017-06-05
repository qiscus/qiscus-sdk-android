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

package com.qiscus.sdk.ui.fragment;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.qiscus.sdk.Qiscus;
import com.qiscus.sdk.R;
import com.qiscus.sdk.data.local.QiscusCacheManager;
import com.qiscus.sdk.data.model.QiscusAccount;
import com.qiscus.sdk.data.model.QiscusChatConfig;
import com.qiscus.sdk.data.model.QiscusChatRoom;
import com.qiscus.sdk.data.model.QiscusComment;
import com.qiscus.sdk.data.remote.QiscusPusherApi;
import com.qiscus.sdk.filepicker.FilePickerBuilder;
import com.qiscus.sdk.filepicker.FilePickerConst;
import com.qiscus.sdk.presenter.QiscusChatPresenter;
import com.qiscus.sdk.ui.QiscusAccountLinkingActivity;
import com.qiscus.sdk.ui.QiscusPhotoViewerActivity;
import com.qiscus.sdk.ui.adapter.QiscusBaseChatAdapter;
import com.qiscus.sdk.ui.view.QiscusAudioRecorderView;
import com.qiscus.sdk.ui.view.QiscusChatButtonView;
import com.qiscus.sdk.ui.view.QiscusChatScrollListener;
import com.qiscus.sdk.ui.view.QiscusRecyclerView;
import com.qiscus.sdk.ui.view.QiscusReplyPreviewView;
import com.qiscus.sdk.util.QiscusAndroidUtil;
import com.qiscus.sdk.util.QiscusFileUtil;
import com.qiscus.sdk.util.QiscusImageUtil;
import com.qiscus.sdk.util.QiscusPermissionsUtil;
import com.qiscus.sdk.util.QiscusRawDataExtractor;
import com.trello.rxlifecycle.components.support.RxFragment;
import com.vanniktech.emoji.EmojiEditText;
import com.vanniktech.emoji.EmojiPopup;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * Created on : September 28, 2016
 * Author     : zetbaitsu
 * Name       : Zetra
 * Email      : zetra@mail.ugm.ac.id
 * GitHub     : https://github.com/zetbaitsu
 * LinkedIn   : https://id.linkedin.com/in/zetbaitsu
 */
public abstract class QiscusBaseChatFragment<T extends QiscusBaseChatAdapter> extends RxFragment
        implements SwipeRefreshLayout.OnRefreshListener, QiscusChatScrollListener.Listener,
        QiscusChatPresenter.View, QiscusAudioRecorderView.RecordListener,
        QiscusPermissionsUtil.PermissionCallbacks, QiscusChatButtonView.ChatButtonClickListener {

    protected static final int RC_PERMISSIONS = 127;

    private static final String[] PERMISSIONS = {
            "android.permission.WRITE_EXTERNAL_STORAGE",
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.RECORD_AUDIO",
            "android.permission.CAMERA"
    };

    private static final String AUDIO_PERMISSION = "android.permission.RECORD_AUDIO";
    private static final String[] FILE_PERMISSION = {
            "android.permission.WRITE_EXTERNAL_STORAGE",
            "android.permission.READ_EXTERNAL_STORAGE",
    };
    private static final String CAMERA_PERMISSION = "android.permission.CAMERA";

    protected static final String CHAT_ROOM_DATA = "chat_room_data";
    protected static final String EXTRA_STARTING_MESSAGE = "extra_starting_message";
    protected static final String EXTRA_SHARE_FILE = "extra_share_file";
    protected static final String COMMENTS_DATA = "saved_comments_data";
    protected static final int TAKE_PICTURE_REQUEST = 1;

    @NonNull protected ViewGroup rootView;
    @Nullable protected ViewGroup emptyChatHolder;
    @NonNull protected SwipeRefreshLayout swipeRefreshLayout;
    @NonNull protected QiscusRecyclerView messageRecyclerView;
    @Nullable protected ViewGroup messageInputPanel;
    @Nullable protected ViewGroup messageEditTextContainer;
    @NonNull protected EditText messageEditText;
    @NonNull protected ImageView sendButton;
    @Nullable protected View newMessageButton;
    @NonNull protected View loadMoreProgressBar;
    @Nullable protected ImageView emptyChatImageView;
    @Nullable protected TextView emptyChatTitleView;
    @Nullable protected TextView emptyChatDescView;
    @Nullable protected ViewGroup attachmentPanel;
    @Nullable protected View addImageLayout;
    @Nullable protected ImageView addImageButton;
    @Nullable protected TextView addImageTextView;
    @Nullable protected View takeImageLayout;
    @Nullable protected ImageView takeImageButton;
    @Nullable protected TextView takeImageTextView;
    @Nullable protected View addFileLayout;
    @Nullable protected ImageView addFileButton;
    @Nullable protected TextView addFileTextView;
    @Nullable protected View recordAudioLayout;
    @Nullable protected ImageView recordAudioButton;
    @Nullable protected TextView recordAudioTextView;
    @Nullable protected ImageView hideAttachmentButton;
    @Nullable protected ImageView toggleEmojiButton;
    @Nullable protected QiscusAudioRecorderView recordAudioPanel;
    @Nullable protected QiscusReplyPreviewView replyPreviewView;

    protected QiscusChatConfig chatConfig;
    protected QiscusChatRoom qiscusChatRoom;
    protected String startingMessage;
    protected File shareFile;
    protected T chatAdapter;
    protected QiscusChatPresenter qiscusChatPresenter;
    protected Animation animation;
    protected LinearLayoutManager chatLayoutManager;
    private QiscusAccount qiscusAccount;
    private boolean fieldMessageEmpty = true;
    private CommentSelectedListener commentSelectedListener;
    private RoomChangedListener roomChangedListener;
    private EmojiPopup emojiPopup;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(getResourceLayout(), container, false);
        onLoadView(view);
        return view;
    }

    protected abstract int getResourceLayout();

    protected void onLoadView(View view) {
        rootView = getRootView(view);
        emptyChatHolder = getEmptyChatHolder(view);
        swipeRefreshLayout = getSwipeRefreshLayout(view);
        messageRecyclerView = getMessageRecyclerView(view);
        messageInputPanel = getMessageInputPanel(view);
        messageEditTextContainer = getMessageEditTextContainer(view);
        messageEditText = getMessageEditText(view);
        sendButton = getSendButton(view);
        newMessageButton = getNewMessageButton(view);
        loadMoreProgressBar = getLoadMoreProgressBar(view);

        emptyChatImageView = getEmptyChatImageView(view);
        emptyChatTitleView = getEmptyChatTitleView(view);
        emptyChatDescView = getEmptyChatDescView(view);

        attachmentPanel = getAttachmentPanel(view);
        hideAttachmentButton = getHideAttachmentButton(view);

        addImageLayout = getAddImageLayout(view);
        addImageButton = getAddImageButton(view);
        addImageTextView = getAddImageTextView(view);

        takeImageLayout = getTakeImageLayout(view);
        takeImageButton = getTakeImageButton(view);
        takeImageTextView = getTakeImageTextView(view);

        addFileLayout = getAddFileLayout(view);
        addFileButton = getAddFileButton(view);
        addFileTextView = getAddFileTextView(view);

        recordAudioLayout = getRecordAudioLayout(view);
        recordAudioButton = getRecordAudioButton(view);
        recordAudioTextView = getRecordAudioTextView(view);

        toggleEmojiButton = getToggleEmojiButton(view);
        recordAudioPanel = getRecordAudioPanel(view);
        replyPreviewView = getReplyPreviewView(view);

        if (toggleEmojiButton != null && !(messageEditText instanceof EmojiEditText)) {
            throw new RuntimeException("Please use EmojiEditText as message text field if you want to using EmojiKeyboard.");
        }

        messageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                onMessageEditTextChanged(s);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        messageEditText.setOnClickListener(v -> {
            if (emojiPopup != null && emojiPopup.isShowing()) {
                toggleEmoji();
            }
        });

        sendButton.setOnClickListener(v -> {
            String message = messageEditText.getText().toString().trim();
            if (message.isEmpty()) {
                showAttachmentPanel();
            } else {
                sendMessage(message);
            }
        });

        if (newMessageButton != null) {
            newMessageButton.setOnClickListener(v -> scrollToBottom());
        }
        if (addImageButton != null) {
            addImageButton.setOnClickListener(v -> addImage());
        }
        if (takeImageButton != null) {
            takeImageButton.setOnClickListener(v -> takeImage());
        }
        if (addFileButton != null) {
            addFileButton.setOnClickListener(v -> addFile());
        }
        if (recordAudioButton != null) {
            recordAudioButton.setOnClickListener(v -> recordAudio());
        }
        if (toggleEmojiButton != null) {
            toggleEmojiButton.setOnClickListener(v -> toggleEmoji());
        }
        if (hideAttachmentButton != null) {
            hideAttachmentButton.setOnClickListener(v -> hideAttachmentPanel());
        }
        if (recordAudioPanel != null) {
            recordAudioPanel.setRecordListener(this);
        }
    }

    @NonNull
    protected abstract ViewGroup getRootView(View view);

    @Nullable
    protected abstract ViewGroup getEmptyChatHolder(View view);

    @NonNull
    protected abstract SwipeRefreshLayout getSwipeRefreshLayout(View view);

    @NonNull
    protected abstract QiscusRecyclerView getMessageRecyclerView(View view);

    @Nullable
    protected abstract ViewGroup getMessageInputPanel(View view);

    @Nullable
    protected abstract ViewGroup getMessageEditTextContainer(View view);

    @NonNull
    protected abstract EditText getMessageEditText(View view);

    @NonNull
    protected abstract ImageView getSendButton(View view);

    @Nullable
    protected abstract View getNewMessageButton(View view);

    @NonNull
    protected abstract View getLoadMoreProgressBar(View view);

    @Nullable
    protected abstract ImageView getEmptyChatImageView(View view);

    @Nullable
    protected abstract TextView getEmptyChatTitleView(View view);

    @Nullable
    protected abstract TextView getEmptyChatDescView(View view);

    @Nullable
    protected abstract ViewGroup getAttachmentPanel(View view);

    @Nullable
    protected abstract View getAddImageLayout(View view);

    @Nullable
    protected abstract ImageView getAddImageButton(View view);

    @Nullable
    protected TextView getAddImageTextView(View view) {
        return null;
    }

    @Nullable
    protected abstract View getTakeImageLayout(View view);

    @Nullable
    protected abstract ImageView getTakeImageButton(View view);

    @Nullable
    protected TextView getTakeImageTextView(View view) {
        return null;
    }

    @Nullable
    protected abstract View getAddFileLayout(View view);

    @Nullable
    protected abstract ImageView getAddFileButton(View view);

    @Nullable
    protected TextView getAddFileTextView(View view) {
        return null;
    }

    @Nullable
    protected abstract View getRecordAudioLayout(View view);

    @Nullable
    protected abstract ImageView getRecordAudioButton(View view);

    @Nullable
    protected TextView getRecordAudioTextView(View view) {
        return null;
    }

    @Nullable
    public abstract ImageView getHideAttachmentButton(View view);

    @Nullable
    protected abstract ImageView getToggleEmojiButton(View view);

    @Nullable
    protected abstract QiscusAudioRecorderView getRecordAudioPanel(View view);

    @Nullable
    protected abstract QiscusReplyPreviewView getReplyPreviewView(View view);

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Activity activity = getActivity();
        if (activity instanceof CommentSelectedListener) {
            commentSelectedListener = (CommentSelectedListener) activity;
        }

        if (activity instanceof RoomChangedListener) {
            roomChangedListener = (RoomChangedListener) activity;
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        requestPermissions();
        onCreateChatComponents(savedInstanceState);
    }

    protected void onCreateChatComponents(Bundle savedInstanceState) {
        chatConfig = onLoadChatConfig();

        resolveChatRoom(savedInstanceState);
        resolveStartingMessage();
        resolveShareFile();

        onApplyChatConfig();

        swipeRefreshLayout.setOnRefreshListener(this);

        animation = onLoadAnimation();

        onClearNotification();

        qiscusAccount = Qiscus.getQiscusAccount();

        chatAdapter = onCreateChatAdapter();
        chatAdapter.setOnItemClickListener((view, position) ->
                onItemCommentClick((QiscusComment) chatAdapter.getData().get(position)));
        chatAdapter.setOnLongItemClickListener((view, position) ->
                onItemCommentLongClick((QiscusComment) chatAdapter.getData().get(position)));
        chatAdapter.setChatButtonClickListener(this);
        messageRecyclerView.setUpAsBottomList();
        chatLayoutManager = (LinearLayoutManager) messageRecyclerView.getLayoutManager();
        messageRecyclerView.setAdapter(chatAdapter);
        messageRecyclerView.addOnScrollListener(new QiscusChatScrollListener(chatLayoutManager, this));

        setupEmojiPopup();

        qiscusChatPresenter = new QiscusChatPresenter(this, qiscusChatRoom);
        if (savedInstanceState == null) {
            qiscusChatPresenter.loadComments(20);
        } else {
            ArrayList<QiscusComment> comments = savedInstanceState.getParcelableArrayList(COMMENTS_DATA);
            if (comments == null) {
                qiscusChatPresenter.loadComments(20);
            } else {
                showComments(comments);
            }
        }

        if (commentSelectedListener != null) {
            commentSelectedListener.onCommentSelected(chatAdapter.getSelectedComments());
        }

        if (startingMessage != null && !startingMessage.isEmpty()) {
            sendMessage(startingMessage);
        }

        if (shareFile != null) {
            sendFile(shareFile);
        }
    }

    protected void setupEmojiPopup() {
        if (messageEditText instanceof EmojiEditText && toggleEmojiButton != null) {
            emojiPopup = EmojiPopup.Builder.fromRootView(rootView)
                    .setOnSoftKeyboardCloseListener(this::dismissEmoji)
                    .setOnEmojiPopupShownListener(() -> toggleEmojiButton.setImageResource(chatConfig.getShowKeyboardIcon()))
                    .setOnEmojiPopupDismissListener(() -> toggleEmojiButton.setImageResource(chatConfig.getShowEmojiIcon()))
                    .build((EmojiEditText) messageEditText);
        }
    }

    public void replyComment(QiscusComment originComment) {
        if (replyPreviewView != null) {
            replyPreviewView.bind(originComment);
            hideAttachmentPanel();
        }
    }

    protected void hideAttachmentPanel() {
        if (attachmentPanel != null) {
            attachmentPanel.setVisibility(View.GONE);
            if (messageEditTextContainer != null) {
                messageEditTextContainer.setVisibility(View.VISIBLE);
            }
            if (replyPreviewView != null && replyPreviewView.getOriginComment() != null) {
                replyPreviewView.setVisibility(View.VISIBLE);
            }
        }
    }

    protected void showAttachmentPanel() {
        if (attachmentPanel != null && attachmentPanel.getVisibility() == View.GONE) {
            attachmentPanel.setVisibility(View.VISIBLE);
            if (messageEditTextContainer != null) {
                messageEditTextContainer.setVisibility(View.GONE);
                QiscusAndroidUtil.hideKeyboard(getActivity(), messageEditText);
            }
            if (replyPreviewView != null) {
                replyPreviewView.setVisibility(View.GONE);
            }
        }
    }

    protected QiscusChatConfig onLoadChatConfig() {
        return Qiscus.getChatConfig();
    }

    protected void resolveChatRoom(Bundle savedInstanceState) {
        qiscusChatRoom = getArguments().getParcelable(CHAT_ROOM_DATA);
        if (qiscusChatRoom == null && savedInstanceState != null) {
            qiscusChatRoom = savedInstanceState.getParcelable(CHAT_ROOM_DATA);
        }

        if (qiscusChatRoom == null) {
            getActivity().finish();
            return;
        }
    }

    protected void resolveStartingMessage() {
        startingMessage = getArguments().getString(EXTRA_STARTING_MESSAGE);
        getArguments().remove(EXTRA_STARTING_MESSAGE);
    }

    protected void resolveShareFile() {
        shareFile = (File) getArguments().getSerializable(EXTRA_SHARE_FILE);
        getArguments().remove(EXTRA_SHARE_FILE);
    }

    protected void onApplyChatConfig() {
        Drawable buttonBg;

        rootView.setBackground(chatConfig.getChatRoomBackground());
        swipeRefreshLayout.setColorSchemeResources(chatConfig.getSwipeRefreshColorScheme());
        sendButton.setImageResource(chatConfig.getShowAttachmentPanelIcon());
        messageEditText.setHint(chatConfig.getMessageFieldHint());

        if (emptyChatImageView != null) {
            emptyChatImageView.setImageResource(chatConfig.getEmptyRoomImageResource());
        }
        if (emptyChatTitleView != null) {
            emptyChatTitleView.setText(chatConfig.getEmptyRoomTitle());
        }
        if (emptyChatDescView != null) {
            emptyChatDescView.setText(chatConfig.getEmptyRoomSubtitle());
        }
        if (addImageButton != null) {
            addImageButton.setImageResource(chatConfig.getAddPictureIcon());
            buttonBg = ContextCompat.getDrawable(Qiscus.getApps(), R.drawable.qiscus_gallery_button_bg);
            buttonBg.setColorFilter(ContextCompat.getColor(Qiscus.getApps(),
                    chatConfig.getAddPictureBackgroundColor()), PorterDuff.Mode.SRC_ATOP);
            addImageButton.setBackground(buttonBg);
        }
        if (addImageTextView != null) {
            addImageTextView.setText(chatConfig.getAddPictureText());
        }
        if (takeImageButton != null) {
            takeImageButton.setImageResource(chatConfig.getTakePictureIcon());
            buttonBg = ContextCompat.getDrawable(Qiscus.getApps(), R.drawable.qiscus_camera_button_bg);
            buttonBg.setColorFilter(ContextCompat.getColor(Qiscus.getApps(),
                    chatConfig.getTakePictureBackgroundColor()), PorterDuff.Mode.SRC_ATOP);
            takeImageButton.setBackground(buttonBg);
        }
        if (takeImageTextView != null) {
            takeImageTextView.setText(chatConfig.getTakePictureText());
        }
        if (addFileButton != null) {
            addFileButton.setImageResource(chatConfig.getAddFileIcon());
            buttonBg = ContextCompat.getDrawable(Qiscus.getApps(), R.drawable.qiscus_file_button_bg);
            buttonBg.setColorFilter(ContextCompat.getColor(Qiscus.getApps(),
                    chatConfig.getAddFileBackgroundColor()), PorterDuff.Mode.SRC_ATOP);
            addFileButton.setBackground(buttonBg);
        }
        if (addFileTextView != null) {
            addFileTextView.setText(chatConfig.getAddFileText());
        }
        if (recordAudioButton != null) {
            recordAudioButton.setImageResource(chatConfig.getRecordAudioIcon());
            buttonBg = ContextCompat.getDrawable(Qiscus.getApps(), R.drawable.qiscus_record_button_bg);
            buttonBg.setColorFilter(ContextCompat.getColor(Qiscus.getApps(),
                    chatConfig.getRecordBackgroundColor()), PorterDuff.Mode.SRC_ATOP);
            recordAudioButton.setBackground(buttonBg);
        }
        if (recordAudioTextView != null) {
            recordAudioTextView.setText(chatConfig.getRecordText());
        }
        if (hideAttachmentButton != null) {
            hideAttachmentButton.setImageResource(chatConfig.getHideAttachmentPanelIcon());
            buttonBg = ContextCompat.getDrawable(Qiscus.getApps(), R.drawable.qiscus_keyboard_button_bg);
            buttonBg.setColorFilter(ContextCompat.getColor(Qiscus.getApps(),
                    chatConfig.getHideAttachmentPanelBackgroundColor()), PorterDuff.Mode.SRC_ATOP);
            hideAttachmentButton.setBackground(buttonBg);
        }
        if (recordAudioPanel != null) {
            recordAudioPanel.setButtonStopRecord(chatConfig.getStopRecordIcon());
            recordAudioPanel.setButtonCancelRecord(chatConfig.getCancelRecordIcon());
        }
        if (toggleEmojiButton != null) {
            toggleEmojiButton.setImageResource(chatConfig.getShowEmojiIcon());
        }

        if (addImageLayout != null) {
            addImageLayout.setVisibility(chatConfig.isEnableAddPicture() ? View.VISIBLE : View.GONE);
        }
        if (takeImageLayout != null) {
            takeImageLayout.setVisibility(chatConfig.isEnableTakePicture() ? View.VISIBLE : View.GONE);
        }
        if (addFileLayout != null) {
            addFileLayout.setVisibility(chatConfig.isEnableAddFile() ? View.VISIBLE : View.GONE);
        }
        if (recordAudioLayout != null) {
            recordAudioLayout.setVisibility(chatConfig.isEnableRecordAudio() ? View.VISIBLE : View.GONE);
        }
        if (replyPreviewView != null) {
            replyPreviewView.setBarColor(ContextCompat.getColor(Qiscus.getApps(), chatConfig.getReplyBarColor()));
            replyPreviewView.setSenderColor(ContextCompat.getColor(Qiscus.getApps(), chatConfig.getReplySenderColor()));
            replyPreviewView.setContentColor(ContextCompat.getColor(Qiscus.getApps(), chatConfig.getReplyMessageColor()));
        }
    }

    protected Animation onLoadAnimation() {
        return AnimationUtils.loadAnimation(getActivity(), R.anim.qiscus_simple_grow);
    }

    protected void onClearNotification() {
        NotificationManagerCompat.from(getActivity()).cancel(qiscusChatRoom.getId());
        QiscusCacheManager.getInstance().clearMessageNotifItems(qiscusChatRoom.getId());
    }

    protected abstract T onCreateChatAdapter();

    @Override
    public void onResume() {
        super.onResume();
        onClearNotification();
        QiscusCacheManager.getInstance().setLastChatActivity(true, qiscusChatRoom.getId());
    }

    @Override
    public void onPause() {
        super.onPause();
        QiscusCacheManager.getInstance().setLastChatActivity(false, qiscusChatRoom.getId());
    }

    protected void onItemCommentClick(QiscusComment qiscusComment) {
        if (chatAdapter.getSelectedComments().isEmpty()) {
            if (qiscusComment.getState() > QiscusComment.STATE_SENDING) {
                if (qiscusComment.getType() == QiscusComment.Type.FILE
                        || qiscusComment.getType() == QiscusComment.Type.IMAGE
                        || qiscusComment.getType() == QiscusComment.Type.AUDIO) {
                    qiscusChatPresenter.downloadFile(qiscusComment);
                } else if (qiscusComment.getType() == QiscusComment.Type.ACCOUNT_LINKING) {
                    accountLinkingClick(qiscusComment);
                }
            } else if (qiscusComment.getState() == QiscusComment.STATE_FAILED) {
                showFailedCommentDialog(qiscusComment);
            }
        } else {
            if (qiscusComment.getType() == QiscusComment.Type.TEXT
                    || qiscusComment.getType() == QiscusComment.Type.LINK
                    || qiscusComment.getType() == QiscusComment.Type.IMAGE
                    || qiscusComment.getType() == QiscusComment.Type.AUDIO
                    || qiscusComment.getType() == QiscusComment.Type.FILE
                    || qiscusComment.getType() == QiscusComment.Type.REPLY) {
                toggleSelectComment(qiscusComment);
            }
        }
    }

    protected void accountLinkingClick(QiscusComment qiscusComment) {
        try {
            JSONObject payload = QiscusRawDataExtractor.getPayload(qiscusComment);
            JSONObject params = payload.getJSONObject("params");
            startActivity(QiscusAccountLinkingActivity.generateIntent(getActivity(), params.optString("view_title"),
                    payload.getString("url"), payload.getString("redirect_url"), params.optString("success_message")));
        } catch (JSONException e) {
            Log.e("Qiscus", e.getMessage());
        }
    }

    protected void showFailedCommentDialog(QiscusComment qiscusComment) {
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.qiscus_failed_send_message_dialog_title)
                .setItems(new CharSequence[]{getString(R.string.qiscus_resend),
                        getString(R.string.qiscus_delete)}, (dialog, which) -> {
                    if (which == 0) {
                        qiscusChatPresenter.resendComment(qiscusComment);
                    } else {
                        qiscusChatPresenter.deleteComment(qiscusComment);
                    }
                })
                .setCancelable(true)
                .create()
                .show();
    }

    protected void onItemCommentLongClick(QiscusComment qiscusComment) {
        if (chatAdapter.getSelectedComments().isEmpty()
                && (qiscusComment.getType() == QiscusComment.Type.TEXT
                || qiscusComment.getType() == QiscusComment.Type.LINK
                || qiscusComment.getType() == QiscusComment.Type.IMAGE
                || qiscusComment.getType() == QiscusComment.Type.AUDIO
                || qiscusComment.getType() == QiscusComment.Type.FILE
                || qiscusComment.getType() == QiscusComment.Type.REPLY)) {
            toggleSelectComment(qiscusComment);
        }
    }

    protected void toggleSelectComment(QiscusComment qiscusComment) {
        qiscusComment.setSelected(!qiscusComment.isSelected());
        refreshComment(qiscusComment);
        if (commentSelectedListener != null) {
            commentSelectedListener.onCommentSelected(chatAdapter.getSelectedComments());
        }
    }

    public List<QiscusComment> getSelectedComments() {
        return chatAdapter.getSelectedComments();
    }

    public void clearSelectedComments() {
        chatAdapter.clearSelectedComments();
    }

    protected void onMessageEditTextChanged(CharSequence message) {
        if (message == null || message.toString().trim().isEmpty()) {
            if (!fieldMessageEmpty) {
                fieldMessageEmpty = true;
                sendButton.startAnimation(animation);
                sendButton.setImageResource(chatConfig.getShowAttachmentPanelIcon());
                QiscusPusherApi.getInstance().setUserTyping(qiscusChatRoom.getId(), qiscusChatRoom.getLastTopicId(), false);
            }
        } else {
            if (fieldMessageEmpty) {
                fieldMessageEmpty = false;
                sendButton.startAnimation(animation);
                sendButton.setImageResource(chatConfig.getSendButtonIcon());
                QiscusPusherApi.getInstance().setUserTyping(qiscusChatRoom.getId(), qiscusChatRoom.getLastTopicId(), true);
            }
        }
    }

    public void sendMessage(String message) {
        message = message.trim();
        if (!message.isEmpty()) {
            qiscusChatPresenter.sendComment(message);
            messageEditText.setText("");
        }
    }

    public void sendFile(File file) {
        qiscusChatPresenter.sendFile(file);
    }

    protected void addImage() {
        if (QiscusPermissionsUtil.hasPermissions(getActivity(), FILE_PERMISSION)) {
            FilePickerBuilder.getInstance(getActivity())
                    .setMaxCount(1)
                    .addVideoPicker()
                    .pickPhoto(this);
            hideAttachmentPanel();
        } else {
            requestPermissions();
        }
    }

    protected void takeImage() {
        if (QiscusPermissionsUtil.hasPermissions(getActivity(), FILE_PERMISSION)
                && QiscusPermissionsUtil.hasPermissions(getActivity(), CAMERA_PERMISSION)) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                File photoFile = null;
                try {
                    photoFile = QiscusImageUtil.createImageFile();
                } catch (IOException ex) {
                    showError(getString(R.string.qiscus_chat_error_failed_write));
                }

                if (photoFile != null) {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                    } else {
                        intent.putExtra(MediaStore.EXTRA_OUTPUT,
                                FileProvider.getUriForFile(getActivity(), Qiscus.getProviderAuthorities(), photoFile));
                    }
                    startActivityForResult(intent, TAKE_PICTURE_REQUEST);
                }
                hideAttachmentPanel();
            }
        } else {
            requestPermissions();
        }
    }

    protected void addFile() {
        if (QiscusPermissionsUtil.hasPermissions(getActivity(), FILE_PERMISSION)) {
            FilePickerBuilder.getInstance(getActivity())
                    .setMaxCount(1)
                    .pickFile(this);
            hideAttachmentPanel();
        } else {
            requestPermissions();
        }
    }

    protected void recordAudio() {
        if (QiscusPermissionsUtil.hasPermissions(getActivity(), AUDIO_PERMISSION)) {
            if (recordAudioPanel != null) {
                recordAudioPanel.setVisibility(View.VISIBLE);
                if (messageInputPanel != null) {
                    messageInputPanel.setVisibility(View.GONE);
                }
                try {
                    recordAudioPanel.startRecord();
                } catch (IOException e) {
                    e.printStackTrace();
                    showError(getString(R.string.qiscus_failed_record_audio));
                    recordAudioPanel.cancelRecord();
                } catch (IllegalStateException e) {
                    showError(getString(R.string.qiscus_microphone_in_use));
                    recordAudioPanel.cancelRecord();
                }
            }
        } else {
            requestPermissions();
        }
    }

    protected void toggleEmoji() {
        boolean lastShowing = emojiPopup.isShowing();
        emojiPopup.toggle();
        if (!lastShowing && !emojiPopup.isShowing()) {
            emojiPopup.toggle();
        }
    }

    protected void dismissEmoji() {
        if (emojiPopup != null && emojiPopup.isShowing()) {
            emojiPopup.dismiss();
        }
    }

    @Override
    public void initRoomData(QiscusChatRoom qiscusChatRoom, List<QiscusComment> comments) {
        this.qiscusChatRoom = qiscusChatRoom;
        if (roomChangedListener != null) {
            roomChangedListener.onRoomUpdated(qiscusChatRoom);
        }
        showComments(comments);
    }

    @Override
    public void showComments(List<QiscusComment> qiscusComments) {
        if (!qiscusComments.isEmpty()) {
            chatAdapter.addOrUpdate(qiscusComments);
        }
        if (chatAdapter.isEmpty() && qiscusComments.isEmpty()) {
            if (emptyChatHolder != null) {
                emptyChatHolder.setVisibility(View.VISIBLE);
            }
        } else {
            if (emptyChatHolder != null) {
                emptyChatHolder.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onLoadMore(List<QiscusComment> qiscusComments) {
        chatAdapter.addOrUpdate(qiscusComments);
        if (chatAdapter.isEmpty() && qiscusComments.isEmpty()) {
            if (emptyChatHolder != null) {
                emptyChatHolder.setVisibility(View.VISIBLE);
            }
        } else {
            if (emptyChatHolder != null) {
                emptyChatHolder.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onSendingComment(QiscusComment qiscusComment) {
        chatAdapter.addOrUpdate(qiscusComment);
        scrollToBottom();
        if (emptyChatHolder != null) {
            emptyChatHolder.setVisibility(View.GONE);
        }
    }

    @Override
    public void onSuccessSendComment(QiscusComment qiscusComment) {
        chatAdapter.addOrUpdate(qiscusComment);
    }

    @Override
    public void onFailedSendComment(QiscusComment qiscusComment) {
        chatAdapter.addOrUpdate(qiscusComment);
    }

    @Override
    public void onNewComment(QiscusComment qiscusComment) {
        chatAdapter.addOrUpdate(qiscusComment);
        if (!qiscusComment.getSenderEmail().equalsIgnoreCase(qiscusAccount.getEmail()) && shouldShowNewMessageButton()) {
            if (newMessageButton != null && newMessageButton.getVisibility() == View.GONE) {
                newMessageButton.setVisibility(View.VISIBLE);
                newMessageButton.startAnimation(animation);
            }
        } else {
            scrollToBottom();
        }
        if (emptyChatHolder != null) {
            emptyChatHolder.setVisibility(View.GONE);
        }
    }

    @Override
    public void onCommentDeleted(QiscusComment qiscusComment) {
        chatAdapter.remove(qiscusComment);
    }

    @Override
    public void refreshComment(QiscusComment qiscusComment) {
        chatAdapter.addOrUpdate(qiscusComment);
    }

    @Override
    public void updateLastDeliveredComment(int lastDeliveredCommentId) {
        chatAdapter.updateLastDeliveredComment(lastDeliveredCommentId);
    }

    @Override
    public void updateLastReadComment(int lastReadCommentId) {
        chatAdapter.updateLastReadComment(lastReadCommentId);
    }

    private boolean shouldShowNewMessageButton() {
        return chatLayoutManager.findFirstVisibleItemPosition() > 2;
    }

    private void loadMoreComments() {
        if (loadMoreProgressBar.getVisibility() == View.GONE && chatAdapter.getItemCount() > 0) {
            QiscusComment qiscusComment = (QiscusComment) chatAdapter.getData().get(chatAdapter.getItemCount() - 1);
            if (qiscusComment.getId() == -1 || qiscusComment.getCommentBeforeId() > 0) {
                qiscusChatPresenter.loadOlderCommentThan(qiscusComment);
            }
        }
    }

    protected void scrollToBottom() {
        messageRecyclerView.smoothScrollToPosition(0);
        if (newMessageButton != null) {
            newMessageButton.setVisibility(View.GONE);
        }
    }

    @Override
    public void onFileDownloaded(File file, String mimeType) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            intent.setDataAndType(Uri.fromFile(file), mimeType);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        } else {
            intent.setDataAndType(FileProvider.getUriForFile(getActivity(), Qiscus.getProviderAuthorities(), file), mimeType);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            showError(getString(R.string.qiscus_chat_error_no_handler));
        }
    }

    @Override
    public void startPhotoViewer(QiscusComment qiscusComment) {
        startActivity(QiscusPhotoViewerActivity.generateIntent(getActivity(), qiscusComment));
    }

    @Override
    public void onTopOffListMessage() {
        loadMoreComments();
    }

    @Override
    public void onMiddleOffListMessage() {
    }

    @Override
    public void onBottomOffListMessage() {
        if (newMessageButton != null) {
            newMessageButton.setVisibility(View.GONE);
        }
    }

    @Override
    public void showError(String errorMessage) {
        Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showLoading() {
        try {
            swipeRefreshLayout.setRefreshing(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void showLoadMoreLoading() {
        try {
            swipeRefreshLayout.setRefreshing(false);
            loadMoreProgressBar.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void dismissLoading() {
        try {
            swipeRefreshLayout.setRefreshing(false);
            loadMoreProgressBar.setVisibility(View.GONE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRefresh() {
        if (chatAdapter.getData().size() > 0) {
            loadMoreComments();
            swipeRefreshLayout.setRefreshing(false);
        } else {
            qiscusChatPresenter.loadComments(20);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FilePickerConst.REQUEST_CODE_PHOTO && resultCode == Activity.RESULT_OK) {
            if (data == null) {
                showError(getString(R.string.qiscus_chat_error_failed_open_picture));
                return;
            }
            ArrayList<String> paths = data.getStringArrayListExtra(FilePickerConst.KEY_SELECTED_MEDIA);
            if (paths.size() > 0) {
                sendFile(new File(paths.get(0)));
            }
        } else if (requestCode == FilePickerConst.REQUEST_CODE_DOC && resultCode == Activity.RESULT_OK) {
            if (data == null) {
                showError(getString(R.string.qiscus_chat_error_failed_open_file));
                return;
            }
            ArrayList<String> paths = data.getStringArrayListExtra(FilePickerConst.KEY_SELECTED_DOCS);
            if (paths.size() > 0) {
                sendFile(new File(paths.get(0)));
            }
        } else if (requestCode == TAKE_PICTURE_REQUEST && resultCode == Activity.RESULT_OK) {
            try {
                sendFile(QiscusFileUtil.from(Uri.parse(QiscusCacheManager.getInstance().getLastImagePath())));
            } catch (Exception e) {
                showError(getString(R.string.qiscus_chat_error_failed_read_picture));
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onStartRecord() {
        hideAttachmentPanel();
    }

    @Override
    public void onCancelRecord() {
        if (recordAudioPanel != null) {
            recordAudioPanel.setVisibility(View.GONE);
        }
        if (messageInputPanel != null) {
            messageInputPanel.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onStopRecord(File audioFile) {
        sendFile(audioFile);
        onCancelRecord();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(CHAT_ROOM_DATA, qiscusChatRoom);
        ArrayList<QiscusComment> comments = new ArrayList<>();
        int size = chatAdapter.getData().size();
        for (int i = 0; i < size; i++) {
            comments.add((QiscusComment) chatAdapter.getData().get(i));
        }
        outState.putParcelableArrayList(COMMENTS_DATA, comments);
    }

    @Override
    public void onStop() {
        if (emojiPopup != null) {
            emojiPopup.dismiss();
        }

        super.onStop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        QiscusPusherApi.getInstance().setUserTyping(qiscusChatRoom.getId(), qiscusChatRoom.getLastTopicId(), false);
        QiscusCacheManager.getInstance().setLastChatActivity(false, qiscusChatRoom.getId());
        chatAdapter.detachView();
        if (recordAudioPanel != null) {
            recordAudioPanel.cancelRecord();
        }
        qiscusChatPresenter.detachView();
    }

    protected void requestPermissions() {
        if (!QiscusPermissionsUtil.hasPermissions(getActivity(), PERMISSIONS)) {
            QiscusPermissionsUtil.requestPermissions(this, getString(R.string.qiscus_permission_request_title),
                    RC_PERMISSIONS, PERMISSIONS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        QiscusPermissionsUtil.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {

    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        QiscusPermissionsUtil.checkDeniedPermissionsNeverAskAgain(this, getString(R.string.qiscus_permission_message),
                R.string.qiscus_grant, R.string.qiscus_denny, perms);
    }

    @Override
    public void onChatButtonClick(JSONObject jsonButton) {
        qiscusChatPresenter.clickChatButton(jsonButton);
    }

    public interface CommentSelectedListener {
        void onCommentSelected(List<QiscusComment> selectedComments);
    }

    public interface RoomChangedListener {
        void onRoomUpdated(QiscusChatRoom qiscusChatRoom);
    }
}
