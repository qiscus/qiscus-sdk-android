package com.qiscus.sdk.chat.presentation.mobile.chatroom.viewholder

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import com.qiscus.sdk.chat.presentation.mobile.R
import com.qiscus.sdk.chat.presentation.model.MessageViewModel
import com.qiscus.sdk.chat.presentation.uikit.adapter.message.MessageViewHolder
import com.qiscus.sdk.chat.presentation.uikit.adapter.message.MessageViewHolderFactory

/**
 * Created on : October 17, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
class DefaultViewHolderFactory(private val context: Context) : MessageViewHolderFactory {
    override fun isUsingThisFactory(messageViewModel: MessageViewModel): Boolean {
        return true
    }

    override fun onCreateViewHolder(parent: ViewGroup?): MessageViewHolder {
        return TextViewHolder(LayoutInflater.from(context).inflate(R.layout.item_message, parent, false))
    }
}