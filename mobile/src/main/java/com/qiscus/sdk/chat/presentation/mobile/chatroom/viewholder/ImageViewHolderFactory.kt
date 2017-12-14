package com.qiscus.sdk.chat.presentation.mobile.chatroom.viewholder

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.qiscus.sdk.chat.domain.model.FileAttachmentMessage
import com.qiscus.sdk.chat.presentation.mobile.R
import com.qiscus.sdk.chat.presentation.model.MessageImageViewModel
import com.qiscus.sdk.chat.presentation.model.MessageViewModel
import com.qiscus.sdk.chat.presentation.uikit.adapter.message.MessageViewHolder
import com.qiscus.sdk.chat.presentation.uikit.adapter.message.MessageViewHolderFactory

/**
 * Created on : October 17, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
class ImageViewHolderFactory(private val context: Context) : MessageViewHolderFactory {
    override fun isUsingThisFactory(messageViewModel: MessageViewModel): Boolean {
        return messageViewModel is MessageImageViewModel
    }

    override fun onCreateViewHolder(parent: ViewGroup?): MessageViewHolder {
        return ImageViewHolder(LayoutInflater.from(context).inflate(R.layout.item_message_image, parent, false))
    }
}

class ImageViewHolder(view: View) : MessageViewHolder(view) {
    private val senderAvatarView: ImageView = itemView.findViewById(R.id.senderAvatar)
    private val senderNameView: TextView = itemView.findViewById(R.id.senderName)
    private val captionView: TextView = itemView.findViewById(R.id.caption)
    private val imageView: ImageView = itemView.findViewById(R.id.image)

    override fun bind(messageViewModel: MessageViewModel) {
        senderNameView.text = messageViewModel.message.sender.name
        captionView.text = (messageViewModel as MessageImageViewModel).spannableCaption
        Glide.with(imageView)
                .load((messageViewModel.message as FileAttachmentMessage).attachmentUrl)
                .into(imageView)
    }
}