package com.qiscus.sdk.chat.presentation.mobile.chatroom.viewholder

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import com.qiscus.sdk.chat.presentation.mobile.R
import com.qiscus.sdk.chat.presentation.model.CommentViewModel
import com.qiscus.sdk.chat.presentation.uikit.adapter.comment.CommentViewHolder
import com.qiscus.sdk.chat.presentation.uikit.adapter.comment.CommentViewHolderFactory

/**
 * Created on : October 17, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
class DefaultViewHolderFactory(private val context: Context) : CommentViewHolderFactory {
    override fun isUsingThisFactory(commentViewModel: CommentViewModel): Boolean {
        return true
    }

    override fun onCreateViewHolder(parent: ViewGroup?): CommentViewHolder {
        return TextViewHolder(LayoutInflater.from(context).inflate(R.layout.item_comment, parent, false))
    }
}