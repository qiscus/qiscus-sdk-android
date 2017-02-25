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
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
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
public abstract class QiscusBaseMessageViewHolder<E extends QiscusComment> extends RecyclerView.ViewHolder implements
        View.OnClickListener, View.OnLongClickListener {

    @Nullable protected ImageView firstMessageBubbleIndicatorView;
    @NonNull protected View messageBubbleView;
    @Nullable protected TextView dateView;
    @Nullable protected TextView timeView;
    @Nullable protected ImageView messageStateIndicatorView;
    @Nullable protected ImageView avatarView;
    @Nullable protected TextView senderNameView;

    private OnItemClickListener itemClickListener;
    private OnLongItemClickListener longItemClickListener;

    protected boolean needToShowDate;
    protected boolean messageFromMe;
    protected boolean needToShowFirstMessageBubbleIndicator;
    protected boolean groupChat;

    protected Drawable rightBubbleDrawable;
    protected Drawable leftBubbleDrawable;
    protected int rightBubbleColor;
    protected int leftBubbleColor;
    protected int rightBubbleTextColor;
    protected int leftBubbleTextColor;
    protected int rightBubbleTimeColor;
    protected int leftBubbleTimeColor;
    protected int rightLinkTextColor;
    protected int leftLinkTextColor;
    protected int failedToSendMessageColor;
    protected int readIconColor;
    protected int dateColor;
    protected Drawable selectionBackground;

    public QiscusBaseMessageViewHolder(View itemView, OnItemClickListener itemClickListener,
                                       OnLongItemClickListener longItemClickListener) {
        super(itemView);
        this.itemClickListener = itemClickListener;
        this.longItemClickListener = longItemClickListener;

        firstMessageBubbleIndicatorView = getFirstMessageBubbleIndicatorView(itemView);
        messageBubbleView = getMessageBubbleView(itemView);
        dateView = getDateView(itemView);
        timeView = getTimeView(itemView);
        messageStateIndicatorView = getMessageStateIndicatorView(itemView);
        avatarView = getAvatarView(itemView);
        senderNameView = getSenderNameView(itemView);

        messageBubbleView.setOnClickListener(this);
        messageBubbleView.setOnLongClickListener(this);
        loadChatConfig();
    }

    protected void loadChatConfig() {
        rightBubbleColor = ContextCompat.getColor(Qiscus.getApps(), Qiscus.getChatConfig().getRightBubbleColor());
        rightBubbleTextColor = ContextCompat.getColor(Qiscus.getApps(), Qiscus.getChatConfig().getRightBubbleTextColor());
        rightBubbleTimeColor = ContextCompat.getColor(Qiscus.getApps(), Qiscus.getChatConfig().getRightBubbleTimeColor());
        rightLinkTextColor = ContextCompat.getColor(Qiscus.getApps(), Qiscus.getChatConfig().getRightLinkTextColor());
        rightBubbleDrawable = ContextCompat.getDrawable(Qiscus.getApps(), R.drawable.qiscus_rounded_primary_light_chat_bg);
        rightBubbleDrawable.setColorFilter(rightBubbleColor, PorterDuff.Mode.SRC_ATOP);

        leftBubbleColor = ContextCompat.getColor(Qiscus.getApps(), Qiscus.getChatConfig().getLeftBubbleColor());
        leftBubbleTextColor = ContextCompat.getColor(Qiscus.getApps(), Qiscus.getChatConfig().getLeftBubbleTextColor());
        leftBubbleTimeColor = ContextCompat.getColor(Qiscus.getApps(), Qiscus.getChatConfig().getLeftBubbleTimeColor());
        leftLinkTextColor = ContextCompat.getColor(Qiscus.getApps(), Qiscus.getChatConfig().getLeftLinkTextColor());
        leftBubbleDrawable = ContextCompat.getDrawable(Qiscus.getApps(), R.drawable.qiscus_rounded_primary_chat_bg);
        leftBubbleDrawable.setColorFilter(leftBubbleColor, PorterDuff.Mode.SRC_ATOP);

        failedToSendMessageColor = ContextCompat.getColor(Qiscus.getApps(), Qiscus.getChatConfig().getFailedToSendMessageColor());
        readIconColor = ContextCompat.getColor(Qiscus.getApps(), Qiscus.getChatConfig().getReadIconColor());
        dateColor = ContextCompat.getColor(Qiscus.getApps(), Qiscus.getChatConfig().getDateColor());
        selectionBackground = new ColorDrawable(ContextCompat.getColor(Qiscus.getApps(),
                Qiscus.getChatConfig().getSelectedBubbleBackgroundColor()));
        selectionBackground.setAlpha(51);
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

    @Nullable
    protected abstract ImageView getAvatarView(View itemView);

    @Nullable
    protected abstract TextView getSenderNameView(View itemView);

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

    public void setGroupChat(boolean groupChat) {
        this.groupChat = groupChat;
    }

    public void bind(E qiscusComment) {
        setUpColor();

        showDateOrNot(qiscusComment);
        showTime(qiscusComment);
        showIconReadOrNot(qiscusComment);

        if (firstMessageBubbleIndicatorView != null) {
            firstMessageBubbleIndicatorView.setVisibility(needToShowFirstMessageBubbleIndicator ? View.VISIBLE : View.GONE);
        }

        showSenderAvatar(qiscusComment);
        showSenderName(qiscusComment);

        showMessage(qiscusComment);

        onCommentSelected(qiscusComment);
    }

    private void showSenderName(E qiscusComment) {
        if (senderNameView != null && !messageFromMe && groupChat) {
            if (needToShowFirstMessageBubbleIndicator) {
                senderNameView.setVisibility(View.VISIBLE);
                senderNameView.setText(String.format("~ %s", qiscusComment.getSender()));
            } else {
                senderNameView.setVisibility(View.GONE);
            }

        }
    }

    private void showSenderAvatar(E qiscusComment) {
        if (avatarView != null && !messageFromMe) {
            if (needToShowFirstMessageBubbleIndicator) {
                avatarView.setVisibility(View.VISIBLE);
                Glide.with(avatarView.getContext())
                        .load(qiscusComment.getSenderAvatar())
                        .dontAnimate()
                        .placeholder(R.drawable.ic_qiscus_avatar)
                        .error(R.drawable.ic_qiscus_avatar)
                        .into(avatarView);
            } else {
                avatarView.setVisibility(View.GONE);
            }
        }
    }

    protected abstract void showMessage(E qiscusComment);

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
            switch (qiscusComment.getState()) {
                case QiscusComment.STATE_SENDING:
                    messageStateIndicatorView.setColorFilter(rightBubbleTimeColor);
                    messageStateIndicatorView.setImageResource(R.drawable.ic_qiscus_info_time);
                    break;
                case QiscusComment.STATE_ON_QISCUS:
                    messageStateIndicatorView.setColorFilter(rightBubbleTimeColor);
                    messageStateIndicatorView.setImageResource(R.drawable.ic_qiscus_sending);
                    break;
                case QiscusComment.STATE_DELIVERED:
                    messageStateIndicatorView.setColorFilter(rightBubbleTimeColor);
                    messageStateIndicatorView.setImageResource(R.drawable.ic_qiscus_read);
                    break;
                case QiscusComment.STATE_READ:
                    messageStateIndicatorView.setColorFilter(readIconColor);
                    messageStateIndicatorView.setImageResource(R.drawable.ic_qiscus_read);
                    break;
                case QiscusComment.STATE_FAILED:
                    messageStateIndicatorView.setColorFilter(failedToSendMessageColor);
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

    protected void onCommentSelected(E qiscusComment) {
        itemView.setBackground(qiscusComment.isSelected() ? selectionBackground : null);
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
