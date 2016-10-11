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
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
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
public abstract class QiscusBaseImageMessageViewHolder extends QiscusBaseMessageViewHolder<QiscusComment>
        implements QiscusComment.ProgressListener, QiscusComment.DownloadingListener {

    @NonNull protected ImageView thumbnailView;
    @Nullable protected ViewGroup imageHolderLayout;
    @Nullable protected ImageView imageFrameView;
    @Nullable protected TextView fileNameView;
    @Nullable protected CircleProgress progressView;
    @Nullable protected ImageView downloadIconView;

    public QiscusBaseImageMessageViewHolder(View itemView, OnItemClickListener itemClickListener, OnLongItemClickListener longItemClickListener) {
        super(itemView, itemClickListener, longItemClickListener);
        thumbnailView = getThumbnailView(itemView);
        imageHolderLayout = getImageHolderLayout(itemView);
        imageFrameView = getImageFrameView(itemView);
        fileNameView = getFileNameView(itemView);
        progressView = getProgressView(itemView);
        downloadIconView = getDownloadIconView(itemView);
    }

    @NonNull
    protected abstract ImageView getThumbnailView(View itemView);

    @Nullable
    protected abstract ViewGroup getImageHolderLayout(View itemView);

    @Nullable
    protected abstract ImageView getImageFrameView(View itemView);

    @Nullable
    protected abstract TextView getFileNameView(View itemView);

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
            progressView.setFinishedColor(messageFromMe ? rightBubbleColor : leftBubbleColor);
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
        if (fileNameView != null) {
            fileNameView.setText(qiscusComment.getAttachmentName());
        }
    }

    private void showOthersImage(final QiscusComment qiscusComment) {
        File localPath = Qiscus.getDataStore().getLocalPath(qiscusComment.getId());
        if (localPath == null) {
            if (imageHolderLayout != null) {
                imageHolderLayout.setVisibility(View.VISIBLE);
            }
            thumbnailView.setVisibility(View.GONE);
        } else {
            if (imageHolderLayout != null) {
                imageHolderLayout.setVisibility(View.INVISIBLE);
            }
            thumbnailView.setVisibility(View.VISIBLE);
            showImage(localPath);
        }
    }

    private void showMyImage(final QiscusComment qiscusComment) {
        if (qiscusComment.getState() == QiscusComment.STATE_SENDING) {
            if (imageHolderLayout != null) {
                imageHolderLayout.setVisibility(View.INVISIBLE);
            }
            thumbnailView.setVisibility(View.VISIBLE);
            Glide.with(thumbnailView.getContext())
                    .load(new File(qiscusComment.getAttachmentUri().toString()))
                    .error(R.drawable.ic_qiscus_img)
                    .into(thumbnailView);
        } else {
            File localPath = Qiscus.getDataStore().getLocalPath(qiscusComment.getId());
            if (localPath == null) {
                if (imageHolderLayout != null) {
                    imageHolderLayout.setVisibility(View.VISIBLE);
                }
                thumbnailView.setVisibility(View.GONE);
            } else {
                if (imageHolderLayout != null) {
                    imageHolderLayout.setVisibility(View.INVISIBLE);
                }
                thumbnailView.setVisibility(View.VISIBLE);
                showImage(localPath);
            }
        }
    }

    protected void showImage(File file) {
        Glide.with(thumbnailView.getContext())
                .load(file)
                .error(R.drawable.ic_qiscus_img)
                .listener(new RequestListener<File, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, File model, Target<GlideDrawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, File model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        if (imageHolderLayout != null) {
                            imageHolderLayout.setVisibility(View.INVISIBLE);
                        }
                        thumbnailView.setVisibility(View.VISIBLE);
                        return false;
                    }
                })
                .into(thumbnailView);
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
