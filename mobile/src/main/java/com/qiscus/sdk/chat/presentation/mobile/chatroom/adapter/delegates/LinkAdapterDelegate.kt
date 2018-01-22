package com.qiscus.sdk.chat.presentation.mobile.chatroom.adapter.delegates

import android.content.Context
import android.support.v7.util.SortedList
import android.support.v7.widget.RecyclerView
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.qiscus.sdk.chat.domain.common.Patterns
import com.qiscus.sdk.chat.presentation.mobile.R
import com.qiscus.sdk.chat.presentation.model.LinkPreviewListener
import com.qiscus.sdk.chat.presentation.model.MessageLinkViewModel
import com.qiscus.sdk.chat.presentation.model.MessageViewModel
import com.qiscus.sdk.chat.presentation.uikit.adapter.ItemClickListener
import com.qiscus.sdk.chat.presentation.uikit.adapter.ItemLongClickListener
import com.qiscus.sdk.chat.presentation.util.ClickSpan
import com.qiscus.sdk.chat.presentation.util.startCustomTabActivity
import com.qiscus.sdk.chat.presentation.uikit.widget.WebPreviewView

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
        return messageViewModel is MessageLinkViewModel && messageViewModel.message.sender == account.user
    }

    override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_qiscus_message_link_me, parent, false)
        return LinkViewHolder(view, itemClickListener, itemLongClickListener)
    }
}

open class LinkViewHolder @JvmOverloads constructor(view: View,
                                                    itemClickListener: ItemClickListener? = null,
                                                    itemLongClickListener: ItemLongClickListener? = null)
    : TextViewHolder(view, itemClickListener, itemLongClickListener), LinkPreviewListener {

    private val webPreview: WebPreviewView = itemView.findViewById(R.id.preview)

    override fun renderMessageContents(messageViewModel: MessageViewModel) {
        super.renderMessageContents(messageViewModel)
        renderLinks(messageViewModel as MessageLinkViewModel)
    }

    open protected fun renderLinks(messageViewModel: MessageLinkViewModel) {
        messageViewModel.linkPreviewListener = this
        messageViewModel.loadLinkPreview()
        val message = messageView.text.toString()
        val matcher = Patterns.AUTOLINK_WEB_URL.matcher(message)
        while (matcher.find()) {
            val start = matcher.start()
            if (start > 0 && message[start - 1] == '@') {
                continue
            }
            val end = matcher.end()
            clickify(start, end) {
                var url = message.substring(start, end)
                if (!url.startsWith("http")) {
                    url = "http://" + url
                }
                messageView.context.startCustomTabActivity(url)
            }
        }
    }

    private fun clickify(start: Int, end: Int, onClick: () -> Unit) {
        val text = messageView.text
        val span = ClickSpan(onClick)

        if (start == -1) {
            return
        }

        if (text is Spannable) {
            text.setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        } else {
            val s = SpannableString.valueOf(text)
            s.setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            messageView.text = s
        }
    }

    override fun onLinkPreviewReady(messageViewModel: MessageLinkViewModel) {
        webPreview.bind(messageViewModel.previewData)
    }
}