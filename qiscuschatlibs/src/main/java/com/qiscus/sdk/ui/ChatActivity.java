package com.qiscus.sdk.ui;

import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.qiscus.sdk.Qiscus;
import com.qiscus.library.chat.R;
import com.qiscus.library.chat.R2;
import com.qiscus.sdk.data.local.CacheManager;
import com.qiscus.sdk.data.model.QiscusAccount;
import com.qiscus.sdk.data.model.QiscusChatRoom;
import com.qiscus.sdk.data.model.QiscusComment;
import com.qiscus.sdk.presenter.ChatPresenter;
import com.qiscus.sdk.ui.adapter.ChatAdapter;
import com.qiscus.sdk.ui.view.BaseRecyclerView;
import com.qiscus.sdk.ui.view.ChatScrollListener;
import com.qiscus.sdk.util.AndroidUtilities;
import com.qiscus.sdk.util.FileUtil;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnTextChanged;

public class ChatActivity extends BaseActivity implements ChatPresenter.View,
        SwipeRefreshLayout.OnRefreshListener, ChatScrollListener.Listener {
    private static final String CHAT_ROOM_DATA = "chat_room_data";
    private static final int TAKE_PICTURE_REQUEST = 1;
    private static final int PICK_IMAGE_REQUEST = 2;
    private static final int PICK_FILE_REQUEST = 3;

    @BindView(R2.id.tv_name)
    TextView tvName;
    @BindView(R2.id.empty_chat)
    LinearLayout emptyChat;
    @BindView(R2.id.swipe_layout)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R2.id.list_message)
    BaseRecyclerView listMessage;
    @BindView(R2.id.field_message)
    EditText fieldMessage;
    @BindView(R2.id.button_send)
    ImageView buttonSend;
    @BindView(R2.id.button_new_message)
    View buttonNewMessage;
    @BindView(R2.id.progressBar)
    ProgressBar progressBar;

    private QiscusChatRoom qiscusChatRoom;
    private ChatAdapter adapter;
    private ChatPresenter chatPresenter;
    private LinearLayoutManager chatLayoutManager;
    private QiscusAccount qiscusAccount;
    private Animation animation;
    private boolean fieldMessageEmpty = true;

    public static Intent generateIntent(Context context, QiscusChatRoom qiscusChatRoom) {
        Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtra(CHAT_ROOM_DATA, qiscusChatRoom);
        return intent;
    }

    @Override
    protected int getResourceLayout() {
        return R.layout.activity_chat;
    }

    @Override
    protected void onViewReady(Bundle savedInstanceState) {
        resolveChatRoom(savedInstanceState);

        requestStoragePermission();

        swipeRefreshLayout.setColorSchemeResources(R.color.primary, R.color.accent);
        swipeRefreshLayout.setOnRefreshListener(this);

        animation = AnimationUtils.loadAnimation(this, R.anim.simple_grow);

        CacheManager.getInstance().clearMessageNotifItems(qiscusChatRoom.getId());
        NotificationManagerCompat.from(this).cancel(qiscusChatRoom.getId());

        qiscusAccount = Qiscus.getQiscusAccount();

        tvName.setText(qiscusChatRoom.getName());

        adapter = new ChatAdapter(this);
        adapter.setOnItemClickListener((view, position) -> onItemCommentClick(adapter.getData().get(position)));
        adapter.setOnLongItemClickListener((view, position) -> onItemCommentLongClick(adapter.getData().get(position)));
        listMessage.setUpAsBottomList();
        chatLayoutManager = (LinearLayoutManager) listMessage.getLayoutManager();
        listMessage.setAdapter(adapter);
        listMessage.addOnScrollListener(new ChatScrollListener(chatLayoutManager, this));

        chatPresenter = new ChatPresenter(this, qiscusChatRoom);
        new Handler().postDelayed(() -> chatPresenter.loadComments(20), 400);
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

    private void onItemCommentClick(QiscusComment qiscusComment) {
        if (qiscusComment.getState() == QiscusComment.STATE_ON_QISCUS || qiscusComment.getState() == QiscusComment.STATE_ON_PUSHER) {
            if (qiscusComment.getType() == QiscusComment.Type.FILE || qiscusComment.getType() == QiscusComment.Type.IMAGE) {
                chatPresenter.downloadFile(qiscusComment);
            }
        } else if (qiscusComment.getState() == QiscusComment.STATE_FAILED) {
            chatPresenter.resendComment(qiscusComment);
        }
    }

    private void onItemCommentLongClick(QiscusComment qiscusComment) {
        if (qiscusComment.getType() == QiscusComment.Type.TEXT) {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText(AndroidUtilities.getString(R.string.chat_activity_label_clipboard), qiscusComment.getMessage());
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, AndroidUtilities.getString(R.string.chat_activity_copied_message), Toast.LENGTH_SHORT).show();
        }
    }

    @OnTextChanged(R2.id.field_message)
    public void onFieldMessageChanged(CharSequence message) {
        if (message == null || message.toString().trim().isEmpty()) {
            if (!fieldMessageEmpty) {
                fieldMessageEmpty = true;
                buttonSend.startAnimation(animation);
                buttonSend.setImageResource(R.drawable.ic_send_off);
            }
        } else {
            if (fieldMessageEmpty) {
                fieldMessageEmpty = false;
                buttonSend.startAnimation(animation);
                buttonSend.setImageResource(R.drawable.ic_send_on);
            }
        }
    }

    @OnClick(R2.id.button_send)
    public void sendMessage() {
        String message = fieldMessage.getText().toString().trim();
        if (!message.isEmpty()) {
            chatPresenter.sendComment(message);
            fieldMessage.setText("");
        }
    }

    @OnClick(R2.id.button_add_image)
    public void addImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @OnClick(R2.id.button_pick_picture)
    public void takePicture() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                showError(AndroidUtilities.getString(R.string.chat_error_failed_write));
            }

            if (photoFile != null) {
                intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                startActivityForResult(intent, TAKE_PICTURE_REQUEST);
            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        CacheManager.getInstance().cacheLastImagePath("file:" + image.getAbsolutePath());
        return image;
    }

    @OnClick(R2.id.button_add_file)
    public void addFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        startActivityForResult(intent, PICK_FILE_REQUEST);
    }

    @Override
    public void showComments(List<QiscusComment> qiscusComments) {
        adapter.refreshWithData(qiscusComments);
        if (adapter.isEmpty() && qiscusComments.isEmpty()) {
            emptyChat.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onLoadMore(List<QiscusComment> qiscusComments) {
        adapter.addOrUpdate(qiscusComments);
    }

    @Override
    public void onSendingComment(QiscusComment qiscusComment) {
        adapter.addOrUpdate(qiscusComment);
        scrollToBottom();
        emptyChat.setVisibility(View.GONE);
    }

    @Override
    public void onSuccessSendComment(QiscusComment qiscusComment) {
        adapter.addOrUpdate(qiscusComment);
    }

    @Override
    public void onFailedSendComment(QiscusComment qiscusComment) {
        adapter.addOrUpdate(qiscusComment);
    }

    @Override
    public void onNewComment(QiscusComment qiscusComment) {
        adapter.addOrUpdate(qiscusComment);
        if (!qiscusComment.getSenderEmail().equalsIgnoreCase(qiscusAccount.getEmail()) && shouldShowNewMessageButton()) {
            if (buttonNewMessage.getVisibility() == View.GONE) {
                buttonNewMessage.setVisibility(View.VISIBLE);
                buttonNewMessage.startAnimation(animation);
            }
        } else {
            scrollToBottom();
        }
        emptyChat.setVisibility(View.GONE);
    }

    @Override
    public void refreshComment(QiscusComment qiscusComment) {
        adapter.addOrUpdate(qiscusComment);
    }

    private boolean shouldShowNewMessageButton() {
        return chatLayoutManager.findFirstVisibleItemPosition() > 2;
    }

    private void loadMoreComments() {
        if (progressBar.getVisibility() == View.GONE && adapter.getItemCount() > 0) {
            QiscusComment qiscusComment = adapter.getData().get(adapter.getItemCount() - 1);
            if (qiscusComment.getCommentBeforeId() > 0) {
                chatPresenter.loadOlderCommentThan(qiscusComment);
            }
        }
    }

    @OnClick(R2.id.button_new_message)
    public void scrollToBottom() {
        listMessage.smoothScrollToPosition(0);
        buttonNewMessage.setVisibility(View.GONE);
    }

    @Override
    public void onFileDownloaded(File file, String mimeType) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(file), mimeType);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            showError(AndroidUtilities.getString(R.string.chat_error_no_handler));
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
        buttonNewMessage.setVisibility(View.GONE);
    }

    @Override
    public void showError(String errorMessage) {
        Toast.makeText(ChatActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
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
            progressBar.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void dismissLoading() {
        try {
            swipeRefreshLayout.setRefreshing(false);
            progressBar.setVisibility(View.GONE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRefresh() {
        if (adapter.getData().size() > 0) {
            loadMoreComments();
            swipeRefreshLayout.setRefreshing(false);
        } else {
            chatPresenter.loadComments(20);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK) {
            if (data == null) {
                showError(AndroidUtilities.getString(R.string.chat_error_failed_open_picture));
                return;
            }
            try {
                chatPresenter.sendFile(FileUtil.from(data.getData()));
            } catch (IOException e) {
                showError(AndroidUtilities.getString(R.string.chat_error_failed_read_picture));
                e.printStackTrace();
            }
        } else if (requestCode == PICK_FILE_REQUEST && resultCode == RESULT_OK) {
            if (data == null) {
                showError(AndroidUtilities.getString(R.string.chat_error_failed_open_file));
                return;
            }
            try {
                chatPresenter.sendFile(FileUtil.from(data.getData()));
            } catch (IOException e) {
                showError(AndroidUtilities.getString(R.string.chat_error_failed_read_file));
                e.printStackTrace();
            }
        } else if (requestCode == TAKE_PICTURE_REQUEST && resultCode == RESULT_OK) {
            try {
                chatPresenter.sendFile(FileUtil.from(Uri.parse(CacheManager.getInstance().getLastImagePath())));
            } catch (Exception e) {
                showError(AndroidUtilities.getString(R.string.chat_error_failed_read_picture));
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(CHAT_ROOM_DATA, qiscusChatRoom);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        chatPresenter.detachView();
    }
}
