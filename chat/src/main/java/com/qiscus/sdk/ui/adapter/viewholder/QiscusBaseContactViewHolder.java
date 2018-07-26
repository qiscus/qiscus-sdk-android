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

package com.qiscus.sdk.ui.adapter.viewholder;

import android.support.annotation.NonNull;
import android.view.View;
import android.widget.TextView;

import com.qiscus.sdk.chat.core.data.model.QiscusComment;
import com.qiscus.sdk.ui.adapter.OnItemClickListener;
import com.qiscus.sdk.ui.adapter.OnLongItemClickListener;

/**
 * Created on : August 14, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
public abstract class QiscusBaseContactViewHolder extends QiscusBaseMessageViewHolder<QiscusComment> {
    @NonNull protected TextView contactNameView;
    @NonNull protected TextView contactIdView;

    public QiscusBaseContactViewHolder(View itemView, OnItemClickListener itemClickListener,
                                       OnLongItemClickListener longItemClickListener) {
        super(itemView, itemClickListener, longItemClickListener);
        contactNameView = getContactNameView(itemView);
        contactIdView = getContactIdView(itemView);
    }

    @NonNull
    public abstract TextView getContactNameView(View itemView);


    @NonNull
    public abstract TextView getContactIdView(View itemView);

    @Override
    protected void showMessage(QiscusComment qiscusComment) {
        contactNameView.setText(qiscusComment.getContact().getName());
        contactIdView.setText(qiscusComment.getContact().getValue());
    }
}
