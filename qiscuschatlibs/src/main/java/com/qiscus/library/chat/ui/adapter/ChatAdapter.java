package com.qiscus.library.chat.ui.adapter;

import android.content.Context;
import android.view.ViewGroup;

import com.qiscus.library.chat.R;
import com.qiscus.library.chat.data.local.LocalDataManager;
import com.qiscus.library.chat.data.model.AccountInfo;
import com.qiscus.library.chat.data.model.Comment;
import com.qiscus.library.chat.ui.adapter.viewholder.MessageViewHolder;
import com.qiscus.library.chat.util.DateUtil;

/**
 * Created on : May 30, 2016
 * Author     : zetbaitsu
 * Name       : Zetra
 * Email      : zetra@mail.ugm.ac.id
 * GitHub     : https://github.com/zetbaitsu
 * LinkedIn   : https://id.linkedin.com/in/zetbaitsu
 */
public class ChatAdapter extends SortedRecyclerAdapter<Comment, MessageViewHolder> {
    private static final int TYPE_MESSAGE_ME = 1;
    private static final int TYPE_MESSAGE_OTHER = 2;
    private static final int TYPE_PICTURE_ME = 3;
    private static final int TYPE_PICTURE_OTHER = 4;
    private static final int TYPE_FILE_ME = 5;
    private static final int TYPE_FILE_OTHER = 6;

    private AccountInfo accountInfo;

    public ChatAdapter(Context context) {
        super(context);
        accountInfo = LocalDataManager.getInstance().getAccountInfo();
    }

    @Override
    protected Class<Comment> getItemClass() {
        return Comment.class;
    }

    @Override
    protected int compare(Comment lhs, Comment rhs) {
        return rhs.getTime().compareTo(lhs.getTime());
    }

    @Override
    public int getItemViewType(int position) {
        Comment comment = data.get(position);
        if (comment.getSenderEmail().equals(accountInfo.getEmail())) {
            switch (comment.getType()) {
                case TEXT:
                    return TYPE_MESSAGE_ME;
                case IMAGE:
                    return TYPE_PICTURE_ME;
                case FILE:
                    return TYPE_FILE_ME;
                default:
                    return TYPE_MESSAGE_ME;
            }
        } else {
            switch (comment.getType()) {
                case TEXT:
                    return TYPE_MESSAGE_OTHER;
                case IMAGE:
                    return TYPE_PICTURE_OTHER;
                case FILE:
                    return TYPE_FILE_OTHER;
                default:
                    return TYPE_MESSAGE_OTHER;
            }
        }
    }

    @Override
    protected int getItemResourceLayout(int viewType) {
        switch (viewType) {
            case TYPE_MESSAGE_ME:
                return R.layout.item_chat_text_me;
            case TYPE_MESSAGE_OTHER:
                return R.layout.item_chat_text;
            case TYPE_PICTURE_ME:
                return R.layout.item_chat_img_me;
            case TYPE_PICTURE_OTHER:
                return R.layout.item_chat_img;
            case TYPE_FILE_ME:
                return R.layout.item_chat_file_me;
            case TYPE_FILE_OTHER:
                return R.layout.item_chat_file;
            default:
                return R.layout.item_chat_text;
        }
    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MessageViewHolder(getView(parent, viewType), itemClickListener, longItemClickListener);
    }

    @Override
    public void onBindViewHolder(MessageViewHolder holder, int position) {
        if (position == getItemCount() - 1) {
            holder.setShowDate(true);
        } else {
            holder.setShowDate(!DateUtil.isDateEqualIgnoreTime(data.get(position).getTime(), data.get(position + 1).getTime()));
        }

        if (!accountInfo.getEmail().equals(data.get(position).getSenderEmail())) {
            holder.setFromMe(false);
        } else {
            holder.setFromMe(true);
        }

        if (holder.isShowDate()) {
            holder.setShowBubble(true);
        } else if (data.get(position).getSenderEmail().equals(data.get(position + 1).getSenderEmail())) {
            holder.setShowBubble(false);
        } else {
            holder.setShowBubble(true);
        }

        super.onBindViewHolder(holder, position);
    }

    @Override
    public int findPosition(Comment item) {
        if (data == null) {
            return -1;
        }

        int size = data.size();
        for (int i = 0; i < size; i++) {
            if (data.get(i).equals(item)) {
                return i;
            }
        }

        return -1;
    }
}
