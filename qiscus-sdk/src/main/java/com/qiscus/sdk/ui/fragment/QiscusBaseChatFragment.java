package com.qiscus.sdk.ui.fragment;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.widget.SwipeRefreshLayout;
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
import com.qiscus.sdk.R2;
import com.qiscus.sdk.data.local.QiscusCacheManager;
import com.qiscus.sdk.data.model.QiscusAccount;
import com.qiscus.sdk.data.model.QiscusChatConfig;
import com.qiscus.sdk.data.model.QiscusChatRoom;
import com.qiscus.sdk.data.model.QiscusComment;
import com.qiscus.sdk.presenter.QiscusChatPresenter;
import com.qiscus.sdk.ui.adapter.QiscusBaseChatAdapter;
import com.qiscus.sdk.ui.view.QiscusChatScrollListener;
import com.qiscus.sdk.ui.view.QiscusRecyclerView;
import com.qiscus.sdk.util.QiscusFileUtil;
import com.qiscus.sdk.util.QiscusImageUtil;
import com.trello.rxlifecycle.components.support.RxFragment;

import java.io.File;
import java.io.IOException;
import java.util.List;

import butterknife.OnClick;

/**
 * Created on : September 28, 2016
 * Author     : zetbaitsu
 * Name       : Zetra
 * Email      : zetra@mail.ugm.ac.id
 * GitHub     : https://github.com/zetbaitsu
 * LinkedIn   : https://id.linkedin.com/in/zetbaitsu
 */
public abstract class QiscusBaseChatFragment<Adapter extends QiscusBaseChatAdapter> extends RxFragment
        implements SwipeRefreshLayout.OnRefreshListener, QiscusChatScrollListener.Listener, QiscusChatPresenter.View {
    protected static final String CHAT_ROOM_DATA = "chat_room_data";
    protected static final int TAKE_PICTURE_REQUEST = 1;
    protected static final int PICK_IMAGE_REQUEST = 2;
    protected static final int PICK_FILE_REQUEST = 3;

    @Nullable protected ViewGroup emptyChatHolder;
    @NonNull protected SwipeRefreshLayout swipeRefreshLayout;
    @NonNull protected QiscusRecyclerView messageRecyclerView;
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

    protected QiscusChatConfig chatConfig;
    protected QiscusChatRoom qiscusChatRoom;
    protected Adapter chatAdapter;
    protected QiscusChatPresenter qiscusChatPresenter;
    protected Animation animation;
    protected LinearLayoutManager chatLayoutManager;
    private QiscusAccount qiscusAccount;
    private boolean fieldMessageEmpty = true;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(getResourceLayout(), container, false);
        onLoadView(view);
        return view;
    }

    protected abstract int getResourceLayout();

    protected void onLoadView(View view) {
        emptyChatHolder = getEmptyChatHolder(view);
        swipeRefreshLayout = getSwipeRefreshLayout(view);
        messageRecyclerView = getMessageRecyclerView(view);
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

        if (addImageButton != null) {
            addImageButton.setOnClickListener(v -> addImage());
        }
        if (takeImageButton != null) {
            takeImageButton.setOnClickListener(v -> takeImage());
        }
        if (addFileButton != null) {
            addFileButton.setOnClickListener(v -> addFile());
        }
    }

    @Nullable
    protected abstract ViewGroup getEmptyChatHolder(View view);

    @NonNull
    protected abstract SwipeRefreshLayout getSwipeRefreshLayout(View view);

    @NonNull
    protected abstract QiscusRecyclerView getMessageRecyclerView(View view);

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

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        onCreateChatComponents(savedInstanceState);
    }

    protected void onCreateChatComponents(Bundle savedInstanceState) {
        chatConfig = onLoadChatConfig();

        resolveChatRoom(savedInstanceState);

        onApplyChatConfig();

        swipeRefreshLayout.setOnRefreshListener(this);

        animation = onLoadAnimation();

        onClearNotification();

        qiscusAccount = Qiscus.getQiscusAccount();

        chatAdapter = onCreateChatAdapter();
        chatAdapter.setOnItemClickListener((view, position) -> onItemCommentClick((QiscusComment) chatAdapter.getData().get(position)));
        chatAdapter.setOnLongItemClickListener((view, position) -> onItemCommentLongClick((QiscusComment) chatAdapter.getData().get(position)));
        messageRecyclerView.setUpAsBottomList();
        chatLayoutManager = (LinearLayoutManager) messageRecyclerView.getLayoutManager();
        messageRecyclerView.setAdapter(chatAdapter);
        messageRecyclerView.addOnScrollListener(new QiscusChatScrollListener(chatLayoutManager, this));

        qiscusChatPresenter = new QiscusChatPresenter(this, qiscusChatRoom);
        new Handler().postDelayed(() -> qiscusChatPresenter.loadComments(20), 400);
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

    protected void onApplyChatConfig() {
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
    }

    protected Animation onLoadAnimation() {
        return AnimationUtils.loadAnimation(getActivity(), R.anim.qiscus_simple_grow);
    }

    protected void onClearNotification() {
        NotificationManagerCompat.from(getActivity()).cancel(qiscusChatRoom.getId());
        QiscusCacheManager.getInstance().clearMessageNotifItems(qiscusChatRoom.getId());
    }

    protected abstract Adapter onCreateChatAdapter();

    protected void onItemCommentClick(QiscusComment qiscusComment) {
        if (qiscusComment.getState() == QiscusComment.STATE_ON_QISCUS || qiscusComment.getState() == QiscusComment.STATE_ON_PUSHER) {
            if (qiscusComment.getType() == QiscusComment.Type.FILE || qiscusComment.getType() == QiscusComment.Type.IMAGE) {
                qiscusChatPresenter.downloadFile(qiscusComment);
            }
        } else if (qiscusComment.getState() == QiscusComment.STATE_FAILED) {
            qiscusChatPresenter.resendComment(qiscusComment);
        }
    }

    protected void onItemCommentLongClick(QiscusComment qiscusComment) {
        if (qiscusComment.getType() == QiscusComment.Type.TEXT) {
            ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText(getString(R.string.chat_activity_label_clipboard), qiscusComment.getMessage());
            clipboard.setPrimaryClip(clip);
            Toast.makeText(getActivity(), getString(R.string.chat_activity_copied_message), Toast.LENGTH_SHORT).show();
        }
    }

    protected void onMessageEditTextChanged(CharSequence message) {
        if (message == null || message.toString().trim().isEmpty()) {
            if (!fieldMessageEmpty) {
                fieldMessageEmpty = true;
                sendButton.startAnimation(animation);
                sendButton.setImageResource(chatConfig.getSendInactiveIcon());
            }
        } else {
            if (fieldMessageEmpty) {
                fieldMessageEmpty = false;
                sendButton.startAnimation(animation);
                sendButton.setImageResource(chatConfig.getSendActiveIcon());
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
                intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                startActivityForResult(intent, TAKE_PICTURE_REQUEST);
            }
        }
    }

    protected void addFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        startActivityForResult(intent, PICK_FILE_REQUEST);
    }

    @Override
    public void showComments(List<QiscusComment> qiscusComments) {
        chatAdapter.refreshWithData(qiscusComments);
        if (chatAdapter.isEmpty() && qiscusComments.isEmpty()) {
            if (emptyChatHolder != null) {
                emptyChatHolder.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onLoadMore(List<QiscusComment> qiscusComments) {
        chatAdapter.addOrUpdate(qiscusComments);
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
    public void refreshComment(QiscusComment qiscusComment) {
        chatAdapter.addOrUpdate(qiscusComment);
    }

    private boolean shouldShowNewMessageButton() {
        return chatLayoutManager.findFirstVisibleItemPosition() > 2;
    }

    private void loadMoreComments() {
        if (loadMoreProgressBar.getVisibility() == View.GONE && chatAdapter.getItemCount() > 0) {
            QiscusComment qiscusComment = (QiscusComment) chatAdapter.getData().get(chatAdapter.getItemCount() - 1);
            if (qiscusComment.getCommentBeforeId() > 0) {
                qiscusChatPresenter.loadOlderCommentThan(qiscusComment);
            }
        }
    }

    @OnClick(R2.id.button_new_message)
    public void scrollToBottom() {
        messageRecyclerView.smoothScrollToPosition(0);
        if (newMessageButton != null) {
            newMessageButton.setVisibility(View.GONE);
        }
    }

    @Override
    public void onFileDownloaded(File file, String mimeType) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(file), mimeType);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            showError(getString(R.string.chat_error_no_handler));
        }
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
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            if (data == null) {
                showError(getString(R.string.chat_error_failed_open_picture));
                return;
            }
            try {
                qiscusChatPresenter.sendFile(QiscusFileUtil.from(data.getData()));
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
                qiscusChatPresenter.sendFile(QiscusFileUtil.from(data.getData()));
            } catch (IOException e) {
                showError(getString(R.string.chat_error_failed_read_file));
                e.printStackTrace();
            }
        } else if (requestCode == TAKE_PICTURE_REQUEST && resultCode == Activity.RESULT_OK) {
            try {
                qiscusChatPresenter.sendFile(QiscusFileUtil.from(Uri.parse(QiscusCacheManager.getInstance().getLastImagePath())));
            } catch (Exception e) {
                showError(getString(R.string.chat_error_failed_read_picture));
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(CHAT_ROOM_DATA, qiscusChatRoom);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        qiscusChatPresenter.detachView();
    }
}
