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
import android.support.v7.widget.AppCompatSeekBar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.lzyzsd.circleprogress.CircleProgress;
import com.qiscus.sdk.R;
import com.qiscus.sdk.ui.adapter.OnItemClickListener;
import com.qiscus.sdk.ui.adapter.OnLongItemClickListener;

/**
 * Created on : November 02, 2016
 * Author     : zetbaitsu
 * Name       : Zetra
 * Email      : zetra@mail.ugm.ac.id
 * GitHub     : https://github.com/zetbaitsu
 * LinkedIn   : https://id.linkedin.com/in/zetbaitsu
 */
public class QiscusAudioViewHolder extends QiscusBaseAudioMessageViewHolder {
    public QiscusAudioViewHolder(View itemView, OnItemClickListener itemClickListener, OnLongItemClickListener longItemClickListener) {
        super(itemView, itemClickListener, longItemClickListener);
    }

    @Nullable
    @Override
    protected ImageView getFirstMessageBubbleIndicatorView(View itemView) {
        return (ImageView) itemView.findViewById(R.id.bubble);
    }

    @NonNull
    @Override
    protected View getMessageBubbleView(View itemView) {
        return itemView.findViewById(R.id.message);
    }

    @Nullable
    @Override
    protected TextView getDateView(View itemView) {
        return (TextView) itemView.findViewById(R.id.date);
    }

    @Nullable
    @Override
    protected TextView getTimeView(View itemView) {
        return (TextView) itemView.findViewById(R.id.time);
    }

    @Nullable
    @Override
    protected ImageView getMessageStateIndicatorView(View itemView) {
        return (ImageView) itemView.findViewById(R.id.icon_read);
    }

    @NonNull
    @Override
    protected ImageView getPlayButton(View itemView) {
        return (ImageView) itemView.findViewById(R.id.iv_play);
    }

    @NonNull
    @Override
    protected AppCompatSeekBar getSeekBar(View itemView) {
        return (AppCompatSeekBar) itemView.findViewById(R.id.seekbar);
    }

    @NonNull
    @Override
    protected TextView getDurationView(View itemView) {
        return (TextView) itemView.findViewById(R.id.duration);
    }

    @Nullable
    @Override
    protected CircleProgress getProgressView(View itemView) {
        return (CircleProgress) itemView.findViewById(R.id.progress);
    }
}
