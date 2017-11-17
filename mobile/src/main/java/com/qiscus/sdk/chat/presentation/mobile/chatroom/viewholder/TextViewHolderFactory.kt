package com.qiscus.sdk.chat.presentation.mobile.chatroom.viewholder

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.qiscus.sdk.chat.presentation.mobile.R
import com.qiscus.sdk.chat.presentation.model.MessageTextViewModel
import com.qiscus.sdk.chat.presentation.model.MessageViewModel
import com.qiscus.sdk.chat.presentation.uikit.adapter.message.MessageViewHolder
import com.qiscus.sdk.chat.presentation.uikit.adapter.message.MessageViewHolderFactory

/**
 * Created on : October 17, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
class TextViewHolderFactory(private val context: Context) : MessageViewHolderFactory {
    override fun isUsingThisFactory(messageViewModel: MessageViewModel): Boolean {
        return messageViewModel is MessageTextViewModel
    }

    override fun onCreateViewHolder(parent: ViewGroup?): MessageViewHolder {
        return TextViewHolder(LayoutInflater.from(context).inflate(R.layout.item_message, parent, false))
    }
}

class TextViewHolder(view: View) : MessageViewHolder(view) {
    private val senderAvatarView: ImageView = itemView.findViewById(R.id.senderAvatar) as ImageView
    private val senderNameView: TextView = itemView.findViewById(R.id.senderName) as TextView
    private val messageView: TextView = itemView.findViewById(R.id.message) as TextView

    override fun bind(messageViewModel: MessageViewModel) {
        senderNameView.text = messageViewModel.message.sender.name
        messageView.text = messageViewModel.spannableMessage
    }
}