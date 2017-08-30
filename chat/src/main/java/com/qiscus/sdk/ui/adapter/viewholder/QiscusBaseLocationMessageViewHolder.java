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

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.qiscus.nirmana.Nirmana;
import com.qiscus.sdk.R;
import com.qiscus.sdk.data.model.QiscusComment;
import com.qiscus.sdk.ui.adapter.OnItemClickListener;
import com.qiscus.sdk.ui.adapter.OnLongItemClickListener;

/**
 * Created on : August 16, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
public abstract class QiscusBaseLocationMessageViewHolder extends QiscusBaseMessageViewHolder<QiscusComment> {

    @NonNull protected ImageView mapImageView;
    @NonNull protected TextView locationNameView;
    @NonNull protected TextView locationAddressView;
    @Nullable protected ImageView imageFrameView;

    public QiscusBaseLocationMessageViewHolder(View itemView, OnItemClickListener itemClickListener,
                                               OnLongItemClickListener longItemClickListener) {
        super(itemView, itemClickListener, longItemClickListener);
        mapImageView = getMapImageView(itemView);
        locationNameView = getLocationNameView(itemView);
        locationAddressView = getLocationAddressView(itemView);
        imageFrameView = getImageFrameView(itemView);
    }

    @NonNull
    public abstract ImageView getMapImageView(View itemView);

    @NonNull
    public abstract TextView getLocationNameView(View itemView);

    @NonNull
    public abstract TextView getLocationAddressView(View itemView);

    @Nullable
    protected abstract ImageView getImageFrameView(View itemView);

    @Override
    protected void setUpColor() {
        super.setUpColor();
        locationNameView.setTextColor(messageFromMe ? rightBubbleTextColor : leftBubbleTextColor);
        locationAddressView.setTextColor(messageFromMe ? rightBubbleTextColor : leftBubbleTextColor);
        if (imageFrameView != null) {
            imageFrameView.setColorFilter(messageFromMe ? rightBubbleColor : leftBubbleColor);
        }
    }

    @Override
    protected void showMessage(QiscusComment qiscusComment) {
        Nirmana.getInstance().get()
                .load(qiscusComment.getLocation().getThumbnailUrl())
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .error(R.drawable.ic_qiscus_placehalder_map)
                .placeholder(R.drawable.ic_qiscus_placehalder_map)
                .dontAnimate()
                .into(mapImageView);
        locationNameView.setText(qiscusComment.getLocation().getName());
        locationAddressView.setText(qiscusComment.getLocation().getAddress());
    }
}
