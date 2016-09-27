package com.qiscus.sdk.ui.adapter.viewholder;

import android.support.annotation.NonNull;
import android.view.View;
import android.widget.TextView;

import com.qiscus.sdk.data.model.QiscusComment;
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
public abstract class QiscusBaseTextMessageViewHolder extends QiscusBaseMessageViewHolder<QiscusComment> {

    @NonNull protected TextView messageTextView;

    public QiscusBaseTextMessageViewHolder(View itemView, OnItemClickListener itemClickListener, OnLongItemClickListener longItemClickListener) {
        super(itemView, itemClickListener, longItemClickListener);
        messageTextView = getMessageTextView(itemView);
    }

    @NonNull
    public abstract TextView getMessageTextView(View itemView);

    @Override
    protected void setUpColor() {
        messageTextView.setTextColor(messageFromMe ? rightBubbleTextColor : leftBubbleTextColor);
        super.setUpColor();
    }

    @Override
    protected void showMessage(QiscusComment qiscusComment) {
        messageTextView.setText(qiscusComment.getMessage());
    }
}
