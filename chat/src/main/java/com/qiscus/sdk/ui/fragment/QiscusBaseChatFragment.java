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
import android.database.Cursor;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.annotation.ColorInt;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.qiscus.jupuk.JupukBuilder;
import com.qiscus.jupuk.JupukConst;
import com.qiscus.manggil.ui.MentionsEditText;
import com.qiscus.sdk.Qiscus;
import com.qiscus.sdk.R;
import com.qiscus.sdk.chat.core.data.local.QiscusCacheManager;
import com.qiscus.sdk.chat.core.data.model.QiscusAccount;
import com.qiscus.sdk.chat.core.data.model.QiscusChatRoom;
import com.qiscus.sdk.chat.core.data.model.QiscusComment;
import com.qiscus.sdk.chat.core.data.model.QiscusCommentDraft;
import com.qiscus.sdk.chat.core.data.model.QiscusContact;
import com.qiscus.sdk.chat.core.data.model.QiscusLocation;
import com.qiscus.sdk.chat.core.data.model.QiscusPhoto;
import com.qiscus.sdk.chat.core.data.model.QiscusReplyCommentDraft;
import com.qiscus.sdk.chat.core.data.remote.QiscusPusherApi;
import com.qiscus.sdk.chat.core.util.QiscusAndroidUtil;
import com.qiscus.sdk.chat.core.util.QiscusErrorLogger;
import com.qiscus.sdk.chat.core.util.QiscusFileUtil;
import com.qiscus.sdk.chat.core.util.QiscusNumberUtil;
import com.qiscus.sdk.chat.core.util.QiscusRawDataExtractor;
import com.qiscus.sdk.chat.core.util.QiscusTextUtil;
import com.qiscus.sdk.data.model.QiscusChatConfig;
import com.qiscus.sdk.presenter.QiscusChatPresenter;
import com.qiscus.sdk.ui.QiscusAccountLinkingActivity;
import com.qiscus.sdk.ui.QiscusPhotoViewerActivity;
import com.qiscus.sdk.ui.QiscusSendPhotoConfirmationActivity;
import com.qiscus.sdk.ui.adapter.CommentChainingListener;
import com.qiscus.sdk.ui.adapter.OnUploadIconClickListener;
import com.qiscus.sdk.ui.adapter.QiscusBaseChatAdapter;
import com.qiscus.sdk.ui.view.QiscusAudioRecorderView;
import com.qiscus.sdk.ui.view.QiscusCarouselItemView;
import com.qiscus.sdk.ui.view.QiscusChatButtonView;
import com.qiscus.sdk.ui.view.QiscusChatScrollListener;
import com.qiscus.sdk.ui.view.QiscusEditText;
import com.qiscus.sdk.ui.view.QiscusMentionSuggestionView;
import com.qiscus.sdk.ui.view.QiscusRecyclerView;
import com.qiscus.sdk.ui.view.QiscusReplyPreviewView;
import com.qiscus.sdk.util.QiscusImageUtil;
import com.qiscus.sdk.util.QiscusKeyboardUtil;
import com.qiscus.sdk.util.QiscusPermissionsUtil;
import com.trello.rxlifecycle.components.support.RxFragment;
import com.vanniktech.emoji.EmojiEditText;
import com.vanniktech.emoji.EmojiPopup;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created on : September 28, 2016
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
public abstract class QiscusBaseChatFragment<T extends QiscusBaseChatAdapter> extends RxFragment
        implements SwipeRefreshLayout.OnRefreshListener, QiscusChatScrollListener.Listener,
        QiscusChatPresenter.View, QiscusAudioRecorderView.RecordListener,
        QiscusPermissionsUtil.PermissionCallbacks, QiscusChatButtonView.ChatButtonClickListener,
        CommentChainingListener, QiscusCarouselItemView.CarouselItemClickListener, OnUploadIconClickListener {

    protected static final int RC_PERMISSIONS = 127;
    protected static final int RC_CAMERA_PERMISSION = 128;
    protected static final int RC_AUDIO_PERMISSION = 129;
    protected static final int RC_FILE_PERMISSION = 130;
    protected static final int RC_LOCATION_PERMISSION = 131;
    protected static final String CHAT_ROOM_DATA = "chat_room_data";
    protected static final String EXTRA_STARTING_MESSAGE = "extra_starting_message";
    protected static final String EXTRA_SHARE_FILES = "extra_share_files";
    protected static final String EXTRA_AUTO_SEND = "extra_auto_send";
    protected static final String EXTRA_FORWARD_COMMENTS = "extra_forward_comments";
    protected static final String EXTRA_SCROLL_TO_COMMENT = "extra_scroll_to_comment";
    protected static final String COMMENTS_LOADED_SIZE = "comments_loaded_size";
    protected static final String COMMENTS_LAYOUT_MANAGER = "comments_layout_manager";
    protected static final int TAKE_PICTURE_REQUEST = 1;
    protected static final int PICK_CONTACT_REQUEST = 2;
    protected static final int PICK_LOCATION_REQUEST = 3;
    protected static final int SEND_PICTURE_CONFIRMATION_REQUEST = 4;
    protected static final int SHOW_MEDIA_DETAIL = 5;
    private static final String[] PERMISSIONS = {
            "android.permission.WRITE_EXTERNAL_STORAGE",
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.RECORD_AUDIO",
            "android.permission.CAMERA",
            "android.permission.ACCESS_COARSE_LOCATION",
            "android.permission.ACCESS_FINE_LOCATION",
    };
    private static final String AUDIO_PERMISSION = "android.permission.RECORD_AUDIO";
    private static final String[] FILE_PERMISSION = {
            "android.permission.WRITE_EXTERNAL_STORAGE",
            "android.permission.READ_EXTERNAL_STORAGE"
    };
    private static final String[] CAMERA_PERMISSION = {
            "android.permission.CAMERA",
            "android.permission.WRITE_EXTERNAL_STORAGE",
            "android.permission.READ_EXTERNAL_STORAGE",
    };
    private static final String[] LOCATION_PERMISSION = {
            "android.permission.ACCESS_COARSE_LOCATION",
            "android.permission.ACCESS_FINE_LOCATION"
    };
    @NonNull
    protected ViewGroup rootView;
    @Nullable
    protected ViewGroup emptyChatHolder;
    @NonNull
    protected SwipeRefreshLayout swipeRefreshLayout;
    @NonNull
    protected QiscusRecyclerView messageRecyclerView;

    @Nullable
    protected ViewGroup messageInputPanel;
    @Nullable
    protected ViewGroup messageEditTextContainer;
    @NonNull
    protected EditText messageEditText;
    @NonNull
    protected ImageView sendButton;
    @Nullable
    protected QiscusMentionSuggestionView mentionSuggestionView;

    @Nullable
    protected View newMessageButton;
    @NonNull
    protected View loadMoreProgressBar;

    @Nullable
    protected ImageView emptyChatImageView;
    @Nullable
    protected TextView emptyChatTitleView;
    @Nullable
    protected TextView emptyChatDescView;

    @Nullable
    protected ViewGroup attachmentPanel;

    @Nullable
    protected View addImageLayout;
    @Nullable
    protected ImageView addImageButton;
    @Nullable
    protected TextView addImageTextView;

    @Nullable
    protected View takeImageLayout;
    @Nullable
    protected ImageView takeImageButton;
    @Nullable
    protected TextView takeImageTextView;

    @Nullable
    protected View addFileLayout;
    @Nullable
    protected ImageView addFileButton;
    @Nullable
    protected TextView addFileTextView;

    @Nullable
    protected View recordAudioLayout;
    @Nullable
    protected ImageView recordAudioButton;
    @Nullable
    protected TextView recordAudioTextView;

    @Nullable
    protected View addContactLayout;
    @Nullable
    protected ImageView addContactButton;
    @Nullable
    protected TextView addContactTextView;

    @Nullable
    protected View addLocationLayout;
    @Nullable
    protected ImageView addLocationButton;
    @Nullable
    protected TextView addLocationTextView;

    @Nullable
    protected ImageView hideAttachmentButton;
    @Nullable
    protected ImageView toggleEmojiButton;

    @Nullable
    protected QiscusAudioRecorderView recordAudioPanel;
    @Nullable
    protected QiscusReplyPreviewView replyPreviewView;

    @Nullable
    protected View goToBottomButton;

    protected QiscusChatConfig chatConfig;
    protected QiscusChatRoom qiscusChatRoom;
    protected String startingMessage;
    protected List<File> shareFiles;
    protected boolean autoSendExtra;
    protected List<QiscusComment> forwardComments;
    protected T chatAdapter;
    protected QiscusChatPresenter qiscusChatPresenter;
    protected Animation animation;
    protected LinearLayoutManager chatLayoutManager;
    private QiscusAccount qiscusAccount;
    private boolean fieldMessageEmpty = true;
    private CommentSelectedListener commentSelectedListener;
    private RoomChangedListener roomChangedListener;
    private EmojiPopup emojiPopup;

    private Runnable commentHighlightTask;

    private boolean typing;
    private Runnable stopTypingNotifyTask;

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
        mentionSuggestionView = getMentionSuggestionView(view);

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

        addContactLayout = getAddContactLayout(view);
        addContactButton = getAddContactButton(view);
        addContactTextView = getAddContactTextView(view);

        addLocationLayout = getAddLocationLayout(view);
        addLocationButton = getAddLocationButton(view);
        addLocationTextView = getAddLocationTextView(view);

        toggleEmojiButton = getToggleEmojiButton(view);
        recordAudioPanel = getRecordAudioPanel(view);
        replyPreviewView = getReplyPreviewView(view);
        goToBottomButton = getGotoBottomButton(view);

        if (toggleEmojiButton != null && !(messageEditText instanceof EmojiEditText)) {
            throw new RuntimeException("Please use EmojiEditText as message text field if you want to using EmojiKeyboard.");
        }

        messageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (!typing) {
                    typing = true;
                    notifyServerTyping(true);
                }
                QiscusAndroidUtil.cancelRunOnUIThread(stopTypingNotifyTask);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                onMessageEditTextChanged(s);
                QiscusAndroidUtil.runOnUIThread(stopTypingNotifyTask, 800);
            }
        });

        messageEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                String message = messageEditText.getText().toString().trim();
                if (messageEditText instanceof MentionsEditText) {
                    message = ((MentionsEditText) messageEditText).getMentionsTextEncoded().toString().trim();
                }
                if (!message.isEmpty()) {
                    sendMessage(message);
                    QiscusKeyboardUtil.hideKeyboard(getActivity(), messageEditText);
                }
                return true;
            }
            return false;
        });

        messageEditText.setOnClickListener(v -> {
            if (emojiPopup != null && emojiPopup.isShowing()) {
                toggleEmoji();
            }
        });

        sendButton.setOnClickListener(v -> {
            String message = messageEditText.getText().toString().trim();
            if (messageEditText instanceof MentionsEditText) {
                message = ((MentionsEditText) messageEditText).getMentionsTextEncoded().toString().trim();
            }
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
        if (addContactButton != null) {
            addContactButton.setOnClickListener(v -> addContact());
        }
        if (addLocationButton != null) {
            addLocationButton.setOnClickListener(v -> addLocation());
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
        if (goToBottomButton != null) {
            goToBottomButton.setOnClickListener(v -> {
                messageRecyclerView.scrollToPosition(0);
                QiscusAndroidUtil.runOnUIThread(() -> {
                    if (goToBottomButton != null) {
                        goToBottomButton.setVisibility(View.GONE);
                    }
                }, 320);
            });
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

    @NonNull
    protected abstract QiscusMentionSuggestionView getMentionSuggestionView(View view);

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
    protected abstract View getAddContactLayout(View view);

    @Nullable
    protected abstract ImageView getAddContactButton(View view);

    @Nullable
    protected TextView getAddContactTextView(View view) {
        return null;
    }

    @Nullable
    protected abstract View getAddLocationLayout(View view);

    @Nullable
    protected abstract ImageView getAddLocationButton(View view);

    @Nullable
    protected TextView getAddLocationTextView(View view) {
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

    @Nullable
    protected abstract View getGotoBottomButton(View view);

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
        resolveAutoSendExtra();
        resolveForwardComments();

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
        chatAdapter.setUploadIconClickListener(this);
        chatAdapter.setReplyItemClickListener(comment -> scrollToComment(comment.getReplyTo()));
        chatAdapter.setChatButtonClickListener(this);
        chatAdapter.setCarouselItemClickListener(this);
        chatAdapter.setCommentChainingListener(this);
        messageRecyclerView.setUpAsBottomList();
        chatLayoutManager = (LinearLayoutManager) messageRecyclerView.getLayoutManager();
        messageRecyclerView.setAdapter(chatAdapter);
        messageRecyclerView.addOnScrollListener(new QiscusChatScrollListener(chatLayoutManager, this));

        setupGifKeyboard();
        setupEmojiPopup();
        setupMentionEditText();

        stopTypingNotifyTask = () -> {
            typing = false;
            notifyServerTyping(false);
        };

        qiscusChatPresenter = new QiscusChatPresenter(this, qiscusChatRoom);
        if (savedInstanceState == null) {
            qiscusChatPresenter.loadComments(20);
        } else {
            Parcelable layoutManagerState = savedInstanceState.getParcelable(COMMENTS_LAYOUT_MANAGER);
            int commentsLoadedSize = savedInstanceState.getInt(COMMENTS_LOADED_SIZE);
            if (commentsLoadedSize == 0 && layoutManagerState == null) {
                qiscusChatPresenter.loadComments(20);
            } else {
                showComments(qiscusChatPresenter.loadLocalComments(commentsLoadedSize));
                chatAdapter.setQiscusChatRoom(qiscusChatRoom);
                updateMentionSuggestionData();
                messageRecyclerView.getLayoutManager().onRestoreInstanceState(layoutManagerState);
            }
        }

        if (commentSelectedListener != null) {
            commentSelectedListener.onCommentSelected(chatAdapter.getSelectedComments());
        }

        handleExtra();
        handleForward();
    }

    protected void handleForward() {
        if (forwardComments != null) {
            QiscusAndroidUtil.runOnUIThread(() -> qiscusChatPresenter.forward(forwardComments), 800);
        }
    }

    private void handleExtra() {
        if (startingMessage != null && !startingMessage.isEmpty() && shareFiles != null && !shareFiles.isEmpty()) {
            sendMessage(startingMessage);
            QiscusAndroidUtil.runOnUIThread(() -> sendFiles(shareFiles), 800);
            return;
        }

        if (autoSendExtra) {
            if (startingMessage != null && !startingMessage.isEmpty()) {
                QiscusAndroidUtil.runOnUIThread(() -> sendMessage(startingMessage), 800);
            }

            if (shareFiles != null && !shareFiles.isEmpty()) {
                QiscusAndroidUtil.runOnUIThread(() -> sendFiles(shareFiles), 800);
            }
        } else {
            if (startingMessage != null && !startingMessage.isEmpty()) {
                messageEditText.setText(startingMessage);
                messageEditText.post(() -> messageEditText.setSelection(messageEditText.getText().length()));
                QiscusKeyboardUtil.showKeyboard(getActivity(), messageEditText);
            }

            if (shareFiles != null && !shareFiles.isEmpty()) {
                if (QiscusImageUtil.isImage(shareFiles.get(0))) {
                    List<QiscusPhoto> qiscusPhotos = new ArrayList<>();
                    for (File shareFile : shareFiles) {
                        qiscusPhotos.add(new QiscusPhoto(shareFile));
                    }
                    startActivityForResult(QiscusSendPhotoConfirmationActivity.generateIntent(getActivity(),
                            qiscusChatRoom, qiscusPhotos),
                            SEND_PICTURE_CONFIRMATION_REQUEST);
                } else {
                    QiscusAndroidUtil.runOnUIThread(() -> sendFiles(shareFiles), 800);
                }
            }
        }
    }

    protected void setupGifKeyboard() {
        if (messageEditText instanceof QiscusEditText) {
            ((QiscusEditText) messageEditText).setCommitListener(infoCompat -> {
                try {
                    File imageFile = QiscusFileUtil.from(infoCompat.getContentUri());
                    String imageName = QiscusFileUtil.createTimestampFileName("gif");
                    List<QiscusPhoto> qiscusPhotos = new ArrayList<>();
                    qiscusPhotos.add(new QiscusPhoto(QiscusFileUtil.rename(imageFile, imageName)));
                    startActivityForResult(QiscusSendPhotoConfirmationActivity.generateIntent(getActivity(),
                            qiscusChatRoom, qiscusPhotos),
                            SEND_PICTURE_CONFIRMATION_REQUEST);
                } catch (IOException e) {
                    showError(getString(R.string.qiscus_error_gif));
                }
            });
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

    protected void setupMentionEditText() {
        if (messageEditText instanceof MentionsEditText && mentionSuggestionView != null && qiscusChatRoom.isGroup()) {
            mentionSuggestionView.bind((MentionsEditText) messageEditText);
        }
    }

    public void replyComment(QiscusComment originComment) {
        if (replyPreviewView != null) {
            replyPreviewView.bind(originComment);
            hideAttachmentPanel();
            QiscusKeyboardUtil.showKeyboard(getActivity(), messageEditText);
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
                QiscusKeyboardUtil.hideKeyboard(getActivity(), messageEditText);
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
        shareFiles = (List<File>) getArguments().getSerializable(EXTRA_SHARE_FILES);
        getArguments().remove(EXTRA_SHARE_FILES);
    }

    protected void resolveAutoSendExtra() {
        autoSendExtra = getArguments().getBoolean(EXTRA_AUTO_SEND, true);
        getArguments().remove(EXTRA_AUTO_SEND);
    }

    protected void resolveForwardComments() {
        forwardComments = getArguments().getParcelableArrayList(EXTRA_FORWARD_COMMENTS);
        getArguments().remove(EXTRA_FORWARD_COMMENTS);
    }

    protected void resolveScrollToComment() {
        QiscusComment comment = getArguments().getParcelable(EXTRA_SCROLL_TO_COMMENT);
        getArguments().remove(EXTRA_FORWARD_COMMENTS);
        if (comment != null) {
            scrollToComment(comment);
        }
    }

    protected void onApplyChatConfig() {
        Drawable buttonBg;

        rootView.setBackground(chatConfig.getChatRoomBackground());
        swipeRefreshLayout.setColorSchemeResources(chatConfig.getSwipeRefreshColorScheme());
        sendButton.setImageResource(chatConfig.getShowAttachmentPanelIcon());

        messageEditText.setHint(chatConfig.getMessageFieldHint());
        messageEditText.setHintTextColor(ContextCompat.getColor(Qiscus.getApps(),
                chatConfig.getMessageFieldHintColor()));
        messageEditText.setTextColor(ContextCompat.getColor(Qiscus.getApps(),
                chatConfig.getMessageFieldTextColor()));

        if (emptyChatImageView != null) {
            emptyChatImageView.setImageResource(chatConfig.getEmptyRoomImageResource());
        }
        if (emptyChatTitleView != null) {
            emptyChatTitleView.setText(chatConfig.getEmptyRoomTitle());
            emptyChatTitleView.setTextColor(ContextCompat.getColor(Qiscus.getApps(),
                    chatConfig.getEmptyRoomTitleColor()));
        }
        if (emptyChatDescView != null) {
            emptyChatDescView.setText(chatConfig.getEmptyRoomSubtitle());
            emptyChatDescView.setTextColor(ContextCompat.getColor(Qiscus.getApps(),
                    chatConfig.getEmptyRoomSubtitleColor()));
        }
        if (newMessageButton != null) {
            int accentColor = ContextCompat.getColor(Qiscus.getApps(), chatConfig.getAccentColor());
            Drawable drawable = ContextCompat.getDrawable(Qiscus.getApps(), R.drawable.qiscus_rounded_accent_bg);
            drawable.setColorFilter(accentColor, PorterDuff.Mode.SRC_ATOP);
            newMessageButton.setBackground(drawable);
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
        if (addContactButton != null) {
            addContactButton.setImageResource(chatConfig.getAddContactIcon());
            buttonBg = ContextCompat.getDrawable(Qiscus.getApps(), R.drawable.qiscus_contact_button_bg);
            buttonBg.setColorFilter(ContextCompat.getColor(Qiscus.getApps(),
                    chatConfig.getAddContactBackgroundColor()), PorterDuff.Mode.SRC_ATOP);
            addContactButton.setBackground(buttonBg);
        }
        if (addContactTextView != null) {
            addContactTextView.setText(chatConfig.getAddContactText());
        }
        if (addLocationButton != null) {
            addLocationButton.setImageResource(chatConfig.getAddLocationIcon());
            buttonBg = ContextCompat.getDrawable(Qiscus.getApps(), R.drawable.qiscus_location_button_bg);
            buttonBg.setColorFilter(ContextCompat.getColor(Qiscus.getApps(),
                    chatConfig.getAddLocationBackgroundColor()), PorterDuff.Mode.SRC_ATOP);
            addLocationButton.setBackground(buttonBg);
        }
        if (addLocationTextView != null) {
            addLocationTextView.setText(chatConfig.getAddLocationText());
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
        if (addContactLayout != null) {
            addContactLayout.setVisibility(chatConfig.isEnableAddContact() ? View.VISIBLE : View.GONE);
        }
        if (addLocationLayout != null) {
            addLocationLayout.setVisibility(chatConfig.isEnableAddLocation() ? View.VISIBLE : View.GONE);
        }
    }

    protected Animation onLoadAnimation() {
        return AnimationUtils.loadAnimation(getActivity(), R.anim.qiscus_simple_grow);
    }

    protected void onClearNotification() {
        NotificationManagerCompat.from(getActivity()).cancel(QiscusNumberUtil.convertToInt(qiscusChatRoom.getId()));
        QiscusCacheManager.getInstance().clearMessageNotifItems(qiscusChatRoom.getId());
    }

    protected abstract T onCreateChatAdapter();

    @Override
    public void onResume() {
        super.onResume();
        onClearNotification();
        QiscusCacheManager.getInstance().setLastChatActivity(true, qiscusChatRoom.getId());
        notifyLatestRead();
        showCommentDraft();
    }

    private void showCommentDraft() {
        if (QiscusTextUtil.isNotBlank(startingMessage) && !autoSendExtra) {
            return;
        }
        QiscusCommentDraft draftComment = QiscusCacheManager.getInstance().getDraftComment(qiscusChatRoom.getId());
        if (draftComment != null) {
            if (messageEditText instanceof MentionsEditText) {
                ((MentionsEditText) messageEditText).setMentionsTextEncoded(draftComment.getMessage(),
                        qiscusChatRoom.getMember());
            } else {
                messageEditText.setText(draftComment.getMessage());
            }
            messageEditText.post(() -> messageEditText.setSelection(messageEditText.getText().length()));
            if (draftComment instanceof QiscusReplyCommentDraft && replyPreviewView != null) {
                replyPreviewView.bind(((QiscusReplyCommentDraft) draftComment).getRepliedComment());
            }
            QiscusKeyboardUtil.showKeyboard(getActivity(), messageEditText);
        }
    }

    private void notifyLatestRead() {
        QiscusComment qiscusComment = chatAdapter.getLatestSentComment();
        if (qiscusComment != null) {
            QiscusPusherApi.getInstance()
                    .setUserRead(qiscusChatRoom.getId(), qiscusComment.getId());
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        QiscusCacheManager.getInstance().setLastChatActivity(false, qiscusChatRoom.getId());
        saveCommentDraft();
    }

    private void saveCommentDraft() {
        String message = messageEditText.getText().toString();
        if (messageEditText instanceof MentionsEditText) {
            message = ((MentionsEditText) messageEditText).getMentionsTextEncoded().toString();
        }
        if (QiscusTextUtil.isNotBlank(message)) {
            if (replyPreviewView != null) {
                QiscusComment repliedComment = replyPreviewView.getOriginComment();
                if (repliedComment != null) {
                    QiscusCacheManager.getInstance()
                            .setDraftComment(qiscusChatRoom.getId(),
                                    new QiscusReplyCommentDraft(message, repliedComment));
                    return;
                }
            }
            QiscusCacheManager.getInstance()
                    .setDraftComment(qiscusChatRoom.getId(), new QiscusCommentDraft(message));
        } else {
            QiscusCacheManager.getInstance().clearDraftComment(qiscusChatRoom.getId());
        }
    }

    protected void onItemCommentClick(QiscusComment qiscusComment) {
        if (chatAdapter.getSelectedComments().isEmpty()) {
            if (qiscusComment.getState() > QiscusComment.STATE_SENDING) {
                if (qiscusComment.getType() == QiscusComment.Type.FILE
                        || qiscusComment.getType() == QiscusComment.Type.IMAGE
                        || qiscusComment.getType() == QiscusComment.Type.VIDEO
                        || qiscusComment.getType() == QiscusComment.Type.AUDIO) {
                    qiscusChatPresenter.downloadFile(qiscusComment);
                } else if (qiscusComment.getType() == QiscusComment.Type.ACCOUNT_LINKING) {
                    accountLinkingClick(qiscusComment);
                } else if (qiscusComment.getType() == QiscusComment.Type.CONTACT) {
                    addToPhoneContact(qiscusComment.getContact());
                } else if (qiscusComment.getType() == QiscusComment.Type.LOCATION) {
                    openMap(qiscusComment.getLocation());
                } else if (qiscusComment.getType() == QiscusComment.Type.CUSTOM) {
                    onCustomCommentClick(qiscusComment);
                }
            } else if (qiscusComment.getState() == QiscusComment.STATE_FAILED) {
                showFailedCommentDialog(qiscusComment);
            } else if (qiscusComment.getState() == QiscusComment.STATE_SENDING
                    || qiscusComment.getState() == QiscusComment.STATE_PENDING) {
                showPendingCommentDialog(qiscusComment);
            }
        } else {
            if (qiscusComment.getType() == QiscusComment.Type.TEXT
                    || qiscusComment.getType() == QiscusComment.Type.LINK
                    || qiscusComment.getType() == QiscusComment.Type.IMAGE
                    || qiscusComment.getType() == QiscusComment.Type.AUDIO
                    || qiscusComment.getType() == QiscusComment.Type.VIDEO
                    || qiscusComment.getType() == QiscusComment.Type.FILE
                    || qiscusComment.getType() == QiscusComment.Type.REPLY
                    || qiscusComment.getType() == QiscusComment.Type.CONTACT
                    || qiscusComment.getType() == QiscusComment.Type.LOCATION) {
                toggleSelectComment(qiscusComment);
            } else if (qiscusComment.getType() == QiscusComment.Type.CUSTOM) {
                onCustomCommentLongClick(qiscusComment);
            }
        }
    }

    protected void onCustomCommentLongClick(QiscusComment qiscusComment) {

    }

    protected void onCustomCommentClick(QiscusComment qiscusComment) {

    }

    protected void openMap(QiscusLocation location) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(location.getMapUrl()));
        startActivity(intent);
    }

    protected void addToPhoneContact(QiscusContact contact) {
        String type = ContactsContract.Intents.Insert.PHONE;
        if ("email".equals(contact.getType())) {
            type = ContactsContract.Intents.Insert.EMAIL;
        }
        String finalType = type;
        AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                .setMessage(R.string.qiscus_add_contact_confirmation)
                .setPositiveButton(R.string.qiscus_new_contact, (dialog, which) -> {
                    Intent intent = new Intent(Intent.ACTION_INSERT);
                    intent.setType(ContactsContract.Contacts.CONTENT_TYPE);
                    intent.putExtra(ContactsContract.Intents.Insert.NAME, contact.getName());
                    intent.putExtra(finalType, contact.getValue());
                    startActivity(intent);
                    dialog.dismiss();
                })
                .setNegativeButton(R.string.qiscus_existing_contact, (dialog, which) -> {
                    Intent intent = new Intent(Intent.ACTION_INSERT_OR_EDIT);
                    intent.setType(ContactsContract.Contacts.CONTENT_ITEM_TYPE);
                    intent.putExtra(ContactsContract.Intents.Insert.NAME, contact.getName());
                    intent.putExtra(finalType, contact.getValue());
                    startActivity(intent);
                    dialog.dismiss();
                })
                .setCancelable(true)
                .create();

        alertDialog.setOnShowListener(dialog -> {
            @ColorInt int accent = ContextCompat.getColor(getActivity(), chatConfig.getAccentColor());
            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(accent);
            alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(accent);
        });

        alertDialog.show();
    }

    protected void accountLinkingClick(QiscusComment qiscusComment) {
        try {
            JSONObject payload = QiscusRawDataExtractor.getPayload(qiscusComment);
            JSONObject params = payload.getJSONObject("params");
            startActivity(QiscusAccountLinkingActivity.generateIntent(getActivity(), params.optString("view_title"),
                    payload.getString("url"), payload.getString("redirect_url"), params.optString("success_message")));
        } catch (JSONException e) {
            QiscusErrorLogger.print(e);
        }
    }

    protected void showPendingCommentDialog(QiscusComment qiscusComment) {
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.qiscus_sending_message_dialog_title)
                .setItems(new CharSequence[]{getString(R.string.qiscus_delete)},
                        (dialog, which) -> qiscusChatPresenter.deleteComment(qiscusComment))
                .setCancelable(true)
                .create()
                .show();
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
                || qiscusComment.getType() == QiscusComment.Type.VIDEO
                || qiscusComment.getType() == QiscusComment.Type.FILE
                || qiscusComment.getType() == QiscusComment.Type.REPLY
                || qiscusComment.getType() == QiscusComment.Type.CONTACT
                || qiscusComment.getType() == QiscusComment.Type.LOCATION)) {
            toggleSelectComment(qiscusComment);
        }
    }

    protected void toggleSelectComment(QiscusComment qiscusComment) {
        qiscusComment.setSelected(!qiscusComment.isSelected());
        chatAdapter.notifyDataSetChanged();
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

    protected void highlightComment(QiscusComment qiscusComment) {
        qiscusComment.setHighlighted(true);
        chatAdapter.notifyDataSetChanged();
        commentHighlightTask = () -> {
            qiscusComment.setHighlighted(false);
            chatAdapter.notifyDataSetChanged();
        };
        QiscusAndroidUtil.runOnUIThread(commentHighlightTask, 2000);
    }

    protected void onMessageEditTextChanged(CharSequence message) {
        if (QiscusTextUtil.isBlank(message.toString())) {
            if (!fieldMessageEmpty) {
                fieldMessageEmpty = true;
                sendButton.startAnimation(animation);
                sendButton.setImageResource(chatConfig.getShowAttachmentPanelIcon());
            }
        } else {
            if (fieldMessageEmpty) {
                fieldMessageEmpty = false;
                sendButton.startAnimation(animation);
                sendButton.setImageResource(chatConfig.getSendButtonIcon());
            }
        }
    }

    private void notifyServerTyping(boolean typing) {
        if (!qiscusChatRoom.isChannel()) {
            QiscusPusherApi.getInstance().setUserTyping(qiscusChatRoom.getId(), typing);
        }
    }

    public void sendQiscusComment(QiscusComment qiscusComment) {
        qiscusChatPresenter.resendComment(qiscusComment);
    }

    public void sendMessage(String message) {
        message = message.trim();
        if (!message.isEmpty()) {
            if (replyPreviewView != null) {
                QiscusComment repliedComment = replyPreviewView.getOriginComment();
                if (repliedComment != null) {
                    qiscusChatPresenter.sendReplyComment(message, repliedComment);
                    messageEditText.setText("");
                    replyPreviewView.close();
                    return;
                }
            }
            qiscusChatPresenter.sendComment(message);
            messageEditText.setText("");
        }
    }

    public void sendFile(File file) {
        qiscusChatPresenter.sendFile(file);
    }

    public void sendFiles(List<File> files) {
        for (File file : files) {
            sendFile(file);
        }
    }

    public void sendFile(File file, String caption) {
        qiscusChatPresenter.sendFile(file, caption);
    }

    public void sendContact(QiscusContact contact) {
        qiscusChatPresenter.sendContact(contact);
    }

    public void sendLocation(QiscusLocation location) {
        qiscusChatPresenter.sendLocation(location);
    }

    protected void addImage() {
        if (QiscusPermissionsUtil.hasPermissions(getActivity(), FILE_PERMISSION)) {
            new JupukBuilder().setMaxCount(10)
                    .enableVideoPicker(true)
                    .setColorPrimary(ContextCompat.getColor(getActivity(), chatConfig.getAppBarColor()))
                    .setColorPrimaryDark(ContextCompat.getColor(getActivity(), chatConfig.getStatusBarColor()))
                    .setColorAccent(ContextCompat.getColor(getActivity(), chatConfig.getAccentColor()))
                    .pickPhoto(this);
            hideAttachmentPanel();
        } else {
            requestAddFilePermission();
        }
    }

    protected void takeImage() {
        if (QiscusPermissionsUtil.hasPermissions(getActivity(), CAMERA_PERMISSION)) {
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
            requestCameraPermission();
        }
    }

    protected void addFile() {
        if (QiscusPermissionsUtil.hasPermissions(getActivity(), FILE_PERMISSION)) {
            new JupukBuilder().setMaxCount(1)
                    .setColorPrimary(ContextCompat.getColor(getActivity(), chatConfig.getAppBarColor()))
                    .setColorPrimaryDark(ContextCompat.getColor(getActivity(), chatConfig.getStatusBarColor()))
                    .setColorAccent(ContextCompat.getColor(getActivity(), chatConfig.getAccentColor()))
                    .pickDoc(this);
            hideAttachmentPanel();
        } else {
            requestAddFilePermission();
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
            requestRecordAudioPermission();
        }
    }

    protected void addContact() {
        Intent contactPickerIntent = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
        startActivityForResult(contactPickerIntent, PICK_CONTACT_REQUEST);
        hideAttachmentPanel();
    }

    protected void addLocation() {
        if (QiscusPermissionsUtil.hasPermissions(getActivity(), LOCATION_PERMISSION)) {
            PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
            try {
                startActivityForResult(builder.build(getActivity()), PICK_LOCATION_REQUEST);
            } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
                e.printStackTrace();
            }
            hideAttachmentPanel();
        } else {
            requestAddLocationPermission();
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

    protected void updateMentionSuggestionData() {
        if (mentionSuggestionView != null && qiscusChatRoom.isGroup()
                && Qiscus.getChatConfig().getMentionConfig().isEnableMention()) {
            mentionSuggestionView.setRoomMembers(qiscusChatRoom.getMember());
        }
        if (replyPreviewView != null) {
            replyPreviewView.updateMember(qiscusChatRoom.getMember());
        }
    }

    @Override
    public void initRoomData(QiscusChatRoom qiscusChatRoom, List<QiscusComment> comments) {
        this.qiscusChatRoom = qiscusChatRoom;
        if (roomChangedListener != null) {
            roomChangedListener.onRoomUpdated(qiscusChatRoom);
        }
        chatAdapter.setQiscusChatRoom(qiscusChatRoom);
        updateMentionSuggestionData();
        showComments(comments);
        resolveScrollToComment();
    }

    @Override
    public void onRoomChanged(QiscusChatRoom qiscusChatRoom) {
        this.qiscusChatRoom = qiscusChatRoom;
        if (roomChangedListener != null) {
            roomChangedListener.onRoomUpdated(qiscusChatRoom);
        }
    }

    @Override
    public void showComments(List<QiscusComment> qiscusComments) {
        if (!qiscusComments.isEmpty()) {
            chatAdapter.mergeLocalAndRemoteData(qiscusComments);
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
        chatAdapter.notifyDataSetChanged();
    }

    @Override
    public void refreshComment(QiscusComment qiscusComment) {
        chatAdapter.update(qiscusComment);
    }

    @Override
    public void notifyDataChanged() {
        chatAdapter.notifyDataSetChanged();
    }

    @Override
    public void updateLastDeliveredComment(long lastDeliveredCommentId) {
        chatAdapter.updateLastDeliveredComment(lastDeliveredCommentId);
    }

    @Override
    public void updateLastReadComment(long lastReadCommentId) {
        chatAdapter.updateLastReadComment(lastReadCommentId);
    }

    @Override
    public void showCommentsAndScrollToTop(List<QiscusComment> qiscusComments) {
        if (!qiscusComments.isEmpty()) {
            chatAdapter.addOrUpdate(qiscusComments);
            messageRecyclerView.scrollToPosition(chatAdapter.getItemCount() - 1);
            highlightComment((QiscusComment) chatAdapter.getData().get(chatAdapter.getItemCount() - 1));
        }
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

    protected void scrollToComment(QiscusComment comment) {
        int position = chatAdapter.findPosition(comment);
        if (position >= 0) {
            messageRecyclerView.scrollToPosition(position);
            highlightComment((QiscusComment) chatAdapter.getData().get(position));
        } else {
            qiscusChatPresenter.loadUntilComment(comment);
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
        startActivityForResult(QiscusPhotoViewerActivity.generateIntent(getActivity(), qiscusComment),
                SHOW_MEDIA_DETAIL);
    }

    @Override
    public void onTopOffListMessage() {
        loadMoreComments();
    }

    @Override
    public void onMiddleOffListMessage() {
        if (goToBottomButton != null && goToBottomButton.getVisibility() == View.GONE) {
            goToBottomButton.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onBottomOffListMessage() {
        if (newMessageButton != null) {
            newMessageButton.setVisibility(View.GONE);
        }
        if (goToBottomButton != null && goToBottomButton.getVisibility() == View.VISIBLE) {
            goToBottomButton.setVisibility(View.GONE);
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
    public void showDeleteLoading() {
        try {
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
        if (requestCode == JupukConst.REQUEST_CODE_PHOTO && resultCode == Activity.RESULT_OK) {
            if (data == null) {
                showError(getString(R.string.qiscus_chat_error_failed_open_picture));
                return;
            }
            ArrayList<String> paths = data.getStringArrayListExtra(JupukConst.KEY_SELECTED_MEDIA);
            if (paths.size() > 0) {
                List<QiscusPhoto> qiscusPhotos = new ArrayList<>(paths.size());
                for (String path : paths) {
                    qiscusPhotos.add(new QiscusPhoto(new File(path)));
                }
                startActivityForResult(QiscusSendPhotoConfirmationActivity.generateIntent(getActivity(),
                        qiscusChatRoom, qiscusPhotos),
                        SEND_PICTURE_CONFIRMATION_REQUEST);
            }
        } else if (requestCode == JupukConst.REQUEST_CODE_DOC && resultCode == Activity.RESULT_OK) {
            if (data == null) {
                showError(getString(R.string.qiscus_chat_error_failed_open_file));
                return;
            }
            ArrayList<String> paths = data.getStringArrayListExtra(JupukConst.KEY_SELECTED_DOCS);
            if (paths.size() > 0) {
                sendFile(new File(paths.get(0)));
            }
        } else if (requestCode == TAKE_PICTURE_REQUEST && resultCode == Activity.RESULT_OK) {
            try {
                File imageFile = QiscusFileUtil.from(Uri.parse(QiscusCacheManager.getInstance().getLastImagePath()));
                List<QiscusPhoto> qiscusPhotos = new ArrayList<>();
                qiscusPhotos.add(new QiscusPhoto(imageFile));
                startActivityForResult(QiscusSendPhotoConfirmationActivity.generateIntent(getActivity(),
                        qiscusChatRoom, qiscusPhotos),
                        SEND_PICTURE_CONFIRMATION_REQUEST);
            } catch (Exception e) {
                showError(getString(R.string.qiscus_chat_error_failed_read_picture));
                e.printStackTrace();
            }
        } else if (requestCode == SEND_PICTURE_CONFIRMATION_REQUEST && resultCode == Activity.RESULT_OK) {
            if (data == null) {
                showError(getString(R.string.qiscus_chat_error_failed_open_picture));
                return;
            }

            Map<String, String> captions = (Map<String, String>)
                    data.getSerializableExtra(QiscusSendPhotoConfirmationActivity.EXTRA_CAPTIONS);
            List<QiscusPhoto> qiscusPhotos = data.getParcelableArrayListExtra(QiscusSendPhotoConfirmationActivity.EXTRA_QISCUS_PHOTOS);
            if (qiscusPhotos != null) {
                for (QiscusPhoto qiscusPhoto : qiscusPhotos) {
                    sendFile(qiscusPhoto.getPhotoFile(), captions.get(qiscusPhoto.getPhotoFile().getAbsolutePath()));
                }
            } else {
                showError(getString(R.string.qiscus_chat_error_failed_read_picture));
            }
        } else if (requestCode == SHOW_MEDIA_DETAIL && resultCode == Activity.RESULT_OK) {
            if (data == null) {
                return;
            }
            if (data.getBooleanExtra(QiscusPhotoViewerActivity.EXTRA_MEDIA_DELETED, false)
                    || data.getBooleanExtra(QiscusPhotoViewerActivity.EXTRA_MEDIA_UPDATED, false)) {
                chatAdapter.notifyDataSetChanged();
            }
        } else if (requestCode == PICK_CONTACT_REQUEST && resultCode == Activity.RESULT_OK) {
            if (data == null) {
                showError(getString(R.string.qiscus_chat_error_failed_read_contact));
                return;
            }
            Uri contactUri = data.getData();
            String[] projection = new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER,
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME};
            Cursor cursor = getContext().getContentResolver().query(contactUri, projection, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                int numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                String name = cursor.getString(nameIndex);
                String number = cursor.getString(numberIndex);
                sendContact(new QiscusContact(name, number, "phone"));
            }

            if (cursor != null) {
                cursor.close();
            }
        } else if (requestCode == PICK_LOCATION_REQUEST && resultCode == Activity.RESULT_OK) {
            Place place = PlacePicker.getPlace(getActivity(), data);
            QiscusLocation location = new QiscusLocation();
            location.setName(place.getName().toString());
            location.setAddress(place.getAddress().toString());
            location.setLatitude(place.getLatLng().latitude);
            location.setLongitude(place.getLatLng().longitude);
            sendLocation(location);
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
        outState.putInt(COMMENTS_LOADED_SIZE, chatAdapter.getData().size());
        outState.putParcelable(COMMENTS_LAYOUT_MANAGER, messageRecyclerView.getLayoutManager().onSaveInstanceState());
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
        notifyLatestRead();
        if (commentHighlightTask != null) {
            QiscusAndroidUtil.cancelRunOnUIThread(commentHighlightTask);
        }
        notifyServerTyping(false);
        chatAdapter.detachView();
        if (recordAudioPanel != null) {
            recordAudioPanel.cancelRecord();
        }
        qiscusChatPresenter.detachView();
    }

    protected void requestPermissions() {
        if (!Qiscus.getChatConfig().isEnableRequestPermission()) {
            return;
        }

        if (!QiscusPermissionsUtil.hasPermissions(getActivity(), PERMISSIONS)) {
            QiscusPermissionsUtil.requestPermissions(this, getString(R.string.qiscus_permission_request_title),
                    RC_PERMISSIONS, PERMISSIONS);
        }
    }

    protected void requestCameraPermission() {
        if (!QiscusPermissionsUtil.hasPermissions(getActivity(), CAMERA_PERMISSION)) {
            QiscusPermissionsUtil.requestPermissions(this, getString(R.string.qiscus_permission_request_title),
                    RC_CAMERA_PERMISSION, CAMERA_PERMISSION);
        }
    }

    protected void requestAddFilePermission() {
        if (!QiscusPermissionsUtil.hasPermissions(getActivity(), FILE_PERMISSION)) {
            QiscusPermissionsUtil.requestPermissions(this, getString(R.string.qiscus_permission_request_title),
                    RC_FILE_PERMISSION, FILE_PERMISSION);
        }
    }

    protected void requestRecordAudioPermission() {
        if (!QiscusPermissionsUtil.hasPermissions(getActivity(), AUDIO_PERMISSION)) {
            QiscusPermissionsUtil.requestPermissions(this, getString(R.string.qiscus_permission_request_title),
                    RC_AUDIO_PERMISSION, AUDIO_PERMISSION);
        }
    }

    protected void requestAddLocationPermission() {
        if (!QiscusPermissionsUtil.hasPermissions(getActivity(), LOCATION_PERMISSION)) {
            QiscusPermissionsUtil.requestPermissions(this, getString(R.string.qiscus_permission_request_title),
                    RC_LOCATION_PERMISSION, LOCATION_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        QiscusPermissionsUtil.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        switch (requestCode) {
            case RC_CAMERA_PERMISSION:
                takeImage();
                break;
            case RC_AUDIO_PERMISSION:
                recordAudio();
                break;
            case RC_FILE_PERMISSION:
                addImage();
                break;
            case RC_LOCATION_PERMISSION:
                addLocation();
                break;
        }
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

    @Override
    public void onCarouselItemClick(JSONObject payload) {
        qiscusChatPresenter.clickCarouselItem(payload);
    }

    @Override
    public void onCommentChainingBreak(QiscusComment insertedComment, QiscusComment commentBefore) {
        qiscusChatPresenter.loadCommentsAfter(commentBefore);
    }

    @Override
    public void onRealtimeStatusChanged(boolean connected) {
        if (connected) {
            QiscusComment qiscusComment = chatAdapter.getLatestSentComment();
            if (qiscusComment != null) {
                qiscusChatPresenter.loadCommentsAfter(qiscusComment);
            }
        }
    }

    /**
     * will delete all comments on adapter that have timestamp before or same with {@code timestamp}
     */
    @Override
    public void clearCommentsBefore(long timestamp) {
        chatAdapter.clearCommentsBefore(timestamp);
        if (chatAdapter.isEmpty()) {
            if (emptyChatHolder != null) {
                emptyChatHolder.setVisibility(View.VISIBLE);
            }
        } else if (emptyChatHolder != null) {
            emptyChatHolder.setVisibility(View.GONE);
        }
    }

    /**
     * Callback when an error happening while load comments
     *
     * @param throwable the error
     */
    @Override
    public void onLoadCommentsError(Throwable throwable) {

    }

    public void deleteCommentsForMe(List<QiscusComment> selectedComments) {
        qiscusChatPresenter.deleteCommentsForMe(selectedComments, chatConfig.getDeleteCommentConfig().isEnableHardDelete());
    }

    public void deleteCommentsForEveryone(List<QiscusComment> selectedComments) {
        qiscusChatPresenter.deleteCommentsForEveryone(selectedComments, chatConfig.getDeleteCommentConfig().isEnableHardDelete());
    }

    @Override
    public void onUploadIconClick(View view, int position) {
        qiscusChatPresenter.resendComment((QiscusComment) chatAdapter.getData().get(position));
    }

    public interface CommentSelectedListener {
        void onCommentSelected(List<QiscusComment> selectedComments);
    }

    public interface RoomChangedListener {
        void onRoomUpdated(QiscusChatRoom qiscusChatRoom);
    }
}
