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
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.qiscus.sdk.Qiscus;
import com.qiscus.sdk.R;
import com.qiscus.sdk.data.model.QiscusComment;
import com.qiscus.sdk.ui.adapter.OnItemClickListener;
import com.qiscus.sdk.ui.adapter.OnLongItemClickListener;

/**
 * Created on : August 18, 2016
 * Author     : zetbaitsu
 * Name       : Zetra
 * Email      : zetra@mail.ugm.ac.id
 * GitHub     : https://github.com/zetbaitsu
 * LinkedIn   : https://id.linkedin.com/in/zetbaitsu
 */
public abstract class QiscusBaseMessageViewHolder<Data extends QiscusComment> extends RecyclerView.ViewHolder implements
        View.OnClickListener, View.OnLongClickListener {

    @Nullable protected ImageView firstMessageBubbleIndicatorView;
    @NonNull protected View messageBubbleView;
    @Nullable protected TextView dateView;
    @Nullable protected TextView timeView;
    @Nullable protected ImageView messageStateIndicatorView;

    private OnItemClickListener itemClickListener;
    private OnLongItemClickListener longItemClickListener;

    protected boolean needToShowDate;
    protected boolean messageFromMe;
    protected boolean needToShowFirstMessageBubbleIndicator;

    protected Drawable rightBubbleDrawable;
    protected Drawable leftBubbleDrawable;
    protected int rightBubbleColor;
    protected int leftBubbleColor;
    protected int rightBubbleTextColor;
    protected int leftBubbleTextColor;
    protected int rightBubbleTimeColor;
    protected int leftBubbleTimeColor;
    protected int failedToSendMessageColor;
    protected int dateColor;

    public QiscusBaseMessageViewHolder(View itemView, OnItemClickListener itemClickListener, OnLongItemClickListener longItemClickListener) {
        super(itemView);
        this.itemClickListener = itemClickListener;
        this.longItemClickListener = longItemClickListener;

        firstMessageBubbleIndicatorView = getFirstMessageBubbleIndicatorView(itemView);
        messageBubbleView = getMessageBubbleView(itemView);
        dateView = getDateView(itemView);
        timeView = getTimeView(itemView);
        messageStateIndicatorView = getMessageStateIndicatorView(itemView);

        messageBubbleView.setOnClickListener(this);
        messageBubbleView.setOnLongClickListener(this);
        loadChatConfig();
    }

    protected void loadChatConfig() {
        rightBubbleColor = ContextCompat.getColor(Qiscus.getApps(), Qiscus.getChatConfig().getRightBubbleColor());
        rightBubbleTextColor = ContextCompat.getColor(Qiscus.getApps(), Qiscus.getChatConfig().getRightBubbleTextColor());
        rightBubbleTimeColor = ContextCompat.getColor(Qiscus.getApps(), Qiscus.getChatConfig().getRightBubbleTimeColor());
        rightBubbleDrawable = ContextCompat.getDrawable(Qiscus.getApps(), R.drawable.qiscus_rounded_primary_light_chat_bg);
        rightBubbleDrawable.setColorFilter(rightBubbleColor, PorterDuff.Mode.SRC_ATOP);

        leftBubbleColor = ContextCompat.getColor(Qiscus.getApps(), Qiscus.getChatConfig().getLeftBubbleColor());
        leftBubbleTextColor = ContextCompat.getColor(Qiscus.getApps(), Qiscus.getChatConfig().getLeftBubbleTextColor());
        leftBubbleTimeColor = ContextCompat.getColor(Qiscus.getApps(), Qiscus.getChatConfig().getLeftBubbleTimeColor());
        leftBubbleDrawable = ContextCompat.getDrawable(Qiscus.getApps(), R.drawable.qiscus_rounded_primary_chat_bg);
        leftBubbleDrawable.setColorFilter(leftBubbleColor, PorterDuff.Mode.SRC_ATOP);

        failedToSendMessageColor = ContextCompat.getColor(Qiscus.getApps(), Qiscus.getChatConfig().getFailedToSendMessageColor());
        dateColor = ContextCompat.getColor(Qiscus.getApps(), Qiscus.getChatConfig().getDateColor());
    }

    @Nullable
    protected abstract ImageView getFirstMessageBubbleIndicatorView(View itemView);

    @NonNull
    protected abstract View getMessageBubbleView(View itemView);

    @Nullable
    protected abstract TextView getDateView(View itemView);

    @Nullable
    protected abstract TextView getTimeView(View itemView);

    @Nullable
    protected abstract ImageView getMessageStateIndicatorView(View itemView);

    public void setNeedToShowDate(boolean needToShowDate) {
        this.needToShowDate = needToShowDate;
    }

    public boolean isNeedToShowDate() {
        return needToShowDate;
    }

    public void setMessageFromMe(boolean messageFromMe) {
        this.messageFromMe = messageFromMe;
    }

    public void setNeedToShowFirstMessageBubbleIndicator(boolean needToShowFirstMessageBubbleIndicator) {
        this.needToShowFirstMessageBubbleIndicator = needToShowFirstMessageBubbleIndicator;
    }

    public void bind(Data qiscusComment) {
        setUpColor();

        showDateOrNot(qiscusComment);
        showTime(qiscusComment);
        showIconReadOrNot(qiscusComment);

        if (firstMessageBubbleIndicatorView != null) {
            firstMessageBubbleIndicatorView.setVisibility(needToShowFirstMessageBubbleIndicator ? View.VISIBLE : View.GONE);
        }

        showMessage(qiscusComment);
    }

    protected abstract void showMessage(Data qiscusComment);

    protected void setUpColor() {
        if (messageFromMe) {
            messageBubbleView.setBackground(rightBubbleDrawable);

            if (firstMessageBubbleIndicatorView != null) {
                firstMessageBubbleIndicatorView.setColorFilter(rightBubbleColor);
            }

        } else {
            messageBubbleView.setBackground(leftBubbleDrawable);

            if (firstMessageBubbleIndicatorView != null) {
                firstMessageBubbleIndicatorView.setColorFilter(leftBubbleColor);
            }
        }

        if (dateView != null) {
            dateView.setTextColor(dateColor);
        }
    }

    protected void showTime(QiscusComment qiscusComment) {
        if (timeView != null) {
            if (qiscusComment.getState() == QiscusComment.STATE_FAILED) {
                timeView.setText(R.string.sending_failed);
                timeView.setTextColor(failedToSendMessageColor);
            } else {
                timeView.setText(Qiscus.getChatConfig().getTimeFormat().format(qiscusComment.getTime()));
                timeView.setTextColor(messageFromMe ? rightBubbleTimeColor : leftBubbleTimeColor);
            }
        }
    }

    protected void showIconReadOrNot(QiscusComment qiscusComment) {
        if (messageStateIndicatorView != null) {
            messageStateIndicatorView.setColorFilter(qiscusComment.getState() == QiscusComment.STATE_FAILED ?
                    failedToSendMessageColor : rightBubbleTimeColor);
            switch (qiscusComment.getState()) {
                case QiscusComment.STATE_SENDING:
                    messageStateIndicatorView.setImageResource(R.drawable.ic_qiscus_info_time);
                    break;
                case QiscusComment.STATE_ON_QISCUS:
                    messageStateIndicatorView.setImageResource(R.drawable.ic_qiscus_sending);
                    break;
                case QiscusComment.STATE_ON_PUSHER:
                    messageStateIndicatorView.setImageResource(R.drawable.ic_qiscus_read);
                    break;
                case QiscusComment.STATE_FAILED:
                    messageStateIndicatorView.setImageResource(R.drawable.ic_qiscus_sending_failed);
                    break;
            }
        }
    }

    protected void showDateOrNot(QiscusComment qiscusComment) {
        if (dateView != null) {
            if (needToShowDate) {
                dateView.setText(Qiscus.getChatConfig().getDateFormat().format(qiscusComment.getTime()));
                dateView.setVisibility(View.VISIBLE);
            } else {
                dateView.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (v.equals(messageBubbleView)) {
            itemClickListener.onItemClick(v, getAdapterPosition());
        }
    }

    @Override
    public boolean onLongClick(View v) {
        if (longItemClickListener != null) {
            longItemClickListener.onLongItemClick(v, getAdapterPosition());
            return true;
        }
        return false;
    }
}
