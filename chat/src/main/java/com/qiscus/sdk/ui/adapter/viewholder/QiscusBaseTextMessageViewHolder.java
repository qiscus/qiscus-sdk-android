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
import android.text.Spannable;
import android.view.View;
import android.widget.TextView;

import com.qiscus.sdk.Qiscus;
import com.qiscus.sdk.chat.core.data.model.QiscusComment;
import com.qiscus.sdk.data.model.QiscusMentionConfig;
import com.qiscus.sdk.ui.adapter.OnItemClickListener;
import com.qiscus.sdk.ui.adapter.OnLongItemClickListener;
import com.qiscus.sdk.ui.view.ClickableMovementMethod;
import com.qiscus.sdk.chat.core.util.QiscusTextUtil;

/**
 * Created on : September 27, 2016
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
public abstract class QiscusBaseTextMessageViewHolder extends QiscusBaseMessageViewHolder<QiscusComment> {

    @NonNull protected TextView messageTextView;

    public QiscusBaseTextMessageViewHolder(View itemView, OnItemClickListener itemClickListener,
                                           OnLongItemClickListener longItemClickListener) {
        super(itemView, itemClickListener, longItemClickListener);
        messageTextView = getMessageTextView(itemView);
        messageTextView.setMovementMethod(ClickableMovementMethod.getInstance());
        messageTextView.setClickable(false);
        messageTextView.setLongClickable(false);
    }

    @NonNull
    protected abstract TextView getMessageTextView(View itemView);

    @Override
    protected void setUpColor() {
        messageTextView.setTextColor(messageFromMe ? rightBubbleTextColor : leftBubbleTextColor);
        messageTextView.setLinkTextColor(messageFromMe ? rightLinkTextColor : leftLinkTextColor);
        super.setUpColor();
    }

    @Override
    protected void showMessage(QiscusComment qiscusComment) {
        QiscusMentionConfig mentionConfig = Qiscus.getChatConfig().getMentionConfig();
        if (mentionConfig.isEnableMention()) {
            Spannable spannable = QiscusTextUtil.createQiscusSpannableText(
                    qiscusComment.getMessage(),
                    roomMembers,
                    messageFromMe ? mentionConfig.getRightMentionAllColor() : mentionConfig.getLeftMentionAllColor(),
                    messageFromMe ? mentionConfig.getRightMentionOtherColor() : mentionConfig.getLeftMentionOtherColor(),
                    messageFromMe ? mentionConfig.getRightMentionMeColor() : mentionConfig.getLeftMentionMeColor(),
                    mentionConfig.getMentionClickHandler()
            );
            messageTextView.setText(spannable);
        } else {
            messageTextView.setText(qiscusComment.getMessage());
        }
    }
}
