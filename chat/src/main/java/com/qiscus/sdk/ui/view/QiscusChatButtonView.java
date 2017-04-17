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

package com.qiscus.sdk.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.qiscus.sdk.R;

import org.json.JSONObject;

/**
 * Created on : April 06, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
public class QiscusChatButtonView extends FrameLayout implements View.OnClickListener {
    private TextView button;
    private JSONObject jsonButton;
    private ChatButtonClickListener chatButtonClickListener;

    public QiscusChatButtonView(Context context, JSONObject jsonButton) {
        super(context);
        this.jsonButton = jsonButton;
        injectViews();
        initLayout();
    }

    public QiscusChatButtonView(Context context, AttributeSet attrs) {
        super(context, attrs);
        injectViews();
    }

    private void injectViews() {
        inflate(getContext(), R.layout.view_qiscus_chat_button, this);
        button = (TextView) findViewById(R.id.button);
    }

    private void initLayout() {
        button.setText(jsonButton.optString("label", "Button"));
        button.setOnClickListener(this);
    }

    public TextView getButton() {
        return button;
    }

    public void setChatButtonClickListener(ChatButtonClickListener chatButtonClickListener) {
        this.chatButtonClickListener = chatButtonClickListener;
    }

    @Override
    public void onClick(View v) {
        chatButtonClickListener.onChatButtonClick(jsonButton);
    }

    public interface ChatButtonClickListener {
        void onChatButtonClick(JSONObject jsonButton);
    }
}
