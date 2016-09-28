package com.qiscus.dragonfly;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.qiscus.sdk.ui.adapter.OnItemClickListener;
import com.qiscus.sdk.ui.adapter.OnLongItemClickListener;
import com.qiscus.sdk.ui.adapter.viewholder.QiscusBaseTextMessageViewHolder;

/**
 * Created on : September 27, 2016
 * Author     : zetbaitsu
 * Name       : Zetra
 * Email      : zetra@mail.ugm.ac.id
 * GitHub     : https://github.com/zetbaitsu
 * LinkedIn   : https://id.linkedin.com/in/zetbaitsu
 */
public class MyMessageViewHolder extends QiscusBaseTextMessageViewHolder {
    public MyMessageViewHolder(View itemView, OnItemClickListener itemClickListener, OnLongItemClickListener longItemClickListener) {
        super(itemView, itemClickListener, longItemClickListener);
    }

    @NonNull
    @Override
    public TextView getMessageTextView(View itemView) {
        return (TextView) itemView.findViewById(R.id.message);
    }

    @Nullable
    @Override
    protected ImageView getFirstMessageBubbleIndicatorView(View itemView) {
        return null;
    }

    @NonNull
    @Override
    protected View getMessageBubbleView(View itemView) {
        return itemView.findViewById(R.id.bubble);
    }

    @Nullable
    @Override
    public TextView getDateView(View itemView) {
        return null;
    }

    @Nullable
    @Override
    public TextView getTimeView(View itemView) {
        return null;
    }

    @Nullable
    @Override
    public ImageView getMessageStateIndicatorView(View itemView) {
        return null;
    }
}
