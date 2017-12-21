package com.qiscus.sdk.chat.presentation.mobile.chatroom.adapter

import android.content.Context
import android.support.v7.util.SortedList
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.qiscus.sdk.chat.presentation.mobile.R
import com.qiscus.sdk.chat.presentation.model.MessageLocationViewModel
import com.qiscus.sdk.chat.presentation.model.MessageViewModel
import com.qiscus.sdk.chat.presentation.uikit.adapter.ItemClickListener
import com.qiscus.sdk.chat.presentation.uikit.adapter.ItemLongClickListener

/**
 * Created on : December 21, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
class LocationAdapterDelegate @JvmOverloads constructor(private val context: Context,
                                                        private val itemClickListener: ItemClickListener? = null,
                                                        private val itemLongClickListener: ItemLongClickListener? = null)
    : MessageAdapterDelegate() {

    override fun isForViewType(data: SortedList<MessageViewModel>, position: Int): Boolean {
        val messageViewModel = data[position]
        return messageViewModel is MessageLocationViewModel && messageViewModel.message.sender == account.user
    }

    override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_qiscus_message_location_me, parent, false)
        return LocationViewHolder(view, itemClickListener, itemLongClickListener)
    }
}

open class LocationViewHolder @JvmOverloads constructor(view: View,
                                                        itemClickListener: ItemClickListener? = null,
                                                        itemLongClickListener: ItemLongClickListener? = null)
    : MessageViewHolder(view, itemClickListener, itemLongClickListener) {

    private val mapImageView: ImageView = itemView.findViewById(R.id.thumbnail)
    private val locationNameView: TextView = itemView.findViewById(R.id.location_name)
    private val locationAddressView: TextView = itemView.findViewById(R.id.location_address)

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
        locationNameView.text = (messageViewModel as MessageLocationViewModel).locationName
        locationAddressView.text = messageViewModel.locationAddress
        Glide.with(mapImageView)
                .load(messageViewModel.thumbnailUrl)
                .apply(RequestOptions()
                        .placeholder(R.drawable.ic_qiscus_place_holder_map)
                        .error(R.drawable.ic_qiscus_place_holder_map)
                ).into(mapImageView)
    }
}