package com.qiscus.sdk.chat.presentation.mobile.chatroom.adapter.delegates

import android.support.v7.util.SortedList
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.qiscus.sdk.chat.core.Qiscus
import com.qiscus.sdk.chat.domain.model.MessageState
import com.qiscus.sdk.chat.presentation.mobile.R
import com.qiscus.sdk.chat.presentation.model.MessageCardViewModel
import com.qiscus.sdk.chat.presentation.model.MessageViewModel
import com.qiscus.sdk.chat.presentation.uikit.adapter.AdapterDelegate
import com.qiscus.sdk.chat.presentation.uikit.adapter.ItemClickListener
import com.qiscus.sdk.chat.presentation.uikit.adapter.ItemLongClickListener
import com.qiscus.sdk.chat.presentation.uikit.util.getColor
import com.qiscus.sdk.chat.presentation.uikit.util.isEqualIgnoreTime
import com.qiscus.sdk.chat.presentation.uikit.util.toHour
import com.qiscus.sdk.chat.presentation.uikit.util.toTodayOrDate

/**
 * Created on : December 20, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
abstract class MessageAdapterDelegate : AdapterDelegate<SortedList<MessageViewModel>>() {
    protected val account = Qiscus.instance.component.dataComponent.accountRepository.getAccount().blockingGet()

    override fun onBindViewHolder(data: SortedList<MessageViewModel>, position: Int, holder: RecyclerView.ViewHolder) {
        determineIsNeedToShowDate(data, position, holder as BaseMessageViewHolder)
        determineIsNeedToShowFirstMessageIndicator(data, position, holder)
        holder.bind(data[position])
    }

    private fun determineIsNeedToShowFirstMessageIndicator(data: SortedList<MessageViewModel>, position: Int, holder: BaseMessageViewHolder) {
        if (holder.needToShowDate || data[position + 1] is MessageCardViewModel) {
            holder.needToShowFirstMessageBubbleIndicator = true
        } else {
            holder.needToShowFirstMessageBubbleIndicator = data[position].message.sender != data[position + 1].message.sender
        }
    }

    private fun determineIsNeedToShowDate(data: SortedList<MessageViewModel>, position: Int, holder: BaseMessageViewHolder) {
        if (position == data.size() - 1) {
            holder.needToShowDate = true
        } else {
            holder.needToShowDate = !data[position].message.date.isEqualIgnoreTime(data[position + 1].message.date)
        }
    }
}

abstract class BaseMessageViewHolder @JvmOverloads constructor(view: View,
                                                               protected var itemClickListener: ItemClickListener? = null,
                                                               protected var itemLongClickListener: ItemLongClickListener? = null)
    : RecyclerView.ViewHolder(view), View.OnClickListener, View.OnLongClickListener {

    var needToShowDate: Boolean = false
    var needToShowFirstMessageBubbleIndicator: Boolean = false
    var needToShowSenderName: Boolean = false

    protected val dateView: TextView by lazy {
        determineDateView()
    }

    protected val firstMessageBubbleIndicatorView: ImageView by lazy {
        determineFirstMessageBubbleIndicatorView()
    }

    protected val bubbleView: ViewGroup by lazy {
        determineBubbleView()
    }

    protected val timeView: TextView by lazy {
        determineTimeView()
    }

    init {
        bubbleView.setOnClickListener(this)
        bubbleView.setOnLongClickListener(this)
    }

    abstract fun determineDateView(): TextView

    abstract fun determineFirstMessageBubbleIndicatorView(): ImageView

    abstract fun determineBubbleView(): ViewGroup

    abstract fun determineTimeView(): TextView

    open fun bind(messageViewModel: MessageViewModel) {
        renderDate(messageViewModel)
        renderFirstMessageBubbleIndicator(messageViewModel)
        renderTime(messageViewModel)
        renderMessageContents(messageViewModel)
    }

    abstract fun renderMessageContents(messageViewModel: MessageViewModel)

    open protected fun renderDate(messageViewModel: MessageViewModel) {
        dateView.text = messageViewModel.message.date.toTodayOrDate()
        dateView.visibility = if (needToShowDate) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    open protected fun renderFirstMessageBubbleIndicator(messageViewModel: MessageViewModel) {
        firstMessageBubbleIndicatorView.visibility = if (needToShowFirstMessageBubbleIndicator) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    open protected fun renderTime(messageViewModel: MessageViewModel) {
        timeView.text = messageViewModel.message.date.toHour()
    }

    override fun onClick(v: View?) {
        if (v == bubbleView) {
            val position = adapterPosition
            if (position >= 0) {
                itemClickListener?.onItemClick(v, position)
            }
        }
    }

    override fun onLongClick(v: View?): Boolean {
        if (itemLongClickListener != null) {
            val position = adapterPosition
            if (position >= 0) {
                itemLongClickListener?.onItemLongClick(v!!, position)
            }
            return true
        }
        return false
    }
}

abstract class MessageViewHolder @JvmOverloads constructor(view: View,
                                                           itemClickListener: ItemClickListener? = null,
                                                           itemLongClickListener: ItemLongClickListener? = null)
    : BaseMessageViewHolder(view, itemClickListener, itemLongClickListener) {

    protected val normalTimeColor: Int = getColor(resId = R.color.qiscus_my_message_time)
    protected val failedTintColor: Int = getColor(resId = R.color.qiscus_message_failed_tint)
    protected val readTintColor: Int = getColor(resId = R.color.qiscus_message_read_tint)

    protected val messageStateView: ImageView by lazy {
        determineMessageStateView()
    }

    abstract fun determineMessageStateView(): ImageView

    override fun bind(messageViewModel: MessageViewModel) {
        super.bind(messageViewModel)
        renderMessageState(messageViewModel)
    }

    open protected fun renderMessageState(messageViewModel: MessageViewModel) {
        messageStateView.setImageResource(when (messageViewModel.message.state) {
            MessageState.PENDING -> R.drawable.ic_qiscus_info_time
            MessageState.SENDING -> R.drawable.ic_qiscus_info_time
            MessageState.ON_SERVER -> R.drawable.ic_qiscus_sending
            MessageState.DELIVERED -> R.drawable.ic_qiscus_read
            MessageState.READ -> R.drawable.ic_qiscus_read
            MessageState.FAILED -> R.drawable.ic_qiscus_sending_failed
        })

        messageStateView.setColorFilter(when (messageViewModel.message.state) {
            MessageState.PENDING -> normalTimeColor
            MessageState.SENDING -> normalTimeColor
            MessageState.ON_SERVER -> normalTimeColor
            MessageState.DELIVERED -> normalTimeColor
            MessageState.READ -> readTintColor
            MessageState.FAILED -> failedTintColor
        })
    }

    override fun renderTime(messageViewModel: MessageViewModel) {
        super.renderTime(messageViewModel)
        timeView.setTextColor(when (messageViewModel.message.state) {
            MessageState.PENDING -> normalTimeColor
            MessageState.SENDING -> normalTimeColor
            MessageState.ON_SERVER -> normalTimeColor
            MessageState.DELIVERED -> normalTimeColor
            MessageState.READ -> normalTimeColor
            MessageState.FAILED -> failedTintColor
        })
    }
}

abstract class OpponentMessageViewHolder @JvmOverloads constructor(view: View,
                                                                   itemClickListener: ItemClickListener? = null,
                                                                   itemLongClickListener: ItemLongClickListener? = null)
    : BaseMessageViewHolder(view, itemClickListener, itemLongClickListener) {

    protected val senderNameView: TextView by lazy {
        determineSenderNameView()
    }

    protected val senderAvatarView: ImageView by lazy {
        determineSenderAvatarView()
    }

    abstract fun determineSenderNameView(): TextView

    abstract fun determineSenderAvatarView(): ImageView

    override fun bind(messageViewModel: MessageViewModel) {
        super.bind(messageViewModel)
        renderSenderName(messageViewModel)
        renderSenderAvatar(messageViewModel)
    }

    open protected fun renderSenderName(messageViewModel: MessageViewModel) {
        senderNameView.text = "~ ${messageViewModel.message.sender.name}"
        senderNameView.visibility = if (needToShowSenderName) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    open protected fun renderSenderAvatar(messageViewModel: MessageViewModel) {
        Glide.with(senderAvatarView)
                .load(messageViewModel.message.sender.avatar)
                .into(senderAvatarView)
        senderAvatarView.visibility = if (needToShowFirstMessageBubbleIndicator) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }
}