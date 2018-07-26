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
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;

import com.qiscus.sdk.Qiscus;
import com.qiscus.sdk.R;
import com.qiscus.sdk.chat.core.data.model.QiscusComment;
import com.qiscus.sdk.ui.adapter.OnItemClickListener;
import com.qiscus.sdk.ui.adapter.OnLongItemClickListener;
import com.qiscus.sdk.ui.view.QiscusChatButtonView;
import com.qiscus.sdk.chat.core.util.QiscusRawDataExtractor;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created on : March 01, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
public abstract class QiscusBaseButtonMessageViewHolder extends QiscusBaseTextMessageViewHolder {

    @NonNull protected ViewGroup buttonsContainer;

    protected int buttonsTextColor;
    protected Drawable buttonsBackgroundDrawable;
    protected int buttonsBackgroundColor;

    private QiscusChatButtonView.ChatButtonClickListener chatButtonClickListener;

    public QiscusBaseButtonMessageViewHolder(View itemView, OnItemClickListener itemClickListener,
                                             OnLongItemClickListener longItemClickListener,
                                             QiscusChatButtonView.ChatButtonClickListener chatButtonClickListener) {
        super(itemView, itemClickListener, longItemClickListener);
        this.chatButtonClickListener = chatButtonClickListener;
        buttonsContainer = getButtonsContainer(itemView);
    }

    @Override
    protected void loadChatConfig() {
        super.loadChatConfig();
        buttonsTextColor = ContextCompat.getColor(Qiscus.getApps(), Qiscus.getChatConfig().getButtonBubbleTextColor());
        buttonsBackgroundColor = ContextCompat.getColor(Qiscus.getApps(), Qiscus.getChatConfig().getButtonBubbleBackBackground());
        buttonsBackgroundDrawable = ContextCompat.getDrawable(Qiscus.getApps(), R.drawable.qiscus_button_chat_bg);
        buttonsBackgroundDrawable.setColorFilter(buttonsBackgroundColor, PorterDuff.Mode.SRC_ATOP);
    }

    @NonNull
    protected abstract ViewGroup getButtonsContainer(View itemView);

    @Override
    protected void showMessage(QiscusComment qiscusComment) {
        super.showMessage(qiscusComment);
        buttonsContainer.removeAllViews();
        try {
            JSONObject payload = QiscusRawDataExtractor.getPayload(qiscusComment);
            setUpButtons(payload.getJSONArray("buttons"));
        } catch (JSONException | NullPointerException e) {
            buttonsContainer.setVisibility(View.GONE);
        }
    }

    protected void setUpButtons(JSONArray buttons) {
        if (buttons == null) {
            return;
        }

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
            if (i == buttonViews.size() - 1) {
                buttonViews.get(i).getButton().setBackground(buttonsBackgroundDrawable);
            } else {
                buttonViews.get(i).getButton().setBackgroundColor(buttonsBackgroundColor);
            }
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
