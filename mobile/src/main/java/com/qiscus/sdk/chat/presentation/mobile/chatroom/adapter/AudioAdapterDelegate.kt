package com.qiscus.sdk.chat.presentation.mobile.chatroom.adapter

import android.content.Context
import android.support.v7.util.SortedList
import android.support.v7.widget.AppCompatSeekBar
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.qiscus.sdk.chat.presentation.mobile.R
import com.qiscus.sdk.chat.presentation.model.MessageAudioViewModel
import com.qiscus.sdk.chat.presentation.model.MessageViewModel
import com.qiscus.sdk.chat.presentation.uikit.adapter.ItemClickListener
import com.qiscus.sdk.chat.presentation.uikit.adapter.ItemLongClickListener
import com.qiscus.sdk.chat.presentation.uikit.widget.CircleProgress

/**
 * Created on : December 21, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
class AudioAdapterDelegate @JvmOverloads constructor(private val context: Context,
                                                     private val itemClickListener: ItemClickListener? = null,
                                                     private val itemLongClickListener: ItemLongClickListener? = null)
    : MessageAdapterDelegate() {

    override fun isForViewType(data: SortedList<MessageViewModel>, position: Int): Boolean {
        val messageViewModel = data[position]
        return messageViewModel is MessageAudioViewModel && messageViewModel.message.sender == account.user
    }

    override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_qiscus_message_audio_me, parent, false)
        return AudioViewHolder(view, itemClickListener, itemLongClickListener)
    }
}

open class AudioViewHolder @JvmOverloads constructor(view: View,
                                                     itemClickListener: ItemClickListener? = null,
                                                     itemLongClickListener: ItemLongClickListener? = null)
    : MessageViewHolder(view, itemClickListener, itemLongClickListener) {

    private val playButton: ImageView = itemView.findViewById(R.id.iv_play)
    private val seekBar: AppCompatSeekBar = itemView.findViewById(R.id.seekbar)
    private val durationView: TextView = itemView.findViewById(R.id.duration)
    private val progressView: CircleProgress = itemView.findViewById(R.id.progress)

    init {
        seekBar.setOnTouchListener { _, _ -> true }
    }

    override fun determineDateView(): TextView {
        return itemView.findViewById(R.id.date)
    }

    override fun determineFirstMessageBubbleIndicatorView(): ImageView {
        return itemView.findViewById(R.id.bubble)
    }

    override fun determineBubbleView(): ViewGroup {
        return itemView.findViewById(R.id.message)
    }

    override fun determineTimeView(): TextView {
        return itemView.findViewById(R.id.time)
    }

    override fun determineMessageStateView(): ImageView {
        return itemView.findViewById(R.id.icon_read)
    }

    override fun renderMessageContents(messageViewModel: MessageViewModel) {
        TODO()
    }

    open protected fun renderProgress(messageViewModel: MessageAudioViewModel) {
        TODO()
    }
}