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

import android.annotation.SuppressLint;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.content.ContextCompat;
import androidx.core.util.PatternsCompat;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ClickableSpan;
import android.view.View;

import com.qiscus.sdk.Qiscus;
import com.qiscus.sdk.chat.core.data.model.QiscusComment;
import com.qiscus.sdk.chat.core.data.model.urlsextractor.PreviewData;
import com.qiscus.sdk.ui.adapter.OnItemClickListener;
import com.qiscus.sdk.ui.adapter.OnLongItemClickListener;
import com.qiscus.sdk.ui.view.QiscusLinkPreviewView;

import java.util.regex.Matcher;

/**
 * Created on : December 09, 2016
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
public abstract class QiscusBaseLinkViewHolder extends QiscusBaseTextMessageViewHolder implements QiscusComment.LinkPreviewListener {
    @NonNull
    protected QiscusLinkPreviewView linkPreviewView;

    private QiscusComment qiscusComment;

    public QiscusBaseLinkViewHolder(View itemView, OnItemClickListener itemClickListener,
                                    OnLongItemClickListener longItemClickListener) {
        super(itemView, itemClickListener, longItemClickListener);
        linkPreviewView = getLinkPreviewView(itemView);
        messageTextView.setOnLongClickListener(v -> onLongClick(itemView));
    }

    @NonNull
    protected abstract QiscusLinkPreviewView getLinkPreviewView(View itemView);

    @Override
    public void bind(QiscusComment qiscusComment) {
        super.bind(qiscusComment);
        this.qiscusComment = qiscusComment;
        linkPreviewView.clearView();
        qiscusComment.setLinkPreviewListener(this);
        qiscusComment.loadLinkPreviewData();
    }

    @Override
    protected void showMessage(QiscusComment qiscusComment) {
        super.showMessage(qiscusComment);
        setUpLinks();
    }

    private void setUpLinks() {
        String message = messageTextView.getText().toString();
        @SuppressLint("RestrictedApi")
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

    @Override
    protected void setUpColor() {
        super.setUpColor();
        linkPreviewView.setTitleColor(messageFromMe ? rightBubbleTextColor : leftBubbleTextColor);
        linkPreviewView.setDescriptionColor(messageFromMe ? rightBubbleTextColor : leftBubbleTextColor);
    }

    @Override
    public void onLinkPreviewReady(QiscusComment qiscusComment, PreviewData previewData) {
        if (qiscusComment.equals(this.qiscusComment)) {
            linkPreviewView.bind(previewData);
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
