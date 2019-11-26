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
import androidx.appcompat.widget.AppCompatSeekBar;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.qiscus.sdk.Qiscus;
import com.qiscus.sdk.chat.core.data.model.QMessage;
import com.qiscus.sdk.ui.adapter.OnItemClickListener;
import com.qiscus.sdk.ui.adapter.OnLongItemClickListener;
import com.qiscus.sdk.ui.view.QiscusProgressView;

/**
 * Created on : September 27, 2016
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
public abstract class QiscusBaseAudioMessageViewHolder extends QiscusBaseMessageViewHolder<QMessage>
        implements QMessage.ProgressListener, QMessage.DownloadingListener, QMessage.PlayingAudioListener {

    @NonNull
    protected ImageView playButton;
    @NonNull
    protected AppCompatSeekBar seekBar;
    @NonNull
    protected TextView durationView;
    @Nullable
    protected QiscusProgressView progressView;

    protected int playIcon;
    protected int pauseIcon;

    private QMessage qiscusMessage;

    public QiscusBaseAudioMessageViewHolder(View itemView, OnItemClickListener itemClickListener,
                                            OnLongItemClickListener longItemClickListener) {
        super(itemView, itemClickListener, longItemClickListener);
        playButton = getPlayButton(itemView);
        seekBar = getSeekBar(itemView);
        durationView = getDurationView(itemView);
        progressView = getProgressView(itemView);
        seekBar.setOnTouchListener((v, event) -> true);
    }

    @NonNull
    protected abstract ImageView getPlayButton(View itemView);

    @NonNull
    protected abstract AppCompatSeekBar getSeekBar(View itemView);

    @NonNull
    protected abstract TextView getDurationView(View itemView);

    @Nullable
    protected abstract QiscusProgressView getProgressView(View itemView);

    @Override
    protected void loadChatConfig() {
        super.loadChatConfig();
        playIcon = Qiscus.getChatConfig().getPlayAudioIcon();
        pauseIcon = Qiscus.getChatConfig().getPauseAudioIcon();
    }

    @Override
    public void bind(QMessage qiscusMessage) {
        super.bind(qiscusMessage);
        this.qiscusMessage = qiscusMessage;
        qiscusMessage.setProgressListener(this);
        qiscusMessage.setDownloadingListener(this);
        qiscusMessage.setPlayingAudioListener(this);
        setUpPlayButton(qiscusMessage);
        showProgressOrNot(qiscusMessage);
    }

    protected void setUpPlayButton(QMessage qiscusMessage) {
        playButton.setImageResource(qiscusMessage.isPlayingAudio() ? pauseIcon : playIcon);
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
        if (progressView != null) {
            progressView.setFinishedColor(messageFromMe ? rightBubbleColor : leftBubbleColor);
            progressView.setUnfinishedColor(messageFromMe ? rightBubbleColor : leftBubbleColor);
        }
        super.setUpColor();
    }

    @Override
    protected void showMessage(QMessage qiscusMessage) {
        playButton.setOnClickListener(v -> playAudio(qiscusMessage));
        seekBar.setMax(qiscusMessage.getAudioDuration());
        seekBar.setProgress(qiscusMessage.isPlayingAudio() ? qiscusMessage.getCurrentAudioPosition() : 0);
        setTimeRemaining(qiscusMessage.isPlayingAudio() ?
                qiscusMessage.getAudioDuration() - qiscusMessage.getCurrentAudioPosition()
                : qiscusMessage.getAudioDuration());
    }

    @Override
    public void onProgress(QMessage qiscusMessage, int percentage) {
        if (progressView != null) {
            progressView.setProgress(percentage);
        }
    }

    @Override
    public void onDownloading(QMessage qiscusMessage, boolean downloading) {
        if (progressView != null) {
            progressView.setVisibility(downloading ? View.VISIBLE : View.GONE);
        }
    }

    protected void playAudio(QMessage qiscusMessage) {
        if (qiscusMessage.getAudioDuration() > 0) {
            qiscusMessage.playAudio();
        } else {
            onClick(messageBubbleView);
        }
    }

    private void setTimeRemaining(long duration) {
        durationView.setText(DateUtils.formatElapsedTime(duration / 1000));
    }

    @Override
    public void onPlayingAudio(QMessage qiscusMessage, int currentPosition) {
        if (qiscusMessage.equals(this.qiscusMessage)) {
            playButton.setImageResource(pauseIcon);
            seekBar.setProgress(currentPosition);
            setTimeRemaining(qiscusMessage.getAudioDuration() - currentPosition);
        }
    }

    @Override
    public void onPauseAudio(QMessage qiscusMessage) {
        if (qiscusMessage.equals(this.qiscusMessage)) {
            playButton.setImageResource(playIcon);
        }
    }

    @Override
    public void onStopAudio(QMessage qiscusMessage) {
        if (qiscusMessage.equals(this.qiscusMessage)) {
            playButton.setImageResource(playIcon);
            seekBar.setProgress(0);
            setTimeRemaining(qiscusMessage.getAudioDuration());
        }
    }
}
