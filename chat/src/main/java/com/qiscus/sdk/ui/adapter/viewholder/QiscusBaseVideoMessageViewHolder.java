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

import com.qiscus.nirmana.Nirmana;
import com.qiscus.sdk.R;
import com.qiscus.sdk.ui.adapter.OnUploadIconClickListener;
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

    public QiscusBaseVideoMessageViewHolder(View itemView, OnItemClickListener itemClickListener,
                                            OnLongItemClickListener longItemClickListener,
                                            OnUploadIconClickListener downloadIconClickListener) {
        super(itemView, itemClickListener, longItemClickListener, downloadIconClickListener);
    }

    @Override
    protected void showLocalFileImage(File localPath) {
        Nirmana.getInstance().get()
                .load(localPath)
                .dontAnimate()
                .placeholder(R.drawable.qiscus_image_placeholder)
                .error(R.drawable.qiscus_image_placeholder)
                .into(thumbnailView);
    }
}
