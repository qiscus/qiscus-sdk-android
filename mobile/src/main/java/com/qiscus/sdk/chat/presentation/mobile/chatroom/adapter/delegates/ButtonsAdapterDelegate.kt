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
import com.qiscus.sdk.chat.presentation.uikit.util.getColor
import com.qiscus.sdk.chat.presentation.uikit.util.getDrawable
import com.qiscus.sdk.chat.presentation.uikit.widget.ChatButtonView

/**
 * Created on : December 21, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
class ButtonsAdapterDelegate(private val context: Context,
                             private val chatButtonClickListener: ChatButtonView.ChatButtonClickListener)
    : MessageAdapterDelegate() {

    override fun isForViewType(data: SortedList<MessageViewModel>, position: Int): Boolean {
        val messageViewModel = data[position]
        return messageViewModel is MessageButtonsViewModel && messageViewModel.message.sender == account.user
    }

    override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_qiscus_message_button_me, parent, false)
        return ButtonsViewHolder(view, chatButtonClickListener)
    }
}

open class ButtonsViewHolder(view: View,
                             private val chatButtonClickListener: ChatButtonView.ChatButtonClickListener)
    : TextViewHolder(view) {

    private val buttonsContainer: ViewGroup = itemView.findViewById(R.id.buttons_container)

    private val buttonTextColor = getColor(resId = R.color.qiscus_my_message_buttons_text)
    private val buttonBackgroundColor = getColor(resId = R.color.qiscus_my_message_buttons_bg)
    private val buttonBackgroundDrawable = getDrawable(resId = R.drawable.qiscus_my_buttons_bg)

    override fun renderMessageContents(messageViewModel: MessageViewModel) {
        super.renderMessageContents(messageViewModel)
        renderButtons(messageViewModel as MessageButtonsViewModel)
    }

    open protected fun renderButtons(messageViewModel: MessageButtonsViewModel) {
        if (messageViewModel.buttons.isEmpty()) {
            buttonsContainer.visibility = View.GONE
            return
        }

        buttonsContainer.removeAllViews()
        messageViewModel.buttons.forEachIndexed { index, buttonViewModel ->
            val buttonView = ChatButtonView(buttonsContainer.context, buttonViewModel)
            buttonView.setChatButtonClickListener(chatButtonClickListener)
            buttonView.setTextColor(buttonTextColor)
            if (index == messageViewModel.buttons.size - 1) {
                buttonView.background = buttonBackgroundDrawable
            } else {
                buttonView.setBackgroundColor(buttonBackgroundColor)
            }
            buttonsContainer.addView(buttonView)
        }

        buttonsContainer.visibility = View.VISIBLE
    }
}