package com.qiscus.sdk.chat.presentation.uikit.adapter.message

import android.view.ViewGroup
import com.qiscus.sdk.chat.presentation.model.MessageViewModel
import com.qiscus.sdk.chat.presentation.uikit.adapter.SortedAdapter
import com.qiscus.sdk.chat.presentation.uikit.util.indexOfFirst

/**
 * Created on : October 17, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
class MessageAdapter : SortedAdapter<MessageViewModel, MessageViewHolder>() {

    private val viewHolderFactories: MutableList<MessageViewHolderFactory> = mutableListOf()

    fun registerViewHolderFactory(messageViewHolderFactory: MessageViewHolderFactory) {
        viewHolderFactories.add(messageViewHolderFactory)
    }

    override fun getItemViewType(position: Int): Int {
        return viewHolderFactories.indexOfFirst { it.isUsingThisFactory(data[position]) }
    }

    override fun getItemClass(): Class<MessageViewModel> {
        return MessageViewModel::class.java
    }

    override fun compare(lhs: MessageViewModel, rhs: MessageViewModel): Int {
        return lhs.message.date.compareTo(rhs.message.date)
    }

    fun addOrUpdate(messageViewModel: MessageViewModel) {
        val pos = data.indexOfFirst { it.message.messageId == messageViewModel.message.messageId }
        if (pos >= 0) {
            data.updateItemAt(pos, messageViewModel)
        } else {
            data.add(messageViewModel)
        }
        notifyDataSetChanged()
    }

    fun removeMessage(messageViewModel: MessageViewModel) {
        val pos = data.indexOfFirst { it.message.messageId == messageViewModel.message.messageId }
        if (pos >= 0) {
            data.removeItemAt(pos)
        }
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return data.size()
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): MessageViewHolder {
        return viewHolderFactories[viewType].onCreateViewHolder(parent)
    }

    override fun onBindViewHolder(holder: MessageViewHolder?, position: Int) {
        holder!!.bind(data[position])
    }
}