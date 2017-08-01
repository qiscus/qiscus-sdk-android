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

package com.qiscus.sdk.ui.adapter;

import android.content.Context;
import android.view.ViewGroup;

import com.qiscus.sdk.R;
import com.qiscus.sdk.data.model.QiscusComment;
import com.qiscus.sdk.ui.adapter.viewholder.QiscusAccountLinkingViewHolder;
import com.qiscus.sdk.ui.adapter.viewholder.QiscusAudioViewHolder;
import com.qiscus.sdk.ui.adapter.viewholder.QiscusBaseMessageViewHolder;
import com.qiscus.sdk.ui.adapter.viewholder.QiscusButtonMessageViewHolder;
import com.qiscus.sdk.ui.adapter.viewholder.QiscusCardMessageViewHolder;
import com.qiscus.sdk.ui.adapter.viewholder.QiscusFileViewHolder;
import com.qiscus.sdk.ui.adapter.viewholder.QiscusImageViewHolder;
import com.qiscus.sdk.ui.adapter.viewholder.QiscusLinkViewHolder;
import com.qiscus.sdk.ui.adapter.viewholder.QiscusReplyViewHolder;
import com.qiscus.sdk.ui.adapter.viewholder.QiscusSystemMessageViewHolder;
import com.qiscus.sdk.ui.adapter.viewholder.QiscusTextViewHolder;
import com.qiscus.sdk.ui.adapter.viewholder.QiscusVideoViewHolder;

/**
 * Created on : May 30, 2016
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
public class QiscusChatAdapter extends QiscusBaseChatAdapter<QiscusComment, QiscusBaseMessageViewHolder<QiscusComment>> {
    private static final int TYPE_MESSAGE_ME = 1;
    private static final int TYPE_MESSAGE_OTHER = 2;
    private static final int TYPE_IMAGE_ME = 3;
    private static final int TYPE_IMAGE_OTHER = 4;
    private static final int TYPE_VIDEO_ME = 5;
    private static final int TYPE_VIDEO_OTHER = 6;
    private static final int TYPE_FILE_ME = 7;
    private static final int TYPE_FILE_OTHER = 8;
    private static final int TYPE_AUDIO_ME = 9;
    private static final int TYPE_AUDIO_OTHER = 10;
    private static final int TYPE_LINK_ME = 11;
    private static final int TYPE_LINK_OTHER = 12;
    private static final int TYPE_ACCOUNT_LINKING = 13;
    private static final int TYPE_BUTTONS = 14;
    private static final int TYPE_MESSAGE_MULTI_LINE_ME = 15;
    private static final int TYPE_MESSAGE_MULTI_LINE_OTHER = 16;
    private static final int TYPE_MESSAGE_REPLY_ME = 17;
    private static final int TYPE_MESSAGE_REPLY_OTHER = 18;
    private static final int TYPE_SYSTEM_EVENT = 19;
    private static final int TYPE_MESSAGE_CARD = 20;

    public QiscusChatAdapter(Context context, boolean groupChat) {
        super(context, groupChat);
    }

    public QiscusChatAdapter(Context context) {
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
                return qiscusComment.getMessage().contains(System.getProperty("line.separator"))
                        ? TYPE_MESSAGE_MULTI_LINE_ME : TYPE_MESSAGE_ME;
            case LINK:
                return TYPE_LINK_ME;
            case IMAGE:
                return TYPE_IMAGE_ME;
            case VIDEO:
                return TYPE_VIDEO_ME;
            case AUDIO:
                return TYPE_AUDIO_ME;
            case FILE:
                return TYPE_FILE_ME;
            case ACCOUNT_LINKING:
                return TYPE_ACCOUNT_LINKING;
            case BUTTONS:
                return TYPE_BUTTONS;
            case REPLY:
                return TYPE_MESSAGE_REPLY_ME;
            case CARD:
                return TYPE_MESSAGE_CARD;
            case SYSTEM_EVENT:
                return TYPE_SYSTEM_EVENT;
            default:
                return TYPE_MESSAGE_ME;
        }
    }

    @Override
    protected int getItemViewTypeOthersMessage(QiscusComment qiscusComment, int position) {
        switch (qiscusComment.getType()) {
            case TEXT:
                return qiscusComment.getMessage().contains(System.getProperty("line.separator"))
                        ? TYPE_MESSAGE_MULTI_LINE_OTHER : TYPE_MESSAGE_OTHER;
            case LINK:
                return TYPE_LINK_OTHER;
            case IMAGE:
                return TYPE_IMAGE_OTHER;
            case VIDEO:
                return TYPE_VIDEO_OTHER;
            case AUDIO:
                return TYPE_AUDIO_OTHER;
            case FILE:
                return TYPE_FILE_OTHER;
            case ACCOUNT_LINKING:
                return TYPE_ACCOUNT_LINKING;
            case BUTTONS:
                return TYPE_BUTTONS;
            case REPLY:
                return TYPE_MESSAGE_REPLY_OTHER;
            case CARD:
                return TYPE_MESSAGE_CARD;
            case SYSTEM_EVENT:
                return TYPE_SYSTEM_EVENT;
            default:
                return TYPE_MESSAGE_OTHER;
        }
    }

    @Override
    protected int getItemResourceLayout(int viewType) {
        switch (viewType) {
            case TYPE_MESSAGE_ME:
                return R.layout.item_qiscus_chat_text_me;
            case TYPE_MESSAGE_OTHER:
                return R.layout.item_qiscus_chat_text;
            case TYPE_LINK_ME:
                return R.layout.item_qiscus_chat_link_me;
            case TYPE_LINK_OTHER:
                return R.layout.item_qiscus_chat_link;
            case TYPE_IMAGE_ME:
                return R.layout.item_qiscus_chat_img_me;
            case TYPE_IMAGE_OTHER:
                return R.layout.item_qiscus_chat_img;
            case TYPE_VIDEO_ME:
                return R.layout.item_qiscus_chat_video_me;
            case TYPE_VIDEO_OTHER:
                return R.layout.item_qiscus_chat_video;
            case TYPE_AUDIO_ME:
                return R.layout.item_qiscus_chat_audio_me;
            case TYPE_AUDIO_OTHER:
                return R.layout.item_qiscus_chat_audio;
            case TYPE_FILE_ME:
                return R.layout.item_qiscus_chat_file_me;
            case TYPE_FILE_OTHER:
                return R.layout.item_qiscus_chat_file;
            case TYPE_ACCOUNT_LINKING:
                return R.layout.item_qiscus_chat_linking;
            case TYPE_BUTTONS:
                return R.layout.item_qiscus_chat_button;
            case TYPE_MESSAGE_MULTI_LINE_ME:
                return R.layout.item_qiscus_chat_multi_line_text_me;
            case TYPE_MESSAGE_MULTI_LINE_OTHER:
                return R.layout.item_qiscus_chat_multi_line_text;
            case TYPE_MESSAGE_REPLY_ME:
                return R.layout.item_qiscus_chat_reply_me;
            case TYPE_MESSAGE_REPLY_OTHER:
                return R.layout.item_qiscus_chat_reply;
            case TYPE_MESSAGE_CARD:
                return R.layout.item_qiscus_chat_card;
            case TYPE_SYSTEM_EVENT:
                return R.layout.item_qiscus_chat_system_event;
            default:
                return R.layout.item_qiscus_chat_text;
        }
    }

    @Override
    public QiscusBaseMessageViewHolder<QiscusComment> onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_MESSAGE_ME:
            case TYPE_MESSAGE_OTHER:
            case TYPE_MESSAGE_MULTI_LINE_ME:
            case TYPE_MESSAGE_MULTI_LINE_OTHER:
                return new QiscusTextViewHolder(getView(parent, viewType), itemClickListener, longItemClickListener);
            case TYPE_LINK_ME:
            case TYPE_LINK_OTHER:
                return new QiscusLinkViewHolder(getView(parent, viewType), itemClickListener, longItemClickListener);
            case TYPE_IMAGE_ME:
            case TYPE_IMAGE_OTHER:
                return new QiscusImageViewHolder(getView(parent, viewType), itemClickListener, longItemClickListener);
            case TYPE_VIDEO_ME:
            case TYPE_VIDEO_OTHER:
                return new QiscusVideoViewHolder(getView(parent, viewType), itemClickListener, longItemClickListener);
            case TYPE_AUDIO_ME:
            case TYPE_AUDIO_OTHER:
                return new QiscusAudioViewHolder(getView(parent, viewType), itemClickListener, longItemClickListener);
            case TYPE_FILE_ME:
            case TYPE_FILE_OTHER:
                return new QiscusFileViewHolder(getView(parent, viewType), itemClickListener, longItemClickListener);
            case TYPE_ACCOUNT_LINKING:
                return new QiscusAccountLinkingViewHolder(getView(parent, viewType), itemClickListener, longItemClickListener);
            case TYPE_BUTTONS:
                return new QiscusButtonMessageViewHolder(getView(parent, viewType), itemClickListener,
                        longItemClickListener, chatButtonClickListener);
            case TYPE_MESSAGE_REPLY_ME:
            case TYPE_MESSAGE_REPLY_OTHER:
                return new QiscusReplyViewHolder(getView(parent, viewType), itemClickListener, longItemClickListener,
                        replyItemClickListener);
            case TYPE_MESSAGE_CARD:
                return new QiscusCardMessageViewHolder(getView(parent, viewType), itemClickListener,
                        longItemClickListener, chatButtonClickListener);
            case TYPE_SYSTEM_EVENT:
                return new QiscusSystemMessageViewHolder(getView(parent, viewType), itemClickListener, longItemClickListener);
            default:
                return new QiscusTextViewHolder(getView(parent, viewType), itemClickListener, longItemClickListener);
        }
    }
}
