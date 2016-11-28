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

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.qiscus.sdk.R;
import com.qiscus.sdk.data.model.QiscusChatRoom;
import com.qiscus.sdk.data.model.QiscusRoomMember;
import com.trello.rxlifecycle.components.support.RxFragment;

public class QiscusGroupDetailFragment extends RxFragment {
    protected static final String CHAT_ROOM_DATA = "chat_room_data";
    protected static final int PICK_IMAGE_REQUEST = 2;

    protected CollapsingToolbarLayout collapsingToolbar;
    protected ImageView groupPicture;
    protected ImageView buttonEditGroupPicture;
    protected TextView groupName;
    protected ImageView buttonEditGroupName;
    protected TextView membersTitle;
    protected ImageView buttonAddMember;
    protected ViewGroup memberContainer;
    protected View buttonLeaveGroup;

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
        collapsingToolbar = (CollapsingToolbarLayout) view.findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setCollapsedTitleTextColor(Color.WHITE);
        collapsingToolbar.setExpandedTitleColor(Color.WHITE);
        groupPicture = (ImageView) view.findViewById(R.id.group_photo);
        buttonEditGroupPicture = (ImageView) view.findViewById(R.id.button_edit_picture);
        groupName = (TextView) view.findViewById(R.id.group_name);
        buttonEditGroupName = (ImageView) view.findViewById(R.id.button_edit_group_name);
        membersTitle = (TextView) view.findViewById(R.id.member_title);
        buttonAddMember = (ImageView) view.findViewById(R.id.button_add_member);
        memberContainer = (ViewGroup) view.findViewById(R.id.member_container);
        buttonLeaveGroup = view.findViewById(R.id.button_leave_group);

        buttonEditGroupPicture.setOnClickListener(v -> pickGroupPicture());
        buttonEditGroupName.setOnClickListener(v -> editGroupName());
        buttonAddMember.setOnClickListener(v -> addNewMember());
        buttonLeaveGroup.setOnClickListener(v -> leaveGroup());
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        onCreateGroupDetailComponents(savedInstanceState);
    }

    protected void onCreateGroupDetailComponents(Bundle savedInstanceState) {
        resolveChatRoom(savedInstanceState);

        collapsingToolbar.setTitle(qiscusChatRoom.getName());
        groupName.setText(qiscusChatRoom.getName());

        Glide.with(this)
                .load(qiscusChatRoom.getLastCommentMessage())
                .dontAnimate()
                .placeholder(R.drawable.ic_qiscus_avatar)
                .error(R.drawable.ic_qiscus_avatar)
                .into(groupPicture);

        membersTitle.setText("Members (" + qiscusChatRoom.getMember().size() + ")");

        for (QiscusRoomMember member : qiscusChatRoom.getMember()) {
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

    protected void pickGroupPicture() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    protected void editGroupName() {
        Toast.makeText(getActivity(), "Edit", Toast.LENGTH_SHORT).show();
    }

    private void addNewMember() {
        Toast.makeText(getActivity(), "Add", Toast.LENGTH_SHORT).show();
    }

    protected void leaveGroup() {
        Toast.makeText(getActivity(), "Leave", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            if (data == null) {
                //showError(getString(R.string.chat_error_failed_open_picture));
                return;
            }
            /*try {
                qiscusChatPresenter.sendFile(QiscusFileUtil.from(data.getData()));
            } catch (IOException e) {
                showError(getString(R.string.chat_error_failed_read_picture));
                e.printStackTrace();
            }*/
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(CHAT_ROOM_DATA, qiscusChatRoom);
    }
}
