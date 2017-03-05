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
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationManagerCompat;
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
import com.qiscus.sdk.presenter.QiscusChatPresenter;
import com.qiscus.sdk.ui.QiscusPhotoViewerActivity;
import com.qiscus.sdk.ui.adapter.QiscusBaseChatAdapter;
import com.qiscus.sdk.ui.view.QiscusAudioRecorderView;
import com.qiscus.sdk.ui.view.QiscusChatScrollListener;
import com.qiscus.sdk.ui.view.QiscusRecyclerView;
import com.qiscus.sdk.util.QiscusFileUtil;
import com.qiscus.sdk.util.QiscusImageUtil;
import com.qiscus.sdk.util.QiscusPermissionsUtil;
import com.trello.rxlifecycle.components.support.RxFragment;
import com.vanniktech.emoji.EmojiEditText;
import com.vanniktech.emoji.EmojiPopup;

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
        QiscusChatPresenter.View, QiscusAudioRecorderView.RecordListener, QiscusPermissionsUtil.PermissionCallbacks {

    protected static final int RC_PERMISSIONS = 1;
    protected static final int RC_STORAGE_PERMISSION = 2;
    protected static final int RC_RECORD_AUDIO_PERMISSION = 3;

    private static final String[] PERMISSIONS = {
            "android.permission.WRITE_EXTERNAL_STORAGE",
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.RECORD_AUDIO"
    };

    protected static final String CHAT_ROOM_DATA = "chat_room_data";
    protected static final String EXTRA_STARTING_MESSAGE = "extra_starting_message";
    protected static final String COMMENTS_DATA = "saved_comments_data";
    protected static final int TAKE_PICTURE_REQUEST = 1;
    protected static final int PICK_IMAGE_REQUEST = 2;
    protected static final int PICK_FILE_REQUEST = 3;

    @NonNull protected ViewGroup rootView;
    @Nullable protected ViewGroup emptyChatHolder;
    @NonNull protected SwipeRefreshLayout swipeRefreshLayout;
    @NonNull protected QiscusRecyclerView messageRecyclerView;
    @Nullable protected ViewGroup messageInputPanel;
    @NonNull protected EditText messageEditText;
    @NonNull protected ImageView sendButton;
    @Nullable protected View newMessageButton;
    @NonNull protected View loadMoreProgressBar;
    @Nullable protected ImageView emptyChatImageView;
    @Nullable protected TextView emptyChatTitleView;
    @Nullable protected TextView emptyChatDescView;
    @Nullable protected ImageView addImageButton;
    @Nullable protected ImageView takeImageButton;
    @Nullable protected ImageView addFileButton;
    @Nullable protected ImageView recordAudioButton;
    @Nullable protected QiscusAudioRecorderView recordAudioPanel;

    protected QiscusChatConfig chatConfig;
    protected QiscusChatRoom qiscusChatRoom;
    protected String startingMessage;
    protected T chatT;
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
        messageEditText = getMessageEditText(view);
        sendButton = getSendButton(view);
        newMessageButton = getNewMessageButton(view);
        loadMoreProgressBar = getLoadMoreProgressBar(view);
        emptyChatImageView = getEmptyChatImageView(view);
        emptyChatTitleView = getEmptyChatTitleView(view);
        emptyChatDescView = getEmptyChatDescView(view);
        addImageButton = getAddImageButton(view);
        takeImageButton = getTakeImageButton(view);
        addFileButton = getAddFileButton(view);
        recordAudioButton = getRecordAudioButton(view);
        recordAudioPanel = getRecordAudioPanel(view);

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

        sendButton.setOnClickListener(v -> sendMessage());

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
    protected abstract ImageView getAddImageButton(View view);

    @Nullable
    protected abstract ImageView getTakeImageButton(View view);

    @Nullable
    protected abstract ImageView getAddFileButton(View view);

    @Nullable
    protected abstract ImageView getRecordAudioButton(View view);

    @Nullable
    protected abstract QiscusAudioRecorderView getRecordAudioPanel(View view);

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

        onApplyChatConfig();

        swipeRefreshLayout.setOnRefreshListener(this);

        animation = onLoadAnimation();

        onClearNotification();

        qiscusAccount = Qiscus.getQiscusAccount();

        chatT = onCreateChatAdapter();
        chatT.setOnItemClickListener((view, position) ->
                onItemCommentClick((QiscusComment) chatT.getData().get(position)));
        chatT.setOnLongItemClickListener((view, position) ->
                onItemCommentLongClick((QiscusComment) chatT.getData().get(position)));
        messageRecyclerView.setUpAsBottomList();
        chatLayoutManager = (LinearLayoutManager) messageRecyclerView.getLayoutManager();
        messageRecyclerView.setAdapter(chatT);
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
            commentSelectedListener.onCommentSelected(chatT.getSelectedComments());
        }

        if (startingMessage != null && !startingMessage.isEmpty()) {
            qiscusChatPresenter.sendComment(startingMessage);
        }
    }

    protected void setupEmojiPopup() {
        if (messageEditText instanceof EmojiEditText){
            emojiPopup = EmojiPopup.Builder.fromRootView(rootView)
                    .setOnSoftKeyboardCloseListener(() -> emojiPopup.dismiss())
                    .build((EmojiEditText) messageEditText);
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

    protected void onApplyChatConfig() {
        rootView.setBackground(chatConfig.getChatRoomBackground());
        swipeRefreshLayout.setColorSchemeResources(chatConfig.getSwipeRefreshColorScheme());
        if (emptyChatImageView != null) {
            emptyChatImageView.setImageResource(chatConfig.getEmptyRoomImageResource());
        }
        if (emptyChatTitleView != null) {
            emptyChatTitleView.setText(chatConfig.getEmptyRoomTitle());
        }
        if (emptyChatDescView != null) {
            emptyChatDescView.setText(chatConfig.getEmptyRoomSubtitle());
        }
        messageEditText.setHint(chatConfig.getMessageFieldHint());
        if (addImageButton != null) {
            addImageButton.setImageResource(chatConfig.getAddPictureIcon());
        }
        if (takeImageButton != null) {
            takeImageButton.setImageResource(chatConfig.getTakePictureIcon());
        }
        if (addFileButton != null) {
            addFileButton.setImageResource(chatConfig.getAddFileIcon());
        }
        if (recordAudioButton != null) {
            recordAudioButton.setImageResource(chatConfig.getRecordAudioIcon());
        }
        if (recordAudioPanel != null) {
            recordAudioPanel.setButtonStopRecord(chatConfig.getStopRecordIcon());
            recordAudioPanel.setButtonCancelRecord(chatConfig.getCancelRecordIcon());
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
        if (chatT.getSelectedComments().isEmpty()) {
            if (qiscusComment.getState() > QiscusComment.STATE_SENDING) {
                if (qiscusComment.getType() == QiscusComment.Type.FILE
                        || qiscusComment.getType() == QiscusComment.Type.IMAGE
                        || qiscusComment.getType() == QiscusComment.Type.AUDIO) {
                    qiscusChatPresenter.downloadFile(qiscusComment);
                }
            } else if (qiscusComment.getState() == QiscusComment.STATE_FAILED) {
                showFailedCommentDialog(qiscusComment);
            }
        } else {
            toggleSelectComment(qiscusComment);
        }
    }

    protected void showFailedCommentDialog(QiscusComment qiscusComment) {
        new AlertDialog.Builder(getActivity())
                .setTitle("Message send failed")
                .setItems(new CharSequence[]{"Resend", "Delete"}, (dialog, which) -> {
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
        if (chatT.getSelectedComments().isEmpty()) {
            toggleSelectComment(qiscusComment);
        }
    }

    protected void toggleSelectComment(QiscusComment qiscusComment) {
        qiscusComment.setSelected(!qiscusComment.isSelected());
        refreshComment(qiscusComment);
        if (commentSelectedListener != null) {
            commentSelectedListener.onCommentSelected(chatT.getSelectedComments());
        }
    }

    public List<QiscusComment> getSelectedComments() {
        return chatT.getSelectedComments();
    }

    public void clearSelectedComments() {
        chatT.clearSelectedComments();
    }

    protected void onMessageEditTextChanged(CharSequence message) {
        if (message == null || message.toString().trim().isEmpty()) {
            if (!fieldMessageEmpty) {
                fieldMessageEmpty = true;
                sendButton.startAnimation(animation);
                sendButton.setImageResource(chatConfig.getSendInactiveIcon());
                QiscusPusherApi.getInstance().setUserTyping(qiscusChatRoom.getId(), qiscusChatRoom.getLastTopicId(), false);
            }
        } else {
            if (fieldMessageEmpty) {
                fieldMessageEmpty = false;
                sendButton.startAnimation(animation);
                sendButton.setImageResource(chatConfig.getSendActiveIcon());
                QiscusPusherApi.getInstance().setUserTyping(qiscusChatRoom.getId(), qiscusChatRoom.getLastTopicId(), true);
            }
        }
    }

    protected void sendMessage() {
        String message = messageEditText.getText().toString().trim();
        if (!message.isEmpty()) {
            qiscusChatPresenter.sendComment(message);
            messageEditText.setText("");
        }
    }

    protected void sendFile(File file) {
        qiscusChatPresenter.sendFile(file);
    }

    protected void addImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    protected void takeImage() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = QiscusImageUtil.createImageFile();
            } catch (IOException ex) {
                showError(getString(R.string.chat_error_failed_write));
            }

            if (photoFile != null) {
                intent.putExtra(MediaStore.EXTRA_OUTPUT,
                        FileProvider.getUriForFile(getActivity(), Qiscus.getProviderAuthorities(), photoFile));
                startActivityForResult(intent, TAKE_PICTURE_REQUEST);
            }
        }
    }

    protected void addFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        startActivityForResult(intent, PICK_FILE_REQUEST);
    }

    protected void recordAudio() {
        if (recordAudioPanel != null) {
            recordAudioPanel.setVisibility(View.VISIBLE);
            if (messageInputPanel != null) {
                messageInputPanel.setVisibility(View.GONE);
            }
            try {
                recordAudioPanel.startRecord();
            } catch (IOException e) {
                e.printStackTrace();
                showError("Failed to record audio!");
                recordAudioPanel.cancelRecord();
            } catch (IllegalStateException e) {
                showError("Can not record audio, microphone may be in use!");
                recordAudioPanel.cancelRecord();
            }
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
            chatT.addOrUpdate(qiscusComments);
        }
        if (chatT.isEmpty() && qiscusComments.isEmpty()) {
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
        chatT.addOrUpdate(qiscusComments);
        if (chatT.isEmpty() && qiscusComments.isEmpty()) {
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
        chatT.addOrUpdate(qiscusComment);
        scrollToBottom();
        if (emptyChatHolder != null) {
            emptyChatHolder.setVisibility(View.GONE);
        }
    }

    @Override
    public void onSuccessSendComment(QiscusComment qiscusComment) {
        chatT.addOrUpdate(qiscusComment);
    }

    @Override
    public void onFailedSendComment(QiscusComment qiscusComment) {
        chatT.addOrUpdate(qiscusComment);
    }

    @Override
    public void onNewComment(QiscusComment qiscusComment) {
        chatT.addOrUpdate(qiscusComment);
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
        chatT.remove(qiscusComment);
    }

    @Override
    public void refreshComment(QiscusComment qiscusComment) {
        chatT.addOrUpdate(qiscusComment);
    }

    @Override
    public void updateLastDeliveredComment(int lastDeliveredCommentId) {
        chatT.updateLastDeliveredComment(lastDeliveredCommentId);
    }

    @Override
    public void updateLastReadComment(int lastReadCommentId) {
        chatT.updateLastReadComment(lastReadCommentId);
    }

    private boolean shouldShowNewMessageButton() {
        return chatLayoutManager.findFirstVisibleItemPosition() > 2;
    }

    private void loadMoreComments() {
        if (loadMoreProgressBar.getVisibility() == View.GONE && chatT.getItemCount() > 0) {
            QiscusComment qiscusComment = (QiscusComment) chatT.getData().get(chatT.getItemCount() - 1);
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
        intent.setDataAndType(FileProvider.getUriForFile(getActivity(), Qiscus.getProviderAuthorities(), file), mimeType);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            showError(getString(R.string.chat_error_no_handler));
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
        if (chatT.getData().size() > 0) {
            loadMoreComments();
            swipeRefreshLayout.setRefreshing(false);
        } else {
            qiscusChatPresenter.loadComments(20);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            if (data == null) {
                showError(getString(R.string.chat_error_failed_open_picture));
                return;
            }
            try {
                sendFile(QiscusFileUtil.from(data.getData()));
            } catch (IOException e) {
                showError(getString(R.string.chat_error_failed_read_picture));
                e.printStackTrace();
            }
        } else if (requestCode == PICK_FILE_REQUEST && resultCode == Activity.RESULT_OK) {
            if (data == null) {
                showError(getString(R.string.chat_error_failed_open_file));
                return;
            }
            try {
                sendFile(QiscusFileUtil.from(data.getData()));
            } catch (IOException e) {
                showError(getString(R.string.chat_error_failed_read_file));
                e.printStackTrace();
            }
        } else if (requestCode == TAKE_PICTURE_REQUEST && resultCode == Activity.RESULT_OK) {
            try {
                sendFile(QiscusFileUtil.from(Uri.parse(QiscusCacheManager.getInstance().getLastImagePath())));
            } catch (Exception e) {
                showError(getString(R.string.chat_error_failed_read_picture));
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onStartRecord() {

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
        int size = chatT.getData().size();
        for (int i = 0; i < size; i++) {
            comments.add((QiscusComment) chatT.getData().get(i));
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
        chatT.detachView();
        if (recordAudioPanel != null) {
            recordAudioPanel.cancelRecord();
        }
        qiscusChatPresenter.detachView();
    }

    protected void requestPermissions() {
        if (!QiscusPermissionsUtil.hasPermissions(getActivity(), PERMISSIONS)) {
            QiscusPermissionsUtil.requestPermissions(this, "Please grant permissions to make apps working properly!",
                    RC_PERMISSIONS, PERMISSIONS);
        }
    }

    protected void requestStoragePermission() {
        if (!QiscusPermissionsUtil.hasPermissions(getActivity(), PERMISSIONS[0], PERMISSIONS[1])) {
            QiscusPermissionsUtil.requestPermissions(this, "To make this apps working properly we " +
                            "need to access external storage to save your chatting data. " +
                            "So please allow the apps to access the storage!",
                    RC_STORAGE_PERMISSION, PERMISSIONS[0], PERMISSIONS[1]);
        }
    }

    protected void requestAudioRecordPermission() {
        if (!QiscusPermissionsUtil.hasPermissions(getActivity(), PERMISSIONS[3])) {
            QiscusPermissionsUtil.requestPermissions(this, "We need your permission to record audio to able send audio message!",
                    RC_RECORD_AUDIO_PERMISSION, PERMISSIONS[3]);
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
        QiscusPermissionsUtil.checkDeniedPermissionsNeverAskAgain(this,
                "Please grant permissions to make apps working properly!", R.string.ok, R.string.cancel, perms);
    }

    public interface CommentSelectedListener {
        void onCommentSelected(List<QiscusComment> selectedComments);
    }

    public interface RoomChangedListener {
        void onRoomUpdated(QiscusChatRoom qiscusChatRoom);
    }
}
