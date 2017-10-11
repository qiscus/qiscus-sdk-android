package com.qiscus.sdk.chat.presentation.mobile.listconversation

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
import com.qiscus.sdk.chat.presentation.mobile.chatroom.chatRoomIntent
import com.qiscus.sdk.chat.presentation.model.ConversationViewModel

/**
 * Created on : October 04, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
class ConversationAdapter(private val context: Context) : SortedAdapter<ConversationViewModel, ConversationAdapter.VH>() {

    fun addOrUpdate(conversationViewModel: ConversationViewModel) {
        val pos = data.indexOfFirst { it.room.id == conversationViewModel.room.id }
        if (pos >= 0) {
            data.updateItemAt(pos, conversationViewModel)
        } else {
            data.add(conversationViewModel)
        }
        notifyDataSetChanged()
    }

    fun removeConversation(conversationViewModel: ConversationViewModel) {
        val pos = data.indexOfFirst { it.room.id == conversationViewModel.room.id }
        if (pos >= 0) {
            data.removeItemAt(pos)
        }
        notifyDataSetChanged()
    }

    override fun getItemClass(): Class<ConversationViewModel> {
        return ConversationViewModel::class.java
    }

    override fun compare(lhs: ConversationViewModel, rhs: ConversationViewModel): Int {
        return if (lhs.lastComment == null) {
            1
        } else if (rhs.lastComment == null) {
            -1
        } else if (lhs.lastComment == null && rhs.lastComment == null) {
            0
        } else {
            rhs.lastComment!!.comment.date.compareTo(lhs.lastComment!!.comment.date)
        }
    }

    override fun areContentsTheSame(oldE: ConversationViewModel, newE: ConversationViewModel): Boolean {
        return oldE.room == newE.room
    }

    override fun areItemsTheSame(oldE: ConversationViewModel, newE: ConversationViewModel): Boolean {
        return oldE.room == newE.room
    }

    override fun getItemCount(): Int {
        return data.size()
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): VH {
        return VH(LayoutInflater.from(context).inflate(R.layout.item_conversation, parent, false))
    }

    override fun onBindViewHolder(holder: VH?, position: Int) {
        holder!!.bind(data[position])
    }

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        private val roomAvatarView: ImageView = view.findViewById(R.id.roomAvatar) as ImageView
        private val roomNameView: TextView = view.findViewById(R.id.roomName) as TextView
        private val lastMessageView: TextView = view.findViewById(R.id.lastMessage) as TextView

        fun bind(conversationViewModel: ConversationViewModel) {
            roomNameView.text = conversationViewModel.room.name
            lastMessageView.text = conversationViewModel.lastComment?.spannableMessage

            roomAvatarView.setOnClickListener {
                it.context.startActivity(it.context.chatRoomIntent(conversationViewModel.room))
            }
        }
    }
}