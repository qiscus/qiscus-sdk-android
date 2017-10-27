package com.qiscus.sdk.chat.presentation.mobile.chatroom.viewholder

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.qiscus.sdk.chat.presentation.mobile.R
import com.qiscus.sdk.chat.presentation.model.CommentTextViewModel
import com.qiscus.sdk.chat.presentation.model.CommentViewModel
import com.qiscus.sdk.chat.presentation.uikit.adapter.comment.CommentViewHolder
import com.qiscus.sdk.chat.presentation.uikit.adapter.comment.CommentViewHolderFactory

/**
 * Created on : October 17, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
class TextViewHolderFactory(private val context: Context) : CommentViewHolderFactory {
    override fun isUsingThisFactory(commentViewModel: CommentViewModel): Boolean {
        return commentViewModel is CommentTextViewModel
    }

    override fun onCreateViewHolder(parent: ViewGroup?): CommentViewHolder {
        return TextViewHolder(LayoutInflater.from(context).inflate(R.layout.item_comment, parent, false))
    }
}

class TextViewHolder(view: View) : CommentViewHolder(view) {
    private val senderAvatarView: ImageView = itemView.findViewById(R.id.senderAvatar) as ImageView
    private val senderNameView: TextView = itemView.findViewById(R.id.senderName) as TextView
    private val messageView: TextView = itemView.findViewById(R.id.message) as TextView

    override fun bind(commentViewModel: CommentViewModel) {
        senderNameView.text = commentViewModel.comment.sender.name
        messageView.text = commentViewModel.spannableMessage
    }
}