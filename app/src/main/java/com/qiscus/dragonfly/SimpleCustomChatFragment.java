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

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;

import com.qiscus.sdk.data.model.QiscusChatRoom;
import com.qiscus.sdk.data.model.QiscusComment;
import com.qiscus.sdk.ui.adapter.QiscusChatAdapter;
import com.qiscus.sdk.ui.fragment.QiscusChatFragment;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created on : October 27, 2016
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
public class SimpleCustomChatFragment extends QiscusChatFragment {
    private View mInputPanel;
    private TextView mLockedView;

    public static SimpleCustomChatFragment newInstance(QiscusChatRoom qiscusChatRoom) {
        SimpleCustomChatFragment fragment = new SimpleCustomChatFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(CHAT_ROOM_DATA, qiscusChatRoom);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    protected int getResourceLayout() {
        return R.layout.simple_custom_fragment_chat;
    }

    @Override
    protected void onLoadView(View view) {
        super.onLoadView(view);
        mInputPanel = view.findViewById(R.id.box);
        mLockedView = (TextView) view.findViewById(R.id.lock);
        mLockedView.setOnLongClickListener(v -> {
            openLockedChat();
            return true;
        });
    }

    @Override
    protected void onCreateChatComponents(Bundle savedInstanceState) {
        super.onCreateChatComponents(savedInstanceState);
        lockChatAfter(2000);
    }

    private void lockChatAfter(int duration) {
        new Handler().postDelayed(() -> {
            mInputPanel.setVisibility(View.GONE);
            mLockedView.setVisibility(View.VISIBLE);
            sendLockedMessage(true);
        }, duration);
    }

    private void openLockedChat() {
        mInputPanel.setVisibility(View.VISIBLE);
        mLockedView.setVisibility(View.GONE);
        lockChatAfter(2000);
        sendLockedMessage(false);
    }

    private void sendLockedMessage(boolean locked) {
        String message = locked ? "Locked" : "Lock Opened";
        String description = locked ? "This chat room is locked" : "This chat room opened";
        JSONObject payload = new JSONObject();
        try {
            payload.put("locked", locked).put("description", description);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        QiscusComment comment = QiscusComment.generateCustomMessage(message, "lock_message", payload,
                qiscusChatRoom.getId(), qiscusChatRoom.getLastTopicId());
        sendQiscusComment(comment);
    }

    @Override
    protected QiscusChatAdapter onCreateChatAdapter() {
        return new CustomChatAdapter(getActivity(), qiscusChatRoom.isGroup());
    }
}
