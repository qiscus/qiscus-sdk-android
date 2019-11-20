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

import com.qiscus.sdk.chat.core.data.model.QChatRoom;
import com.qiscus.sdk.chat.core.data.model.QiscusComment;
import com.qiscus.sdk.chat.core.data.remote.QiscusApi;
import com.qiscus.sdk.ui.adapter.QiscusChatAdapter;
import com.qiscus.sdk.ui.fragment.QiscusChatFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created on : October 27, 2016
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
public class SimpleCustomChatFragment extends QiscusChatFragment {
    private View mInputPanel;
    private TextView mLockedView;

    public static SimpleCustomChatFragment newInstance(QChatRoom qChatRoom) {
        SimpleCustomChatFragment fragment = new SimpleCustomChatFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(CHAT_ROOM_DATA, qChatRoom);
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
        mLockedView = view.findViewById(R.id.lock);
        mLockedView.setOnLongClickListener(v -> {
            openLockedChat();
            return true;
        });
    }

    @Override
    protected void onCreateChatComponents(Bundle savedInstanceState) {
        super.onCreateChatComponents(savedInstanceState);
        lockChatAfter(10000);
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
        lockChatAfter(10000);
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
        QiscusComment comment = QiscusComment.generateCustomMessage(qChatRoom.getId(), message, "lock_message", payload);
        sendQiscusComment(comment);
    }

    @Override
    protected QiscusChatAdapter onCreateChatAdapter() {
        return new CustomChatAdapter(getActivity(), qChatRoom.getType().equals("group"));
    }

    @Override
    protected void onCustomCommentClick(QiscusComment qiscusComment) {
        Toast.makeText(getActivity(), qiscusComment.getMessage(), Toast.LENGTH_SHORT).show();
    }

    public void actionClearComments() {
        ArrayList<Long> roomIds = new ArrayList<>();
        roomIds.add(qChatRoom.getId());
        QiscusApi.getInstance()
                .clearMessagesByChatRoomIds(roomIds)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aVoid -> {
                    chatAdapter.clear();
                    chatAdapter.notifyDataSetChanged();

                }, e -> Toast.makeText(getActivity(), "Cant clear comments", Toast.LENGTH_SHORT).show());
    }
}
