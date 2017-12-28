package com.qiscus.sdk.chat.presentation.mobile.chatroom.adapter.delegates

import android.content.Context
import android.support.v7.util.SortedList
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.qiscus.sdk.chat.domain.model.FileAttachmentMessage
import com.qiscus.sdk.chat.domain.model.FileAttachmentProgress
import com.qiscus.sdk.chat.domain.model.MessageState
import com.qiscus.sdk.chat.presentation.mobile.R
import com.qiscus.sdk.chat.presentation.model.MessageFileViewModel
import com.qiscus.sdk.chat.presentation.model.MessageViewModel
import com.qiscus.sdk.chat.presentation.model.ProgressListener
import com.qiscus.sdk.chat.presentation.model.TransferListener
import com.qiscus.sdk.chat.presentation.uikit.adapter.ItemClickListener
import com.qiscus.sdk.chat.presentation.uikit.adapter.ItemLongClickListener
import com.qiscus.sdk.chat.presentation.uikit.widget.CircleProgress

/**
 * Created on : December 21, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
class FileAdapterDelegate @JvmOverloads constructor(private val context: Context,
                                                    private val itemClickListener: ItemClickListener? = null,
                                                    private val itemLongClickListener: ItemLongClickListener? = null)
    : MessageAdapterDelegate() {

    override fun isForViewType(data: SortedList<MessageViewModel>, position: Int): Boolean {
        val messageViewModel = data[position]
        return messageViewModel is MessageFileViewModel && messageViewModel.message.sender == account.user
    }

    override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_qiscus_message_file_me, parent, false)
        return FileViewHolder(view, itemClickListener, itemLongClickListener)
    }

    override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) {
        (holder as FileViewHolder).attach()
    }

    override fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) {
        (holder as FileViewHolder).detach()
    }
}

open class FileViewHolder @JvmOverloads constructor(view: View,
                                                    itemClickListener: ItemClickListener? = null,
                                                    itemLongClickListener: ItemLongClickListener? = null)
    : MessageViewHolder(view, itemClickListener, itemLongClickListener), TransferListener, ProgressListener {

    private val fileNameView: TextView = itemView.findViewById(R.id.file_name)
    private val fileTypeView: TextView = itemView.findViewById(R.id.file_type)
    private val downloadIconView: ImageView = itemView.findViewById(R.id.iv_download)
    private val progressView: CircleProgress = itemView.findViewById(R.id.progress)

    protected lateinit var messageViewModel: MessageFileViewModel

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
        this.messageViewModel = messageViewModel as MessageFileViewModel
        messageViewModel.transferListener = this
        messageViewModel.progressListener = this
        renderFileName(messageViewModel)
        renderFileType(messageViewModel)
        renderDownloadIcon(messageViewModel)
    }

    open protected fun renderFileName(messageViewModel: MessageFileViewModel) {
        fileNameView.text = (messageViewModel.message as FileAttachmentMessage).attachmentName
    }

    open protected fun renderFileType(messageViewModel: MessageFileViewModel) {
        fileTypeView.text = messageViewModel.fileType
    }

    open protected fun renderDownloadIcon(messageViewModel: MessageFileViewModel) {
        downloadIconView.setImageResource(when (messageViewModel.message.state) {
            MessageState.FAILED -> R.drawable.ic_qiscus_upload
            MessageState.PENDING -> R.drawable.ic_qiscus_upload
            MessageState.SENDING -> R.drawable.ic_qiscus_upload
            else -> R.drawable.ic_qiscus_download
        })

        if ((messageViewModel.message as FileAttachmentMessage).file != null
                && messageViewModel.message.state.intValue >= MessageState.ON_SERVER.intValue) {
            downloadIconView.visibility = View.GONE
            progressView.visibility = View.GONE
        } else {
            onTransfer(messageViewModel.transfer)
            renderProgress(messageViewModel.progress)
        }
    }

    open protected fun renderProgress(attachmentProgress: FileAttachmentProgress?) {
        if (attachmentProgress != null) {
            progressView.progress = attachmentProgress.progress
        }
    }

    override fun onTransfer(transfer: Boolean) {
        if (transfer) {
            progressView.visibility = View.VISIBLE
            downloadIconView.visibility = View.GONE
        } else {
            progressView.visibility = View.GONE
            downloadIconView.visibility = View.VISIBLE
        }
    }

    override fun onProgress(fileAttachmentProgress: FileAttachmentProgress) {
        if (fileAttachmentProgress.fileAttachmentMessage.messageId == messageViewModel.message.messageId) {
            renderProgress(fileAttachmentProgress)
        }
    }

    open fun attach() {
        messageViewModel.listenAttachmentProgress()
    }

    open fun detach() {
        messageViewModel.stopListenAttachmentProgress()
    }
}