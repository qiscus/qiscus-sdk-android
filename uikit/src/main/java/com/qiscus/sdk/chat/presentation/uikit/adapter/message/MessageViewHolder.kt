package com.qiscus.sdk.chat.presentation.uikit.adapter.message

import android.support.v7.widget.RecyclerView
import android.view.View
import com.qiscus.sdk.chat.presentation.model.MessageViewModel

/**
 * Created on : October 17, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
abstract class MessageViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {
    abstract fun bind(messageViewModel: MessageViewModel)
}