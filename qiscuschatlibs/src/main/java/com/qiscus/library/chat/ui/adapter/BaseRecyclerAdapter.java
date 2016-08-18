package com.qiscus.library.chat.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.qiscus.library.chat.ui.adapter.viewholder.BaseItemViewHolder;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

/**
 * Created on : August 18, 2016
 * Author     : zetbaitsu
 * Name       : Zetra
 * Email      : zetra@mail.ugm.ac.id
 * GitHub     : https://github.com/zetbaitsu
 * LinkedIn   : https://id.linkedin.com/in/zetbaitsu
 */
public abstract class BaseRecyclerAdapter<Data, Holder extends BaseItemViewHolder> extends
        RecyclerView.Adapter<Holder> {
    protected Context context;
    protected List<Data> data;
    protected OnItemClickListener itemClickListener;
    protected OnLongItemClickListener longItemClickListener;

    public BaseRecyclerAdapter(Context context) {
        this.context = context;
        data = new ArrayList<>();
        Timber.tag(getClass().getSimpleName());
    }

    public BaseRecyclerAdapter(Context context, List<Data> data) {
        this.context = context;
        this.data = data;
        Timber.tag(getClass().getSimpleName());
    }

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

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public void setOnItemClickListener(OnItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public interface OnLongItemClickListener {
        void onLongItemClick(View view, int position);
    }

    public void setOnLongItemClickListener(OnLongItemClickListener longItemClickListener) {
        this.longItemClickListener = longItemClickListener;
    }

    public List<Data> getData() {
        return data;
    }

    public void add(Data item) {
        data.add(item);
        notifyItemInserted(data.size() - 1);
    }

    public void add(Data item, int position) {
        data.add(position, item);
        notifyItemInserted(position);
    }

    public void add(final List<Data> items) {
        data.addAll(items);
        notifyDataSetChanged();
    }

    public void addOrUpdate(Data item) {
        int i = data.indexOf(item);
        if (i >= 0) {
            data.set(i, item);
            notifyItemChanged(i);
        } else {
            add(item);
        }
    }

    public void addOrUpdate(final List<Data> items) {
        final int size = items.size();
        for (int i = 0; i < size; i++) {
            Data item = items.get(i);
            int x = data.indexOf(item);
            if (x >= 0) {
                data.set(x, item);
            } else {
                data.add(item);
            }
        }
        notifyDataSetChanged();
    }

    public void refreshWithData(List<Data> items) {
        data.clear();
        data.addAll(items);
        notifyDataSetChanged();
    }

    public void remove(int position) {
        if (position >= 0 && position < data.size()) {
            data.remove(position);
            notifyItemRemoved(position);
        }
    }

    public void remove(Data item) {
        int position = data.indexOf(item);
        remove(position);
    }

    public void clear() {
        data.clear();
        notifyDataSetChanged();
    }
}
