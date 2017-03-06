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

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.TextView;

import com.qiscus.sdk.Qiscus;
import com.qiscus.sdk.R;
import com.qiscus.sdk.data.model.QiscusComment;
import com.qiscus.sdk.ui.adapter.OnItemClickListener;
import com.qiscus.sdk.ui.adapter.OnLongItemClickListener;

/**
 * Created on : March 01, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
public abstract class QiscusBaseAccountLinkingMessageViewHolder extends QiscusBaseTextMessageViewHolder {

    @NonNull protected TextView accountLinkingView;

    protected int accountLinkingTextColor;
    protected Drawable accountLinkingBackgroundDrawable;
    protected int accountLinkingBackgroundColor;
    protected String accountLinkingText;

    private OnItemClickListener itemClickListener;

    public QiscusBaseAccountLinkingMessageViewHolder(View itemView, OnItemClickListener itemClickListener,
                                                     OnLongItemClickListener longItemClickListener) {
        super(itemView, itemClickListener, longItemClickListener);
        this.itemClickListener = itemClickListener;
        accountLinkingView = getAccountLinkingView(itemView);
        accountLinkingView.setOnClickListener(this);
    }

    @Override
    protected void loadChatConfig() {
        super.loadChatConfig();
        accountLinkingText = Qiscus.getChatConfig().getAccountLinkingText();
        accountLinkingTextColor = ContextCompat.getColor(Qiscus.getApps(), Qiscus.getChatConfig().getAccountLinkingTextColor());
        accountLinkingBackgroundColor = ContextCompat.getColor(Qiscus.getApps(), Qiscus.getChatConfig().getAccountLinkingBackground());
        accountLinkingBackgroundDrawable = ContextCompat.getDrawable(Qiscus.getApps(), R.drawable.qiscus_button_account_linking_bg);
        accountLinkingBackgroundDrawable.setColorFilter(accountLinkingBackgroundColor, PorterDuff.Mode.SRC_ATOP);
    }

    @Override
    protected void setUpColor() {
        super.setUpColor();
        accountLinkingView.setTextColor(accountLinkingTextColor);
        accountLinkingView.setBackground(accountLinkingBackgroundDrawable);
    }

    @NonNull
    protected abstract TextView getAccountLinkingView(View itemView);

    @Override
    protected void showMessage(QiscusComment qiscusComment) {
        super.showMessage(qiscusComment);
        accountLinkingView.setText(accountLinkingText);
    }

    @Override
    public void onClick(View v) {
        if (v.equals(accountLinkingView)) {
            itemClickListener.onItemClick(v, getAdapterPosition());
        }
    }
}
