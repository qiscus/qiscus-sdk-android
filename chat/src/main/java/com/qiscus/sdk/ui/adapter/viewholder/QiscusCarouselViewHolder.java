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
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.qiscus.sdk.Qiscus;
import com.qiscus.sdk.R;
import com.qiscus.sdk.chat.core.data.model.QiscusComment;
import com.qiscus.sdk.ui.adapter.OnItemClickListener;
import com.qiscus.sdk.ui.adapter.OnLongItemClickListener;
import com.qiscus.sdk.ui.view.QiscusCarouselItemView;
import com.qiscus.sdk.ui.view.QiscusChatButtonView;
import com.qiscus.sdk.chat.core.util.QiscusRawDataExtractor;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created on : January 02, 2018
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
public class QiscusCarouselViewHolder extends QiscusBaseMessageViewHolder<QiscusComment> {

    private ViewGroup cardsContainer;

    protected int titleTextColor;
    protected int descriptionTextColor;

    protected int buttonsTextColor;
    protected int buttonsBackgroundColor;

    private QiscusCarouselItemView.CarouselItemClickListener carouselItemClickListener;
    private QiscusChatButtonView.ChatButtonClickListener chatButtonClickListener;

    public QiscusCarouselViewHolder(View itemView, OnItemClickListener itemClickListener,
                                    OnLongItemClickListener longItemClickListener,
                                    QiscusCarouselItemView.CarouselItemClickListener carouselItemClickListener,
                                    QiscusChatButtonView.ChatButtonClickListener chatButtonClickListener) {
        super(itemView, itemClickListener, longItemClickListener);
        cardsContainer = itemView.findViewById(R.id.cards_container);
        this.carouselItemClickListener = carouselItemClickListener;
        this.chatButtonClickListener = chatButtonClickListener;
    }

    @Override
    protected void loadChatConfig() {
        super.loadChatConfig();
        titleTextColor = ContextCompat.getColor(Qiscus.getApps(), Qiscus.getChatConfig().getCardTitleColor());
        descriptionTextColor = ContextCompat.getColor(Qiscus.getApps(), Qiscus.getChatConfig().getCardDescriptionColor());
        buttonsTextColor = ContextCompat.getColor(Qiscus.getApps(), Qiscus.getChatConfig().getCardButtonTextColor());
        buttonsBackgroundColor = ContextCompat.getColor(Qiscus.getApps(), Qiscus.getChatConfig().getCardButtonBackground());
    }

    @Nullable
    @Override
    protected ImageView getFirstMessageBubbleIndicatorView(View itemView) {
        return null;
    }

    @NonNull
    @Override
    protected View getMessageBubbleView(View itemView) {
        return itemView.findViewById(R.id.message);
    }

    @Nullable
    @Override
    protected TextView getDateView(View itemView) {
        return (TextView) itemView.findViewById(R.id.date);
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
            setUpCards(payload);
        } catch (JSONException | NullPointerException e) {
            e.printStackTrace();
            cardsContainer.setVisibility(View.GONE);
        }
    }

    private void setUpCards(JSONObject payload) {
        JSONArray cards = payload.optJSONArray("cards");
        int size = cards.length();
        cardsContainer.removeAllViews();
        for (int i = 0; i < size; i++) {
            try {
                QiscusCarouselItemView carouselItemView = new QiscusCarouselItemView(cardsContainer.getContext());
                carouselItemView.setPayload(cards.getJSONObject(i));
                carouselItemView.setTitleTextColor(titleTextColor);
                carouselItemView.setDescriptionTextColor(descriptionTextColor);
                carouselItemView.setButtonsTextColor(buttonsTextColor);
                carouselItemView.setButtonsBackgroundColor(buttonsBackgroundColor);
                carouselItemView.setCarouselItemClickListener(carouselItemClickListener);
                carouselItemView.setChatButtonClickListener(chatButtonClickListener);
                carouselItemView.render();
                cardsContainer.addView(carouselItemView);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        cardsContainer.setVisibility(View.VISIBLE);
    }
}
