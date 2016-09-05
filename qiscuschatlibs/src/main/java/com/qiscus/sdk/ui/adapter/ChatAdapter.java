package com.qiscus.sdk.ui.adapter;

import android.content.Context;
import android.view.ViewGroup;

import com.qiscus.sdk.Qiscus;
import com.qiscus.library.chat.R;
import com.qiscus.sdk.data.model.QiscusAccount;
import com.qiscus.sdk.data.model.QiscusComment;
import com.qiscus.sdk.ui.adapter.viewholder.MessageViewHolder;
import com.qiscus.sdk.util.DateUtil;

/**
 * Created on : May 30, 2016
 * Author     : zetbaitsu
 * Name       : Zetra
 * Email      : zetra@mail.ugm.ac.id
 * GitHub     : https://github.com/zetbaitsu
 * LinkedIn   : https://id.linkedin.com/in/zetbaitsu
 */
public class ChatAdapter extends SortedRecyclerAdapter<QiscusComment, MessageViewHolder> {
    private static final int TYPE_MESSAGE_ME = 1;
    private static final int TYPE_MESSAGE_OTHER = 2;
    private static final int TYPE_PICTURE_ME = 3;
    private static final int TYPE_PICTURE_OTHER = 4;
    private static final int TYPE_FILE_ME = 5;
    private static final int TYPE_FILE_OTHER = 6;

    private QiscusAccount qiscusAccount;

    public ChatAdapter(Context context) {
        super(context);
        qiscusAccount = Qiscus.getQiscusAccount();
    }

    @Override
    protected Class<QiscusComment> getItemClass() {
        return QiscusComment.class;
    }

    @Override
    protected int compare(QiscusComment lhs, QiscusComment rhs) {
        return rhs.getTime().compareTo(lhs.getTime());
    }

    @Override
    public int getItemViewType(int position) {
        QiscusComment qiscusComment = data.get(position);
        if (qiscusComment.getSenderEmail().equals(qiscusAccount.getEmail())) {
            switch (qiscusComment.getType()) {
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
            switch (qiscusComment.getType()) {
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

        if (!qiscusAccount.getEmail().equals(data.get(position).getSenderEmail())) {
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
    public int findPosition(QiscusComment item) {
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
