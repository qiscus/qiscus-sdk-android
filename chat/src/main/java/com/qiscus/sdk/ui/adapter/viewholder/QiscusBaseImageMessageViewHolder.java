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

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.qiscus.nirmana.Nirmana;
import com.qiscus.sdk.Qiscus;
import com.qiscus.sdk.R;
import com.qiscus.sdk.data.model.QiscusComment;
import com.qiscus.sdk.ui.adapter.OnItemClickListener;
import com.qiscus.sdk.ui.adapter.OnLongItemClickListener;
import com.qiscus.sdk.ui.view.QiscusProgressView;
import com.qiscus.sdk.util.QiscusAndroidUtil;
import com.qiscus.sdk.util.QiscusImageUtil;

import java.io.File;

/**
 * Created on : September 27, 2016
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
public abstract class QiscusBaseImageMessageViewHolder extends QiscusBaseMessageViewHolder<QiscusComment>
        implements QiscusComment.ProgressListener, QiscusComment.DownloadingListener {

    @NonNull protected ImageView thumbnailView;
    @Nullable protected ImageView imageFrameView;
    @Nullable protected QiscusProgressView progressView;
    @Nullable protected ImageView downloadIconView;

    protected int rightProgressFinishedColor;
    protected int leftProgressFinishedColor;

    public QiscusBaseImageMessageViewHolder(View itemView, OnItemClickListener itemClickListener,
                                            OnLongItemClickListener longItemClickListener) {
        super(itemView, itemClickListener, longItemClickListener);
        thumbnailView = getThumbnailView(itemView);
        imageFrameView = getImageFrameView(itemView);
        progressView = getProgressView(itemView);
        downloadIconView = getDownloadIconView(itemView);
    }

    @Override
    protected void loadChatConfig() {
        super.loadChatConfig();
        rightProgressFinishedColor = ContextCompat.getColor(Qiscus.getApps(), Qiscus.getChatConfig().getRightProgressFinishedColor());
        leftProgressFinishedColor = ContextCompat.getColor(Qiscus.getApps(), Qiscus.getChatConfig().getLeftProgressFinishedColor());
    }

    @NonNull
    protected abstract ImageView getThumbnailView(View itemView);

    @Nullable
    protected abstract ImageView getImageFrameView(View itemView);

    @Nullable
    protected abstract QiscusProgressView getProgressView(View itemView);

    @Nullable
    protected abstract ImageView getDownloadIconView(View itemView);

    @Override
    public void bind(QiscusComment qiscusComment) {
        super.bind(qiscusComment);
        qiscusComment.setProgressListener(this);
        qiscusComment.setDownloadingListener(this);
        setUpDownloadIcon(qiscusComment);
        showProgressOrNot(qiscusComment);
        if (qiscusComment.getState() == QiscusComment.STATE_PENDING
                || qiscusComment.getState() == QiscusComment.STATE_SENDING) {
            qiscusComment.setDownloading(true);
        }
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
            progressView.setVisibility(qiscusComment.isDownloading() ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    protected void setUpColor() {
        if (imageFrameView != null) {
            imageFrameView.setColorFilter(messageFromMe ? rightBubbleColor : leftBubbleColor);
        }
        if (progressView != null) {
            progressView.setFinishedColor(messageFromMe ? rightProgressFinishedColor : leftProgressFinishedColor);
        }
        super.setUpColor();
    }

    @Override
    protected void showMessage(QiscusComment qiscusComment) {
        showImage(qiscusComment);
    }

    protected void showImage(QiscusComment qiscusComment) {
        if (QiscusAndroidUtil.isUrl(qiscusComment.getAttachmentUri().toString())) { //We have sent it
            showSentImage(qiscusComment);
        } else { //Still uploading the image
            showSendingImage(qiscusComment);
        }
    }

    protected void showSentImage(QiscusComment qiscusComment) {
        File localPath = Qiscus.getDataStore().getLocalPath(qiscusComment.getId());
        if (localPath == null) { //If the image not yet downloaded
            showDownloadIcon(true);
            showBlurryImage(qiscusComment);
        } else {
            showDownloadIcon(false);
            showLocalFileImage(localPath);
        }
    }

    protected void showLocalFileImage(File localPath) {
        Nirmana.getInstance().get()
                .load(localPath)
                .dontAnimate()
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .placeholder(R.drawable.qiscus_image_placeholder)
                .error(R.drawable.qiscus_image_placeholder)
                .into(thumbnailView);
    }

    protected void showSendingImage(QiscusComment qiscusComment) {
        showDownloadIcon(true);
        File localPath = new File(qiscusComment.getAttachmentUri().toString());
        if (localPath.exists()) { //If the image still exist
            showLocalFileImage(localPath);
        } else { //If the image file have been removed
            Nirmana.getInstance().get()
                    .load(R.drawable.qiscus_image_placeholder)
                    .dontAnimate()
                    .into(thumbnailView);
        }
    }

    protected void showBlurryImage(QiscusComment qiscusComment) {
        Nirmana.getInstance().get()
                .load(QiscusImageUtil.generateBlurryThumbnailUrl(qiscusComment.getAttachmentUri().toString()))
                .dontAnimate()
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .placeholder(R.drawable.qiscus_image_placeholder)
                .error(R.drawable.qiscus_image_placeholder)
                .into(thumbnailView);
    }

    protected void showDownloadIcon(boolean show) {
        if (downloadIconView != null) {
            downloadIconView.setVisibility(show ? View.VISIBLE : View.GONE);
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
