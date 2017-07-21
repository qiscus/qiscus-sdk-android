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
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;
import android.widget.TextView;

import com.qiscus.nirmana.Nirmana;
import com.qiscus.sdk.R;
import com.qiscus.sdk.ui.fragment.QiscusPhotoFragment;
import com.qiscus.sdk.ui.view.QiscusCircularImageView;
import com.trello.rxlifecycle.components.support.RxAppCompatActivity;

import java.io.File;

/**
 * Created on : June 14, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
public class QiscusSendPhotoConfirmationActivity extends RxAppCompatActivity {
    private static final String EXTRA_ROOM_NAME = "room_name";
    private static final String EXTRA_ROOM_AVATAR = "room_avatar";
    public static final String EXTRA_IMAGE_FILE = "image_file";


    public static Intent generateIntent(Context context, String roomName, String roomAvatar, File imageFile) {
        Intent intent = new Intent(context, QiscusSendPhotoConfirmationActivity.class);
        intent.putExtra(EXTRA_ROOM_NAME, roomName);
        intent.putExtra(EXTRA_ROOM_AVATAR, roomAvatar);
        intent.putExtra(EXTRA_IMAGE_FILE, imageFile);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qiscus_send_photo_confirmation);

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

        File imageFile = (File) getIntent().getSerializableExtra(EXTRA_IMAGE_FILE);
        if (imageFile != null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, QiscusPhotoFragment.newInstance(imageFile),
                            QiscusPhotoFragment.class.getSimpleName())
                    .commit();
        } else {
            finish();
            return;
        }

        findViewById(R.id.cancel).setOnClickListener(v -> finish());
        findViewById(R.id.submit).setOnClickListener(v -> {
            Intent data = new Intent();
            data.putExtra(EXTRA_IMAGE_FILE, imageFile);
            setResult(RESULT_OK, data);
            finish();
        });
    }
}
