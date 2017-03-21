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
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.qiscus.sdk.Qiscus;
import com.qiscus.sdk.R;
import com.qiscus.sdk.data.model.QiscusChatConfig;
import com.qiscus.sdk.filepicker.fragment.DocPickerFragment;
import com.qiscus.sdk.filepicker.fragment.MediaPickerFragment;

import java.util.ArrayList;

public class FilePickerActivity extends AppCompatActivity implements PickerManagerListener {
    private Toolbar toolbar;
    private TextView tvTitle;
    private int type;
    private QiscusChatConfig chatConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        chatConfig = onLoadChatConfig();

        onSetStatusBarColor();

        setContentView(R.layout.activity_qiscus_file_picker);

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
            type = intent.getIntExtra(FilePickerConst.EXTRA_PICKER_TYPE, FilePickerConst.MEDIA_PICKER);

            if (type == FilePickerConst.MEDIA_PICKER) {
                tvTitle.setText(R.string.qiscus_select_media);
            } else {
                tvTitle.setText(R.string.qiscus_select_file);
            }

            PickerManager.getInstance(this).setPickerManagerListener(this);
            openSpecificFragment(type);
        }
    }

    private void openSpecificFragment(int type) {
        if (type == FilePickerConst.MEDIA_PICKER) {
            addFragment(this, R.id.container, MediaPickerFragment.newInstance());
        } else {
            if (PickerManager.getInstance(this).isDocSupport()) {
                PickerManager.getInstance(this).addDocTypes();
            }
            addFragment(this, R.id.container, DocPickerFragment.newInstance());
        }
    }

    @Override
    public void onItemSelected(int currentCount) {

    }

    @Override
    public void onSingleItemSelected(ArrayList<String> paths) {
        returnData(paths);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setResult(RESULT_CANCELED);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case FilePickerConst.REQUEST_CODE_MEDIA_DETAIL:
                if (resultCode == RESULT_OK) {
                    returnData(type == FilePickerConst.MEDIA_PICKER ? PickerManager.getInstance(this).getSelectedPhotos()
                            : PickerManager.getInstance(this).getSelectedFiles());
                }
                break;
        }
    }

    private void returnData(ArrayList<String> paths) {
        Intent intent = new Intent();
        intent.putStringArrayListExtra(type == FilePickerConst.MEDIA_PICKER ? FilePickerConst.KEY_SELECTED_MEDIA
                : FilePickerConst.KEY_SELECTED_DOCS, paths);
        setResult(RESULT_OK, intent);
        finish();
    }

    private void addFragment(AppCompatActivity activity, int contentId, Fragment fragment) {
        FragmentTransaction transaction = activity.getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.slide_left_in, R.anim.slide_left_out);
        transaction.add(contentId, fragment, fragment.getClass().getSimpleName());
        transaction.commit();
    }
}
