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
import android.widget.FrameLayout;
import android.widget.TextView;

import com.qiscus.sdk.R;
import com.qiscus.sdk.data.model.QiscusRoomMember;

/**
 * Created on : November 29, 2016
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
public class QiscusGroupMemberView extends FrameLayout {
    private QiscusCircularImageView imageViewAvatar;
    private TextView textViewName;
    private TextView textViewEmail;

    private QiscusRoomMember user;

    public QiscusGroupMemberView(Context context, QiscusRoomMember user) {
        this(context, user, null);
    }

    public QiscusGroupMemberView(Context context, QiscusRoomMember user, OnClickListener clickListener) {
        super(context);
        this.user = user;
        injectViews();
        initLayout();
        setOnClickListener(clickListener);
    }

    public QiscusGroupMemberView(Context context, AttributeSet attrs) {
        super(context, attrs);
        injectViews();
    }

    protected void injectViews() {
        inflate(getContext(), R.layout.item_qiscus_group_member, this);
        imageViewAvatar = (QiscusCircularImageView) findViewById(R.id.profile_picture);
        textViewName = (TextView) findViewById(R.id.name);
        textViewEmail = (TextView) findViewById(R.id.status);
    }

    protected void initLayout() {
        imageViewAvatar.setImageUrl(user.getAvatar(), R.drawable.ic_qiscus_avatar);
        textViewName.setText(user.getUsername());
        textViewEmail.setText(user.getEmail());
    }
}
