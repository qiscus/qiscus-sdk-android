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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.qiscus.jupuk.JupukBuilder;
import com.qiscus.jupuk.JupukConst;
import com.qiscus.nirmana.Nirmana;
import com.qiscus.sdk.Qiscus;
import com.qiscus.sdk.R;
import com.qiscus.sdk.data.model.QiscusChatConfig;
import com.qiscus.sdk.data.model.QiscusPhoto;
import com.qiscus.sdk.ui.adapter.QiscusPhotoAdapter;
import com.qiscus.sdk.ui.adapter.QiscusPhotoPagerAdapter;
import com.qiscus.sdk.ui.fragment.QiscusPhotoFragment;
import com.qiscus.sdk.ui.view.QiscusCircularImageView;
import com.trello.rxlifecycle.components.support.RxAppCompatActivity;
import com.vanniktech.emoji.EmojiEditText;
import com.vanniktech.emoji.EmojiPopup;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created on : June 14, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
public class QiscusSendPhotoConfirmationActivity extends RxAppCompatActivity implements ViewPager.OnPageChangeListener {
    private static final String EXTRA_ROOM_NAME = "room_name";
    private static final String EXTRA_ROOM_AVATAR = "room_avatar";
    public static final String EXTRA_QISCUS_PHOTOS = "qiscus_photos";
    public static final String EXTRA_CAPTION = "caption";

    private ViewGroup rootView;
    private EditText messageEditText;

    private ViewPager viewPager;
    private List<QiscusPhoto> qiscusPhotos;
    private int position = -1;

    private ImageView toggleEmojiButton;
    private EmojiPopup emojiPopup;

    private RecyclerView recyclerView;
    private QiscusPhotoAdapter photoAdapter;

    private QiscusChatConfig chatConfig;

    public static Intent generateIntent(Context context, String roomName, String roomAvatar, List<QiscusPhoto> qiscusPhotos) {
        Intent intent = new Intent(context, QiscusSendPhotoConfirmationActivity.class);
        intent.putExtra(EXTRA_ROOM_NAME, roomName);
        intent.putExtra(EXTRA_ROOM_AVATAR, roomAvatar);
        intent.putParcelableArrayListExtra(EXTRA_QISCUS_PHOTOS, (ArrayList<QiscusPhoto>) qiscusPhotos);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qiscus_send_photo_confirmation);

        chatConfig = Qiscus.getChatConfig();
        rootView = (ViewGroup) findViewById(R.id.root_view);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView tvTitle = (TextView) findViewById(R.id.tv_title);
        ImageView ivAvatar = (QiscusCircularImageView) findViewById(R.id.profile_picture);
        findViewById(R.id.back).setOnClickListener(v -> onBackPressed());
        setSupportActionBar(toolbar);

        tvTitle.setText(getIntent().getStringExtra(EXTRA_ROOM_NAME));
        Nirmana.getInstance().get().load(getIntent().getStringExtra(EXTRA_ROOM_AVATAR))
                .error(R.drawable.ic_qiscus_avatar)
                .placeholder(R.drawable.ic_qiscus_avatar)
                .dontAnimate()
                .into(ivAvatar);

        viewPager = (ViewPager) findViewById(R.id.view_pager);
        messageEditText = (EditText) findViewById(R.id.field_message);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        photoAdapter = new QiscusPhotoAdapter(this);
        photoAdapter.setOnItemClickListener((view, position) -> {
            updateRecycleViewPosition(position);
            viewPager.setCurrentItem(position);
        });
        recyclerView.setAdapter(photoAdapter);

        messageEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                confirm();
                return true;
            }
            return false;
        });

        toggleEmojiButton = (ImageView) findViewById(R.id.button_add_emoticon);
        toggleEmojiButton.setOnClickListener(v -> toggleEmoji());

        setupEmojiPopup();

        messageEditText.setOnClickListener(v -> {
            if (emojiPopup != null && emojiPopup.isShowing()) {
                toggleEmoji();
            }
        });

        viewPager.addOnPageChangeListener(this);

        qiscusPhotos = getIntent().getParcelableArrayListExtra(EXTRA_QISCUS_PHOTOS);
        if (qiscusPhotos != null) {
            initPhotos();
        } else {
            finish();
            return;
        }

        findViewById(R.id.button_send).setOnClickListener(v -> confirm());
    }

    private void confirm() {
        dismissEmoji();
        String caption = messageEditText.getText().toString();
        Intent data = new Intent();
        data.putParcelableArrayListExtra(EXTRA_QISCUS_PHOTOS, (ArrayList<QiscusPhoto>) qiscusPhotos);
        data.putExtra(EXTRA_CAPTION, caption);
        setResult(RESULT_OK, data);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.send_photo_action, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == R.id.action_add_images) {
            new JupukBuilder().enableVideoPicker(true)
                    .setColorPrimary(ContextCompat.getColor(this, chatConfig.getAppBarColor()))
                    .setColorPrimaryDark(ContextCompat.getColor(this, chatConfig.getStatusBarColor()))
                    .setColorAccent(ContextCompat.getColor(this, chatConfig.getAccentColor()))
                    .pickPhoto(this);
        }
        return super.onOptionsItemSelected(item);
    }

    private void initPhotos() {
        List<QiscusPhotoFragment> fragments = new ArrayList<>();
        for (int i = 0; i < qiscusPhotos.size(); i++) {
            QiscusPhoto qiscusPhoto = qiscusPhotos.get(i);
            fragments.add(QiscusPhotoFragment.newInstance(qiscusPhoto.getPhotoFile()));
        }
        if (position == -1) {
            position = 0;
        }
        QiscusPhotoPagerAdapter pagerAdapter = new QiscusPhotoPagerAdapter(getSupportFragmentManager(), fragments);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setCurrentItem(position);
        photoAdapter.refreshWithData(qiscusPhotos);
        recyclerView.setVisibility(qiscusPhotos.size() > 1 ? View.VISIBLE : View.GONE);
        updateRecycleViewPosition(position);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        this.position = position;
        updateRecycleViewPosition(position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    private void updateRecycleViewPosition(int position) {
        photoAdapter.updateSelected(position);
        recyclerView.smoothScrollToPosition(position);
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
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
                this.qiscusPhotos = qiscusPhotos;
                getIntent().putParcelableArrayListExtra(EXTRA_QISCUS_PHOTOS, (ArrayList<QiscusPhoto>) this.qiscusPhotos);
                position = 0;
                initPhotos();
            }
        }
    }

    public void showError(String errorMessage) {
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
    }
}
