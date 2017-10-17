package com.qiscus.sdk.chat.presentation.uikit.comment

import android.support.v7.widget.RecyclerView
import android.view.View
import com.qiscus.sdk.chat.presentation.model.CommentViewModel

/**
 * Created on : October 17, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
abstract class CommentViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {
    abstract fun bind(commentViewModel: CommentViewModel)
}