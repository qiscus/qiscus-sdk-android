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
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.request.RequestOptions;
import com.qiscus.nirmana.Nirmana;
import com.qiscus.sdk.Qiscus;
import com.qiscus.sdk.R;
import com.qiscus.sdk.chat.core.data.model.QAccount;
import com.qiscus.sdk.chat.core.data.model.QiscusComment;
import com.qiscus.sdk.chat.core.util.QiscusTextUtil;
import com.qiscus.sdk.data.model.QiscusMentionConfig;
import com.qiscus.sdk.ui.adapter.OnItemClickListener;
import com.qiscus.sdk.ui.adapter.OnLongItemClickListener;
import com.qiscus.sdk.ui.adapter.ReplyItemClickListener;
import com.qiscus.sdk.util.QiscusImageUtil;

import java.io.File;
import java.util.regex.Matcher;


/**
 * Created on : June 05, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
public abstract class QiscusBaseReplyMessageViewHolder extends QiscusBaseTextMessageViewHolder {
    @NonNull
    protected ViewGroup originMessageView;
    @NonNull
    protected TextView originSenderTextView;
    @NonNull
    protected TextView originMessageTextView;
    @Nullable
    protected View barView;
    @Nullable
    protected ImageView originIconView;
    @Nullable
    protected ImageView originImageView;

    protected int barColor;
    protected int originSenderColor;
    protected int originMessageColor;

    private ReplyItemClickListener replyItemClickListener;
    private QAccount qAccount;

    public QiscusBaseReplyMessageViewHolder(View itemView, OnItemClickListener itemClickListener,
                                            OnLongItemClickListener longItemClickListener,
                                            ReplyItemClickListener replyItemClickListener) {
        super(itemView, itemClickListener, longItemClickListener);
        this.replyItemClickListener = replyItemClickListener;
        qAccount = Qiscus.getQiscusAccount();
        originMessageView = getOriginMessageView(itemView);
        originSenderTextView = getOriginSenderTextView(itemView);
        originMessageTextView = getOriginMessageTextView(itemView);
        barView = getBarView(itemView);
        originIconView = getOriginIconView(itemView);
        originImageView = getOriginImageView(itemView);
    }

    @NonNull
    protected abstract ViewGroup getOriginMessageView(View itemView);

    @NonNull
    protected abstract TextView getOriginSenderTextView(View itemView);

    @NonNull
    protected abstract TextView getOriginMessageTextView(View itemView);

    @Nullable
    protected abstract View getBarView(View itemView);

    @Nullable
    protected abstract ImageView getOriginIconView(View itemView);

    @Nullable
    protected abstract ImageView getOriginImageView(View itemView);

    @Override
    protected void loadChatConfig() {
        super.loadChatConfig();
        barColor = ContextCompat.getColor(Qiscus.getApps(), Qiscus.getChatConfig().getReplyBarColor());
        originSenderColor = ContextCompat.getColor(Qiscus.getApps(), Qiscus.getChatConfig().getReplySenderColor());
        originMessageColor = ContextCompat.getColor(Qiscus.getApps(), Qiscus.getChatConfig().getReplyMessageColor());
    }

    @Override
    protected void setUpColor() {
        super.setUpColor();
        if (barView != null) {
            barView.setBackgroundColor(barColor);
        }
        originSenderTextView.setTextColor(originSenderColor);
        originMessageTextView.setTextColor(originMessageColor);
    }

    @Override
    protected void showMessage(QiscusComment qiscusComment) {
        super.showMessage(qiscusComment);
        setUpLinks();
        originMessageView.setOnClickListener(v -> {
            if (replyItemClickListener != null) {
                replyItemClickListener.onReplyItemClick(qiscusComment);
            }
        });
        QiscusComment originComment = qiscusComment.getReplyTo();
        originSenderTextView.setText(originComment.getSenderEmail().equals(qAccount.getId()) ?
                QiscusTextUtil.getString(R.string.qiscus_you) : Qiscus.getChatConfig().getRoomSenderNameInterceptor()
                .getSenderName(originComment));
        originSenderTextView.setTextColor(ContextCompat
                .getColor(Qiscus.getApps(), Qiscus
                        .getChatConfig()
                        .getRoomSenderNameColorInterceptor()
                        .getColor(originComment)));
        if (barView != null) {
            barView.setBackgroundColor(ContextCompat.getColor(Qiscus.getApps(),
                    Qiscus.getChatConfig().getRoomReplyBarColorInterceptor().getColor(originComment)));
        }
        QiscusMentionConfig mentionConfig = Qiscus.getChatConfig().getMentionConfig();
        switch (originComment.getType()) {
            case IMAGE:
            case VIDEO:
                if (originImageView != null) {
                    originImageView.setVisibility(View.VISIBLE);
                    File localPath = Qiscus.getDataStore().getLocalPath(originComment.getId());
                    if (localPath == null) {
                        showBlurryImage(originComment);
                    } else {
                        showImage(localPath);
                    }
                }
                if (originIconView != null) {
                    originIconView.setVisibility(View.GONE);
                }
                if (mentionConfig.isEnableMention()) {
                    originMessageTextView.setText(TextUtils.isEmpty(originComment.getCaption()) ?
                            originComment.getAttachmentName() : QiscusTextUtil.createQiscusSpannableText(
                            originComment.getCaption(),
                            roomMembers,
                            originMessageColor,
                            originMessageColor,
                            originMessageColor,
                            null
                    ));
                } else {
                    originMessageTextView.setText(TextUtils.isEmpty(originComment.getCaption()) ?
                            originComment.getAttachmentName() : originComment.getCaption());
                }
                break;
            case AUDIO:
                if (originImageView != null) {
                    originImageView.setVisibility(View.GONE);
                }
                if (originIconView != null) {
                    originIconView.setVisibility(View.VISIBLE);
                    originIconView.setImageResource(R.drawable.ic_qiscus_add_audio);
                }
                originMessageTextView.setText(QiscusTextUtil.getString(R.string.qiscus_voice_message));
                break;
            case FILE:
                if (originImageView != null) {
                    originImageView.setVisibility(View.GONE);
                }
                if (originIconView != null) {
                    originIconView.setVisibility(View.VISIBLE);
                    originIconView.setImageResource(R.drawable.ic_qiscus_file);
                }
                originMessageTextView.setText(originComment.getAttachmentName());
                break;
            case CONTACT:
                if (originImageView != null) {
                    originImageView.setVisibility(View.GONE);
                }
                if (originIconView != null) {
                    originIconView.setVisibility(View.VISIBLE);
                    originIconView.setImageResource(R.drawable.ic_qiscus_add_contact);
                }
                originMessageTextView.setText(QiscusTextUtil.getString(R.string.qiscus_contact)
                        + ": " + originComment.getContact().getName());
                break;
            case LOCATION:
                if (originImageView != null) {
                    originImageView.setVisibility(View.GONE);
                }
                if (originIconView != null) {
                    originIconView.setVisibility(View.VISIBLE);
                    originIconView.setImageResource(R.drawable.ic_qiscus_location);
                }
                originMessageTextView.setText(originComment.getMessage());
                break;
            default:
                if (originImageView != null) {
                    originImageView.setVisibility(View.GONE);
                }
                if (originIconView != null) {
                    originIconView.setVisibility(View.GONE);
                }
                if (mentionConfig.isEnableMention()) {
                    originMessageTextView.setText(QiscusTextUtil.createQiscusSpannableText(
                            originComment.getMessage(),
                            roomMembers,
                            originMessageColor,
                            originMessageColor,
                            originMessageColor,
                            null
                    ));
                } else {
                    originMessageTextView.setText(originComment.getMessage());
                }
                break;

        }
    }

    private void showImage(File file) {
        if (originImageView != null) {
            Nirmana.getInstance().get()
                    .setDefaultRequestOptions(new RequestOptions()
                            .centerCrop()
                            .dontAnimate()
                            .placeholder(R.drawable.qiscus_image_placeholder)
                            .error(R.drawable.qiscus_image_placeholder))
                    .asBitmap()
                    .load(file)
                    .thumbnail(0.5f)
                    .into(originImageView);
        }
    }

    private void showBlurryImage(QiscusComment qiscusComment) {
        if (originImageView != null) {
            Nirmana.getInstance().get()
                    .setDefaultRequestOptions(new RequestOptions()
                            .centerCrop()
                            .dontAnimate()
                            .placeholder(R.drawable.qiscus_image_placeholder)
                            .error(R.drawable.qiscus_image_placeholder))
                    .asBitmap()
                    .load(QiscusImageUtil.generateBlurryThumbnailUrl(qiscusComment.getAttachmentUri().toString()))
                    .thumbnail(0.5f)
                    .into(originImageView);
        }
    }

    private void setUpLinks() {
        String message = messageTextView.getText().toString();
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
                        .launchUrl(messageTextView.getContext(), Uri.parse(url));
            });
        }
    }

    private void clickify(int start, int end, ClickSpan.OnClickListener listener) {
        CharSequence text = messageTextView.getText();
        ClickSpan span = new ClickSpan(listener);

        if (start == -1) {
            return;
        }

        if (text instanceof Spannable) {
            ((Spannable) text).setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        } else {
            SpannableString s = SpannableString.valueOf(text);
            s.setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            messageTextView.setText(s);
        }
    }

    private static class ClickSpan extends ClickableSpan {
        private ClickSpan.OnClickListener listener;

        public ClickSpan(ClickSpan.OnClickListener listener) {
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
