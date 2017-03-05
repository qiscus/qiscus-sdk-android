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
import android.widget.ImageButton;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.qiscus.sdk.Qiscus;
import com.qiscus.sdk.R;
import com.qiscus.sdk.data.model.QiscusComment;
import com.qiscus.sdk.ui.view.TouchImageView;
import com.qiscus.sdk.util.QiscusDateUtil;
import com.trello.rxlifecycle.components.support.RxAppCompatActivity;

import java.io.File;

/**
 * Created on : March 04, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
public class QiscusPhotoViewerActivity extends RxAppCompatActivity {
    private static final String EXTRA_COMMENT = "extra_comment";

    public static Intent generateIntent(Context context, QiscusComment qiscusComment) {
        Intent intent = new Intent(context, QiscusPhotoViewerActivity.class);
        intent.putExtra(EXTRA_COMMENT, qiscusComment);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qiscus_photo_viewer);

        TouchImageView imageView = (TouchImageView) findViewById(R.id.image_view);
        TextView senderName = (TextView) findViewById(R.id.sender_name);
        TextView date = (TextView) findViewById(R.id.date);
        ImageButton shareButton = (ImageButton) findViewById(R.id.action_share);

        QiscusComment qiscusComment = getIntent().getParcelableExtra(EXTRA_COMMENT);
        File imageFile = Qiscus.getDataStore().getLocalPath(qiscusComment.getId());
        Glide.with(this)
                .load(imageFile)
                .into(imageView);

        senderName.setText(qiscusComment.getSender());
        date.setText(QiscusDateUtil.toFullDateFormat(qiscusComment.getTime()));
        shareButton.setOnClickListener(v -> shareImage(imageFile));

    }

    private void shareImage(File imageFile) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("image/jpg");
        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(imageFile));
        startActivity(Intent.createChooser(intent, "Share"));
    }
}
