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
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.qiscus.nirmana.Nirmana;
import com.qiscus.sdk.Qiscus;
import com.qiscus.sdk.R;
import com.qiscus.sdk.chat.core.data.model.QiscusComment;
import com.qiscus.sdk.chat.core.util.QiscusRawDataExtractor;
import com.qiscus.sdk.ui.adapter.OnItemClickListener;
import com.qiscus.sdk.ui.adapter.OnLongItemClickListener;
import com.qiscus.sdk.ui.view.QiscusChatButtonView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created on : June 09, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
public abstract class QiscusBaseCardMessageViewHolder extends QiscusBaseTextMessageViewHolder {

    @NonNull
    protected View cardView;
    @Nullable
    protected ImageView imageView;
    @Nullable
    protected TextView titleView;
    @Nullable
    protected TextView descriptionView;
    @Nullable
    protected ViewGroup buttonsContainer;

    protected int titleTextColor;
    protected int descriptionTextColor;

    protected int buttonsTextColor;
    protected int buttonsBackgroundColor;

    private QiscusChatButtonView.ChatButtonClickListener chatButtonClickListener;

    public QiscusBaseCardMessageViewHolder(View itemView, OnItemClickListener itemClickListener,
                                           OnLongItemClickListener longItemClickListener,
                                           QiscusChatButtonView.ChatButtonClickListener chatButtonClickListener) {
        super(itemView, itemClickListener, longItemClickListener);
        this.chatButtonClickListener = chatButtonClickListener;
        cardView = getCardView(itemView);
        imageView = getImageView(itemView);
        titleView = getTitleView(itemView);
        descriptionView = getDescriptionView(itemView);
        buttonsContainer = getButtonsContainer(itemView);
    }

    @NonNull
    protected abstract View getCardView(View itemView);

    @Nullable
    protected abstract ImageView getImageView(View itemView);

    @Nullable
    protected abstract TextView getTitleView(View itemView);

    @Nullable
    protected abstract TextView getDescriptionView(View itemView);

    @Nullable
    protected abstract ViewGroup getButtonsContainer(View itemView);

    @Override
    protected void loadChatConfig() {
        super.loadChatConfig();
        titleTextColor = ContextCompat.getColor(Qiscus.getApps(), Qiscus.getChatConfig().getCardTitleColor());
        descriptionTextColor = ContextCompat.getColor(Qiscus.getApps(), Qiscus.getChatConfig().getCardDescriptionColor());
        buttonsTextColor = ContextCompat.getColor(Qiscus.getApps(), Qiscus.getChatConfig().getCardButtonTextColor());
        buttonsBackgroundColor = ContextCompat.getColor(Qiscus.getApps(), Qiscus.getChatConfig().getCardButtonBackground());
    }

    @Override
    protected void setUpColor() {
        super.setUpColor();
        if (titleView != null) {
            titleView.setTextColor(titleTextColor);
        }
        if (descriptionView != null) {
            descriptionView.setTextColor(descriptionTextColor);
        }
    }

    @Override
    protected void showMessage(QiscusComment qiscusComment) {
        super.showMessage(qiscusComment);
        try {
            JSONObject payload = QiscusRawDataExtractor.getPayload(qiscusComment);
            setUpCard(payload);
        } catch (JSONException | NullPointerException e) {
            e.printStackTrace();
            cardView.setVisibility(View.GONE);
        }

        if (TextUtils.isEmpty(qiscusComment.getMessage().trim())) {
            messageBubbleView.setVisibility(View.GONE);
            if (firstMessageBubbleIndicatorView != null) {
                firstMessageBubbleIndicatorView.setVisibility(View.GONE);
            }
        }
    }

    private void setUpCard(JSONObject payload) {
        cardView.setOnClickListener(v -> new CustomTabsIntent.Builder()
                .setToolbarColor(ContextCompat.getColor(Qiscus.getApps(), Qiscus.getChatConfig().getAppBarColor()))
                .setShowTitle(true)
                .addDefaultShareMenuItem()
                .enableUrlBarHiding()
                .build()
                .launchUrl(cardView.getContext(), Uri.parse(payload.optString("url"))));

        if (imageView != null) {
            Nirmana.getInstance().get()
                    .setDefaultRequestOptions(new RequestOptions()
                            .dontAnimate()
                            .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                            .placeholder(R.drawable.qiscus_image_placeholder)
                            .error(R.drawable.qiscus_image_placeholder))
                    .load(payload.optString("image", ""))
                    .into(imageView);
        }
        if (titleView != null) {
            titleView.setText(payload.optString("title", ""));
        }
        if (descriptionView != null) {
            descriptionView.setText(payload.optString("description", ""));
        }

        try {
            setUpButtons(payload.getJSONArray("buttons"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    protected void setUpButtons(JSONArray buttons) {
        if (buttonsContainer == null || buttons == null) {
            return;
        }

        buttonsContainer.removeAllViews();

        int size = buttons.length();
        if (size < 1) {
            return;
        }
        List<QiscusChatButtonView> buttonViews = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            try {
                JSONObject jsonButton = buttons.getJSONObject(i);
                String type = jsonButton.optString("type", "");
                if ("postback".equals(type)) {
                    QiscusChatButtonView button = new QiscusChatButtonView(buttonsContainer.getContext(), jsonButton);
                    button.setChatButtonClickListener(chatButtonClickListener);
                    buttonViews.add(button);
                } else if ("link".equals(type)) {
                    QiscusChatButtonView button = new QiscusChatButtonView(buttonsContainer.getContext(), jsonButton);
                    button.setChatButtonClickListener(jsonButton1 ->
                            openLink(jsonButton1.optJSONObject("payload").optString("url")));
                    buttonViews.add(button);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        for (int i = 0; i < buttonViews.size(); i++) {
            buttonViews.get(i).getButton().setBackgroundColor(buttonsBackgroundColor);
            buttonViews.get(i).getButton().setTextColor(buttonsTextColor);
            buttonsContainer.addView(buttonViews.get(i));
        }

        buttonsContainer.setVisibility(View.VISIBLE);
    }

    private void openLink(String url) {
        new CustomTabsIntent.Builder()
                .setToolbarColor(ContextCompat.getColor(Qiscus.getApps(), Qiscus.getChatConfig().getAppBarColor()))
                .setShowTitle(true)
                .addDefaultShareMenuItem()
                .enableUrlBarHiding()
                .build()
                .launchUrl(messageTextView.getContext(), Uri.parse(url));
    }
}
