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

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.util.Pair;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.qiscus.sdk.R;
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

    private QiscusComment qiscusComment;
    private int position = -1;
    private List<Pair<QiscusComment, File>> qiscusPhotos;

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

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        tvTitle = (TextView) findViewById(R.id.tv_title);
        viewPager = (ViewPager) findViewById(R.id.view_pager);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        senderName = (TextView) findViewById(R.id.sender_name);
        date = (TextView) findViewById(R.id.date);
        ImageButton shareButton = (ImageButton) findViewById(R.id.action_share);
        infoPanel = findViewById(R.id.info_panel);
        fadein = AnimationUtils.loadAnimation(this, R.anim.fadein);
        fadeout = AnimationUtils.loadAnimation(this, R.anim.fadeout);

        findViewById(R.id.back).setOnClickListener(v -> onBackPressed());
        setSupportActionBar(toolbar);
        viewPager.addOnPageChangeListener(this);

        resolveData(savedInstanceState);

        QiscusPhotoViewerPresenter presenter = new QiscusPhotoViewerPresenter(this);
        presenter.loadQiscusPhotos(qiscusComment.getTopicId());

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

    private void shareImage(File imageFile) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("image/jpg");
        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(imageFile));
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
        finish();
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

    private void initPhotos() {
        List<QiscusPhotoFragment> fragments = new ArrayList<>();
        for (int i = 0; i < qiscusPhotos.size(); i++) {
            Pair<QiscusComment, File> qiscusPhoto = qiscusPhotos.get(i);
            fragments.add(QiscusPhotoFragment.newInstance(qiscusPhoto.second));
            if (position == -1 && qiscusPhoto.first.equals(qiscusComment)) {
                position = i;
            }
        }
        viewPager.setAdapter(new QiscusPhotoPagerAdapter(getSupportFragmentManager(), fragments));
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
}
