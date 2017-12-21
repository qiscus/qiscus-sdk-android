package com.qiscus.sdk.chat.presentation.mobile.chatroom.adapter

import android.content.Context
import android.support.v7.util.SortedList
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.qiscus.sdk.chat.presentation.mobile.R
import com.qiscus.sdk.chat.presentation.model.MessageAccountLinkingViewModel
import com.qiscus.sdk.chat.presentation.model.MessageViewModel
import com.qiscus.sdk.chat.presentation.uikit.adapter.ItemClickListener
import com.qiscus.sdk.chat.presentation.uikit.adapter.ItemLongClickListener

/**
 * Created on : December 21, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
class OpponentAccountLinkingAdapterDelegate @JvmOverloads constructor(private val context: Context,
                                                                      private val itemClickListener: ItemClickListener? = null,
                                                                      private val itemLongClickListener: ItemLongClickListener? = null)
    : MessageAdapterDelegate() {

    override fun isForViewType(data: SortedList<MessageViewModel>, position: Int): Boolean {
        val messageViewModel = data[position]
        return messageViewModel is MessageAccountLinkingViewModel && messageViewModel.message.sender != account.user
    }

    override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_qiscus_message_linking, parent, false)
        return OpponentAccountLinkingViewHolder(view, itemClickListener, itemLongClickListener)
    }
}

open class OpponentAccountLinkingViewHolder @JvmOverloads constructor(view: View,
                                                                      itemClickListener: ItemClickListener? = null,
                                                                      itemLongClickListener: ItemLongClickListener? = null)
    : OpponentTextViewHolder(view, itemClickListener, itemLongClickListener) {

    private val buttonLabelView: TextView = itemView.findViewById(R.id.account_linking)

    override fun renderMessageContents(messageViewModel: MessageViewModel) {
        super.renderMessageContents(messageViewModel)
        renderButton(messageViewModel)
    }

    open protected fun renderButton(messageViewModel: MessageViewModel) {
        buttonLabelView.text = (messageViewModel as MessageAccountLinkingViewModel).button.title
    }

    override fun onClick(v: View?) {
        if (v == buttonLabelView) {
            val position = adapterPosition
            if (position >= 0) {
                itemClickListener?.onItemClick(v, position)
            }
        }
    }
}