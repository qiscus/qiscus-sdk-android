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

package com.qiscus.sdk.ui.view;

import android.content.Context;
import android.support.annotation.DrawableRes;
import android.util.AttributeSet;

import com.bumptech.glide.Glide;

import de.hdodenhof.circleimageview.CircleImageView;

public class QiscusCircularImageView extends CircleImageView {
    private String imageUrl;

    public QiscusCircularImageView(Context context) {
        super(context);
    }

    public QiscusCircularImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public QiscusCircularImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setImageUrl(String url) {
        imageUrl = url;
        Glide.with(getContext())
                .load(url)
                .into(this);
    }

    public void setImageUrl(String url, @DrawableRes int placeHolderResourceId) {
        imageUrl = url;
        Glide.with(getContext())
                .load(url)
                .dontAnimate()
                .placeholder(placeHolderResourceId)
                .error(placeHolderResourceId)
                .into(this);
    }

    public void setImageUrl(String url, @DrawableRes int placeHolderResourceId, @DrawableRes int errorResourceId) {
        imageUrl = url;
        Glide.with(getContext())
                .load(url)
                .placeholder(placeHolderResourceId)
                .error(errorResourceId)
                .into(this);
    }

    public String getImageUrl() {
        return imageUrl;
    }
}
