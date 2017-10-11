package com.qiscus.sdk.chat.presentation.mobile.chatroom

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.qiscus.sdk.chat.presentation.android.SortedAdapter
import com.qiscus.sdk.chat.presentation.android.util.indexOfFirst
import com.qiscus.sdk.chat.presentation.mobile.R
import com.qiscus.sdk.chat.presentation.model.CommentViewModel

/**
 * Created on : October 10, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
class CommentAdapter(private val context: Context) : SortedAdapter<CommentViewModel, CommentAdapter.VH>() {

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

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): VH {
        return VH(LayoutInflater.from(context).inflate(R.layout.item_comment, parent, false))
    }

    override fun onBindViewHolder(holder: VH?, position: Int) {
        holder!!.bind(data[position])
    }

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        private val senderAvatarView: ImageView = view.findViewById(R.id.senderAvatar) as ImageView
        private val senderNameView: TextView = view.findViewById(R.id.senderName) as TextView
        private val messageView: TextView = view.findViewById(R.id.message) as TextView

        fun bind(commentViewModel: CommentViewModel) {
            senderNameView.text = commentViewModel.comment.sender.name
            messageView.text = commentViewModel.spannableMessage
        }
    }
}