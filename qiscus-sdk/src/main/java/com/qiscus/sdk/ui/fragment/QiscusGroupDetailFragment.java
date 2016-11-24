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

package com.qiscus.sdk.ui.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.qiscus.sdk.R;
import com.qiscus.sdk.data.model.QiscusChatRoom;
import com.trello.rxlifecycle.components.support.RxFragment;

public class QiscusGroupDetailFragment extends RxFragment {
    protected static final String CHAT_ROOM_DATA = "chat_room_data";

    private ViewGroup memberContainer;

    protected QiscusChatRoom qiscusChatRoom;

    public static QiscusGroupDetailFragment newInstance(QiscusChatRoom qiscusChatRoom) {
        QiscusGroupDetailFragment fragment = new QiscusGroupDetailFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(CHAT_ROOM_DATA, qiscusChatRoom);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(getResourceLayout(), container, false);
        onLoadView(view);
        return view;
    }

    protected int getResourceLayout() {
        return R.layout.fragment_qiscus_group_detail;
    }

    protected void onLoadView(View view) {
        CollapsingToolbarLayout collapsingToolbar = (CollapsingToolbarLayout) view.findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setCollapsedTitleTextColor(Color.WHITE);
        collapsingToolbar.setExpandedTitleColor(Color.WHITE);
        collapsingToolbar.setTitle("Senam Bugar");

        memberContainer = (ViewGroup) view.findViewById(R.id.member_container);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        onCreateGroupDetailComponents(savedInstanceState);
    }

    protected void onCreateGroupDetailComponents(Bundle savedInstanceState) {
        resolveChatRoom(savedInstanceState);
        for (int i = 0; i < 30; i++) {
            View item = LayoutInflater.from(getActivity()).inflate(R.layout.item_qiscus_group_member, null);
            memberContainer.addView(item);
        }
    }

    protected void resolveChatRoom(Bundle savedInstanceState) {
        qiscusChatRoom = getArguments().getParcelable(CHAT_ROOM_DATA);
        if (qiscusChatRoom == null && savedInstanceState != null) {
            qiscusChatRoom = savedInstanceState.getParcelable(CHAT_ROOM_DATA);
        }

        if (qiscusChatRoom == null) {
            getActivity().finish();
            return;
        }
    }
}
