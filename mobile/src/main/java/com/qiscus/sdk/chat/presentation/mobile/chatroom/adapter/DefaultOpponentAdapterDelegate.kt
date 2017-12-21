package com.qiscus.sdk.chat.presentation.mobile.chatroom.adapter

import android.content.Context
import android.support.v7.util.SortedList
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.qiscus.sdk.chat.presentation.mobile.R
import com.qiscus.sdk.chat.presentation.model.MessageViewModel
import com.qiscus.sdk.chat.presentation.uikit.adapter.ItemClickListener
import com.qiscus.sdk.chat.presentation.uikit.adapter.ItemLongClickListener

/**
 * Created on : December 20, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
class DefaultOpponentAdapterDelegate @JvmOverloads constructor(private val context: Context,
                                                               private val itemClickListener: ItemClickListener? = null,
                                                               private val itemLongClickListener: ItemLongClickListener? = null)
    : MessageAdapterDelegate() {

    override fun isForViewType(data: SortedList<MessageViewModel>, position: Int): Boolean {
        return data[position].message.sender != account.user
    }

    override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_qiscus_message_text, parent, false)
        return OpponentTextViewHolder(view, itemClickListener, itemLongClickListener)
    }
}

class DefaultOpponentMultiLineAdapterDelegate @JvmOverloads constructor(private val context: Context,
                                                                        private val itemClickListener: ItemClickListener? = null,
                                                                        private val itemLongClickListener: ItemLongClickListener? = null)
    : MessageAdapterDelegate() {

    override fun isForViewType(data: SortedList<MessageViewModel>, position: Int): Boolean {
        return data[position].message.sender != account.user
                && data[position].readableMessage.contains(System.getProperty("line.separator"))
    }

    override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_qiscus_message_multi_line_text, parent, false)
        return OpponentTextViewHolder(view, itemClickListener, itemLongClickListener)
    }
}