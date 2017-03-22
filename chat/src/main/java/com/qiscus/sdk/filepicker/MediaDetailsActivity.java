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

package com.qiscus.sdk.filepicker;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.qiscus.sdk.Qiscus;
import com.qiscus.sdk.R;
import com.qiscus.sdk.data.model.QiscusChatConfig;
import com.qiscus.sdk.filepicker.adapter.PhotoGridAdapter;
import com.qiscus.sdk.filepicker.model.Media;
import com.qiscus.sdk.filepicker.model.PhotoDirectory;
import com.qiscus.sdk.filepicker.util.MediaStoreHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MediaDetailsActivity extends AppCompatActivity implements PickerManagerListener {
    protected Toolbar toolbar;
    protected TextView tvTitle;
    private QiscusChatConfig chatConfig;
    private RecyclerView recyclerView;
    private TextView emptyView;
    private PhotoGridAdapter photoGridAdapter;
    private int fileType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        chatConfig = onLoadChatConfig();
        onSetStatusBarColor();

        setContentView(R.layout.activity_qiscus_media_details);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        tvTitle = (TextView) findViewById(R.id.tv_title);
        findViewById(R.id.back).setOnClickListener(v -> onBackPressed());
        setSupportActionBar(toolbar);

        applyChatConfig();

        initView();
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

    protected void applyChatConfig() {
        toolbar.setBackgroundResource(chatConfig.getAppBarColor());
        tvTitle.setTextColor(ContextCompat.getColor(this, chatConfig.getTitleColor()));
    }

    private void initView() {
        Intent intent = getIntent();
        if (intent != null) {
            fileType = intent.getIntExtra(FilePickerConst.EXTRA_FILE_TYPE, FilePickerConst.MEDIA_TYPE_IMAGE);
            PhotoDirectory photoDirectory = intent.getParcelableExtra(PhotoDirectory.class.getSimpleName());
            if (photoDirectory != null) {
                setUpView(photoDirectory);
                PickerManager.getInstance().setPickerManagerListener(this);
            }
        }
    }

    private void setUpView(PhotoDirectory photoDirectory) {
        recyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        emptyView = (TextView) findViewById(R.id.empty_view);

        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(3, OrientationHelper.VERTICAL);
        layoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        getDataFromMedia(photoDirectory.getBucketId());
    }

    private void getDataFromMedia(String bucketId) {
        Bundle mediaStoreArgs = new Bundle();
        mediaStoreArgs.putBoolean(FilePickerConst.EXTRA_SHOW_GIF, false);
        mediaStoreArgs.putString(FilePickerConst.EXTRA_BUCKET_ID, bucketId);

        mediaStoreArgs.putInt(FilePickerConst.EXTRA_FILE_TYPE, fileType);

        if (fileType == FilePickerConst.MEDIA_TYPE_IMAGE) {
            MediaStoreHelper.getPhotoDirs(this, mediaStoreArgs, this::updateList);
        } else if (fileType == FilePickerConst.MEDIA_TYPE_VIDEO) {
            MediaStoreHelper.getVideoDirs(this, mediaStoreArgs, this::updateList);
        }
    }

    private void updateList(List<PhotoDirectory> dirs) {
        ArrayList<Media> medias = new ArrayList<>();
        for (int i = 0; i < dirs.size(); i++) {
            medias.addAll(dirs.get(i).getMedias());
        }

        Collections.sort(medias, (a, b) -> b.getId() - a.getId());

        if (medias.size() > 0) {
            emptyView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        } else {
            emptyView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            return;
        }

        if (photoGridAdapter != null) {
            photoGridAdapter.setData(medias);
            photoGridAdapter.notifyDataSetChanged();
        } else {
            photoGridAdapter = new PhotoGridAdapter(this, medias, PickerManager.getInstance().getSelectedPhotos());
            recyclerView.setAdapter(photoGridAdapter);
        }

    }

    @Override
    public void onItemSelected(int currentCount) {

    }

    @Override
    public void onSingleItemSelected(ArrayList<String> paths) {
        setResult(RESULT_OK, null);
        finish();
    }
}
