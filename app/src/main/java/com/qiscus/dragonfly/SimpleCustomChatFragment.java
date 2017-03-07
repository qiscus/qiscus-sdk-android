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
import android.widget.Toast;

import com.qiscus.sdk.data.model.QiscusChatRoom;
import com.qiscus.sdk.ui.fragment.QiscusChatFragment;

/**
 * Created on : October 27, 2016
 * Author     : zetbaitsu
 * Name       : Zetra
 * Email      : zetra@mail.ugm.ac.id
 * GitHub     : https://github.com/zetbaitsu
 * LinkedIn   : https://id.linkedin.com/in/zetbaitsu
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

    @Override
    public void sendMessage(String message) {
        if (chatAdapter.isEmpty()) {
            Toast.makeText(getActivity(), "First message sent!", Toast.LENGTH_SHORT).show();
        }
        super.sendMessage(message);
    }

    private void lockChatAfter(int duration) {
        new Handler().postDelayed(() -> {
            mInputPanel.setVisibility(View.GONE);
            mLockedView.setVisibility(View.VISIBLE);
        }, duration);
    }

    private void openLockedChat() {
        mInputPanel.setVisibility(View.VISIBLE);
        mLockedView.setVisibility(View.GONE);
        lockChatAfter(2000);
    }
}
