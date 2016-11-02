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
import com.qiscus.sdk.data.model.QiscusAccount;
import com.qiscus.sdk.data.model.QiscusComment;
import com.qiscus.sdk.ui.adapter.viewholder.QiscusBaseMessageViewHolder;
import com.qiscus.sdk.util.QiscusAndroidUtil;
import com.qiscus.sdk.util.QiscusDateUtil;

import java.util.List;

/**
 * Created on : September 27, 2016
 * Author     : zetbaitsu
 * Name       : Zetra
 * Email      : zetra@mail.ugm.ac.id
 * GitHub     : https://github.com/zetbaitsu
 * LinkedIn   : https://id.linkedin.com/in/zetbaitsu
 */

public abstract class QiscusBaseChatAdapter<Item extends QiscusComment, Holder extends QiscusBaseMessageViewHolder<Item>> extends RecyclerView.Adapter<Holder> {
    protected Context context;
    protected SortedList<Item> data;
    protected OnItemClickListener itemClickListener;
    protected OnLongItemClickListener longItemClickListener;

    protected QiscusAccount qiscusAccount;

    public QiscusBaseChatAdapter(Context context) {
        this.context = context;
        data = new SortedList<>(getItemClass(), new SortedList.Callback<Item>() {
            @Override
            public int compare(Item lhs, Item rhs) {
                return QiscusBaseChatAdapter.this.compare(lhs, rhs);
            }

            @Override
            public void onInserted(int position, int count) {
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
            }

            @Override
            public boolean areContentsTheSame(Item oldItem, Item newItem) {
                return oldItem.equals(newItem);
            }

            @Override
            public boolean areItemsTheSame(Item oldItem, Item newItem) {
                return oldItem.equals(newItem);
            }
        });
        qiscusAccount = Qiscus.getQiscusAccount();
    }

    protected abstract Class<Item> getItemClass();

    @Override
    public int getItemViewType(int position) {
        Item qiscusComment = data.get(position);
        if (qiscusComment.getSenderEmail().equals(qiscusAccount.getEmail())) {
            return getItemViewTypeMyMessage(qiscusComment, position);
        }
        return getItemViewTypeOthersMessage(qiscusComment, position);
    }

    protected abstract int getItemViewTypeMyMessage(Item qiscusComment, int position);

    protected abstract int getItemViewTypeOthersMessage(Item qiscusComment, int position);

    protected int compare(Item lhs, Item rhs) {
        return lhs.getId() != -1 && rhs.getId() != -1 ?
                QiscusAndroidUtil.compare(rhs.getId(), lhs.getId()) : rhs.getTime().compareTo(lhs.getTime());
    }

    protected View getView(ViewGroup parent, int viewType) {
        return LayoutInflater.from(context).inflate(getItemResourceLayout(viewType), parent, false);
    }

    protected abstract int getItemResourceLayout(int viewType);

    @Override
    public abstract Holder onCreateViewHolder(ViewGroup parent, int viewType);

    @Override
    public void onBindViewHolder(Holder holder, int position) {
        if (position == getItemCount() - 1) {
            holder.setNeedToShowDate(true);
        } else {
            holder.setNeedToShowDate(!QiscusDateUtil.isDateEqualIgnoreTime(data.get(position).getTime(), data.get(position + 1).getTime()));
        }

        if (!qiscusAccount.getEmail().equals(data.get(position).getSenderEmail())) {
            holder.setMessageFromMe(false);
        } else {
            holder.setMessageFromMe(true);
        }

        if (holder.isNeedToShowDate()) {
            holder.setNeedToShowFirstMessageBubbleIndicator(true);
        } else if (data.get(position).getSenderEmail().equals(data.get(position + 1).getSenderEmail())) {
            holder.setNeedToShowFirstMessageBubbleIndicator(false);
        } else {
            holder.setNeedToShowFirstMessageBubbleIndicator(true);
        }

        holder.bind(data.get(position));
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

    public SortedList<Item> getData() {
        return data;
    }

    public boolean isEmpty() {
        return data.size() < 1;
    }

    public int add(Item item) {
        int i = data.add(item);
        notifyItemInserted(i);
        return i;
    }

    public void add(final List<Item> items) {
        data.addAll(items);
        notifyDataSetChanged();
    }

    public void addOrUpdate(Item item) {
        int i = findPosition(item);
        if (i >= 0) {
            data.updateItemAt(i, item);
            notifyItemChanged(i);
        } else {
            add(item);
        }
    }

    public void addOrUpdate(final List<Item> items) {
        for (Item item : items) {
            int i = findPosition(item);
            if (i >= 0) {
                data.updateItemAt(i, item);
            } else {
                data.add(item);
            }
        }
        notifyDataSetChanged();
    }

    public void refreshWithData(List<Item> items) {
        data.clear();
        data.addAll(items);
        notifyDataSetChanged();
    }

    public void remove(int position) {
        if (position >= 0 && position < data.size()) {
            data.removeItemAt(position);
            notifyItemRemoved(position);
        }
    }

    public void remove(Item item) {
        int position = findPosition(item);
        remove(position);
    }

    public void clear() {
        data.clear();
    }

    public int findPosition(Item item) {
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

    public void detachView() {
        int size = data.size();
        for (int i = 0; i < size; i++) {
            data.get(i).destroy();
        }
    }
}
