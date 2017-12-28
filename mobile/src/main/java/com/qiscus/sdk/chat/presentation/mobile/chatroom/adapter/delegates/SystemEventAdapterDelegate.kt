package com.qiscus.sdk.chat.presentation.mobile.chatroom.adapter.delegates

import android.content.Context
import android.support.v7.util.SortedList
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.qiscus.sdk.chat.presentation.mobile.R
import com.qiscus.sdk.chat.presentation.model.MessageSystemEventViewModel
import com.qiscus.sdk.chat.presentation.model.MessageViewModel
import com.qiscus.sdk.chat.presentation.uikit.adapter.AdapterDelegate
import com.qiscus.sdk.chat.presentation.uikit.util.isEqualIgnoreTime
import com.qiscus.sdk.chat.presentation.uikit.util.toTodayOrDate

/**
 * Created on : December 21, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
class SystemEventAdapterDelegate(private val context: Context) : AdapterDelegate<SortedList<MessageViewModel>>() {

    override fun isForViewType(data: SortedList<MessageViewModel>, position: Int): Boolean {
        return data[position] is MessageSystemEventViewModel
    }

    override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        return SystemEventViewHolder(LayoutInflater.from(context).inflate(R.layout.item_qiscus_message_system_event, parent, false))
    }

    override fun onBindViewHolder(data: SortedList<MessageViewModel>, position: Int, holder: RecyclerView.ViewHolder) {
        determineIsNeedToShowDate(data, position, holder as SystemEventViewHolder)
        holder.bind(data[position])
    }

    private fun determineIsNeedToShowDate(data: SortedList<MessageViewModel>, position: Int, holder: SystemEventViewHolder) {
        if (position == data.size() - 1) {
            holder.needToShowDate = true
        } else {
            holder.needToShowDate = !data[position].message.date.isEqualIgnoreTime(data[position + 1].message.date)
        }
    }
}

open class SystemEventViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    var needToShowDate: Boolean = false
    private val dateView: TextView = itemView.findViewById(R.id.date)
    private val messageView: TextView = itemView.findViewById(R.id.contents)

    open fun bind(messageViewModel: MessageViewModel) {
        renderDate(messageViewModel)
        renderMessageContents(messageViewModel)
    }

    open protected fun renderDate(messageViewModel: MessageViewModel) {
        dateView.text = messageViewModel.message.date.toTodayOrDate()
        dateView.visibility = if (needToShowDate) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    open protected fun renderMessageContents(messageViewModel: MessageViewModel) {
        messageView.text = messageViewModel.spannableMessage
    }
}