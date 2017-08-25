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
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.qiscus.nirmana.Nirmana;
import com.qiscus.sdk.Qiscus;
import com.qiscus.sdk.R;
import com.qiscus.sdk.data.model.QiscusComment;
import com.qiscus.sdk.ui.adapter.OnItemClickListener;
import com.qiscus.sdk.ui.adapter.OnLongItemClickListener;
import com.qiscus.sdk.ui.view.QiscusProgressView;
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
    @Nullable protected ViewGroup imageHolderLayout;
    @Nullable protected ImageView blurryImageView;
    @Nullable protected ImageView imageFrameView;
    @Nullable protected QiscusProgressView progressView;
    @Nullable protected ImageView downloadIconView;

    protected int rightProgressFinishedColor;
    protected int leftProgressFinishedColor;

    public QiscusBaseImageMessageViewHolder(View itemView, OnItemClickListener itemClickListener,
                                            OnLongItemClickListener longItemClickListener) {
        super(itemView, itemClickListener, longItemClickListener);
        thumbnailView = getThumbnailView(itemView);
        imageHolderLayout = getImageHolderLayout(itemView);
        blurryImageView = getBlurryImageView(itemView);
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
    protected abstract ViewGroup getImageHolderLayout(View itemView);

    @Nullable
    protected abstract ImageView getBlurryImageView(View view);

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
                downloadIconView.setImageResource(R.drawable.ic_qiscus_upload_big);
            } else {
                downloadIconView.setImageResource(R.drawable.ic_qiscus_download_big);
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
        if (messageFromMe) {
            showMyImage(qiscusComment);
        } else {
            showOthersImage(qiscusComment);
        }
    }

    protected void showOthersImage(final QiscusComment qiscusComment) {
        File localPath = Qiscus.getDataStore().getLocalPath(qiscusComment.getId());
        if (localPath == null) {
            if (imageHolderLayout != null) {
                imageHolderLayout.setVisibility(View.VISIBLE);
                showBlurryImage(qiscusComment);
            }
            thumbnailView.setVisibility(View.GONE);
        } else {
            if (imageHolderLayout != null) {
                imageHolderLayout.setVisibility(View.INVISIBLE);
            }
            thumbnailView.setVisibility(View.VISIBLE);
            showImage(qiscusComment, localPath);
        }
    }

    protected void showMyImage(final QiscusComment qiscusComment) {
        if (qiscusComment.getState() <= QiscusComment.STATE_SENDING) {
            if (imageHolderLayout != null) {
                imageHolderLayout.setVisibility(View.INVISIBLE);
            }
            thumbnailView.setVisibility(View.VISIBLE);
            Nirmana.getInstance().get()
                    .load(new File(qiscusComment.getAttachmentUri().toString()))
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .error(R.drawable.qiscus_image_placeholder)
                    .into(thumbnailView);
        } else {
            File localPath = Qiscus.getDataStore().getLocalPath(qiscusComment.getId());
            if (localPath == null) {
                File file = new File(qiscusComment.getAttachmentUri().toString());
                if (file.exists()) {
                    localPath = file;
                }
            }
            if (localPath == null) {
                if (imageHolderLayout != null) {
                    imageHolderLayout.setVisibility(View.VISIBLE);
                    showBlurryImage(qiscusComment);
                }
                thumbnailView.setVisibility(View.GONE);
            } else {
                if (imageHolderLayout != null) {
                    imageHolderLayout.setVisibility(View.INVISIBLE);
                }
                thumbnailView.setVisibility(View.VISIBLE);
                showImage(qiscusComment, localPath);
            }
        }
    }

    protected void showImage(QiscusComment qiscusComment, File file) {
        Nirmana.getInstance().get()
                .load(file)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .error(R.drawable.qiscus_image_placeholder)
                .listener(new RequestListener<File, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, File model, Target<GlideDrawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, File model,
                                                   Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        if (imageHolderLayout != null) {
                            imageHolderLayout.setVisibility(View.INVISIBLE);
                            showBlurryImage(qiscusComment);
                        }
                        thumbnailView.setVisibility(View.VISIBLE);
                        return false;
                    }
                })
                .into(thumbnailView);
    }

    protected void showBlurryImage(QiscusComment qiscusComment) {
        if (blurryImageView != null) {
            Nirmana.getInstance().get()
                    .load(QiscusImageUtil.generateBlurryThumbnailUrl(qiscusComment.getAttachmentUri().toString()))
                    .dontAnimate()
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .placeholder(R.drawable.qiscus_image_placeholder)
                    .error(R.drawable.qiscus_image_placeholder)
                    .into(blurryImageView);
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
