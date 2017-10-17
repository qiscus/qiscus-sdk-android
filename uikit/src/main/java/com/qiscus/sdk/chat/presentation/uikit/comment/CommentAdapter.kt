package com.qiscus.sdk.chat.presentation.uikit.comment

import android.view.ViewGroup
import com.qiscus.sdk.chat.presentation.model.CommentViewModel
import com.qiscus.sdk.chat.presentation.uikit.SortedAdapter
import com.qiscus.sdk.chat.presentation.uikit.util.indexOfFirst

/**
 * Created on : October 17, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
class CommentAdapter : SortedAdapter<CommentViewModel, CommentViewHolder>() {

    private val viewHolderFactories: MutableList<CommentViewHolderFactory> = mutableListOf()

    fun registerViewHolderFactory(commentViewHolderFactory: CommentViewHolderFactory) {
        viewHolderFactories.add(commentViewHolderFactory)
    }

    override fun getItemViewType(position: Int): Int {
        return viewHolderFactories.indexOfFirst { it.isUsingThisFactory(data[position]) }
    }

    override fun getItemClass(): Class<CommentViewModel> {
        return CommentViewModel::class.java
    }

    override fun compare(lhs: CommentViewModel, rhs: CommentViewModel): Int {
        return lhs.comment.date.compareTo(rhs.comment.date)
    }

    fun addOrUpdate(commentViewModel: CommentViewModel) {
        val pos = data.indexOfFirst { it.comment.commentId == commentViewModel.comment.commentId }
        if (pos >= 0) {
            data.updateItemAt(pos, commentViewModel)
        } else {
            data.add(commentViewModel)
        }
        notifyDataSetChanged()
    }

    fun removeComment(commentViewModel: CommentViewModel) {
        val pos = data.indexOfFirst { it.comment.commentId == commentViewModel.comment.commentId }
        if (pos >= 0) {
            data.removeItemAt(pos)
        }
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return data.size()
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): CommentViewHolder {
        return viewHolderFactories[viewType].onCreateViewHolder(parent)
    }

    override fun onBindViewHolder(holder: CommentViewHolder?, position: Int) {
        holder!!.bind(data[position])
    }
}