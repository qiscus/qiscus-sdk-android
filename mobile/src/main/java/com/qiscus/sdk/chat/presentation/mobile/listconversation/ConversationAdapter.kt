package com.qiscus.sdk.chat.presentation.mobile.listconversation

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.qiscus.sdk.chat.presentation.mobile.R
import com.qiscus.sdk.chat.presentation.model.ConversationView

/**
 * Created on : October 04, 2017
 * Author     : zetbaitsu
 * Name       : Zetra
 * GitHub     : https://github.com/zetbaitsu
 */
class ConversationAdapter(private val context: Context) : RecyclerView.Adapter<ConversationAdapter.VH>() {
    val data: MutableList<ConversationView> = mutableListOf()

    fun addOrUpdate(conversationView: ConversationView) {
        val pos = data.indexOfFirst { it.room.id == conversationView.room.id }
        if (pos >= 0) {
            data[pos] = conversationView
        } else {
            data.add(conversationView)
        }
        notifyDataSetChanged()
    }

    fun removeConversation(conversationView: ConversationView){
        val pos = data.indexOfFirst { it.room.id == conversationView.room.id }
        if (pos >= 0) {
            data.removeAt(pos)
        }
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return data.size
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

        fun bind(conversationView: ConversationView) {
            roomNameView.text = conversationView.room.name
            lastMessageView.text = conversationView.lastComment?.message
        }
    }
}