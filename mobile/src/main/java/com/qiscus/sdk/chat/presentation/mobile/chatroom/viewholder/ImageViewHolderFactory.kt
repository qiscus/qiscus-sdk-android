package com.qiscus.sdk.chat.presentation.mobile.chatroom.viewholder

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.qiscus.sdk.chat.domain.model.FileAttachmentComment
import com.qiscus.sdk.chat.presentation.mobile.R
import com.qiscus.sdk.chat.presentation.model.CommentImageViewModel
import com.qiscus.sdk.chat.presentation.model.CommentViewModel
import com.qiscus.sdk.chat.presentation.uikit.adapter.comment.CommentViewHolder
import com.qiscus.sdk.chat.presentation.uikit.adapter.comment.CommentViewHolderFactory

/**
 * Created on : October 17, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
class ImageViewHolderFactory(private val context: Context) : CommentViewHolderFactory {
    override fun isUsingThisFactory(commentViewModel: CommentViewModel): Boolean {
        return commentViewModel is CommentImageViewModel
    }

    override fun onCreateViewHolder(parent: ViewGroup?): CommentViewHolder {
        return ImageViewHolder(LayoutInflater.from(context).inflate(R.layout.item_comment_image, parent, false))
    }
}

class ImageViewHolder(view: View) : CommentViewHolder(view) {
    private val senderAvatarView: ImageView = itemView.findViewById(R.id.senderAvatar) as ImageView
    private val senderNameView: TextView = itemView.findViewById(R.id.senderName) as TextView
    private val captionView: TextView = itemView.findViewById(R.id.caption) as TextView
    private val imageView: ImageView = itemView.findViewById(R.id.image) as ImageView

    override fun bind(commentViewModel: CommentViewModel) {
        senderNameView.text = commentViewModel.comment.sender.name
        captionView.text = (commentViewModel as CommentImageViewModel).spannableCaption
        Glide.with(imageView)
                .load((commentViewModel.comment as FileAttachmentComment).attachmentUrl)
                .into(imageView)
    }
}