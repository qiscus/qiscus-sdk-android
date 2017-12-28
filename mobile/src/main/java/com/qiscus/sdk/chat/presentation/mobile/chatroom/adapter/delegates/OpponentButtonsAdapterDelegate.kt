package com.qiscus.sdk.chat.presentation.mobile.chatroom.adapter.delegates

import android.content.Context
import android.support.v7.util.SortedList
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.qiscus.sdk.chat.presentation.mobile.R
import com.qiscus.sdk.chat.presentation.model.MessageButtonsViewModel
import com.qiscus.sdk.chat.presentation.model.MessageViewModel
import com.qiscus.sdk.chat.presentation.uikit.adapter.ItemClickListener
import com.qiscus.sdk.chat.presentation.uikit.adapter.ItemLongClickListener

/**
 * Created on : December 21, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
class OpponentButtonsAdapterDelegate @JvmOverloads constructor(private val context: Context,
                                                               private val itemClickListener: ItemClickListener? = null,
                                                               private val itemLongClickListener: ItemLongClickListener? = null)
    : MessageAdapterDelegate() {

    override fun isForViewType(data: SortedList<MessageViewModel>, position: Int): Boolean {
        val messageViewModel = data[position]
        return messageViewModel is MessageButtonsViewModel && messageViewModel.message.sender != account.user
    }

    override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_qiscus_message_button, parent, false)
        return OpponentButtonsViewHolder(view, itemClickListener, itemLongClickListener)
    }
}

open class OpponentButtonsViewHolder @JvmOverloads constructor(view: View,
                                                               itemClickListener: ItemClickListener? = null,
                                                               itemLongClickListener: ItemLongClickListener? = null)
    : OpponentTextViewHolder(view, itemClickListener, itemLongClickListener) {

    private val buttonsContainer: ViewGroup = itemView.findViewById(R.id.buttons_container)

    override fun renderMessageContents(messageViewModel: MessageViewModel) {
        super.renderMessageContents(messageViewModel)
        renderButtons(messageViewModel as MessageButtonsViewModel)
    }

    open protected fun renderButtons(messageViewModel: MessageButtonsViewModel) {
        buttonsContainer.removeAllViews()
        TODO()
    }
}