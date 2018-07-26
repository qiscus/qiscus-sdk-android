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
import android.support.v7.util.SortedList;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.qiscus.sdk.Qiscus;
import com.qiscus.sdk.chat.core.data.model.QiscusAccount;
import com.qiscus.sdk.chat.core.data.model.QiscusChatRoom;
import com.qiscus.sdk.chat.core.data.model.QiscusComment;
import com.qiscus.sdk.chat.core.data.model.QiscusRoomMember;
import com.qiscus.sdk.ui.adapter.viewholder.QiscusBaseMessageViewHolder;
import com.qiscus.sdk.ui.view.QiscusCarouselItemView;
import com.qiscus.sdk.ui.view.QiscusChatButtonView;
import com.qiscus.sdk.chat.core.util.QiscusDateUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created on : September 27, 2016
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
public abstract class QiscusBaseChatAdapter<E extends QiscusComment, H extends QiscusBaseMessageViewHolder<E>>
        extends RecyclerView.Adapter<H> {
    protected Context context;
    protected SortedList<E> data;
    protected OnItemClickListener itemClickListener;
    protected OnLongItemClickListener longItemClickListener;
    protected OnUploadIconClickListener uploadIconClickListener;
    protected QiscusChatButtonView.ChatButtonClickListener chatButtonClickListener;
    protected QiscusCarouselItemView.CarouselItemClickListener carouselItemClickListener;
    protected ReplyItemClickListener replyItemClickListener;
    protected CommentChainingListener commentChainingListener;

    protected QiscusChatRoom qiscusChatRoom;
    protected QiscusAccount qiscusAccount;
    protected long lastDeliveredCommentId;
    protected long lastReadCommentId;
    protected boolean groupChat;
    protected boolean channelRoom;

    private Map<String, QiscusRoomMember> members;

    public QiscusBaseChatAdapter(Context context, boolean groupChat) {
        this(context, groupChat, false);
    }

    public QiscusBaseChatAdapter(Context context, boolean groupChat, boolean channelRoom) {
        this.context = context;
        this.groupChat = groupChat;
        this.channelRoom = channelRoom;
        data = new SortedList<>(getItemClass(), new SortedList.Callback<E>() {
            @Override
            public int compare(E lhs, E rhs) {
                return QiscusBaseChatAdapter.this.compare(lhs, rhs);
            }

            @Override
            public void onInserted(int position, int count) {
                checkChaining(position);
            }

            @Override
            public void onRemoved(int position, int count) {
            }

            @Override
            public void onMoved(int fromPosition, int toPosition) {
                notifyItemMoved(fromPosition, toPosition);
                notifyItemChanged(toPosition);
            }

            @Override
            public void onChanged(int position, int count) {
                checkChaining(position);
            }

            @Override
            public boolean areContentsTheSame(E oldE, E newE) {
                return oldE.equals(newE);
            }

            @Override
            public boolean areItemsTheSame(E oldE, E newE) {
                return oldE.equals(newE);
            }
        });
        qiscusAccount = Qiscus.getQiscusAccount();
        members = new HashMap<>();
    }

    private void checkChaining(int position) {
        if (position < data.size() - 1) {
            QiscusComment comment = data.get(position);
            QiscusComment before = data.get(position + 1);
            if (comment.getState() >= QiscusComment.STATE_ON_QISCUS
                    && before.getState() >= QiscusComment.STATE_ON_QISCUS
                    && comment.getCommentBeforeId() != before.getId()
                    && commentChainingListener != null) {
                commentChainingListener.onCommentChainingBreak(comment, before);
            }
        }
    }

    public boolean isGroupChat() {
        return groupChat;
    }

    public void setGroupChat(boolean groupChat) {
        this.groupChat = groupChat;
    }

    public boolean isChannelRoom() {
        return channelRoom;
    }

    public void setChannelRoom(boolean channelRoom) {
        this.channelRoom = channelRoom;
    }

    public QiscusChatRoom getQiscusChatRoom() {
        return qiscusChatRoom;
    }

    public void setQiscusChatRoom(QiscusChatRoom qiscusChatRoom) {
        this.qiscusChatRoom = qiscusChatRoom;
        updateMember();
    }

    private void updateMember() {
        members.clear();
        for (QiscusRoomMember roomMember : qiscusChatRoom.getMember()) {
            members.put(roomMember.getEmail(), roomMember);
        }
    }

    protected abstract Class<E> getItemClass();

    @Override
    public int getItemViewType(int position) {
        E qiscusComment = data.get(position);
        if (qiscusComment.getType() == QiscusComment.Type.CUSTOM) {
            return getItemViewTypeCustomMessage(qiscusComment, position);
        }
        if (qiscusComment.getSenderEmail().equals(qiscusAccount.getEmail())) {
            return getItemViewTypeMyMessage(qiscusComment, position);
        }
        return getItemViewTypeOthersMessage(qiscusComment, position);
    }

    protected abstract int getItemViewTypeCustomMessage(E qiscusComment, int position);

    protected abstract int getItemViewTypeMyMessage(E qiscusComment, int position);

    protected abstract int getItemViewTypeOthersMessage(E qiscusComment, int position);

    protected int compare(E lhs, E rhs) {
        if (rhs.equals(lhs)) { //Same comments
            return 0;
        } else if (rhs.getId() == -1 && lhs.getId() == -1) { //Not completed comments
            return rhs.getTime().compareTo(lhs.getTime());
        } else if (rhs.getId() != -1 && lhs.getId() != -1) { //Completed comments
            return rhs.getTime().compareTo(lhs.getTime());
        } else if (rhs.getId() == -1) {
            return 1;
        } else if (lhs.getId() == -1) {
            return -1;
        }
        return rhs.getTime().compareTo(lhs.getTime());
    }

    protected View getView(ViewGroup parent, int viewType) {
        return LayoutInflater.from(context).inflate(getItemResourceLayout(viewType), parent, false);
    }

    protected abstract int getItemResourceLayout(int viewType);

    @Override
    public abstract H onCreateViewHolder(ViewGroup parent, int viewType);

    @Override
    public void onBindViewHolder(H holder, int position) {
        holder.setGroupChat(groupChat);
        holder.setChannelRoom(channelRoom);
        holder.setRoomMembers(members);

        determineIsNeedToShowDate(holder, position);
        determineIsCommentFromMe(holder, position);
        determineIsNeedToShowFirstMessageIndicator(holder, position);

        holder.bind(data.get(position));
    }

    protected void determineIsNeedToShowFirstMessageIndicator(H holder, int position) {
        if (holder.isNeedToShowDate() || data.get(position + 1).getType() == QiscusComment.Type.CARD
                || data.get(position + 1).getType() == QiscusComment.Type.CAROUSEL) {
            holder.setNeedToShowFirstMessageBubbleIndicator(true);
        } else if (data.get(position).getSenderEmail().equals(data.get(position + 1).getSenderEmail())) {
            holder.setNeedToShowFirstMessageBubbleIndicator(false);
        } else {
            holder.setNeedToShowFirstMessageBubbleIndicator(true);
        }
    }

    protected void determineIsNeedToShowDate(H holder, int position) {
        if (position == getItemCount() - 1) {
            holder.setNeedToShowDate(true);
        } else {
            holder.setNeedToShowDate(!QiscusDateUtil.isDateEqualIgnoreTime(data.get(position).getTime(),
                    data.get(position + 1).getTime()));
        }
    }

    protected void determineIsCommentFromMe(H holder, int position) {
        if (!qiscusAccount.getEmail().equals(data.get(position).getSenderEmail())) {
            holder.setMessageFromMe(false);
        } else {
            holder.setMessageFromMe(true);
        }
    }

    @Override
    public int getItemCount() {
        try {
            return data.size();
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setOnItemClickListener(OnItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public void setOnLongItemClickListener(OnLongItemClickListener longItemClickListener) {
        this.longItemClickListener = longItemClickListener;
    }

    public void setUploadIconClickListener(OnUploadIconClickListener uploadIconClickListener) {
        this.uploadIconClickListener = uploadIconClickListener;
    }

    public void setChatButtonClickListener(QiscusChatButtonView.ChatButtonClickListener chatButtonClickListener) {
        this.chatButtonClickListener = chatButtonClickListener;
    }

    public void setCarouselItemClickListener(QiscusCarouselItemView.CarouselItemClickListener carouselItemClickListener) {
        this.carouselItemClickListener = carouselItemClickListener;
    }

    public void setReplyItemClickListener(ReplyItemClickListener replyItemClickListener) {
        this.replyItemClickListener = replyItemClickListener;
    }

    public void setCommentChainingListener(CommentChainingListener commentChainingListener) {
        this.commentChainingListener = commentChainingListener;
    }

    public SortedList<E> getData() {
        return data;
    }

    public boolean isEmpty() {
        return data.size() < 1;
    }

    public int add(E e) {
        int i = data.add(e);
        notifyItemInserted(i);
        return i;
    }

    public void add(final List<E> es) {
        data.addAll(es);
        notifyDataSetChanged();
    }

    public void addOrUpdate(E e) {
        int i = findPosition(e);
        if (i >= 0) {
            if (!e.areContentsTheSame(data.get(i))) {
                e.setSelected(data.get(i).isSelected());
                data.updateItemAt(i, e);
            }
            notifyItemChanged(i);
        } else {
            add(e);
        }
    }

    public void addOrUpdate(final List<E> es) {
        for (E e : es) {
            int i = findPosition(e);
            if (i >= 0) {
                if (!e.areContentsTheSame(data.get(i))) {
                    e.setSelected(data.get(i).isSelected());
                    data.updateItemAt(i, e);
                }
            } else {
                data.add(e);
            }
        }
        notifyDataSetChanged();
    }

    public void update(E e) {
        int i = findPosition(e);
        if (i >= 0) {
            if (!e.areContentsTheSame(data.get(i))) {
                e.setSelected(data.get(i).isSelected());
                data.updateItemAt(i, e);
            }
            notifyItemChanged(i);
        }
    }

    public void update(final List<E> es) {
        for (E e : es) {
            int i = findPosition(e);
            if (i >= 0 && !e.areContentsTheSame(data.get(i))) {
                e.setSelected(data.get(i).isSelected());
                data.updateItemAt(i, e);
            }
        }
        notifyDataSetChanged();
    }

    public void mergeLocalAndRemoteData(List<E> es) {
        if (es == null || es.isEmpty()) {
            return;
        }
        if (data.size() == 0) {
            addOrUpdate(es);
            return;
        }

        Date minDate = es.get(0).getTime();
        Date maxDate = es.get(0).getTime();
        for (E e : es) {
            if (minDate.compareTo(e.getTime()) < 0) {
                minDate = e.getTime();
            }

            if (maxDate.compareTo(e.getTime()) > 0) {
                maxDate = e.getTime();
            }
        }
        List<E> keep = new ArrayList<>();
        for (int i = 0; i < data.size(); i++) {
            //Keep not complete comment but in still range of remote comment
            if (data.get(i).getId() == -1 && data.get(i).getTime().compareTo(minDate) >= 0) {
                keep.add(data.get(i));
            }

            //Keep all comment with date more than latest comment
            if (data.get(i).getTime().compareTo(maxDate) >= 0) {
                keep.add(data.get(i));
            }
        }

        if (es.size() < 20) {
            int need = 20 - es.size();
            int size = data.size();
            for (int i = size - 1; i >= 0; i--) {
                if (!es.contains(data.get(i))) {
                    es.add(data.get(i));
                    need--;
                }
                if (need <= 0) {
                    break;
                }
            }
        }
        //Clear old comments
        data.clear();
        //Add all new comments to keep
        keep.addAll(es);
        addOrUpdate(keep);
    }

    public void refreshWithData(List<E> es) {
        data.clear();
        data.addAll(es);
        notifyDataSetChanged();
    }

    public void remove(int position) {
        if (position >= 0 && position < data.size()) {
            data.removeItemAt(position);
            notifyItemRemoved(position);
        }
    }

    public void remove(E e) {
        int position = findPosition(e);
        remove(position);
    }

    public void clear() {
        data.clear();
    }

    public int findPosition(E e) {
        if (data == null) {
            return -1;
        }

        int size = data.size();
        for (int i = 0; i < size; i++) {
            if (data.get(i).equals(e)) {
                return i;
            }
        }

        return -1;
    }

    public void updateLastDeliveredComment(long lastDeliveredCommentId) {
        this.lastDeliveredCommentId = lastDeliveredCommentId;
        updateCommentState();
        notifyDataSetChanged();
    }

    private void updateCommentState() {
        int size = data.size();
        for (int i = 0; i < size; i++) {
            if (data.get(i).getState() > QiscusComment.STATE_SENDING) {
                if (data.get(i).getId() <= lastReadCommentId) {
                    if (data.get(i).getState() == QiscusComment.STATE_READ) {
                        break;
                    }
                    data.get(i).setState(QiscusComment.STATE_READ);
                } else if (data.get(i).getId() <= lastDeliveredCommentId) {
                    if (data.get(i).getState() == QiscusComment.STATE_DELIVERED) {
                        break;
                    }
                    data.get(i).setState(QiscusComment.STATE_DELIVERED);
                }
            }
        }
    }

    public void updateLastReadComment(long lastReadCommentId) {
        this.lastReadCommentId = lastReadCommentId;
        this.lastDeliveredCommentId = lastReadCommentId;
        updateCommentState();
        notifyDataSetChanged();
    }

    public List<E> getSelectedComments() {
        List<E> selectedComments = new ArrayList<>();
        int size = data.size();
        for (int i = size - 1; i >= 0; i--) {
            if (data.get(i).isSelected()) {
                selectedComments.add(data.get(i));
            }
        }
        return selectedComments;
    }

    public void clearSelectedComments() {
        int size = data.size();
        for (int i = size - 1; i >= 0; i--) {
            if (data.get(i).isSelected()) {
                data.get(i).setSelected(false);
            }
        }
        notifyDataSetChanged();
    }

    public QiscusComment getLatestSentComment() {
        int size = data.size();
        for (int i = 0; i < size; i++) {
            QiscusComment comment = data.get(i);
            if (comment.getState() >= QiscusComment.STATE_ON_QISCUS) {
                return comment;
            }
        }
        return null;
    }

    public void detachView() {
        int size = data.size();
        for (int i = 0; i < size; i++) {
            data.get(i).destroy();
        }
    }

    public void clearCommentsBefore(long timestamp) {
        int size = data.size();
        for (int i = size - 1; i >= 0; i--) {
            if (data.get(i).getTime().getTime() <= timestamp) {
                data.get(i).destroy();
                data.removeItemAt(i);
            }
        }
        notifyDataSetChanged();
    }
}
