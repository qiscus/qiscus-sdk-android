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

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.FileProvider;
import android.support.v4.util.Pair;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.qiscus.sdk.Qiscus;
import com.qiscus.sdk.R;
import com.qiscus.sdk.data.model.ForwardCommentHandler;
import com.qiscus.sdk.data.model.QiscusComment;
import com.qiscus.sdk.presenter.QiscusPhotoViewerPresenter;
import com.qiscus.sdk.ui.adapter.QiscusPhotoPagerAdapter;
import com.qiscus.sdk.ui.fragment.QiscusPhotoFragment;
import com.qiscus.sdk.util.QiscusDateUtil;
import com.trello.rxlifecycle.components.support.RxAppCompatActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created on : March 04, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
public class QiscusPhotoViewerActivity extends RxAppCompatActivity implements QiscusPhotoViewerPresenter.View,
        ViewPager.OnPageChangeListener, QiscusPhotoFragment.ClickListener {
    public static final String EXTRA_MEDIA_DELETED = "extra_media_deleted";
    public static final String EXTRA_MEDIA_UPDATED = "extra_media_updated";

    private static final String EXTRA_COMMENT = "extra_comment";
    private static final String KEY_POSITION = "last_position";

    private Toolbar toolbar;
    private TextView tvTitle;
    private ViewPager viewPager;
    private ProgressBar progressBar;
    private TextView senderName;
    private TextView date;
    private View infoPanel;
    private Animation fadein, fadeout;
    private ProgressDialog progressDialog;

    private QiscusComment qiscusComment;
    private int position = -1;
    private List<Pair<QiscusComment, File>> qiscusPhotos;
    private QiscusPhotoPagerAdapter adapter;

    private boolean mediaDeleted;
    private boolean mediaUpdated;

    private QiscusPhotoViewerPresenter presenter;

    private QiscusComment ongoingDownload;

    public static Intent generateIntent(Context context, QiscusComment qiscusComment) {
        Intent intent = new Intent(context, QiscusPhotoViewerActivity.class);
        intent.putExtra(EXTRA_COMMENT, qiscusComment);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_qiscus_photo_viewer);

        presenter = new QiscusPhotoViewerPresenter(this);

        toolbar = findViewById(R.id.toolbar);
        tvTitle = findViewById(R.id.tv_title);
        viewPager = findViewById(R.id.view_pager);
        progressBar = findViewById(R.id.progress_bar);
        senderName = findViewById(R.id.sender_name);
        date = findViewById(R.id.date);
        ImageButton shareButton = findViewById(R.id.action_share);
        infoPanel = findViewById(R.id.info_panel);
        fadein = AnimationUtils.loadAnimation(this, R.anim.qiscus_fadein);
        fadeout = AnimationUtils.loadAnimation(this, R.anim.qiscus_fadeout);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.qiscus_downloading));
        progressDialog.setMax(100);
        progressDialog.setIndeterminate(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setOnCancelListener(dialog -> {
            presenter.cancelDownloading();
            showError(getString(R.string.qiscus_redownload_canceled));
            if (ongoingDownload != null) {
                ongoingDownload.setDownloadingListener(null);
                ongoingDownload.setProgressListener(null);
            }
        });

        findViewById(R.id.back).setOnClickListener(v -> onBackPressed());
        setSupportActionBar(toolbar);
        viewPager.addOnPageChangeListener(this);

        resolveData(savedInstanceState);

        presenter.loadQiscusPhotos(qiscusComment.getRoomId());

        if (!Qiscus.getChatConfig().isEnableShareMedia()) {
            shareButton.setVisibility(View.GONE);
        }

        shareButton.setOnClickListener(v -> {
            Pair<QiscusComment, File> qiscusPhoto = qiscusPhotos.get(position);
            shareImage(qiscusPhoto.second);
        });
    }

    private void resolveData(Bundle savedInstanceState) {
        qiscusComment = getIntent().getParcelableExtra(EXTRA_COMMENT);
        if (qiscusComment == null && savedInstanceState != null) {
            qiscusComment = savedInstanceState.getParcelable(EXTRA_COMMENT);
        }

        if (qiscusComment == null) {
            finish();
            return;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.media_action, menu);
        menu.findItem(R.id.action_forward)
                .setVisible(Qiscus.getChatConfig().isEnableForwardComment());
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == R.id.action_redownload) {
            Pair<QiscusComment, File> qiscusPhoto = qiscusPhotos.get(position);
            ongoingDownload = qiscusPhoto.first;
            ongoingDownload.setDownloadingListener((qiscusComment, downloading) -> {
                if (qiscusComment.equals(ongoingDownload) && !downloading) {
                    mediaUpdated = true;
                    progressDialog.dismiss();
                    ongoingDownload.setDownloadingListener(null);
                    ongoingDownload.setProgressListener(null);
                }
            });
            ongoingDownload.setProgressListener((qiscusComment, percentage) -> {
                if (qiscusComment.equals(ongoingDownload)) {
                    progressDialog.setProgress(percentage);
                }
            });
            progressDialog.show();
            progressDialog.setProgress(0);
            presenter.downloadFile(ongoingDownload);
        } else if (i == R.id.action_delete) {
            Pair<QiscusComment, File> qiscusPhoto = qiscusPhotos.get(position);
            if (qiscusPhoto.second.delete()) {
                mediaDeleted = true;
                if (qiscusPhotos.size() == 1) {
                    onBackPressed();
                } else {
                    qiscusPhotos.remove(position);
                    adapter.getFragments().remove(position);
                    adapter.notifyDataSetChanged();
                    bindInfo();
                }
            } else {
                showError(getString(R.string.qiscus_error_can_not_delete_file));
            }
        } else if (i == R.id.action_forward) {
            ForwardCommentHandler forwardCommentHandler = Qiscus.getChatConfig().getForwardCommentHandler();
            if (forwardCommentHandler == null) {
                throw new NullPointerException("Please set forward handler before.\n" +
                        "Set it using this method Qiscus.getChatConfig().setForwardCommentHandler()");
            }
            Pair<QiscusComment, File> qiscusPhoto = qiscusPhotos.get(position);
            List<QiscusComment> comments = new ArrayList<>();
            comments.add(qiscusPhoto.first);
            forwardCommentHandler.forward(comments);
        }
        return super.onOptionsItemSelected(item);
    }

    private void shareImage(File imageFile) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("image/jpg");
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(imageFile));
        } else {
            intent.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(this, Qiscus.getProviderAuthorities(), imageFile));
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
        startActivity(Intent.createChooser(intent, getString(R.string.qiscus_share_image_title)));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(EXTRA_COMMENT, qiscusComment);
        outState.putInt(KEY_POSITION, position);
    }

    @Override
    public void showError(String errorMessage) {
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void dismissLoading() {
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onLoadQiscusPhotos(List<Pair<QiscusComment, File>> qiscusPhotos) {
        this.qiscusPhotos = qiscusPhotos;
        initPhotos();
    }

    @Override
    public void onFileDownloaded(Pair<QiscusComment, File> qiscusPhoto) {
        for (int i = 0; i < qiscusPhotos.size(); i++) {
            Pair<QiscusComment, File> qiscusPhoto1 = qiscusPhotos.get(i);
            if (qiscusPhoto.first.equals(qiscusPhoto1.first)) {
                adapter.getFragments().set(i, QiscusPhotoFragment.newInstance(qiscusPhoto.second));
                adapter.notifyDataSetChanged();
                bindInfo();
            }
        }
    }

    @Override
    public void closePage() {
        finish();
    }

    private void initPhotos() {
        List<QiscusPhotoFragment> fragments = new ArrayList<>();
        for (int i = 0; i < qiscusPhotos.size(); i++) {
            Pair<QiscusComment, File> qiscusPhoto = qiscusPhotos.get(i);
            fragments.add(QiscusPhotoFragment.newInstance(qiscusPhoto.second));
            if (position == -1 && qiscusPhoto.first.equals(qiscusComment)) {
                position = i;
            }
        }
        adapter = new QiscusPhotoPagerAdapter(getSupportFragmentManager(), fragments);
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(position);
        bindInfo();
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        this.position = position;
        bindInfo();
    }

    private void bindInfo() {
        Pair<QiscusComment, File> qiscusPhoto = qiscusPhotos.get(position);
        senderName.setText(qiscusPhoto.first.getSender());
        date.setText(QiscusDateUtil.toFullDateFormat(qiscusPhoto.first.getTime()));
        tvTitle.setText(getString(R.string.qiscus_photo_viewer_title, (position + 1), qiscusPhotos.size()));
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onPhotoClick() {
        if (infoPanel.getVisibility() == View.VISIBLE) {
            infoPanel.startAnimation(fadeout);
            toolbar.startAnimation(fadeout);
            infoPanel.setVisibility(View.GONE);
            toolbar.setVisibility(View.GONE);
        } else {
            infoPanel.startAnimation(fadein);
            toolbar.startAnimation(fadein);
            infoPanel.setVisibility(View.VISIBLE);
            toolbar.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onBackPressed() {
        if (mediaDeleted || mediaUpdated) {
            Intent data = new Intent();
            data.putExtra(EXTRA_MEDIA_DELETED, mediaDeleted);
            data.putExtra(EXTRA_MEDIA_UPDATED, mediaUpdated);
            setResult(RESULT_OK, data);
        }
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.detachView();
    }
}
