package com.qiscus.sdk.ui.adapter.viewholder;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.lzyzsd.circleprogress.CircleProgress;
import com.qiscus.sdk.R;
import com.qiscus.sdk.ui.adapter.OnItemClickListener;
import com.qiscus.sdk.ui.adapter.OnLongItemClickListener;

/**
 * Created on : September 27, 2016
 * Author     : zetbaitsu
 * Name       : Zetra
 * Email      : zetra@mail.ugm.ac.id
 * GitHub     : https://github.com/zetbaitsu
 * LinkedIn   : https://id.linkedin.com/in/zetbaitsu
 */
public class QiscusFileViewHolder extends QiscusBaseFileMessageViewHolder {

    public QiscusFileViewHolder(View itemView, OnItemClickListener itemClickListener, OnLongItemClickListener longItemClickListener) {
        super(itemView, itemClickListener, longItemClickListener);
    }

    @Nullable
    @Override
    protected ImageView getFirstMessageBubbleIndicatorView(View itemView) {
        return (ImageView) itemView.findViewById(R.id.bubble);
    }

    @NonNull
    @Override
    protected View getMessageBubbleView(View itemView) {
        return itemView.findViewById(R.id.message);
    }

    @Nullable
    @Override
    public TextView getDateView(View itemView) {
        return (TextView) itemView.findViewById(R.id.date);
    }

    @Nullable
    @Override
    public TextView getTimeView(View itemView) {
        return (TextView) itemView.findViewById(R.id.time);
    }

    @Nullable
    @Override
    public ImageView getMessageStateIndicatorView(View itemView) {
        return (ImageView) itemView.findViewById(R.id.icon_read);
    }

    @NonNull
    @Override
    public TextView getFileNameView(View itemView) {
        return (TextView) itemView.findViewById(R.id.file_name);
    }

    @Nullable
    @Override
    public TextView getFileTypeView(View itemView) {
        return (TextView) itemView.findViewById(R.id.file_type);
    }

    @Nullable
    @Override
    public CircleProgress getProgressView(View itemView) {
        return (CircleProgress) itemView.findViewById(R.id.progress);
    }

    @Nullable
    @Override
    public ImageView getDownloadIconView(View itemView) {
        return (ImageView) itemView.findViewById(R.id.iv_download);
    }

}
