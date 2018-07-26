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
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.qiscus.sdk.Qiscus;
import com.qiscus.sdk.R;
import com.qiscus.sdk.chat.core.data.model.QiscusAccount;
import com.qiscus.sdk.chat.core.data.model.QiscusComment;
import com.qiscus.sdk.ui.adapter.OnItemClickListener;
import com.qiscus.sdk.ui.adapter.OnLongItemClickListener;
import com.qiscus.sdk.util.QiscusRawDataExtractor;
import com.qiscus.sdk.util.QiscusTextUtil;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created on : June 07, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
public abstract class QiscusBaseSystemMessageViewHolder extends QiscusBaseTextMessageViewHolder {

    protected int bubbleBackgroundColor;
    protected Drawable bubbleDrawable;
    protected int bubbleTextColor;

    private QiscusAccount qiscusAccount;

    public QiscusBaseSystemMessageViewHolder(View itemView, OnItemClickListener itemClickListener,
                                             OnLongItemClickListener longItemClickListener) {
        super(itemView, itemClickListener, longItemClickListener);
        qiscusAccount = Qiscus.getQiscusAccount();
    }

    @Override
    protected void loadChatConfig() {
        super.loadChatConfig();
        bubbleTextColor = ContextCompat.getColor(Qiscus.getApps(), Qiscus.getChatConfig().getSystemMessageTextColor());
        bubbleBackgroundColor = ContextCompat.getColor(Qiscus.getApps(), Qiscus.getChatConfig().getSystemMessageBubbleColor());
        bubbleDrawable = ContextCompat.getDrawable(Qiscus.getApps(), R.drawable.qiscus_rounded_accent_light_chat_bg);
        bubbleDrawable.setColorFilter(bubbleBackgroundColor, PorterDuff.Mode.SRC_ATOP);
    }

    @Override
    protected void setUpColor() {
        super.setUpColor();
        messageTextView.setTextColor(bubbleTextColor);
        messageTextView.setLinkTextColor(bubbleTextColor);
        messageBubbleView.setBackground(bubbleDrawable);
    }

    @Nullable
    @Override
    protected ImageView getFirstMessageBubbleIndicatorView(View itemView) {
        return null;
    }

    @Nullable
    @Override
    protected TextView getTimeView(View itemView) {
        return null;
    }

    @Nullable
    @Override
    protected ImageView getMessageStateIndicatorView(View itemView) {
        return null;
    }

    @Nullable
    @Override
    protected ImageView getAvatarView(View itemView) {
        return null;
    }

    @Nullable
    @Override
    protected TextView getSenderNameView(View itemView) {
        return null;
    }

    @Override
    protected void showMessage(QiscusComment qiscusComment) {
        try {
            JSONObject payload = QiscusRawDataExtractor.getPayload(qiscusComment);
            String message = payload.optString("subject_email").equals(qiscusAccount.getEmail()) ?
                    QiscusTextUtil.getString(R.string.qiscus_you) : payload.optString("subject_username");
            switch (payload.optString("type")) {
                case "create_room":
                    message += " " + QiscusTextUtil.getString(R.string.qiscus_created_room);
                    message += " '" + payload.optString("room_name") + "'";
                    break;
                case "add_member":
                    message += " " + QiscusTextUtil.getString(R.string.qiscus_added);
                    message += " " + (payload.optString("object_email").equals(qiscusAccount.getEmail()) ?
                            QiscusTextUtil.getString(R.string.qiscus_you) : payload.optString("object_username"));
                    break;
                case "join_room":
                    message += " " + QiscusTextUtil.getString(R.string.qiscus_joined_room);
                    break;
                case "remove_member":
                    message += " " + QiscusTextUtil.getString(R.string.qiscus_removed);
                    message += " " + (payload.optString("object_email").equals(qiscusAccount.getEmail()) ?
                            QiscusTextUtil.getString(R.string.qiscus_you) : payload.optString("object_username"));
                    break;
                case "left_room":
                    message += " " + QiscusTextUtil.getString(R.string.qiscus_left_room);
                    break;
                case "change_room_name":
                    message += " " + QiscusTextUtil.getString(R.string.qiscus_changed_room_name);
                    message += " '" + payload.optString("room_name") + "'";
                    break;
                case "change_room_avatar":
                    message += " " + QiscusTextUtil.getString(R.string.qiscus_changed_room_avatar);
                    break;
                default:
                    message = qiscusComment.getMessage();
            }
            messageTextView.setText(message);
        } catch (JSONException e) {
            super.showMessage(qiscusComment);
        }
    }
}
