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

import com.qiscus.sdk.data.model.QiscusComment;
import com.qiscus.sdk.ui.adapter.QiscusBaseChatAdapter;
import com.qiscus.sdk.ui.adapter.viewholder.QiscusBaseMessageViewHolder;
import com.qiscus.sdk.ui.adapter.viewholder.QiscusFileViewHolder;
import com.qiscus.sdk.ui.adapter.viewholder.QiscusImageViewHolder;
import com.qiscus.sdk.ui.adapter.viewholder.QiscusTextViewHolder;

/**
 * Created on : September 27, 2016
 * Author     : zetbaitsu
 * Name       : Zetra
 * Email      : zetra@mail.ugm.ac.id
 * GitHub     : https://github.com/zetbaitsu
 * LinkedIn   : https://id.linkedin.com/in/zetbaitsu
 */
public class CustomChatAdapter extends QiscusBaseChatAdapter<QiscusComment, QiscusBaseMessageViewHolder<QiscusComment>> {
    private static final int TYPE_MESSAGE_ME = 1;
    private static final int TYPE_MESSAGE_OTHER = 2;
    private static final int TYPE_IMAGE_ME = 3;
    private static final int TYPE_IMAGE_OTHER = 4;
    private static final int TYPE_FILE_ME = 5;
    private static final int TYPE_FILE_OTHER = 6;
    private static final int TYPE_SOUND_ME = 7;
    private static final int TYPE_SOUND_OTHER = 8;

    public CustomChatAdapter(Context context) {
        super(context);
    }

    @Override
    protected Class<QiscusComment> getItemClass() {
        return QiscusComment.class;
    }

    @Override
    protected int getItemViewTypeMyMessage(QiscusComment qiscusComment, int position) {
        switch (qiscusComment.getType()) {
            case TEXT:
                return TYPE_MESSAGE_ME;
            case IMAGE:
                return TYPE_IMAGE_ME;
            case FILE:
                if(qiscusComment.getExtension().equals("mp3")){
                    return TYPE_SOUND_ME;
                } else if(qiscusComment.getExtension().equals("m4a")){
                    return TYPE_SOUND_ME;
                } else
                    return TYPE_FILE_ME;
            case SOUND:
                return TYPE_SOUND_ME;
            default:
                return TYPE_MESSAGE_ME;
        }
    }

    @Override
    protected int getItemViewTypeOthersMessage(QiscusComment qiscusComment, int position) {
        switch (qiscusComment.getType()) {
            case TEXT:
                return TYPE_MESSAGE_OTHER;
            case IMAGE:
                return TYPE_IMAGE_OTHER;
            case FILE:
                if(qiscusComment.getExtension().equals("mp3")){
                    return TYPE_SOUND_OTHER;
                } else if(qiscusComment.getExtension().equals("m4a")){
                    return TYPE_SOUND_OTHER;
                } else
                    return TYPE_FILE_OTHER;
            case SOUND:
                return TYPE_SOUND_OTHER;
            default:
                return TYPE_MESSAGE_OTHER;
        }
    }

    @Override
    protected int getItemResourceLayout(int viewType) {
        switch (viewType) {
            case TYPE_MESSAGE_ME:
                return R.layout.item_chat_text_me;
            case TYPE_MESSAGE_OTHER:
                return com.qiscus.sdk.R.layout.item_qiscus_chat_text;
            case TYPE_IMAGE_ME:
                return com.qiscus.sdk.R.layout.item_qiscus_chat_img_me;
            case TYPE_IMAGE_OTHER:
                return com.qiscus.sdk.R.layout.item_qiscus_chat_img;
            case TYPE_FILE_ME:
                return com.qiscus.sdk.R.layout.item_qiscus_chat_file_me;
            case TYPE_FILE_OTHER:
                return com.qiscus.sdk.R.layout.item_qiscus_chat_file;
            case TYPE_SOUND_ME:
                return com.qiscus.sdk.R.layout.item_qiscus_chat_sound_me;
            case TYPE_SOUND_OTHER:
                return com.qiscus.sdk.R.layout.item_qiscus_chat_sound;
            default:
                return com.qiscus.sdk.R.layout.item_qiscus_chat_text;
        }
    }

    @Override
    public QiscusBaseMessageViewHolder<QiscusComment> onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_MESSAGE_ME:
                return new MyMessageViewHolder(getView(parent, viewType), itemClickListener, longItemClickListener);
            case TYPE_MESSAGE_OTHER:
                return new QiscusTextViewHolder(getView(parent, viewType), itemClickListener, longItemClickListener);
            case TYPE_IMAGE_ME:
            case TYPE_IMAGE_OTHER:
                return new QiscusImageViewHolder(getView(parent, viewType), itemClickListener, longItemClickListener);
            case TYPE_FILE_ME:
            case TYPE_FILE_OTHER:
                return new QiscusFileViewHolder(getView(parent, viewType), itemClickListener, longItemClickListener);
            case TYPE_SOUND_ME:
            case TYPE_SOUND_OTHER:
                return new QiscusFileViewHolder(getView(parent, viewType), itemClickListener, longItemClickListener);
            default:
                return new QiscusTextViewHolder(getView(parent, viewType), itemClickListener, longItemClickListener);
        }
    }
}
