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
import android.view.View;

import com.qiscus.sdk.data.model.QiscusComment;
import com.qiscus.sdk.ui.adapter.OnItemClickListener;
import com.qiscus.sdk.ui.adapter.OnLongItemClickListener;
import com.qiscus.sdk.ui.view.QiscusLinkPreviewView;
import com.schinizer.rxunfurl.model.PreviewData;

/**
 * Created on : December 09, 2016
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
public abstract class QiscusBaseLinkViewHolder extends QiscusBaseTextMessageViewHolder implements QiscusComment.LinkPreviewListener {
    @NonNull protected QiscusLinkPreviewView linkPreviewView;

    private QiscusComment qiscusComment;

    public QiscusBaseLinkViewHolder(View itemView, OnItemClickListener itemClickListener,
                                    OnLongItemClickListener longItemClickListener) {
        super(itemView, itemClickListener, longItemClickListener);
        linkPreviewView = getLinkPreviewView(itemView);
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
}
