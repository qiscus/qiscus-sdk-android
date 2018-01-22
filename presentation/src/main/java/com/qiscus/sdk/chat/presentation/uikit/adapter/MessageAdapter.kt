package com.qiscus.sdk.chat.presentation.uikit.adapter

import android.support.v7.util.SortedList
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.qiscus.sdk.chat.presentation.model.MessageViewModel
import com.qiscus.sdk.chat.presentation.util.indexOfFirst

/**
 * Created on : October 17, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
open class MessageAdapter : SortedAdapter<MessageViewModel, RecyclerView.ViewHolder>() {

    val delegatesManager = AdapterDelegatesManager<SortedList<MessageViewModel>>()

    override fun getItemClass(): Class<MessageViewModel> {
        return MessageViewModel::class.java
    }

    override fun compare(lhs: MessageViewModel, rhs: MessageViewModel): Int {
        return rhs.message.date.compareTo(lhs.message.date)
    }

    fun addOrUpdate(messageViewModel: MessageViewModel) {
        val pos = data.indexOfFirst { it.message.messageId == messageViewModel.message.messageId }
        if (pos >= 0) {
            messageViewModel.selected = data[pos].selected
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

    override fun getItemViewType(position: Int): Int {
        return delegatesManager.getItemViewType(data, position)
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {
        return delegatesManager.onCreateViewHolder(parent!!, viewType)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        delegatesManager.onBindViewHolder(data, position, holder!!)
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder?) {
        delegatesManager.onViewRecycled(holder!!)
    }

    override fun onFailedToRecycleView(holder: RecyclerView.ViewHolder?): Boolean {
        return delegatesManager.onFailedToRecycleView(holder!!)
    }

    override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder?) {
        delegatesManager.onViewAttachedToWindow(holder!!)
    }

    override fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder?) {
        delegatesManager.onViewDetachedFromWindow(holder!!)
    }

    fun getSelectedMessages(): List<MessageViewModel> {
        val size = data.size()
        return (size - 1 downTo 0)
                .filter { data[it].selected }
                .map { data[it] }
    }
}