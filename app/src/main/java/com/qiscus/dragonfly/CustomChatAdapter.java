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

package com.qiscus.dragonfly;

import android.content.Context;
import android.view.ViewGroup;

import com.qiscus.sdk.chat.core.data.model.QMessage;
import com.qiscus.sdk.ui.adapter.QiscusChatAdapter;
import com.qiscus.sdk.ui.adapter.viewholder.QiscusBaseMessageViewHolder;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created on : August 22, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
public class CustomChatAdapter extends QiscusChatAdapter {
    private static final int TYPE_LOCKED_MESSAGE = 2333;

    public CustomChatAdapter(Context context, boolean groupChat) {
        super(context, groupChat);
    }

    @Override
    protected int getItemViewTypeCustomMessage(QMessage qiscusMessage, int position) {
        JSONObject payload = qiscusMessage.getPayload();
        if (payload.optString("type").equals("lock_message")) {
            return TYPE_LOCKED_MESSAGE;
        }
        return super.getItemViewTypeCustomMessage(qiscusMessage, position);
    }

    @Override
    protected int getItemResourceLayout(int viewType) {
        switch (viewType) {
            case TYPE_LOCKED_MESSAGE:
                return R.layout.item_message_locked;
            default:
                return super.getItemResourceLayout(viewType);
        }
    }

    @Override
    public QiscusBaseMessageViewHolder<QMessage> onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_LOCKED_MESSAGE:
                return new LockedMessageViewHolder(getView(parent, viewType), itemClickListener, longItemClickListener);
            default:
                return super.onCreateViewHolder(parent, viewType);
        }
    }
}
