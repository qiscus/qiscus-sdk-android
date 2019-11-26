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

import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.content.ContextCompat;
import androidx.core.util.PatternsCompat;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.qiscus.nirmana.Nirmana;
import com.qiscus.sdk.Qiscus;
import com.qiscus.sdk.R;
import com.qiscus.sdk.chat.core.data.model.QMessage;
import com.qiscus.sdk.chat.core.util.QiscusTextUtil;
import com.qiscus.sdk.data.model.QiscusMentionConfig;
import com.qiscus.sdk.ui.adapter.OnItemClickListener;
import com.qiscus.sdk.ui.adapter.OnLongItemClickListener;
import com.qiscus.sdk.ui.adapter.OnUploadIconClickListener;
import com.qiscus.sdk.ui.view.ClickableMovementMethod;
import com.qiscus.sdk.ui.view.QiscusProgressView;
import com.qiscus.sdk.util.QiscusImageUtil;

import java.io.File;
import java.util.regex.Matcher;

/**
 * Created on : September 27, 2016
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
public abstract class QiscusBaseImageMessageViewHolder extends QiscusBaseMessageViewHolder<QMessage>
        implements QMessage.ProgressListener, QMessage.DownloadingListener {

    @NonNull
    protected ImageView thumbnailView;
    @Nullable
    protected TextView captionView;
    @Nullable
    protected ImageView imageFrameView;
    @Nullable
    protected QiscusProgressView progressView;
    @Nullable
    protected ImageView downloadIconView;

    protected int rightProgressFinishedColor;
    protected int leftProgressFinishedColor;

    protected QMessage qiscusMessage;

    public QiscusBaseImageMessageViewHolder(View itemView, OnItemClickListener itemClickListener,
                                            OnLongItemClickListener longItemClickListener) {
        this(itemView, itemClickListener, longItemClickListener, null);
    }

    public QiscusBaseImageMessageViewHolder(View itemView, OnItemClickListener itemClickListener,
                                            OnLongItemClickListener longItemClickListener,
                                            OnUploadIconClickListener uploadIconClickListener) {
        super(itemView, itemClickListener, longItemClickListener);
        thumbnailView = getThumbnailView(itemView);
        captionView = getCaptionView(itemView);
        imageFrameView = getImageFrameView(itemView);
        progressView = getProgressView(itemView);
        downloadIconView = getDownloadIconView(itemView);
        if (captionView != null) {
            captionView.setMovementMethod(ClickableMovementMethod.getInstance());
            captionView.setClickable(false);
            captionView.setLongClickable(false);
        }

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
    protected abstract ImageView getThumbnailView(View itemView);

    @Nullable
    protected abstract TextView getCaptionView(View itemView);

    @Nullable
    protected abstract ImageView getImageFrameView(View itemView);

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
        if (captionView != null) {
            setUpLinks();
        }
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
        if (imageFrameView != null) {
            imageFrameView.setColorFilter(messageFromMe ? rightBubbleColor : leftBubbleColor);
        }
        if (progressView != null) {
            progressView.setFinishedColor(messageFromMe ? rightProgressFinishedColor : leftProgressFinishedColor);
        }
        if (captionView != null) {
            captionView.setTextColor(messageFromMe ? rightBubbleTextColor : leftBubbleTextColor);
            captionView.setLinkTextColor(messageFromMe ? rightLinkTextColor : leftLinkTextColor);
        }
        super.setUpColor();
    }

    @Override
    protected void showMessage(QMessage qiscusMessage) {
        showImage(qiscusMessage);
        showCaption(qiscusMessage);
    }

    protected void showCaption(QMessage qiscusMessage) {
        if (captionView != null) {
            captionView.setVisibility(TextUtils.isEmpty(qiscusMessage.getCaption()) ? View.GONE : View.VISIBLE);
            QiscusMentionConfig mentionConfig = Qiscus.getChatConfig().getMentionConfig();
            if (mentionConfig.isEnableMention()) {
                Spannable spannable = QiscusTextUtil.createQiscusSpannableText(
                        qiscusMessage.getCaption(),
                        roomMembers,
                        messageFromMe ? mentionConfig.getRightMentionAllColor() : mentionConfig.getLeftMentionAllColor(),
                        messageFromMe ? mentionConfig.getRightMentionOtherColor() : mentionConfig.getLeftMentionOtherColor(),
                        messageFromMe ? mentionConfig.getRightMentionMeColor() : mentionConfig.getLeftMentionMeColor(),
                        mentionConfig.getMentionClickHandler()
                );
                captionView.setText(spannable);
            } else {
                captionView.setText(qiscusMessage.getCaption());
            }
        }
    }

    protected void showImage(QMessage qiscusMessage) {
        if (qiscusMessage.getAttachmentUri().toString().startsWith("http")) { //We have sent it
            showSentImage(qiscusMessage);
        } else { //Still uploading the image
            showSendingImage(qiscusMessage);
        }
    }

    protected void showSentImage(QMessage qiscusMessage) {
        File localPath = Qiscus.getDataStore().getLocalPath(qiscusMessage.getId());
        if (localPath == null) { //If the image not yet downloaded
            showDownloadIcon(true);
            showBlurryImage(qiscusMessage);
        } else {
            showDownloadIcon(false);
            showLocalFileImage(localPath);
        }
    }

    protected void showLocalFileImage(File localPath) {
        Nirmana.getInstance().get()
                .setDefaultRequestOptions(new RequestOptions()
                        .dontAnimate()
                        .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                        .placeholder(R.drawable.qiscus_image_placeholder)
                        .error(R.drawable.qiscus_image_placeholder))
                .load(localPath)
                .into(thumbnailView);
    }

    protected void showSendingImage(QMessage qiscusMessage) {
        showDownloadIcon(true);
        File localPath = new File(qiscusMessage.getAttachmentUri().toString());
        if (localPath.exists()) { //If the image still exist
            showLocalFileImage(localPath);
        } else { //If the image file have been removed
            Nirmana.getInstance().get()
                    .setDefaultRequestOptions(new RequestOptions().dontAnimate())
                    .load(R.drawable.qiscus_image_placeholder)
                    .into(thumbnailView);
        }
    }

    protected void showBlurryImage(QMessage qiscusMessage) {
        Nirmana.getInstance().get()
                .setDefaultRequestOptions(new RequestOptions()
                        .dontAnimate()
                        .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                        .placeholder(R.drawable.qiscus_image_placeholder)
                        .error(R.drawable.qiscus_image_placeholder))
                .load(QiscusImageUtil.generateBlurryThumbnailUrl(qiscusMessage.getAttachmentUri().toString()))
                .into(thumbnailView);
    }

    protected void showDownloadIcon(boolean show) {
        if (downloadIconView != null) {
            downloadIconView.setVisibility(show ? View.VISIBLE : View.GONE);
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

    private void setUpLinks() {
        String message = captionView.getText().toString();
        Matcher matcher = PatternsCompat.AUTOLINK_WEB_URL.matcher(message);
        while (matcher.find()) {
            int start = matcher.start();
            if (start > 0 && message.charAt(start - 1) == '@') {
                continue;
            }
            int end = matcher.end();
            clickify(start, end, () -> {
                String url = message.substring(start, end);
                if (!url.startsWith("http")) {
                    url = "http://" + url;
                }
                new CustomTabsIntent.Builder()
                        .setToolbarColor(ContextCompat.getColor(Qiscus.getApps(), Qiscus.getChatConfig().getAppBarColor()))
                        .setShowTitle(true)
                        .addDefaultShareMenuItem()
                        .enableUrlBarHiding()
                        .build()
                        .launchUrl(captionView.getContext(), Uri.parse(url));
            });
        }
    }

    private void clickify(int start, int end, ClickSpan.OnClickListener listener) {
        CharSequence text = captionView.getText();
        ClickSpan span = new ClickSpan(listener);

        if (start == -1) {
            return;
        }

        if (text instanceof Spannable) {
            ((Spannable) text).setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        } else {
            SpannableString s = SpannableString.valueOf(text);
            s.setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            captionView.setText(s);
        }
    }

    private static class ClickSpan extends ClickableSpan {
        private OnClickListener listener;

        public ClickSpan(OnClickListener listener) {
            this.listener = listener;
        }

        @Override
        public void onClick(View widget) {
            if (listener != null) {
                listener.onClick();
            }
        }

        public interface OnClickListener {
            void onClick();
        }
    }
}
