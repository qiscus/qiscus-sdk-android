package com.qiscus.sdk.chat.presentation.mobile.chatroom.adapter

import android.content.Context
import android.support.v7.util.SortedList
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.qiscus.sdk.chat.domain.common.containsUrl
import com.qiscus.sdk.chat.presentation.mobile.R
import com.qiscus.sdk.chat.presentation.model.MessageTextViewModel
import com.qiscus.sdk.chat.presentation.model.MessageViewModel
import com.qiscus.sdk.chat.presentation.uikit.adapter.ItemClickListener
import com.qiscus.sdk.chat.presentation.uikit.adapter.ItemLongClickListener

/**
 * Created on : December 21, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
class LinkAdapterDelegate @JvmOverloads constructor(private val context: Context,
                                                    private val itemClickListener: ItemClickListener? = null,
                                                    private val itemLongClickListener: ItemLongClickListener? = null)
    : MessageAdapterDelegate() {

    override fun isForViewType(data: SortedList<MessageViewModel>, position: Int): Boolean {
        val messageViewModel = data[position]
        return messageViewModel is MessageTextViewModel && messageViewModel.message.sender == account.user
                && messageViewModel.readableMessage.containsUrl()
    }

    override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_qiscus_message_link_me, parent, false)
        return LinkViewHolder(view, itemClickListener, itemLongClickListener)
    }
}

open class LinkViewHolder @JvmOverloads constructor(view: View,
                                                    itemClickListener: ItemClickListener? = null,
                                                    itemLongClickListener: ItemLongClickListener? = null)
    : TextViewHolder(view, itemClickListener, itemLongClickListener) {

    override fun renderMessageContents(messageViewModel: MessageViewModel) {
        super.renderMessageContents(messageViewModel)
        renderLinks(messageViewModel)
    }

    open protected fun renderLinks(messageViewModel: MessageViewModel) {
        TODO()
    }
}