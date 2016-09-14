package com.qiscus.sdk.ui.adapter;

import android.content.Context;
import android.support.v7.util.SortedList;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.qiscus.sdk.ui.adapter.viewholder.QiscusItemViewHolder;

import java.util.List;


public abstract class QiscusSortedRecyclerAdapter<Item, Holder extends QiscusItemViewHolder> extends
        RecyclerView.Adapter<Holder> {
    protected Context context;
    protected SortedList<Item> data;
    protected OnItemClickListener itemClickListener;
    protected OnLongItemClickListener longItemClickListener;

    public QiscusSortedRecyclerAdapter(Context context) {
        this.context = context;
        data = new SortedList<>(getItemClass(), new SortedList.Callback<Item>() {
            @Override
            public int compare(Item lhs, Item rhs) {
                return QiscusSortedRecyclerAdapter.this.compare(lhs, rhs);
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
    }

    protected abstract Class<Item> getItemClass();

    protected abstract int compare(Item lhs, Item rhs);

    protected View getView(ViewGroup parent, int viewType) {
        return LayoutInflater.from(context).inflate(getItemResourceLayout(viewType), parent, false);
    }

    protected abstract int getItemResourceLayout(int viewType);

    @Override
    public abstract Holder onCreateViewHolder(ViewGroup parent, int viewType);

    @Override
    public void onBindViewHolder(Holder holder, int position) {
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

        int size = data.size() - 1;
        for (int i = size; i >= 0; i--) {
            if (data.get(i).equals(item)) {
                return i;
            }
        }

        return -1;
    }
}
