package com.qiscus.dragonfly;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
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

    public static CustomChatFragment newInstance(QiscusChatRoom qiscusChatRoom) {
        CustomChatFragment fragment = new CustomChatFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(CHAT_ROOM_DATA, qiscusChatRoom);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    protected int getResourceLayout() {
        return com.qiscus.sdk.R.layout.fragment_qiscus_chat;
    }

    @Nullable
    @Override
    protected ViewGroup getEmptyChatHolder(View view) {
        return (ViewGroup) view.findViewById(com.qiscus.sdk.R.id.empty_chat);
    }

    @NonNull
    @Override
    protected SwipeRefreshLayout getSwipeRefreshLayout(View view) {
        return (SwipeRefreshLayout) view.findViewById(com.qiscus.sdk.R.id.swipe_layout);
    }

    @NonNull
    @Override
    protected QiscusRecyclerView getMessageRecyclerView(View view) {
        return (QiscusRecyclerView) view.findViewById(com.qiscus.sdk.R.id.list_message);
    }

    @NonNull
    @Override
    protected EditText getMessageEditText(View view) {
        return (EditText) view.findViewById(com.qiscus.sdk.R.id.field_message);
    }

    @NonNull
    @Override
    protected ImageView getSendButton(View view) {
        return (ImageView) view.findViewById(com.qiscus.sdk.R.id.button_send);
    }

    @Nullable
    @Override
    protected View getNewMessageButton(View view) {
        return view.findViewById(com.qiscus.sdk.R.id.button_new_message);
    }

    @NonNull
    @Override
    protected View getLoadMoreProgressBar(View view) {
        return view.findViewById(com.qiscus.sdk.R.id.progressBar);
    }

    @Nullable
    @Override
    protected ImageView getEmptyChatImageView(View view) {
        return (ImageView) view.findViewById(com.qiscus.sdk.R.id.empty_chat_icon);
    }

    @Nullable
    @Override
    protected TextView getEmptyChatTitleView(View view) {
        return (TextView) view.findViewById(com.qiscus.sdk.R.id.empty_chat_title);
    }

    @Nullable
    @Override
    protected TextView getEmptyChatDescView(View view) {
        return (TextView) view.findViewById(com.qiscus.sdk.R.id.empty_chat_desc);
    }

    @Nullable
    @Override
    protected ImageView getAddImageButton(View view) {
        return (ImageView) view.findViewById(com.qiscus.sdk.R.id.button_add_image);
    }

    @Nullable
    @Override
    protected ImageView getTakeImageButton(View view) {
        return (ImageView) view.findViewById(com.qiscus.sdk.R.id.button_pick_picture);
    }

    @Nullable
    @Override
    protected ImageView getAddFileButton(View view) {
        return (ImageView) view.findViewById(com.qiscus.sdk.R.id.button_add_file);
    }

    @Override
    protected CustomChatAdapter onCreateChatAdapter() {
        return new CustomChatAdapter(getActivity());
    }
}
