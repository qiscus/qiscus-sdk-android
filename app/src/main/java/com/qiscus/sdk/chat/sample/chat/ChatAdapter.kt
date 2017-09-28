package com.qiscus.sdk.chat.sample.chat

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.qiscus.sdk.chat.domain.model.Comment
import com.qiscus.sdk.chat.domain.model.FileAttachmentComment
import com.qiscus.sdk.chat.sample.R

/**
 * Created on : September 25, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
class ChatAdapter(private val context: Context) : RecyclerView.Adapter<ChatAdapter.VH>() {
    val data: MutableList<Comment> = mutableListOf()

    fun addOrUpdate(comment: Comment) {
        val pos = data.indexOfFirst { it.commentId == comment.commentId }
        if (pos >= 0) {
            data[pos] = comment
        } else {
            data.add(comment)
        }
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): VH {
        return VH(LayoutInflater.from(context).inflate(R.layout.item_chat, parent, false))
    }

    override fun onBindViewHolder(holder: VH?, position: Int) {
        holder!!.bind(data[position])
    }

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        private val messageTextView: TextView = view.findViewById(R.id.message) as TextView

        fun bind(comment: Comment) {
            if (comment is FileAttachmentComment) {
                messageTextView.text = String.format("%s: (%s)[%s] -%s-", comment.sender.name,
                        comment.caption, comment.getAttachmentName(), comment.state)
            } else {
                messageTextView.text = String.format("%s: %s -%s-", comment.sender.name, comment.message, comment.state)
            }
        }
    }
}