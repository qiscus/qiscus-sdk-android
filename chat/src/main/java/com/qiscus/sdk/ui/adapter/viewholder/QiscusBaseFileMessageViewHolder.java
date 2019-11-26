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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.qiscus.sdk.Qiscus;
import com.qiscus.sdk.R;
import com.qiscus.sdk.chat.core.data.model.QMessage;
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
public abstract class QiscusBaseFileMessageViewHolder extends QiscusBaseMessageViewHolder<QMessage>
        implements QMessage.ProgressListener, QMessage.DownloadingListener {

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

    protected QMessage qiscusMessage;

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
                if (uploadIconClickListener != null && qiscusMessage.getState() == QMessage.STATE_FAILED) {
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
    public void bind(QMessage qiscusMessage) {
        super.bind(qiscusMessage);
        this.qiscusMessage = qiscusMessage;
        qiscusMessage.setProgressListener(this);
        qiscusMessage.setDownloadingListener(this);
        setUpDownloadIcon(qiscusMessage);
        showProgressOrNot(qiscusMessage);
    }

    protected void setUpDownloadIcon(QMessage qiscusMessage) {
        if (downloadIconView != null) {
            if (qiscusMessage.getState() <= QMessage.STATE_SENDING) {
                downloadIconView.setImageResource(R.drawable.ic_qiscus_upload);
            } else {
                downloadIconView.setImageResource(R.drawable.ic_qiscus_download);
            }
        }
    }

    protected void showProgressOrNot(QMessage qiscusMessage) {
        if (progressView != null) {
            progressView.setProgress(qiscusMessage.getProgress());
            progressView.setVisibility(
                    qiscusMessage.isDownloading()
                            || qiscusMessage.getState() == QMessage.STATE_PENDING
                            || qiscusMessage.getState() == QMessage.STATE_SENDING
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
    protected void showMessage(QMessage qiscusMessage) {
        if (downloadIconView != null) {
            File localPath = Qiscus.getDataStore().getLocalPath(qiscusMessage.getId());
            if (localPath == null) {
                File file = new File(qiscusMessage.getAttachmentUri().toString());
                if (file.exists()) {
                    localPath = file;
                }
            }
            downloadIconView.setVisibility(localPath == null ? View.VISIBLE : View.GONE);
        }

        fileNameView.setText(qiscusMessage.getAttachmentName());

        if (fileTypeView != null) {
            if (qiscusMessage.getExtension().isEmpty()) {
                fileTypeView.setText(R.string.qiscus_unknown_type);
            } else {
                fileTypeView.setText(QiscusTextUtil.getString(R.string.qiscus_file_type,
                        qiscusMessage.getExtension().toUpperCase()));
            }
        }
    }

    @Override
    public void onProgress(QMessage qiscusMessage, int percentage) {
        if (qiscusMessage.equals(this.qiscusMessage) && progressView != null) {
            progressView.setProgress(percentage);
        }
    }

    @Override
    public void onDownloading(QMessage qiscusMessage, boolean downloading) {
        if (qiscusMessage.equals(this.qiscusMessage) && progressView != null) {
            progressView.setVisibility(downloading ? View.VISIBLE : View.GONE);
        }
    }
}
