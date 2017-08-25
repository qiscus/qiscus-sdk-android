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

import android.view.View;

import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.qiscus.nirmana.Nirmana;
import com.qiscus.sdk.Qiscus;
import com.qiscus.sdk.R;
import com.qiscus.sdk.data.model.QiscusComment;
import com.qiscus.sdk.ui.adapter.OnItemClickListener;
import com.qiscus.sdk.ui.adapter.OnLongItemClickListener;

import java.io.File;

/**
 * Created on : June 15, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
public abstract class QiscusBaseVideoMessageViewHolder extends QiscusBaseImageMessageViewHolder {
    public QiscusBaseVideoMessageViewHolder(View itemView, OnItemClickListener itemClickListener,
                                            OnLongItemClickListener longItemClickListener) {
        super(itemView, itemClickListener, longItemClickListener);
    }

    @Override
    protected void showMyImage(final QiscusComment qiscusComment) {
        if (qiscusComment.getState() <= QiscusComment.STATE_SENDING) {
            if (imageHolderLayout != null) {
                imageHolderLayout.setVisibility(View.INVISIBLE);
            }
            thumbnailView.setVisibility(View.VISIBLE);
            Nirmana.getInstance().get()
                    .load(new File(qiscusComment.getAttachmentUri().toString()))
                    .centerCrop()
                    .dontAnimate()
                    .thumbnail(0.5f)
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

    @Override
    protected void showImage(QiscusComment qiscusComment, File file) {
        Nirmana.getInstance().get()
                .load(file)
                .centerCrop()
                .dontAnimate()
                .thumbnail(0.5f)
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
}
