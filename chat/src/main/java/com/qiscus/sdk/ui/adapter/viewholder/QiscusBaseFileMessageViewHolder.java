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
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.qiscus.sdk.Qiscus;
import com.qiscus.sdk.R;
import com.qiscus.sdk.chat.core.data.model.QiscusComment;
import com.qiscus.sdk.chat.core.util.QiscusTextUtil;
import com.qiscus.sdk.ui.adapter.OnItemClickListener;
import com.qiscus.sdk.ui.adapter.OnLongItemClickListener;
import com.qiscus.sdk.ui.adapter.OnUploadIconClickListener;
import com.qiscus.sdk.ui.view.QiscusProgressView;

import java.io.File;

/**
 * Created on : September 27, 2016
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
public abstract class QiscusBaseFileMessageViewHolder extends QiscusBaseMessageViewHolder<QiscusComment>
        implements QiscusComment.ProgressListener, QiscusComment.DownloadingListener {

    @NonNull
    protected TextView fileNameView;
    @Nullable
    protected TextView fileTypeView;
    @Nullable
    protected QiscusProgressView progressView;
    @Nullable
    protected ImageView downloadIconView;

    protected int rightProgressFinishedColor;
    protected int leftProgressFinishedColor;

    protected QiscusComment qiscusComment;

    public QiscusBaseFileMessageViewHolder(View itemView, OnItemClickListener itemClickListener,
                                           OnLongItemClickListener longItemClickListener) {
        this(itemView, itemClickListener, longItemClickListener, null);
    }

    public QiscusBaseFileMessageViewHolder(View itemView, OnItemClickListener itemClickListener,
                                           OnLongItemClickListener longItemClickListener,
                                           OnUploadIconClickListener uploadIconClickListener) {
        super(itemView, itemClickListener, longItemClickListener);
        fileNameView = getFileNameView(itemView);
        fileTypeView = getFileTypeView(itemView);
        progressView = getProgressView(itemView);
        downloadIconView = getDownloadIconView(itemView);
        if (downloadIconView != null) {
            downloadIconView.setOnClickListener(v -> {
                if (uploadIconClickListener != null && qiscusComment.getState() == QiscusComment.STATE_FAILED) {
                    uploadIconClickListener.onUploadIconClick(v, getAdapterPosition());
                } else {
                    onClick(messageBubbleView);
                }
            });
        }
    }

    @Override
    protected void loadChatConfig() {
        super.loadChatConfig();
        rightProgressFinishedColor = ContextCompat.getColor(Qiscus.getApps(), Qiscus.getChatConfig().getRightProgressFinishedColor());
        leftProgressFinishedColor = ContextCompat.getColor(Qiscus.getApps(), Qiscus.getChatConfig().getLeftProgressFinishedColor());
    }

    @NonNull
    protected abstract TextView getFileNameView(View itemView);

    @Nullable
    protected abstract TextView getFileTypeView(View itemView);

    @Nullable
    protected abstract QiscusProgressView getProgressView(View itemView);

    @Nullable
    protected abstract ImageView getDownloadIconView(View itemView);

    @Override
    public void bind(QiscusComment qiscusComment) {
        super.bind(qiscusComment);
        this.qiscusComment = qiscusComment;
        qiscusComment.setProgressListener(this);
        qiscusComment.setDownloadingListener(this);
        setUpDownloadIcon(qiscusComment);
        showProgressOrNot(qiscusComment);
    }

    protected void setUpDownloadIcon(QiscusComment qiscusComment) {
        if (downloadIconView != null) {
            if (qiscusComment.getState() <= QiscusComment.STATE_SENDING) {
                downloadIconView.setImageResource(R.drawable.ic_qiscus_upload);
            } else {
                downloadIconView.setImageResource(R.drawable.ic_qiscus_download);
            }
        }
    }

    protected void showProgressOrNot(QiscusComment qiscusComment) {
        if (progressView != null) {
            progressView.setProgress(qiscusComment.getProgress());
            progressView.setVisibility(
                    qiscusComment.isDownloading()
                            || qiscusComment.getState() == QiscusComment.STATE_PENDING
                            || qiscusComment.getState() == QiscusComment.STATE_SENDING
                            ? View.VISIBLE : View.GONE
            );
        }
    }

    @Override
    protected void setUpColor() {
        if (fileTypeView != null) {
            fileTypeView.setTextColor(messageFromMe ? rightBubbleTimeColor : leftBubbleTimeColor);
        }
        if (progressView != null) {
            progressView.setFinishedColor(messageFromMe ? rightProgressFinishedColor : leftProgressFinishedColor);
        }
        super.setUpColor();
    }

    @Override
    protected void showMessage(QiscusComment qiscusComment) {
        if (downloadIconView != null) {
            File localPath = Qiscus.getDataStore().getLocalPath(qiscusComment.getId());
            if (localPath == null) {
                File file = new File(qiscusComment.getAttachmentUri().toString());
                if (file.exists()) {
                    localPath = file;
                }
            }
            downloadIconView.setVisibility(localPath == null ? View.VISIBLE : View.GONE);
        }

        fileNameView.setText(qiscusComment.getAttachmentName());

        if (fileTypeView != null) {
            if (qiscusComment.getExtension().isEmpty()) {
                fileTypeView.setText(R.string.qiscus_unknown_type);
            } else {
                fileTypeView.setText(QiscusTextUtil.getString(R.string.qiscus_file_type,
                        qiscusComment.getExtension().toUpperCase()));
            }
        }
    }

    @Override
    public void onProgress(QiscusComment qiscusComment, int percentage) {
        if (qiscusComment.equals(this.qiscusComment) && progressView != null) {
            progressView.setProgress(percentage);
        }
    }

    @Override
    public void onDownloading(QiscusComment qiscusComment, boolean downloading) {
        if (qiscusComment.equals(this.qiscusComment) && progressView != null) {
            progressView.setVisibility(downloading ? View.VISIBLE : View.GONE);
        }
    }
}
