package com.qiscus.sdk.ui.adapter.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.qiscus.sdk.ui.adapter.OnItemClickListener;
import com.qiscus.sdk.ui.adapter.OnLongItemClickListener;

import butterknife.ButterKnife;

/**
 * Created on : August 18, 2016
 * Author     : zetbaitsu
 * Name       : Zetra
 * Email      : zetra@mail.ugm.ac.id
 * GitHub     : https://github.com/zetbaitsu
 * LinkedIn   : https://id.linkedin.com/in/zetbaitsu
 */
public abstract class QiscusItemViewHolder<Data> extends RecyclerView.ViewHolder implements
        View.OnClickListener, View.OnLongClickListener {
    private OnItemClickListener itemClickListener;
    private OnLongItemClickListener longItemClickListener;

    public QiscusItemViewHolder(View itemView, OnItemClickListener itemClickListener, OnLongItemClickListener longItemClickListener) {
        super(itemView);
        ButterKnife.bind(this, itemView);
        this.itemClickListener = itemClickListener;
        this.longItemClickListener = longItemClickListener;
        itemView.setOnClickListener(this);
        itemView.setOnLongClickListener(this);
    }

    public abstract void bind(Data data);

    @Override
    public void onClick(View v) {
        if (itemClickListener != null) {
            itemClickListener.onItemClick(v, getAdapterPosition());
        }
    }

    @Override
    public boolean onLongClick(View v) {
        if (longItemClickListener != null) {
            longItemClickListener.onLongItemClick(v, getAdapterPosition());
            return true;
        }
        return false;
    }
}
