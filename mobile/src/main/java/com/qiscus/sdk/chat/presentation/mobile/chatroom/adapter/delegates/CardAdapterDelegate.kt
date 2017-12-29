package com.qiscus.sdk.chat.presentation.mobile.chatroom.adapter.delegates

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
import com.qiscus.sdk.chat.presentation.model.MessageCardViewModel
import com.qiscus.sdk.chat.presentation.model.MessageViewModel
import com.qiscus.sdk.chat.presentation.uikit.util.getColor
import com.qiscus.sdk.chat.presentation.uikit.widget.ChatButtonView

/**
 * Created on : December 21, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
class CardAdapterDelegate(private val context: Context,
                          private val chatButtonClickListener: ChatButtonView.ChatButtonClickListener)
    : MessageAdapterDelegate() {

    override fun isForViewType(data: SortedList<MessageViewModel>, position: Int): Boolean {
        val messageViewModel = data[position]
        return messageViewModel is MessageCardViewModel && messageViewModel.message.sender == account.user
    }

    override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_qiscus_message_card_me, parent, false)
        return CardViewHolder(view, chatButtonClickListener)
    }
}

open class CardViewHolder(view: View,
                          private val chatButtonClickListener: ChatButtonView.ChatButtonClickListener)
    : TextViewHolder(view) {

    private val messageBubbleView: View = itemView.findViewById(R.id.message)
    private val imageView: ImageView = itemView.findViewById(R.id.thumbnail)
    private val titleView: TextView = itemView.findViewById(R.id.title)
    private val descriptionView: TextView = itemView.findViewById(R.id.description)
    private val buttonsContainer: ViewGroup = itemView.findViewById(R.id.buttons_container)

    private val buttonTextColor = getColor(resId = R.color.qiscus_message_card_buttons_text)
    private val buttonBackgroundColor = getColor(resId = R.color.qiscus_message_card_buttons_bg)

    override fun renderMessageContents(messageViewModel: MessageViewModel) {
        super.renderMessageContents(messageViewModel)
        renderMessageBubble(messageViewModel)
        renderCard(messageViewModel)
    }

    open protected fun renderMessageBubble(messageViewModel: MessageViewModel) {
        if (messageViewModel.readableMessage.isBlank()) {
            messageBubbleView.visibility = View.GONE
            firstMessageBubbleIndicatorView.visibility = View.GONE
        } else {
            messageBubbleView.visibility = View.VISIBLE
        }
    }

    open protected fun renderCard(messageViewModel: MessageViewModel) {
        titleView.text = (messageViewModel as MessageCardViewModel).cardTitle
        descriptionView.text = messageViewModel.cardDescription
        Glide.with(imageView)
                .load(messageViewModel.cardImage)
                .apply(RequestOptions().error(R.drawable.qiscus_image_place_holder)
                        .placeholder(R.drawable.qiscus_image_place_holder)
                ).into(imageView)
        renderButtons(messageViewModel)
    }

    private fun renderButtons(messageViewModel: MessageCardViewModel) {
        if (messageViewModel.buttons.isEmpty()) {
            buttonsContainer.visibility = View.GONE
            return
        }

        buttonsContainer.removeAllViews()
        messageViewModel.buttons.forEachIndexed { _, buttonViewModel ->
            val buttonView = ChatButtonView(buttonsContainer.context, buttonViewModel)
            buttonView.setChatButtonClickListener(chatButtonClickListener)
            buttonView.setTextColor(buttonTextColor)
            buttonView.setBackgroundColor(buttonBackgroundColor)
            buttonsContainer.addView(buttonView)
        }

        buttonsContainer.visibility = View.VISIBLE
    }
}