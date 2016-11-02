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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.qiscus.sdk.data.model.QiscusChatRoom;
import com.qiscus.sdk.ui.fragment.QiscusBaseChatFragment;
import com.qiscus.sdk.ui.view.QiscusRecyclerView;

/**
 * Created on : September 28, 2016
 * Author     : zetbaitsu
 * Name       : Zetra
 * Email      : zetra@mail.ugm.ac.id
 * GitHub     : https://github.com/zetbaitsu
 * LinkedIn   : https://id.linkedin.com/in/zetbaitsu
 */
public class CustomChatFragment extends QiscusBaseChatFragment<CustomChatAdapter> {

    private ImageView attachButton;
    private LinearLayout addPanel;

    public static CustomChatFragment newInstance(QiscusChatRoom qiscusChatRoom) {
        CustomChatFragment fragment = new CustomChatFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(CHAT_ROOM_DATA, qiscusChatRoom);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    protected int getResourceLayout() {
        return R.layout.fragment_custom_chat;
    }

    @Override
    protected void onLoadView(View view) {
        super.onLoadView(view);
        swipeRefreshLayout.setProgressViewOffset(false, 0, 128);
        attachButton = (ImageView) view.findViewById(R.id.button_attach);
        addPanel = (LinearLayout) view.findViewById(R.id.add_panel);
        attachButton.setOnClickListener(v -> {
            if (addPanel.getVisibility() == View.GONE) {
                addPanel.startAnimation(animation);
                addPanel.setVisibility(View.VISIBLE);
            } else {
                addPanel.setVisibility(View.GONE);
            }
        });
    }

    @Nullable
    @Override
    protected ViewGroup getEmptyChatHolder(View view) {
        return (ViewGroup) view.findViewById(R.id.empty_chat);
    }

    @NonNull
    @Override
    protected SwipeRefreshLayout getSwipeRefreshLayout(View view) {
        return (SwipeRefreshLayout) view.findViewById(R.id.swipe_layout);
    }

    @NonNull
    @Override
    protected QiscusRecyclerView getMessageRecyclerView(View view) {
        return (QiscusRecyclerView) view.findViewById(R.id.list_message);
    }

    @NonNull
    @Override
    protected EditText getMessageEditText(View view) {
        return (EditText) view.findViewById(R.id.field_message);
    }

    @NonNull
    @Override
    protected ImageView getSendButton(View view) {
        return (ImageView) view.findViewById(R.id.button_send);
    }

    @Nullable
    @Override
    protected View getNewMessageButton(View view) {
        return view.findViewById(R.id.button_new_message);
    }

    @NonNull
    @Override
    protected View getLoadMoreProgressBar(View view) {
        return view.findViewById(R.id.progressBar);
    }

    @Nullable
    @Override
    protected ImageView getEmptyChatImageView(View view) {
        return (ImageView) view.findViewById(R.id.empty_chat_icon);
    }

    @Nullable
    @Override
    protected TextView getEmptyChatTitleView(View view) {
        return (TextView) view.findViewById(R.id.empty_chat_title);
    }

    @Nullable
    @Override
    protected TextView getEmptyChatDescView(View view) {
        return (TextView) view.findViewById(R.id.empty_chat_desc);
    }

    @Nullable
    @Override
    protected ImageView getAddImageButton(View view) {
        return (ImageView) view.findViewById(R.id.button_add_image);
    }

    @Nullable
    @Override
    protected ImageView getTakeImageButton(View view) {
        return (ImageView) view.findViewById(R.id.button_pick_picture);
    }

    @Nullable
    @Override
    protected ImageView getAddFileButton(View view) {
        return (ImageView) view.findViewById(R.id.button_add_file);
    }

    @Nullable
    @Override
    protected ImageButton getChatAudioCancelButton(View view) {
        return (ImageButton) view.findViewById(R.id.chat_audio_cancel);
    }

    @Nullable
    @Override
    protected LinearLayout getLinItemSoundRec(View view) {
        return (LinearLayout) view.findViewById(R.id.linItemSoundRec);
    }

    @Nullable
    @Override
    protected ImageView getButtonAddSound(View view) {
        return (ImageView) view.findViewById(R.id.button_add_sound);
    }

    @Nullable
    @Override
    protected TextView getRecordingTimeText(View view) {
        return (TextView) view.findViewById(R.id.recording_time_text);
    }

    @Override
    protected CustomChatAdapter onCreateChatAdapter() {
        return new CustomChatAdapter(getActivity());
    }
}
