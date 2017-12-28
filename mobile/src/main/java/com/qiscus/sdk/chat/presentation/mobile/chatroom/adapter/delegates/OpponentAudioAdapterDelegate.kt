package com.qiscus.sdk.chat.presentation.mobile.chatroom.adapter.delegates

import android.content.Context
import android.support.v7.util.SortedList
import android.support.v7.widget.AppCompatSeekBar
import android.support.v7.widget.RecyclerView
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.qiscus.sdk.chat.domain.model.FileAttachmentProgress
import com.qiscus.sdk.chat.presentation.mobile.R
import com.qiscus.sdk.chat.presentation.model.*
import com.qiscus.sdk.chat.presentation.uikit.adapter.ItemClickListener
import com.qiscus.sdk.chat.presentation.uikit.adapter.ItemLongClickListener
import com.qiscus.sdk.chat.presentation.uikit.widget.CircleProgress

/**
 * Created on : December 21, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
class OpponentAudioAdapterDelegate @JvmOverloads constructor(private val context: Context,
                                                             private val itemClickListener: ItemClickListener? = null,
                                                             private val itemLongClickListener: ItemLongClickListener? = null)
    : MessageAdapterDelegate() {

    override fun isForViewType(data: SortedList<MessageViewModel>, position: Int): Boolean {
        val messageViewModel = data[position]
        return messageViewModel is MessageAudioViewModel && messageViewModel.message.sender != account.user
    }

    override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_qiscus_message_audio, parent, false)
        return OpponentAudioViewHolder(view, itemClickListener, itemLongClickListener)
    }

    override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) {
        (holder as OpponentAudioViewHolder).attach()
    }

    override fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) {
        (holder as OpponentAudioViewHolder).detach()
    }
}

open class OpponentAudioViewHolder @JvmOverloads constructor(view: View,
                                                             itemClickListener: ItemClickListener? = null,
                                                             itemLongClickListener: ItemLongClickListener? = null)
    : OpponentMessageViewHolder(view, itemClickListener, itemLongClickListener), TransferListener, ProgressListener, PlayingAudioListener {

    private val playButton: ImageView = itemView.findViewById(R.id.iv_play)
    private val seekBar: AppCompatSeekBar = itemView.findViewById(R.id.seekbar)
    private val durationView: TextView = itemView.findViewById(R.id.duration)
    private val progressView: CircleProgress = itemView.findViewById(R.id.progress)

    protected lateinit var messageViewModel: MessageAudioViewModel

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

    override fun determineSenderNameView(): TextView {
        return itemView.findViewById(R.id.name)
    }

    override fun determineSenderAvatarView(): ImageView {
        return itemView.findViewById(R.id.avatar)
    }

    override fun renderMessageContents(messageViewModel: MessageViewModel) {
        this.messageViewModel = messageViewModel as MessageAudioViewModel
        messageViewModel.transferListener = this
        messageViewModel.progressListener = this
        messageViewModel.playingAudioListener = this

        playButton.setOnClickListener { _ -> playAudio(messageViewModel) }
        seekBar.max = messageViewModel.getAudioDuration()
        seekBar.progress = if (messageViewModel.isPlayingAudio()) messageViewModel.getCurrentAudioPosition() else 0

        setTimeRemaining(if (messageViewModel.isPlayingAudio())
            messageViewModel.getAudioDuration() - messageViewModel.getCurrentAudioPosition()
        else
            messageViewModel.getAudioDuration())

        onTransfer(messageViewModel.transfer)
        renderProgress(messageViewModel.progress)
    }

    open protected fun renderProgress(attachmentProgress: FileAttachmentProgress?) {
        if (attachmentProgress != null) {
            progressView.progress = attachmentProgress.progress
        }
    }

    override fun onTransfer(transfer: Boolean) = if (transfer) {
        progressView.visibility = View.VISIBLE
    } else {
        progressView.visibility = View.GONE
    }

    override fun onProgress(fileAttachmentProgress: FileAttachmentProgress) {
        if (fileAttachmentProgress.fileAttachmentMessage.messageId == messageViewModel.message.messageId) {
            renderProgress(fileAttachmentProgress)
        }
    }

    open protected fun playAudio(messageAudioViewModel: MessageAudioViewModel) {
        if (messageAudioViewModel.getAudioDuration() > 0) {
            messageAudioViewModel.playAudio()
        } else {
            onClick(bubbleView)
        }
    }

    override fun onPlayingAudio(messageAudioViewModel: MessageAudioViewModel, currentPosition: Int) {
        if (messageAudioViewModel == messageViewModel) {
            playButton.setImageResource(R.drawable.ic_qiscus_pause_audio)
            seekBar.progress = currentPosition
            setTimeRemaining(messageAudioViewModel.getAudioDuration() - currentPosition)
        }
    }

    override fun onPauseAudio(messageAudioViewModel: MessageAudioViewModel) {
        if (messageAudioViewModel == messageViewModel) {
            playButton.setImageResource(R.drawable.ic_qiscus_play_audio)
        }
    }

    override fun onStopAudio(messageAudioViewModel: MessageAudioViewModel) {
        if (messageAudioViewModel == messageViewModel) {
            playButton.setImageResource(R.drawable.ic_qiscus_play_audio)
            seekBar.progress = 0
            setTimeRemaining(messageAudioViewModel.getAudioDuration())
        }
    }

    private fun setTimeRemaining(duration: Int) {
        durationView.text = DateUtils.formatElapsedTime(duration / 1000L)
    }

    open fun attach() {
        messageViewModel.listenAttachmentProgress()
    }

    open fun detach() {
        messageViewModel.stopListenAttachmentProgress()
    }
}