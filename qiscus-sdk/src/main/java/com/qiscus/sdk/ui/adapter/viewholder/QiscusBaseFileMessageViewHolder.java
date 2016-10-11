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

package com.qiscus.sdk.ui.adapter.viewholder;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.lzyzsd.circleprogress.CircleProgress;
import com.qiscus.sdk.Qiscus;
import com.qiscus.sdk.R;
import com.qiscus.sdk.data.model.QiscusComment;
import com.qiscus.sdk.ui.adapter.OnItemClickListener;
import com.qiscus.sdk.ui.adapter.OnLongItemClickListener;

import java.io.File;

/**
 * Created on : September 27, 2016
 * Author     : zetbaitsu
 * Name       : Zetra
 * Email      : zetra@mail.ugm.ac.id
 * GitHub     : https://github.com/zetbaitsu
 * LinkedIn   : https://id.linkedin.com/in/zetbaitsu
 */
public abstract class QiscusBaseFileMessageViewHolder extends QiscusBaseMessageViewHolder<QiscusComment>
        implements QiscusComment.ProgressListener, QiscusComment.DownloadingListener {

    @NonNull protected TextView fileNameView;
    @Nullable protected TextView fileTypeView;
    @Nullable protected CircleProgress progressView;
    @Nullable protected ImageView downloadIconView;

    public QiscusBaseFileMessageViewHolder(View itemView, OnItemClickListener itemClickListener, OnLongItemClickListener longItemClickListener) {
        super(itemView, itemClickListener, longItemClickListener);
        fileNameView = getFileNameView(itemView);
        fileTypeView = getFileTypeView(itemView);
        progressView = getProgressView(itemView);
        downloadIconView = getDownloadIconView(itemView);
    }

    @NonNull
    protected abstract TextView getFileNameView(View itemView);

    @Nullable
    protected abstract TextView getFileTypeView(View itemView);

    @Nullable
    protected abstract CircleProgress getProgressView(View itemView);

    @Nullable
    protected abstract ImageView getDownloadIconView(View itemView);

    @Override
    public void bind(QiscusComment qiscusComment) {
        super.bind(qiscusComment);
        qiscusComment.setProgressListener(this);
        qiscusComment.setDownloadingListener(this);
        setUpDownloadIcon(qiscusComment);
        showProgressOrNot(qiscusComment);
    }

    protected void setUpDownloadIcon(QiscusComment qiscusComment) {
        if (downloadIconView != null) {
            if (qiscusComment.getState() == QiscusComment.STATE_FAILED || qiscusComment.getState() == QiscusComment.STATE_SENDING) {
                downloadIconView.setImageResource(R.drawable.ic_qiscus_upload);
            } else {
                downloadIconView.setImageResource(R.drawable.ic_qiscus_download);
            }
        }
    }

    protected void showProgressOrNot(QiscusComment qiscusComment) {
        if (progressView != null) {
            progressView.setProgress(qiscusComment.getProgress());
            progressView.setVisibility(qiscusComment.isDownloading() ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    protected void setUpColor() {
        if (fileTypeView != null) {
            fileTypeView.setTextColor(messageFromMe ? rightBubbleTimeColor : leftBubbleTimeColor);
        }
        if (progressView != null) {
            progressView.setFinishedColor(messageFromMe ? rightBubbleColor : leftBubbleColor);
        }
        super.setUpColor();
    }

    @Override
    protected void showMessage(QiscusComment qiscusComment) {
        File localPath = Qiscus.getDataStore().getLocalPath(qiscusComment.getId());
        if (downloadIconView != null) {
            downloadIconView.setVisibility(localPath == null ? View.VISIBLE : View.GONE);
        }
        fileNameView.setText(qiscusComment.getAttachmentName());

        if (fileTypeView != null) {
            if (qiscusComment.getExtension().isEmpty()) {
                fileTypeView.setText(R.string.unkown_type);
            } else {
                fileTypeView.setText(String.format("%s File", qiscusComment.getExtension().toUpperCase()));
            }
        }
    }

    @Override
    public void onProgress(QiscusComment qiscusComment, int percentage) {
        if (progressView != null) {
            progressView.setProgress(percentage);
        }
    }

    @Override
    public void onDownloading(QiscusComment qiscusComment, boolean downloading) {
        if (progressView != null) {
            progressView.setVisibility(downloading ? View.VISIBLE : View.GONE);
        }
    }
}
